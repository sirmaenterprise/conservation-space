import {View, Component, Inject, NgElement, NgScope} from 'app/app';
import $ from 'jquery';
import _ from 'lodash';
import {Logger} from 'services/logging/logger';
import {Eventbus} from 'services/eventbus/eventbus';
import {Configuration} from 'common/application-config';
import {PluginsService} from 'services/plugin/plugins-service';
import {ValidationService} from 'form-builder/validation/validation-service';
import {PropertiesSelectorHelper} from 'idoc/widget/properties-selector/properties-selector-helper';
import {AfterFormValidationEvent} from 'form-builder/validation/after-form-validation-event';
import {EmittableObject} from 'common/event-emitter';
import {InstanceObject} from 'idoc/idoc-context';
import {ModelUtils} from 'models/model-utils';
import {INSTANCE_HEADERS} from 'instance-header/header-constants';
import './form-wrapper.css!';
import template from './form-wrapper.html!text';

export const LABEL_POSITION_LEFT = 'label-left';
export const LABEL_POSITION_ABOVE = 'label-above';
export const LABEL_POSITION_HIDE = 'label-hidden';
export const LABEL_TEXT_LEFT = 'label-text-left';
export const LABEL_TEXT_RIGHT = 'label-text-right';
export const LAYOUT = {HORIZONTAL: 'horizontal-layout', VERTICAL: 'vertical-layout', TABLE: 'table-layout'};

const BEFORE_FORM_RENDER = 'before_render';

/**
 * The form builder is responsible for presenting given object model as an html form where the user could alter the
 * model. Two separated models are used as source for the forms to be built.
 * The validation model contains the object data in a flat map where every property value is bound to its identifier in.
 * The view model is the actual configuration of the form that should be used when the form is build. It contains the
 * fields view statuses, labels, type, conditions, custom field types known as controls (these are more complex
 * components and not just simple form fields) and so on.
 * For every known field type there is a component in the form builder. During the building process the builder compiles
 * one of them for every field in the model and appends them to a form which in turn is appended to the target element.
 *
 * The process of building the form though is more complicated. It consists of a couple of phases.
 *
 * [Resolve the fields status]:
 * The field's status is resolved using some attributes from the view model: displayType, previewEmpty, isMandatory and
 * the form view mode which is a global property.
 * |displayType| : editable, readonly, hidden, system.
 * |isMandatory| : true|false.
 * |previewEmpty| : true|false (if the field should be visible in preview mode if it has no value).
 * |formViewMode| : preview, edit, print.
 *
 * [Apply validators]:
 * Every field in the view model can have validators that performs a formal and conditional validation. A validator
 * could modify the view model as a result of its execution. As a minimum they will set if the field is valid or not and
 * some error message. In other cases the field's status could be changed. In this phase are executed all validators for
 * every field in the view model. When validation completes the view model is considered to be complete and ready for to
 * be used for rendering.
 *
 * [Render(Filter fields)]:
 * According to needs that the client of the form has, the form builder is provided with some additional configurations
 * that affects what actually will be visualized in the form.
 * |renderMandatory| : true|false (if only the mandatory fields have to be rendered in the form).
 * |selectedProperties| : map with property identifiers mapped to definitions (these are the only fields that should be
 * visible in the form).
 * |showAllProperties| : true|false (if all model properties should be visible)
 * The rendering process is as follows:
 * - The view model is traversed and html is build
 * - For every field is resolved its control type
 * - The respective component tag is appended to the html
 * - Regions are handled recursively
 * - Once the html is ready it is compiled and appended to the DOM.
 *
 *
 * Config:
 * {
 *    formViewMode: FormWrapper.FORM_VIEW_MODE_EDIT|FormWrapper.FORM_VIEW_MODE_PREVIEW
 *    renderMandatory: true|false : if only mandatory fields must be rendered (usually regions are not set as mandatory in definitions)
 *    showRegionsNames: true|false
 *    form: the angular's form controller
 *    layout: 'vertical|horizontal|table' the default is 'vertical'
 *    labelTextAlign:
 *    labelPosition:
 *    models: {
 *      viewModel: The model used for the view building. This model is cloned in the constructor and the clone is used.
 *      validationModel: The model that holds the values and error messages as this model is always used as is provided.
 *    }
 * }
 * The configuration object is passed down to the rendered controls as widget-config property. The name is changed
 * because some of the controls are using other components which accepts config property and that will collide with
 * this one if passed under same name.
 */
@Component({
  selector: 'seip-form-wrapper',
  // the wrapper should allow children
  transclude: true,
  properties: {
    'config': 'config',
    'formConfig': 'form-config'
  }
})
@View({
  template: template
})
@Inject(NgElement, Logger, '$compile', NgScope, PluginsService, ValidationService, Eventbus, Configuration)
export class FormWrapper {

  constructor($element, logger, $compile, $scope, pluginsService, validationService, eventbus, configuration) {
    this.$element = $element;
    this.logger = logger;
    this.eventbus = eventbus;
    this.pluginsService = pluginsService;
    this.validationService = validationService;
    this.$compile = $compile;
    this.$scope = $scope;
    this.textareaMinCharsLength = configuration.get(Configuration.UI_TEXTAREA_MIN_CHARS);
    this.formHtml = '';
    // available controls are stored here after they are loaded
    this.controls;
    // The viewModel is a tree structure and the validationModel is a list, that's why we need a way to access the
    // validationModel values from template expressions using the viewModel item identifier. The viewModel is actually
    // flattened here as every model item is mapped to its identifier key.
    this.fieldsMap = {};
    // the angular's form controller is bound to this object trough the form's name attribute
    this.objectDataForm = {};

    this.setFormViewMode(this.config.formViewMode);

    this.config.layout = this.config.layout || LAYOUT.VERTICAL;
    this.config.labelPosition = this.config.labelPosition || LABEL_POSITION_LEFT;
    this.config.labelTextAlign = this.config.labelTextAlign || LABEL_TEXT_LEFT;
    this.config.grid = this.config.grid || '';
    if (this.config.enableHint === undefined) {
      this.config.enableHint = true;
    }
    // determine when to show placeholders in input fields
    this.config.showFieldPlaceholderCondition = this.config.showFieldPlaceholderCondition || LABEL_POSITION_HIDE;
    this.config.showRegionsNames = this.config.showRegionsNames !== undefined ? this.config.showRegionsNames : true;
    this.config.renderMandatory = this.config.renderMandatory !== undefined ? this.config.renderMandatory : false;
    // A clone is needed because many widgets can be linked to same objects but the view model might be changed during
    // view render process which would cause issues if concrete widget has specific configurations like filtered fields
    // for example.
    this.clonedViewModel = this.formConfig.models.viewModel.clone();
    this.selectedProperties = PropertiesSelectorHelper.getSelectedPropertiesArray(this.formConfig.models, this.config.selectedProperties) || [];
    this.setRenderAll(this.config.selectedProperties);

    // a generic watcher that can trigger reinit
    this.$scope.$watch(() => {
      return this.config.shouldReload;
    }, (shouldReload) => {
      if (shouldReload) {
        // reset the flag to avoid this watcher to be triggered again
        this.config.shouldReload = false;
        //after shouldReload has been triggered a new clone of the
        //view model is created after which the filters are reapplied
        this.clonedViewModel = this.formConfig.models.viewModel.clone();
        this.init($element);
      }
    });

    // listen for the form view mode changes and reinit
    this.$scope.$watch(() => {
      return this.config.formViewMode;
    }, () => {
      if (this.config.formViewMode !== this.formViewMode) {
        this.setFormViewMode(this.config.formViewMode);
        this.init($element);
      }
    });

    // listen for definition type changes and reinit
    this.$scope.$watch(() => {
      return this.formConfig.models.definitionId;
    }, (newValue, oldValue) => {
      if (newValue !== oldValue && this.formConfig.models.viewModel && this.formConfig.models.validationModel) {
        this.clonedViewModel = this.formConfig.models.viewModel.clone();
        this.init($element);
      }
    });

    this.$scope.$watch(() => {
      return this.config.selectedProperties;
    }, (newValue, oldValue) => {
      if (newValue !== oldValue) {
        this.selectedProperties = PropertiesSelectorHelper.getSelectedPropertiesArray(this.formConfig.models, this.config.selectedProperties);
        this.setRenderAll(this.config.selectedProperties);
        this.init($element);
      }
    });

    this.$scope.$watch(() => {
      return this.config.showAllProperties;
    }, (newValue, oldValue) => {
      if (newValue !== oldValue) {
        this.renderAll = this.config.showAllProperties;
        this.init($element);
      }
    });

    this.afterFormValidationHandler = this.eventbus.subscribe(AfterFormValidationEvent, (event) => {
      if (_.isEqual(this.formConfig.models.id, event[0].id)) {
        FormWrapper.filterFields(this.clonedViewModel.fields, this.config.renderMandatory, this.selectedProperties, this.renderAll, this.formViewMode, this.formConfig.models.validationModel, this.logger);
      }
    });

    // validation service needs to load its dependencies which is asynchronous before to be ready for use
    this.validationService.init().then(() => {
      this.init($element);
    });
  }

  ngOnDestroy() {
    this.afterFormValidationHandler.unsubscribe();
    $(this.$element).empty();
    $(this.$element).remove();
  }

  init($target) {
    // Clearing the DOM because otherwise validation is still executing on the old DOM while the form is initializing with new models.
    if (this.innerScope) {
      this.innerScope.$destroy();
    }
    $target.empty();
    this.pluginsService.loadComponentModules('form-control', 'type').then((controls) => {
      // Sometimes init is called multiple times in a sequence.
      // Code before this promise is executed multiple times in a row and then code after the promise is executed multiple
      // times which leads to multiplying displayed controls if DOM is not cleared.
      if (this.innerScope) {
        this.innerScope.$destroy();
      }
      $target.empty();
      // reset the map on each init to avoid old data to be left on type change for example
      this.fieldsMap = {};
      // flatten the whole model initially because we don't want only the rendered fields to be present in the model
      this.fieldsMap = this.clonedViewModel.flatModelMap;
      this.controls = controls;

      // Conditions are filtered according to form config for performance reasons.
      FormWrapper.configureConditions(this.clonedViewModel, this.selectedProperties, this.config.renderMandatory, this.formViewMode, this.renderAll);
      // Set preview attribute to every field before validation to be executed to allow conditions to override it if necessary.
      FormWrapper.setFieldPreviewAttribute(this.clonedViewModel.fields, this.formViewMode);
      this.applyValidation();

      this.fieldsBorder = this.config.showInputFieldBorders && (FormWrapper.isPreviewMode(this.formViewMode) || FormWrapper.isPrintMode(this.formViewMode)) ? 'with-border' : '';
      this.formHtml = '<div ng-class="[\'form-content\', formWrapper.formViewMode.toLowerCase(), formWrapper.config.labelPosition, formWrapper.config.labelTextAlign, formWrapper.config.styles.grid]">';
      this.buildForm(this.clonedViewModel.fields, this.formConfig.models.validationModel, this.formViewMode);
      this.formHtml += '</div>';
      this.innerScope = this.$scope.$new();
      $target.append(this.$compile(this.formHtml)(this.innerScope)[0]);
    });
  }

  /**
   * If a selected properties are not provided, we render the whole model.
   * If there are selected properties but not for the type displayed in the form (widget) then the form should be empty.
   *
   * @param selectedProperties The properties passed to the builder to be rendered.
   */
  setRenderAll(selectedProperties) {
    this.renderAll = selectedProperties === undefined;
  }

  static setFieldPreviewAttribute(viewModel, formViewMode) {
    viewModel.forEach((fieldViewModel) => {
      if (ModelUtils.isRegion(fieldViewModel)) {
        FormWrapper.setFieldPreviewAttribute(fieldViewModel.fields, formViewMode);
      }
      fieldViewModel.preview = FormWrapper.isPreview(fieldViewModel, formViewMode);
    });
  }

  applyValidation() {
    this.validationService.validate(this.formConfig.models.validationModel, this.fieldsMap,
      this.formConfig.models.id, BEFORE_FORM_RENDER, null);
  }

  /**
   * Conditions should be constrained according to the form configuration and mode: if there are selected properties to
   * be rendered, if the form is in preview or in edit mode if only the mandatory or all fields need to be rendered.
   * Conditions that don't need to be run are disabled and are not executed during the validation process.
   * Following are the rules used for conditions constraining.
   *
   * As precondition should be known that all fields are rendered in any case but only a specific subset of them  is
   * visible!
   *
   * (1) renderMandatory=true (create, upload, transition and save dialogs) :
   * -- Only the mandatory and invalid fields are visible.
   * -- All conditions are executed.
   *
   * (2) formViewMode=PREVIEW :
   * - Fields are visible according to their definitions (displayType, previewEmpty).
   * - Only VISIBLE|HIDDEN conditions are executed.
   *
   * (3) formViewMode=EDIT and selectedFields.length>0 :
   * - Only the selected properties are visible according to their definitions (displayType, previewEmpty).
   * - All conditions are executed.
   * - MANDATORY|OPTIONAL conditions are executed (fields which are not selected but made mandatory from a condition
   *   are later filtered during the rendering process and are not displayed)
   *
   * (4) showAllFields=true ([show more] is executed in create|upload dialogs) and formViewMode=EDIT
   * - All fields are visible according to their definitions (displayType, previewEmpty).
   * - All conditions should be executed.
   *
   * (5) formViewMode=EDIT and selectedProperties.length=0 :
   * - The form should be empty.
   * - No conditions should be executed.
   *
   * @param clonedViewModel
   * @param selectedProperties
   * @param renderMandatory
   * @param formViewMode
   * @param renderAll
   */
  static configureConditions(clonedViewModel, selectedProperties, renderMandatory, formViewMode, renderAll) {
    if (FormWrapper.isPreviewMode(formViewMode) && !renderMandatory && !renderAll) {
      // (2) enable only VISIBLE/HIDDEN conditions
      FormWrapper.toggleConditions(clonedViewModel.fields, ['visible', 'hidden'], false);
    } else if (renderMandatory || renderAll) {
      // (1) and (4) enable all conditions
      FormWrapper.toggleConditions(clonedViewModel.fields, [], false);
    } else if (selectedProperties.length > 0 && FormWrapper.isEditMode(formViewMode)) {
      // (3) enable all conditions
      FormWrapper.toggleConditions(clonedViewModel.fields, [], false);
    } else if (selectedProperties.length === 0 && FormWrapper.isEditMode(formViewMode)) {
      // (5) disable all conditions
      FormWrapper.toggleConditions(clonedViewModel.fields, [], true);
    }
  }

  /**
   * Set disabled=disable [true|false] to every conditional validator type that is passed with the 'conditionIds' argument.
   * Unmatched conditions are set with the inversed value of the 'disable' attribute.
   * Passing empty 'conditionIds' array is considered  to match 'every condition' and as result every condition in the
   * model would receive disabled='disable'.
   *
   * @param clonedViewModel
   * @param conditionIds
   * @param disable
   */
  static toggleConditions(clonedViewModel, conditionIds, disable) {
    clonedViewModel.forEach((fieldViewModel) => {
      fieldViewModel.validators && fieldViewModel.validators.forEach((validator) => {
        if (validator.id === 'condition') {
          validator.rules.forEach((rule) => {
            FormWrapper.toggleCondition(rule, conditionIds, disable);
          });
        } else if (validator.id === 'mandatory') {
          FormWrapper.toggleCondition(validator, conditionIds, disable);
        }
      });
      if (ModelUtils.isRegion(fieldViewModel)) {
        FormWrapper.toggleConditions(fieldViewModel.fields, conditionIds, disable);
      }
    });
  }

  /**
   * Selected conditions should be disabled/enabled according to the 'disabled' argument. The others should be inverted.
   * Conditions may exist in the mandatory and condition validators.
   * For condition the rules in every condition contains the condition type stored inside the 'renderAs' attribute.
   *
   * @param condition The condition definition.
   * @param conditionIds Array with condition ids that needs to be disabled enabled.
   * @param disable
   */
  static toggleCondition(condition, conditionIds, disable) {
    let type = condition.renderAs || condition.id;
    if (conditionIds.length === 0 || conditionIds.indexOf(type.toLowerCase()) !== -1) {
      condition.disabled = disable;
    } else {
      condition.disabled = !disable;
    }
  }

  /**
   * Filter fields by setting their rendered attribute in the view model.
   *
   * As precondition should be known that all fields are rendered in any case but only a specific subset is visible!
   *
   * - renderMandatory=true (create, upload, transition and save dialogs) :
   * -- Only the mandatory and invalid fields are visible.
   *
   * - selectedFields.length>0 :
   * -- Only the selected fields are visible according to their definitions (displayType, previewEmpty).
   * -- All conditions are applied and some of the selected fields might became hidden.
   *
   * - showAllFields=true ([show more] is executed in create|upload dialogs) and formViewMode=EDIT
   * -- All fields are visible according to their definitions (displayType, previewEmpty).
   *
   * - formViewMode=PREVIEW :
   * -- Fields are visible according to their definitions (displayType, previewEmpty).
   *
   * - selectedFields.length=0 :
   * -- The form should be empty.
   *
   * @param viewModel
   * @param renderMandatory
   * @param selectedProperties
   * @param renderAll
   * @param formViewMode
   * @param validationModel
   * @param logger
   */
  static filterFields(viewModel, renderMandatory, selectedProperties, renderAll, formViewMode, validationModel, logger) {
    if (renderMandatory) {
      FormWrapper.filterMandatoryFields(viewModel);

    } else if (renderAll) {
      FormWrapper.toggleAllFields(viewModel, true, validationModel, formViewMode, logger);

    } else if (selectedProperties.length > 0) {
      FormWrapper.filterSelectedFields(viewModel, selectedProperties, validationModel, formViewMode, logger);

    } else if (selectedProperties.length === 0) {
      FormWrapper.toggleAllFields(viewModel, false, validationModel, formViewMode, logger);

    }
  }

  /**
   * Rendered attribute (visibility) is toggled for all view model fields.
   * - Visibility of the fields depends on their definition configurations and if conditions were applied that change
   * their displayType.
   * - Visibility of the regions depends on their definition configurations, if conditions were applied that change
   * their displayType and if inside the region exists visible fields.
   * According to these dependencies every view model field and region is set with the needed rendered attribute.
   * There are cases where either all fields have to be rendered or none. This controlled by the show argument. While
   * the show=false means that all model fields have to be hidden and this can't be overridden, then if show=true we
   * need to account the field's configuration in order to tell if it has to be rendered or not.
   *
   * @param viewModel
   * @param show Tells if all model fields have to be explicitly switched.
   * @param validationModel
   * @param formViewMode
   * @param logger
   * @returns {boolean}
   */
  static toggleAllFields(viewModel, show, validationModel, formViewMode, logger) {
    let hasVisibleFields = false;
    viewModel.forEach((fieldViewModel) => {
      if (ModelUtils.isRegion(fieldViewModel)) {
        // process region fields
        let hasVisibleRegionFields = FormWrapper.toggleAllFields(fieldViewModel.fields, show, validationModel, formViewMode, logger);
        if (!show) {
          fieldViewModel.rendered = false;
          return false;
        }
        let regionVisible = FormWrapper.isVisibleField(fieldViewModel, validationModel, formViewMode, logger);
        fieldViewModel.rendered = show && regionVisible && hasVisibleRegionFields;
        return false;
      }
      if (!show) {
        fieldViewModel.rendered = false;
      } else {
        let visible = FormWrapper.isVisibleField(fieldViewModel, validationModel, formViewMode, logger);
        fieldViewModel.rendered = show && visible;
      }
      hasVisibleFields = hasVisibleFields || fieldViewModel.rendered;
    });
    return hasVisibleFields;
  }

  /**
   * Decide whether a field should be rendered as visible or not.
   *
   * @param viewModel
   * @param validationModel
   * @param formViewMode
   * @param logger
   * @returns {boolean}
   */
  static isVisibleField(viewModel, validationModel, formViewMode, logger) {
    // The isMandatory property is used to determine if the region should be explicitly marked as non skip able even
    // if only optional fields are present inside it.
    if (ModelUtils.isRegion(viewModel) && viewModel.isMandatory) {
      // Shortcut the rest because region is explicitly marked to be rendered.
      return true;
    }

    // It's possible some fields to be missing in validation model. For example in datatables are populated dummy fields
    // in the view model for selected properties which are not present in given instance. We treat such fields as valid
    // and visible.
    if (!validationModel[viewModel.identifier] && !ModelUtils.isRegion(viewModel)) {
      return true;
    }

    let key = FormWrapper.getVisibilityKey(viewModel, validationModel, formViewMode);
    let isVisible = FormWrapper.IS_VISIBLE[key];
    // If the key is not found assume the field should be visible.
    let unresolvedVisibilityStatus = isVisible === undefined;
    if (unresolvedVisibilityStatus) {
      logger.warn(`Cannot resolve visibility status of field with model ${JSON.stringify(viewModel)}. The field will not be rendered!`);
      return false;
    }

    // See comment in getVisibilityKey method about why the static visibility resolving is not enough.
    if (viewModel._displayType &&
      viewModel.displayType === ValidationService.DISPLAY_TYPE_HIDDEN || viewModel.displayType === ValidationService.DISPLAY_TYPE_SYSTEM) {
      isVisible = false;
    }

    let isValid = true;
    // regions don't have validation model
    if (!ModelUtils.isRegion(viewModel)) {
      isValid = validationModel[viewModel.identifier] && FormWrapper.isValid(validationModel[viewModel.identifier].valid);
    }
    return isVisible || !isValid;
  }

  /**
   * Should mark all not selected properties as rendered=false. Every region that has all its fields marked as hidden
   * should also be marked as hidden.
   *
   * Hidden field, unselected, no condition -> hidden
   * Hidden field, unselected, HIDDEN condition=true -> hidden
   * Hidden field, unselected, HIDDEN condition=false -> hidden
   * Hidden field, unselected, VISIBLE condition=true -> hidden
   * Hidden field, unselected, VISIBLE condition=false -> hidden
   * Hidden field, selected, no condition -> hidden
   * Hidden field, selected, HIDDEN condition=true -> hidden
   * Hidden field, selected, HIDDEN condition=false -> hidden
   * Hidden field, selected, VISIBLE condition=true -> visible
   * Hidden field, selected, VISIBLE condition=false -> visible
   *
   * Visible field, unselected, no condition -> hidden
   * Visible field, unselected, HIDDEN condition=true -> hidden
   * Visible field, unselected, HIDDEN condition=false -> hidden
   * Visible field, unselected, VISIBLE condition=true -> hidden
   * Visible field, unselected, VISIBLE condition=false -> hidden
   * Visible field, selected, no condition -> visible
   * Visible field, selected, HIDDEN condition=true -> hidden
   * Visible field, selected, HIDDEN condition=false -> visible
   * Visible field, selected, VISIBLE condition=true -> visible
   * Visible field, selected, VISIBLE condition=false -> visible
   *
   * @param viewModel
   * @param selectedProperties
   * @param validationModel
   * @param formViewMode
   * @param logger
   * @returns {boolean}
   */
  static filterSelectedFields(viewModel, selectedProperties, validationModel, formViewMode, logger) {
    let hasSelection = false;
    viewModel.forEach((fieldViewModel) => {
      if (ModelUtils.isRegion(fieldViewModel)) {
        let hasSelectedFields = FormWrapper.filterSelectedFields(fieldViewModel.fields, selectedProperties, validationModel, formViewMode, logger);
        fieldViewModel.rendered = hasSelectedFields;
        return false;
      }
      let isSelected = selectedProperties.indexOf(fieldViewModel.identifier) !== -1;
      let visible = FormWrapper.isVisibleField(fieldViewModel, validationModel, formViewMode, logger);
      fieldViewModel.rendered = isSelected && visible;
      hasSelection = hasSelection || fieldViewModel.rendered;
    });
    return hasSelection;
  }

  /**
   * Should mark all the optional fields as rendered=false. Every region that has all only optional fields should also
   * be marked as hidden.
   *
   * @param viewModel
   * @returns {boolean}
   */
  static filterMandatoryFields(viewModel) {
    // flag that shows if at least one field is rendered in the provided model
    let isRendered = false;
    viewModel.forEach((fieldViewModel) => {
      if (ModelUtils.isRegion(fieldViewModel)) {
        let hasRenderedFields = FormWrapper.filterMandatoryFields(fieldViewModel.fields);
        fieldViewModel.rendered = hasRenderedFields;
        return false;
      }
      let isMandatory = fieldViewModel.isMandatory;
      fieldViewModel.rendered = isMandatory;
      isRendered = isRendered || isMandatory;
    });
    return isRendered;
  }

  buildForm(clonedViewModel, validationModel, formViewMode) {
    clonedViewModel.forEach((currentViewModelItem) => {
      let controlType = ModelUtils.defineControlType(currentViewModelItem, this.textareaMinCharsLength);
      this.appendControl(currentViewModelItem, validationModel, controlType, formViewMode);
    });
  }

  appendControl(currentViewModelItem, validationModel, controlType, formViewMode) {
    let control = this.controls[controlType];
    if (!control) {
      this.logger.warn('Not found form control for type: ' + JSON.stringify(currentViewModelItem.modelProperty));
      // if control is not found/implemented build an empty cell
      currentViewModelItem = ModelUtils.buildEmptyCell(currentViewModelItem.identifier);
      controlType = currentViewModelItem.control.identifier;
      control = this.controls[controlType];
    }

    this.formHtml += '<' + control.name + ' id="' + currentViewModelItem.identifier + '-wrapper" '
      + 'validation-model="formWrapper.formConfig.models.validationModel" '
      + 'field-view-model="::formWrapper.fieldsMap[\'' + currentViewModelItem.identifier + '\']" '
      + 'validation-service="::formWrapper.validationService" '
      + 'form="formWrapper.objectDataForm" '
      + 'widget-config="formWrapper.config" '
      + 'form-config="formWrapper.formConfig" '
      + 'object-id="formWrapper.formConfig.models.id" ';
    if (this.config.styles) {
      this.formHtml += 'ng-style="{ \'width\': \'' + this.config.styles.columns[currentViewModelItem.identifier].width + '\' }" ';
    }
    this.formHtml += 'flat-form-view-model="formWrapper.fieldsMap">';

    if (ModelUtils.isRegion(currentViewModelItem)) {
      this.buildForm(currentViewModelItem.fields, validationModel, formViewMode);
    }

    this.formHtml += '</' + control.name + '>';
  }

  setFormViewMode(providedMode) {
    if (this.formConfig.writeAllowed === false && providedMode === FormWrapper.FORM_VIEW_MODE_EDIT) {
      this.formViewMode = FormWrapper.FORM_VIEW_MODE_PREVIEW;
    } else {
      this.formViewMode = providedMode || FormWrapper.FORM_VIEW_MODE_EDIT;
    }
  }

  static isValid(valid) {
    return valid !== undefined ? valid : true;
  }

  static isPreviewMode(formViewMode) {
    return formViewMode === FormWrapper.FORM_VIEW_MODE_PREVIEW;
  }

  static isPrintMode(formViewMode) {
    return formViewMode === FormWrapper.FORM_VIEW_MODE_PRINT;
  }

  static isEditMode(formViewMode) {
    return formViewMode === FormWrapper.FORM_VIEW_MODE_EDIT;
  }

  /**
   * Important note:
   * A method that resolves the statically applied (by definition) visibility. This method won't return correct results
   * if a fields attributes was previously altered by a condition. For example the key FULL_FORCEPREVIEW_HIDDEN_PREVIEW
   * would resolve as visible=true for hidden field with value and form in preview mode, but if there is a condition of
   * type HIDDEN which sets displayType=HIDDEN to the field, then the field must be 'conditional' visible=false!!!
   *
   * Builds a string key that is used to obtain the visibility status of the field from a predefined mapping. The
   * visibility key is build based on a couple of factors. After resolving each of them, the key is appended with
   * predefined string constants separated by underscore:
   * 1. If a field has value or is empty: FULL or EMPTY
   * 2. If the field have to be visible in preview mode if it has no value: FORCEPREVIEW or NOPREVIEW
   * 3. The field's displayType property: EDITABLE, READONLY, HIDDEN, SYSTEM
   * 4. The mode in which the form is rendered: EDIT, PREVIEW, PRINT
   *
   * @returns {string}
   */
  static getVisibilityKey(viewModel, validationModel, formViewMode) {
    let isPreviewEnabled = viewModel.previewEmpty;
    // whether the current field has a value
    let isEmpty = validationModel[viewModel.identifier]
        // this check should work for non boolean fields only
        // for checkboxes that are rendered from boolean fields we should return always false
      && (!validationModel[viewModel.identifier].value || validationModel[viewModel.identifier].value.length === 0);
    let displayType = viewModel.displayType;
    let key = '';
    if (ModelUtils.isRegion(viewModel) || isEmpty !== undefined && !isEmpty) {
      key += 'FULL_';
    } else {
      key += 'EMPTY_';
    }
    if (isPreviewEnabled) {
      key += 'FORCEPREVIEW_';
    } else {
      key += 'NOPREVIEW_';
    }
    key += displayType + '_';
    key += formViewMode;
    return key;
  }

  /**
   * Build a string key that is used to obtain the field view mode: preview|edit.
   * If condition is applied on given field, its default visibility would differ from the one applied by the condition.
   * If the field has backed up displayType (a condition is applied - prefixed with _) then use it for building the key.
   *
   * - If formViewMode=EDIT    | displayType=EDITABLE  | -> preview=false
   * - If formViewMode=EDIT    | displayType=READ_ONLY | -> preview=true
   * - If formViewMode=EDIT    | displayType=HIDDEN    | -> preview=true
   * - If formViewMode=EDIT    | displayType=SYSTEM    | -> preview=true
   * - If formViewMode=PREVIEW |                       | -> preview=true
   * - If formViewMode=PRINT   |                       | -> preview=true
   *
   * @returns {boolean}
   */
  static isPreview(viewModel, formViewMode) {
    let isPreview = false;
    if (!viewModel) {
      return isPreview;
    }
    let previewStatusKey = '';
    previewStatusKey += formViewMode;
    if (FormWrapper.isEditMode(formViewMode)) {
      previewStatusKey += '_';
      let displayType = viewModel.displayType;
      if (viewModel._displayType) {
        displayType = viewModel._displayType;
      }
      previewStatusKey += displayType;
    }
    isPreview = FormWrapper.IS_PREVIEW[previewStatusKey];
    return isPreview === undefined ? true : isPreview;
  }

  getHtml() {
    return this.formHtml;
  }
}

FormWrapper.IS_VISIBLE = {
  "FULL_FORCEPREVIEW_EDITABLE_EDIT": true,
  "FULL_FORCEPREVIEW_HIDDEN_EDIT": false,
  "FULL_FORCEPREVIEW_SYSTEM_EDIT": false,
  "FULL_FORCEPREVIEW_READ_ONLY_EDIT": true,

  "FULL_FORCEPREVIEW_EDITABLE_PREVIEW": true,
  "FULL_FORCEPREVIEW_HIDDEN_PREVIEW": true,
  "FULL_FORCEPREVIEW_SYSTEM_PREVIEW": false,
  "FULL_FORCEPREVIEW_READ_ONLY_PREVIEW": true,

  "FULL_FORCEPREVIEW_EDITABLE_PRINT": true,
  "FULL_FORCEPREVIEW_HIDDEN_PRINT": true,
  "FULL_FORCEPREVIEW_SYSTEM_PRINT": false,
  "FULL_FORCEPREVIEW_READ_ONLY_PRINT": true,

  //

  "FULL_NOPREVIEW_EDITABLE_EDIT": true,
  "FULL_NOPREVIEW_HIDDEN_EDIT": false,
  "FULL_NOPREVIEW_SYSTEM_EDIT": false,
  "FULL_NOPREVIEW_READ_ONLY_EDIT": true,

  "FULL_NOPREVIEW_EDITABLE_PREVIEW": true,
  "FULL_NOPREVIEW_HIDDEN_PREVIEW": true,
  "FULL_NOPREVIEW_SYSTEM_PREVIEW": false,
  "FULL_NOPREVIEW_READ_ONLY_PREVIEW": true,

  "FULL_NOPREVIEW_EDITABLE_PRINT": true,
  "FULL_NOPREVIEW_HIDDEN_PRINT": true,
  "FULL_NOPREVIEW_SYSTEM_PRINT": false,
  "FULL_NOPREVIEW_READ_ONLY_PRINT": true,

  //

  "EMPTY_FORCEPREVIEW_EDITABLE_EDIT": true,
  "EMPTY_FORCEPREVIEW_HIDDEN_EDIT": false,
  "EMPTY_FORCEPREVIEW_SYSTEM_EDIT": false,
  "EMPTY_FORCEPREVIEW_READ_ONLY_EDIT": true,

  "EMPTY_FORCEPREVIEW_EDITABLE_PREVIEW": true,
  "EMPTY_FORCEPREVIEW_HIDDEN_PREVIEW": true,
  "EMPTY_FORCEPREVIEW_SYSTEM_PREVIEW": false,
  "EMPTY_FORCEPREVIEW_READ_ONLY_PREVIEW": true,

  "EMPTY_FORCEPREVIEW_EDITABLE_PRINT": true,
  "EMPTY_FORCEPREVIEW_HIDDEN_PRINT": true,
  "EMPTY_FORCEPREVIEW_SYSTEM_PRINT": false,
  "EMPTY_FORCEPREVIEW_READ_ONLY_PRINT": true,

  "EMPTY_NOPREVIEW_EDITABLE_EDIT": true,
  "EMPTY_NOPREVIEW_HIDDEN_EDIT": false,
  "EMPTY_NOPREVIEW_SYSTEM_EDIT": false,
  "EMPTY_NOPREVIEW_READ_ONLY_EDIT": true,

  "EMPTY_NOPREVIEW_EDITABLE_PREVIEW": false,
  "EMPTY_NOPREVIEW_HIDDEN_PREVIEW": false,
  "EMPTY_NOPREVIEW_SYSTEM_PREVIEW": false,
  "EMPTY_NOPREVIEW_READ_ONLY_PREVIEW": false,

  "EMPTY_NOPREVIEW_EDITABLE_PRINT": false,
  "EMPTY_NOPREVIEW_HIDDEN_PRINT": false,
  "EMPTY_NOPREVIEW_SYSTEM_PRINT": false,
  "EMPTY_NOPREVIEW_READ_ONLY_PRINT": false
};

FormWrapper.IS_PREVIEW = {
  "EDIT_EDITABLE": false,
  "EDIT_HIDDEN": false,
  "EDIT_SYSTEM": false,
  "EDIT_READ_ONLY": true,
  "PREVIEW": true
};

FormWrapper.DISPLAY_TYPE_READ_ONLY = 'READ_ONLY';
FormWrapper.FORM_VIEW_MODE_EDIT = 'EDIT';
FormWrapper.FORM_VIEW_MODE_PREVIEW = 'PREVIEW';
FormWrapper.FORM_VIEW_MODE_PRINT = 'PRINT';
