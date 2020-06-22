import {View, Component, Inject, NgElement, NgScope, NgTimeout} from 'app/app';
import _ from 'lodash';
import {Logger} from 'services/logging/logger';
import {Eventbus} from 'services/eventbus/eventbus';
import {EventEmitter} from 'common/event-emitter';
import {Configuration} from 'common/application-config';
import {PluginsService} from 'services/plugin/plugins-service';
import {ValidationService} from 'form-builder/validation/validation-service';
import {PropertiesSelectorHelper} from 'idoc/widget/properties-selector/properties-selector-helper';
import {AfterFormValidationEvent} from 'form-builder/validation/after-form-validation-event';
import {ModelUtils} from 'models/model-utils';
import {CONTROL_TYPE} from 'models/model-utils';
import './form-wrapper.css!';
import template from './form-wrapper.html!text';

export const LABEL_POSITION_LEFT = 'label-left';
export const LABEL_POSITION_ABOVE = 'label-above';
export const LABEL_POSITION_HIDE = 'label-hidden';
export const LABEL_TEXT_LEFT = 'label-text-left';
export const LABEL_TEXT_RIGHT = 'label-text-right';
export const LAYOUT = {HORIZONTAL: 'horizontal-layout', VERTICAL: 'vertical-layout', TABLE: 'table-layout'};

const BEFORE_FORM_RENDER = 'before_render';
const SYSTEM_PROPERTIES = ['rdf:type', 'content', 'emf:revisionType', 'thumbnailImage'];

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
 *    modeling: true|false : in modeling mode conditions are not processed
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
  template
})
@Inject(NgElement, Logger, '$compile', NgScope, PluginsService, ValidationService, Eventbus, Configuration, NgTimeout)
export class FormWrapper {

  constructor($element, logger, $compile, $scope, pluginsService, validationService, eventbus, configuration, $timeout) {
    this.$element = $element;
    this.logger = logger;
    this.eventbus = eventbus;
    this.pluginsService = pluginsService;
    this.validationService = validationService;
    this.$compile = $compile;
    this.$scope = $scope;
    this.$timeout = $timeout;

    this.eventEmitter = this.config.eventEmitter || new EventEmitter();

    this.textareaMinCharsLength = configuration.get(Configuration.UI_TEXTAREA_MIN_CHARS);
    this.definitionId = this.formConfig.models.definitionId;
    this.instanceId = this.formConfig.models.id;
    // available controls are stored here after they are loaded
    this.controls;
    // The viewModel is a tree structure and the validationModel is a list, that's why we need a way to access the
    // validationModel values from template expressions using the viewModel item identifier. The viewModel is actually
    // flattened here as every model item is mapped to its identifier key.
    this.fieldsMap = {};
    // Holds object properties identifiers which should be visible/rendered in form. Before the form to be set as
    // initialized, these should be loaded and rendered as well.
    this.objectProperties = {};
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
    this.config.renderMandatoryForState = this.config.renderMandatoryForState !== undefined ? this.config.renderMandatoryForState : false;
    // A clone is needed because many widgets can be linked to same objects but the view model might be changed during
    // view render process which would cause issues if concrete widget has specific configurations like filtered fields
    // for example.
    this.cloneViewModel();
    this.selectedProperties = PropertiesSelectorHelper.getSelectedProperties(this.formConfig.models, this.config.selectedProperties);
    this.selectedProperties = Object.keys(this.selectedProperties);
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
        this.cloneViewModel();
        this.init($element);
      }
    });

    // listen for the form view mode changes and reinit
    this.$scope.$watch(() => {
      return this.config.formViewMode;
    }, () => {
      if (this.config.formViewMode !== this.formViewMode) {
        let oldFormViewMode = this.formViewMode;
        this.setFormViewMode(this.config.formViewMode);
        // if the form viewMode is not switched, ignore reinitialization of the form.
        if (oldFormViewMode !== this.formViewMode) {
          this.init($element);
        }
      }
    });

    // listen for definition type changes and reinit
    this.$scope.$watch(() => {
      return this.formConfig.models.definitionId;
    }, (newValue, oldValue) => {
      if (newValue !== oldValue && this.formConfig.models.viewModel && this.formConfig.models.validationModel) {
        this.cloneViewModel();
        this.init($element);
      }
    });

    this.$scope.$watch(() => {
      return this.config.selectedProperties;
    }, (newValue, oldValue) => {
      if (newValue !== oldValue) {
        this.selectedProperties = PropertiesSelectorHelper.getSelectedProperties(this.formConfig.models, this.config.selectedProperties);
        this.selectedProperties = Object.keys(this.selectedProperties);
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

    // validation service needs to load its dependencies which is asynchronous before to be ready for use
    this.validationService.init().then(() => {
      this.init($element);
    });
  }

  ngOnInit() {
    // flag used to subscribe only once to propertyLoaded event
    let subscribedToObjectPropertyLoaded = false;
    this.afterFormValidationHandler = this.eventbus.subscribe(AfterFormValidationEvent, (event) => {

      if (_.isEqual(this.formConfig.models.id, event[0].id)) {
        FormWrapper.filterFields(this.clonedViewModel.fields, this.config.renderMandatory, this.selectedProperties,
          this.renderAll, this.formViewMode, this.formConfig.models.validationModel, this.logger, this.$element, this.config.renderMandatoryForState);
        // if form wrapper properties should be waited to be fully loaded.
        if (FormWrapper.shouldWaitForFormLoaded(this.fieldsMap) && !subscribedToObjectPropertyLoaded) {
          subscribedToObjectPropertyLoaded = true;
          let propertyLoadedHandler = this.eventEmitter.subscribe('formControlLoaded', (evt) => {
            if (this.isViewReady({fieldIdentifier: evt.identifier})) {
              propertyLoadedHandler.unsubscribe();
              subscribedToObjectPropertyLoaded = false;
              this.setFormInitialized();
            }
          });
        } else if (!subscribedToObjectPropertyLoaded) {
          // if form does not need to wait for controls to load, publish formInitialized
          this.setFormInitialized();
        }
      }
    });
  }

  isViewReady(data) {
    let isReady = false;
    if (data.fieldIdentifier) {
      this.objectProperties[data.fieldIdentifier] = true;
      isReady = Object.keys(this.objectProperties).every((key) => {
        return this.objectProperties[key] === true;
      });
    }

    return isReady;
  }

  setFormInitialized() {
    // initialize only once in order to avoid triggering digest (timeout) after each validation
    if (!this.$element.hasClass('initialized')) {
      // Add time for ng-if for rendered flag (set by FormWrapper.filterFields) to evaluate and display the field
      this.$timeout(() => {
        this.$element.addClass('initialized');
        // TODO: aggregated widget still uses polling instead of events.
        if (this.formConfig.eventEmitter) {
          this.formConfig.eventEmitter.publish('formInitialized', true);
        }
      });
    }
  }

  cloneViewModel() {
    this.clonedViewModel = this.formConfig.models.viewModel.clone();
  }

  ngOnDestroy() {
    this.destroyed = true;
    this.afterFormValidationHandler.unsubscribe();
    this.$element.remove();

    this.$timeout(() => {
      this.clonedViewModel = null;
    });
  }

  init($target) {
    // Clearing the DOM because otherwise validation is still executing on the old DOM while the form is initializing with new models.
    if (this.innerScope) {
      this.innerScope.$destroy();
    }
    this.pluginsService.loadComponentModules('form-control', 'type').then((controls) => {
      // Sometimes init is called multiple times in a sequence.
      // Code before this promise is executed multiple times in a row and then code after the promise is executed multiple
      // times which leads to multiplying displayed controls if DOM is not cleared.
      if (this.innerScope) {
        this.innerScope.$destroy();
      }
      // reset the map on each init to avoid old data to be left on type change for example
      this.fieldsMap = {};
      // flatten the whole model initially because we don't want only the rendered fields to be present in the model
      this.fieldsMap = this.clonedViewModel.flatModelMap;
      this.controls = controls;

      // Conditions are filtered according to form config for performance reasons.
      FormWrapper.configureConditions(this.clonedViewModel, this.selectedProperties, this.config.renderMandatory,
        this.formViewMode, this.renderAll, this.config.modeling);
      // Set preview attribute to every field before validation to be executed to allow conditions to override it if necessary.
      FormWrapper.setFieldPreviewAttribute(this.clonedViewModel.fields, this.formViewMode);
      this.applyValidation().then(() => {
        // skip next processing if the component was destroyed during the async validation
        if (this.destroyed) {
          return;
        }
        this.fieldsBorder = this.config.showInputFieldBorders && (FormWrapper.isPreviewMode(this.formViewMode) || FormWrapper.isPrintMode(this.formViewMode)) ? 'with-border' : '';
        let formHtml = `<div ng-class="[\'form-content\', formWrapper.formViewMode.toLowerCase(), formWrapper.config.labelPosition, formWrapper.config.labelTextAlign, formWrapper.config.styles.grid]">
                          ${this.buildForm(this.clonedViewModel.fields, this.formConfig.models.validationModel, this.formViewMode)}
                        </div>`;

        this.innerScope = this.$scope.$new();
        $target.empty();
        $target.append(this.$compile(formHtml)(this.innerScope)[0]);
      });
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
    return this.validationService.validate(this.formConfig.models.validationModel, this.getViewModel(),
      this.formConfig.models.id, BEFORE_FORM_RENDER, null, this.formConfig.models.definitionId, this.instanceId);
  }

  /**
   * Usually form builder is provided with a view model which contains the whole model. In some cases thou, a separated
   * view model is passed with the formConfig.models.fullViewModel config. It's passed from widgets where custom partial
   * view model is used for presenting the view (like DTW). Using partial view model however causes troubles with the
   * model validation process. That's why the fullViewModel is used.
   * In case fullViewModel is provided, then this method overrides the fields from fieldsMap which is used from the form
   * builder to render the fields to flatModelMap to preserve any configurations which have already been done.
   *
   * @returns In case fullViewModel is present, the fullViewModel.flatModelMap is returned. Otherwise the fieldsMap is
   * returned.
   */
  getViewModel() {
    if (this.formConfig.models.fullViewModel) {
      Object.keys(this.formConfig.models.fullViewModel.flatModelMap).forEach((key) => {
        if (this.fieldsMap[key]) {
          this.formConfig.models.fullViewModel.flatModelMap[key] = this.fieldsMap[key];
        }
      });
      return this.formConfig.models.fullViewModel.flatModelMap;
    } else {
      return this.fieldsMap;
    }
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
   * (1) modeling=true
   * - All conditions are suspended
   *
   * (2) renderMandatory=true (create, upload, transition and save dialogs) :
   * -- Only the mandatory and invalid fields are visible.
   * -- All conditions are executed.
   *
   * (3) formViewMode=PREVIEW :
   * - Fields are visible according to their definitions (displayType, previewEmpty).
   * - Only VISIBLE|HIDDEN conditions are executed.
   *
   * (4) formViewMode=EDIT and selectedFields.length>0 :
   * - Only the selected properties are visible according to their definitions (displayType, previewEmpty).
   * - All conditions are executed.
   * - MANDATORY|OPTIONAL conditions are executed (fields which are not selected but made mandatory from a condition
   *   are later filtered during the rendering process and are not displayed)
   *
   * (5) showAllFields=true ([show more] is executed in create|upload dialogs) and formViewMode=EDIT
   * - All fields are visible according to their definitions (displayType, previewEmpty).
   * - All conditions should be executed.
   *
   * (6) formViewMode=EDIT and selectedProperties.length=0 :
   * - The form should be empty.
   * - No conditions should be executed.
   */
  static configureConditions(clonedViewModel, selectedProperties, renderMandatory, formViewMode, renderAll, modeling) {
    if (modeling) {
      FormWrapper.toggleConditions(clonedViewModel.fields, [], true);
    } else if (FormWrapper.isPreviewMode(formViewMode) && !renderMandatory && !renderAll) {
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
   * Unmatched conditions are set with the inverse value of the 'disable' attribute.
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
        validator.rules && validator.rules.forEach((rule) => {
          FormWrapper.toggleCondition(rule, conditionIds, disable);
        });
        // A mandatory validator has no rules
        if (validator.id === 'mandatory') {
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
   * As precondition should be known that only a specific subset of fields is visible!
   *
   * - renderMandatoryForState=true (state transitions with defined mandatory fields)
   * -- Only mandatory for given state and invalid fields are visible.
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
   */
  static filterFields(viewModel, renderMandatory, selectedProperties, renderAll, formViewMode, validationModel, logger, element, renderMandatoryForState) {
    FormWrapper.showFormBorder(element, true);
    if (renderMandatoryForState) {
      let hasVisibleFields = this.filterMandatoryByStateFields(viewModel, validationModel);
      FormWrapper.showFormBorder(element, hasVisibleFields);
    } else if (renderMandatory) {
      let hasVisibleFields = FormWrapper.filterMandatoryFields(viewModel, validationModel);
      FormWrapper.showFormBorder(element, hasVisibleFields);
    } else if (renderAll) {
      FormWrapper.toggleAllFields(viewModel, true, validationModel, formViewMode, logger);
    } else if (selectedProperties.length > 0) {
      FormWrapper.filterSelectedFields(viewModel, selectedProperties, validationModel, formViewMode, logger);
    } else if (selectedProperties.length === 0) {
      FormWrapper.toggleAllFields(viewModel, false, validationModel, formViewMode, logger);
    }
  }

  /**
   * Shows or hides border around the form depending on existing elements
   * @param element form element
   * @param show boolean flag used to show or hide border
   */
  static showFormBorder(element, show) {
    let container = element.closest('.form-container');
    if (container.length === 1) {
      if (show) {
        container.show();
      } else {
        container.hide();
      }
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
    // If the key is not found assume the field should not be rendered.
    let unresolvedVisibilityStatus = isVisible === undefined;
    if (unresolvedVisibilityStatus) {
      logger.warn(`Cannot resolve visibility status of field with model ${JSON.stringify(viewModel)}. The field will not be rendered!`);
      return false;
    }

    // See comment in getVisibilityKey method about why the static visibility resolving is not enough.
    // checks also if the displayType has changed by condition (cached _displayType should not be equal to the actual displayType).
    // If not, don't switch the flag.
    if (viewModel._displayType && (viewModel._displayType !== viewModel.displayType ) &&
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
   * Returns true if the form fields map contains fields that need to be waited to be rendered.
   * @param fieldsMap
   * @returns {boolean}
   */
  static shouldWaitForFormLoaded(fieldsMap) {
    return Object.keys(fieldsMap).some((field) => {
      if (fieldsMap[field].rendered) {
        return true;
      }
    });
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
   * Leaves only fields which have a mandatory validator marked with isMandatoryForState. In case of state transitions
   * only these and the once which have invalid values must be rendered.
   */
  static filterMandatoryByStateFields(viewModel, validationModel) {
    let hasRenderedFields = false;
    viewModel.forEach((fieldViewModel) => {
      if (ModelUtils.isRegion(fieldViewModel)) {
        fieldViewModel.rendered = FormWrapper.filterMandatoryByStateFields(fieldViewModel.fields, validationModel);
        if (fieldViewModel.rendered) {
          hasRenderedFields = true;
        }
        return false;
      }
      let isStaticMandatory = fieldViewModel.isMandatory;
      let isConditionalMandatory = false;
      let validators = fieldViewModel.validators || [];
      validators.forEach((validator) => {
        if (validator.id === 'mandatory' && validator.isMandatoryForState) {
          isConditionalMandatory = true;
        }
      });
      let isValid = validationModel[fieldViewModel.identifier].valid === undefined || validationModel[fieldViewModel.identifier].valid;
      let visibleOptional = fieldViewModel.rendered && validationModel[fieldViewModel.identifier]._wasInvalid;
      fieldViewModel.rendered = isConditionalMandatory || isStaticMandatory || !isValid || visibleOptional;
      if (fieldViewModel.rendered) {
        hasRenderedFields = true;
      }
    });
    return hasRenderedFields;
  }

  /**
   * Should mark all the optional fields as rendered=false. Every region that has all only optional fields should also
   * be marked as hidden.
   *
   * @param viewModel
   * @param validationModel
   * @returns {boolean}
   */
  static filterMandatoryFields(viewModel, validationModel) {
    // flag that shows if at least one field is rendered in the provided model
    let hasRenderedFields = false;
    viewModel.forEach((fieldViewModel) => {
      if (ModelUtils.isRegion(fieldViewModel)) {
        fieldViewModel.rendered = FormWrapper.filterMandatoryFields(fieldViewModel.fields, validationModel);
        if (fieldViewModel.rendered) {
          hasRenderedFields = true;
        }
        return false;
      }
      let isMandatory = fieldViewModel.isMandatory;
      // Some fields like headers don't have `valid` property.
      let isValid = validationModel[fieldViewModel.identifier].valid === undefined || validationModel[fieldViewModel.identifier].valid;
      // In some cases an optional field might have invalid value, in which case it has to be displayed in order the
      // user to be able to correct it. If we don't account whether the field had invalid value then the field would be
      // filtered immediately here after its gets a valid value and it would disappear which is unwanted as the user
      // might not completed the value it wanted. This covers the case where an optional field has invalid value, then
      // in the save/transition dialog user tries to correct the value and the field disappears.
      let visibleOptional = fieldViewModel.rendered && validationModel[fieldViewModel.identifier]._wasInvalid;
      fieldViewModel.rendered = isMandatory || !isValid || visibleOptional;
      if (fieldViewModel.rendered) {
        hasRenderedFields = true;
      }
    });
    return hasRenderedFields;
  }

  buildForm(clonedViewModel, validationModel, formViewMode) {
    let formHtml = '';
    clonedViewModel.forEach((currentViewModelItem) => {
      let controlType = ModelUtils.defineControlType(currentViewModelItem, this.textareaMinCharsLength);
      formHtml += this.appendControl(currentViewModelItem, validationModel, controlType, formViewMode);
    });
    return formHtml;
  }

  appendControl(currentViewModelItem, validationModel, controlType, formViewMode) {
    let appendControlHtml = '';
    let control = this.controls[controlType];
    if (!control) {
      if (SYSTEM_PROPERTIES.indexOf(currentViewModelItem.identifier) === -1) {
        this.logger.warn('Not found form control for type: ' + JSON.stringify(currentViewModelItem.modelProperty));
      }
      // if control is not found/implemented build an empty cell
      currentViewModelItem = ModelUtils.buildEmptyCell(currentViewModelItem.identifier);
      controlType = currentViewModelItem.control.identifier;
      control = this.controls[controlType];
    }

    appendControlHtml += '<' + control.name + ' id="' + currentViewModelItem.identifier + '-wrapper" ';
    if (this.config.styles) {
      appendControlHtml += 'ng-style="{ \'width\': \'' + this.config.styles.columns[currentViewModelItem.identifier].width + '\' }" ';
    }

    if (!ModelUtils.isRegion(currentViewModelItem) && this.fieldsMap[currentViewModelItem.identifier].rendered) {
      // Register properties that contain headers
      this.objectProperties[currentViewModelItem.identifier] = false;
    }

    // instance links in DTW must also be waited upon compilation
    if (this.config.layout === LAYOUT.TABLE && controlType === CONTROL_TYPE.INSTANCE_HEADER && this.config.instanceLinkType !== 'none') {
      this.objectProperties[currentViewModelItem.identifier] = false;
    }

    if (this.config.layout !== LAYOUT.TABLE) {
      appendControlHtml += 'ng-if="::formWrapper.fieldsMap[\'' + currentViewModelItem.identifier + '\'].rendered ? true : undefined" ';
    }
    appendControlHtml += 'form-wrapper="::formWrapper" '
      + 'identifier="\'' + currentViewModelItem.identifier + '\'">';

    if (ModelUtils.isRegion(currentViewModelItem)) {
      appendControlHtml += this.buildForm(currentViewModelItem.fields, validationModel, formViewMode);
    }
    return appendControlHtml + '</' + control.name + '>';
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
    let noValue = false;
    let emptyValue = false;
    let emptyObjectPropValue = false;

    // These check should work for non boolean fields only. For checkboxes that are rendered from boolean fields should
    // be returned always false.
    if (!validationModel[viewModel.identifier] || !validationModel[viewModel.identifier].value) {
      noValue = true;
    } else {
      if (validationModel[viewModel.identifier].value.length === 0) {
        emptyValue = true;
      }
      // Object properties value is actually stored in value.results array.
      if (validationModel[viewModel.identifier].value.results && validationModel[viewModel.identifier].value.results.length === 0) {
        emptyObjectPropValue = true;
      }
    }

    let isEmpty = noValue || emptyValue || emptyObjectPropValue;

    let displayType = viewModel.displayType;
    let key = '';
    if (ModelUtils.isRegion(viewModel) || !isEmpty) {
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

}

FormWrapper.IS_VISIBLE = {
  'FULL_FORCEPREVIEW_EDITABLE_EDIT': true,
  'FULL_FORCEPREVIEW_HIDDEN_EDIT': false,
  'FULL_FORCEPREVIEW_SYSTEM_EDIT': false,
  'FULL_FORCEPREVIEW_READ_ONLY_EDIT': true,

  'FULL_FORCEPREVIEW_EDITABLE_PREVIEW': true,
  'FULL_FORCEPREVIEW_HIDDEN_PREVIEW': true,
  'FULL_FORCEPREVIEW_SYSTEM_PREVIEW': false,
  'FULL_FORCEPREVIEW_READ_ONLY_PREVIEW': true,

  'FULL_FORCEPREVIEW_EDITABLE_PRINT': true,
  'FULL_FORCEPREVIEW_HIDDEN_PRINT': true,
  'FULL_FORCEPREVIEW_SYSTEM_PRINT': false,
  'FULL_FORCEPREVIEW_READ_ONLY_PRINT': true,

  //

  'FULL_NOPREVIEW_EDITABLE_EDIT': true,
  'FULL_NOPREVIEW_HIDDEN_EDIT': false,
  'FULL_NOPREVIEW_SYSTEM_EDIT': false,
  'FULL_NOPREVIEW_READ_ONLY_EDIT': true,

  'FULL_NOPREVIEW_EDITABLE_PREVIEW': true,
  'FULL_NOPREVIEW_HIDDEN_PREVIEW': true,
  'FULL_NOPREVIEW_SYSTEM_PREVIEW': false,
  'FULL_NOPREVIEW_READ_ONLY_PREVIEW': true,

  'FULL_NOPREVIEW_EDITABLE_PRINT': true,
  'FULL_NOPREVIEW_HIDDEN_PRINT': true,
  'FULL_NOPREVIEW_SYSTEM_PRINT': false,
  'FULL_NOPREVIEW_READ_ONLY_PRINT': true,

  //

  'EMPTY_FORCEPREVIEW_EDITABLE_EDIT': true,
  'EMPTY_FORCEPREVIEW_HIDDEN_EDIT': false,
  'EMPTY_FORCEPREVIEW_SYSTEM_EDIT': false,
  'EMPTY_FORCEPREVIEW_READ_ONLY_EDIT': true,

  'EMPTY_FORCEPREVIEW_EDITABLE_PREVIEW': true,
  'EMPTY_FORCEPREVIEW_HIDDEN_PREVIEW': true,
  'EMPTY_FORCEPREVIEW_SYSTEM_PREVIEW': false,
  'EMPTY_FORCEPREVIEW_READ_ONLY_PREVIEW': true,

  'EMPTY_FORCEPREVIEW_EDITABLE_PRINT': true,
  'EMPTY_FORCEPREVIEW_HIDDEN_PRINT': true,
  'EMPTY_FORCEPREVIEW_SYSTEM_PRINT': false,
  'EMPTY_FORCEPREVIEW_READ_ONLY_PRINT': true,

  'EMPTY_NOPREVIEW_EDITABLE_EDIT': true,
  'EMPTY_NOPREVIEW_HIDDEN_EDIT': false,
  'EMPTY_NOPREVIEW_SYSTEM_EDIT': false,
  'EMPTY_NOPREVIEW_READ_ONLY_EDIT': true,

  'EMPTY_NOPREVIEW_EDITABLE_PREVIEW': false,
  'EMPTY_NOPREVIEW_HIDDEN_PREVIEW': false,
  'EMPTY_NOPREVIEW_SYSTEM_PREVIEW': false,
  'EMPTY_NOPREVIEW_READ_ONLY_PREVIEW': false,

  'EMPTY_NOPREVIEW_EDITABLE_PRINT': false,
  'EMPTY_NOPREVIEW_HIDDEN_PRINT': false,
  'EMPTY_NOPREVIEW_SYSTEM_PRINT': false,
  'EMPTY_NOPREVIEW_READ_ONLY_PRINT': false
};

FormWrapper.IS_PREVIEW = {
  'EDIT_EDITABLE': false,
  'EDIT_HIDDEN': false,
  'EDIT_SYSTEM': false,
  'EDIT_READ_ONLY': true,
  'PREVIEW': true
};

FormWrapper.DISPLAY_TYPE_READ_ONLY = 'READ_ONLY';
FormWrapper.FORM_VIEW_MODE_EDIT = 'EDIT';
FormWrapper.FORM_VIEW_MODE_PREVIEW = 'PREVIEW';
FormWrapper.FORM_VIEW_MODE_PRINT = 'PRINT';
