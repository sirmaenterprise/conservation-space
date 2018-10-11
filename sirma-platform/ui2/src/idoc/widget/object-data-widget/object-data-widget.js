import {View, Inject, NgScope, NgCompile, NgElement} from 'app/app';
import {Widget} from 'idoc/widget/widget';
import {Eventbus} from 'services/eventbus/eventbus';
import {WidgetReadyEvent} from 'idoc/widget/widget-ready-event';
import {Refreshable} from 'components/refreshable';
import {FormWrapper} from 'form-builder/form-wrapper';
import {ModelUtils} from 'models/model-utils';
import {InstanceObject} from 'models/instance-object';
import {InstanceModel} from 'models/instance-model';
import {DefinitionModel} from 'models/definition-model';
import {MODE_PREVIEW} from 'idoc/idoc-constants';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {
  SELECT_OBJECT_AUTOMATICALLY,
  SELECT_OBJECT_MANUALLY,
  SELECT_OBJECT_CURRENT
} from 'idoc/widget/object-selector/object-selector';
import {ObjectSelectorHelper} from 'idoc/widget/object-selector/object-selector-helper';
import {PropertiesSelectorHelper, COMMON_PROPERTIES} from 'idoc/widget/properties-selector/properties-selector-helper';
import {StatusCodes} from 'services/rest/status-codes';
import 'instance-header/static-instance-header/static-instance-header';
import {CONTROL_TYPE} from 'models/model-utils';
import _ from 'lodash';
import 'idoc/widget/object-data-widget/object-data-widget.css!';
import template from 'idoc/widget/object-data-widget/object-data-widget.html!text';

export const NO_LINK = 'none';

@Widget
@View({
  template
})
@Inject(NgScope, PromiseAdapter, ObjectSelectorHelper, Eventbus, NgCompile, NgElement, PropertiesSelectorHelper)
export class ObjectDataWidget extends Refreshable {

  constructor($scope, promiseAdapter, objectSelectorHelper, eventbus, $compile, $element, propertiesSelectorHelper) {
    super(eventbus);
    this.promiseAdapter = promiseAdapter;
    this.objectSelectorHelper = objectSelectorHelper;
    this.propertiesSelectorHelper = propertiesSelectorHelper;
    this.$scope = $scope;
    this.$compile = $compile;
    this.$element = $element;

    this.config = this.config || {};
    this.config.collapsibleRegions = true;
    this.config.showAllProperties = false;
    this.config.hintClass = 'form-tooltip';
    this.config.enableHint = true;

    this.formConfig = this.formConfig || {};
    this.formConfig.eventEmitter = this.control;

    // Handle old instances where widget config has different format
    Object.keys(this.config.selectedProperties).forEach((definition) => {
      this.config.selectedProperties[definition] = PropertiesSelectorHelper.transformSelectedProperies(this.config.selectedProperties[definition]);
    });

    if (this.isLinkToInstanceVisible()) {
      this.initObjectLinkConfig();
    }

    this.$scope.$watch(() => {
      return this.config.selectedPropertiesData;
    }, () => {
      this.updatePropertyModel();
      if (this.reloadModel) {
        this.config.shouldReload = true;
        this.reloadModel = false;
      }
    });

    // In modeling mode the widgets are rendered in preview mode to prevent data altering.
    if (!this.context.isModeling()) {
      $scope.$watch(() => {
        return this.context.getMode();
      }, () => {
        this.config.formViewMode = this.context.getMode().toUpperCase();
        if (!this.showAll()) {
          this.config.showAllProperties = false;
        }
      });
    } else {
      this.config.formViewMode = MODE_PREVIEW.toUpperCase();
    }

    $scope.$watchCollection(() => {
      let watchConditions = [this.config.selectObjectMode, this.config.selectedObject];
      if (this.config.selectObjectMode === SELECT_OBJECT_AUTOMATICALLY) {
        watchConditions.push(this.config.criteria);
      }
      return watchConditions;
    }, () => {
      this.loadModels();
    });

    $scope.$watch(() => {
      return this.config.instanceLinkType;
    }, () => {
      this.initObjectLinkConfig();
      this.compileInstanceHeader();
    });

    $scope.$watch(() => {
      return this.modelsAreLoaded();
    }, (newValues) => {
      if (newValues) {

        let formInitializedHandler = this.formConfig.eventEmitter.subscribe('formInitialized', () => {
          formInitializedHandler.unsubscribe();
          this.formInitialized = true;
          this.publishWidgetReadyEvent();
        });
      }
    });
  }

  publishWidgetReadyEvent() {
    if (this.formInitialized && (this.isLinkToInstanceVisible() ? this.objectLinkLoaded : true)) {
      this.eventbus.publish(new WidgetReadyEvent({
        widgetId: this.control.getId()
      }));
    }
  }

  updatePropertyModel() {
    let flatViewModel = _.get(this.formConfig, 'models.viewModel.flatModelMap');
    if (flatViewModel) {
      for (let propertyId in flatViewModel) {
        if (flatViewModel.hasOwnProperty(propertyId)) {
          this.addCodelistControl(flatViewModel[propertyId]);
        }
      }
    }
  }

  initObjectLinkConfig() {
    this.objectLinkConfig = this.objectLinkConfig || {loaded: true};

    let objectLinkSubscription = this.control.subscribe('loaded', () => {
      objectLinkSubscription.unsubscribe();
      this.objectLinkLoaded = true;
      this.publishWidgetReadyEvent();
    });
  }

  getControlTypeFromPropertyData(property) {
    let definitionId = this.formConfig.models.definitionId;
    let propertiesData = this.config.selectedPropertiesData && this.config.selectedPropertiesData[definitionId];
    if (propertiesData) {
      return propertiesData[property.identifier];
    }
  }

  /**
   * Add controlId to specific property. The control identifier is a form-wrapper component and
   * specify the way of property visualization.
   * @param property
   */
  addCodelistControl(property) {
    if (property.codelist) {
      let controlType = this.getControlTypeFromPropertyData(property);
      if (controlType) {
        property.modelProperty.controlId = controlType;
        // identify the form wrapper that model is changed
        this.reloadModel = true;
      } else if (property.modelProperty.controlId === CONTROL_TYPE.CODELIST_LIST) {
        property.modelProperty.controlId = undefined;
        this.reloadModel = true;
      }
    }
  }

  refresh(data) {
    if (this.selectedObjectId === data.objectId) {
      this.config.shouldReload = true;
    }
  }

  showAll() {
    return this.config.formViewMode !== FormWrapper.FORM_VIEW_MODE_PRINT;
  }

  isLinkToInstanceVisible() {
    return this.config.instanceLinkType && this.config.instanceLinkType !== NO_LINK;
  }

  modelsAreLoaded() {
    return this.formConfig.models !== undefined;
  }

  /**
   * Loads models for the selected object if there is one
   */
  loadModels() {
    delete this.formConfig.models;
    delete this.errorMessage;

    return this.objectSelectorHelper.getSelectedObject(this.config, this.context).then((selectedObject) => {
      if (selectedObject) {
        return this.context.getSharedObject(selectedObject, this.control.getId(), true).then((objectInstance) => {
          this.selectedObject = objectInstance;
          this.selectedObjectId = selectedObject;
          this.formConfig.writeAllowed = objectInstance.writeAllowed;
          this.addFormConfigModel(objectInstance);
          this.updatePropertyModel();

          if (this.context.isModeling()) {
            if (this.config.selectObjectMode === SELECT_OBJECT_CURRENT || this.config.selectObjectMode === SELECT_OBJECT_AUTOMATICALLY) {
              // when criteria matches a single object then clear its model values
              ObjectDataWidget.removeModelValues(this.formConfig.models.validationModel._instanceModel);
            }
          }
        }).catch((error) => {
          if (error && error.status === StatusCodes.NOT_FOUND && this.config.selectObjectMode === SELECT_OBJECT_MANUALLY) {
            this.objectSelectorHelper.removeSelectedObjects(this.config, [selectedObject]);
            this.control.saveConfig(this.config);
          }
          throw error;
        });
      }
    }).catch((rejection) => {
      if (this.context.isModeling() && this.config.selectObjectMode === SELECT_OBJECT_AUTOMATICALLY) {
        // if 0 or more than 1 objects are returned by the current criteria, then a dummy model should be built
        if (rejection.noResults || rejection.multipleResults) {
          return this.buildFakeModel();
        }
      }
      // If no object is selected to be displayed mark widget as ready for print
      this.formInitialized = true;
      // if instance link is visible by config, mark it loaded too.
      if (this.isLinkToInstanceVisible()) {
        this.objectLinkLoaded = true;
      }
      this.publishWidgetReadyEvent();
      this.errorMessage = rejection.reason;
    });
  }

  /**
   * In automatic selection mode the user could select one or more types:
   * - In case of single selected type, only the selected properties for that type should be get.
   * - In case of multiple selected types then the first selected type with properties will be get. It's not possible in
   * this case for us to know what object type would be matched from the search or even if it would be a single object
   * or not (this would be an error in real object). In templating mode though it is necessary for the user to see how
   * the widget would look like, so it is built with a fake model.
   *
   * @param objectInstance
   * @returns {*}
   */
  buildFakeModel(objectInstance) {
    let selectedProperties = ObjectDataWidget.getFirstTypeWithSelectedProperties(this.config.selectedProperties);
    let promotedSelectedType = Object.keys(selectedProperties)[0];
    return this.propertiesSelectorHelper.collectPropertiesLabels(selectedProperties).then((collectedLabels) => {
      this.formConfig.models = {
        viewModel: {},
        validationModel: {},
        definitionId: promotedSelectedType
      };
      ObjectDataWidget.recreateModels(this.formConfig.models, objectInstance);
      let fakeViewModel = ModelUtils.createViewModel();
      let fakeValidationModel = ModelUtils.createInstanceModel();
      let flattenSelectedProperties = _.values(selectedProperties)[0] || [];
      flattenSelectedProperties.forEach((name) => {
        let label = ObjectDataWidget.getLabel(collectedLabels, name);
        ModelUtils.addField(fakeViewModel, ModelUtils.createField(name, 'READ_ONLY', 'text', label, false, true, []));
        ModelUtils.addProperty(fakeValidationModel, name, ModelUtils.createProperty('', true));
      });
      this.formConfig.models.viewModel = new DefinitionModel(fakeViewModel);
      this.formConfig.models.validationModel = new InstanceModel(fakeValidationModel);
    });
  }

  /**
   * ODW can display a single object only. That's why it is needed in modeling mode to build a custom model for one of
   * the selected types.
   * Find the first type in the selected properties map where there are selected properties and return a custom map with
   * that type's selected properties skipping the COMMON_PROPERTIES key.
   *
   * @param selectedProperties The original selection that the user made.
   * @returns {{}}
   */
  static getFirstTypeWithSelectedProperties(selectedProperties) {
    let result = {};
    Object.keys(selectedProperties).some((key) => {
      if (key === COMMON_PROPERTIES) {
        return false;
      }
      if (Object.keys(selectedProperties[key]).length > 0) {
        result[key] = Object.keys(selectedProperties[key]);
        return true;
      }
      return false;
    });
    return result;
  }

  static getLabel(labels, id) {
    let found;
    labels.some((labelObj) => {
      if (labelObj.name === id) {
        found = labelObj;
        return true;
      }
      return false;
    });
    return found ? found.labels.join(', ') : '';
  }

  static recreateModels(models, objectInstance) {
    let _objectInstance = objectInstance;
    if (!_objectInstance) {
      _objectInstance = new InstanceObject(
        'fakeInstance', {
          validationModel: {},
          viewModel: {}
        }, null, false);
    }
    let originalModel = _objectInstance.getModels();
    Object.keys(originalModel).forEach((name) => {
      if (models[name]) {
        return;
      }
      let property = originalModel[name];
      if (name === 'viewModel') {
        models[name] = property.clone();
      } else {
        models[name] = property;
      }
    });
  }

  addFormConfigModel(objectInstance) {
    this.formConfig.models = {};
    let originalModel = objectInstance.getModels();
    for (let modelProp in originalModel) {
      if (originalModel.hasOwnProperty(modelProp)) {
        if (modelProp === 'viewModel') {
          this.formConfig.models[modelProp] = originalModel[modelProp].clone();
        } else {
          this.formConfig.models[modelProp] = originalModel[modelProp];
        }
      }
    }
  }

  static removeModelValues(model) {
    Object.keys(model).forEach((name) => {
      model[name].value = null;
      model[name].defaultValue = null;
      model[name].valueLabel = null;
      model[name].defaultValueLabel = null;
    });
  }

  compileInstanceHeader() {
    if (this.newWidgetScope) {
      this.newWidgetScope.$destroy();
    }
    this.newWidgetScope = this.$scope.$new();

    let instanceHeader = $(`<seip-static-instance-header header-type="objectDataWidget.config.instanceLinkType"
      header="objectDataWidget.selectedObject.getHeader(objectDataWidget.config.instanceLinkType)" 
      config="objectDataWidget.objectLinkConfig" event-emitter="objectDataWidget.objectLinkEventEmitter"></seip-static-instance-header>`);

    $(this.control.element.find('.selected-object-header')).empty().append(instanceHeader);
    this.$compile(instanceHeader)(this.newWidgetScope)[0];
  }

  toggleShowAllProperties() {
    this.config.showAllProperties = !this.config.showAllProperties;
  }

  ngOnDestroy() {
    super.ngOnDestroy();

    if (this.element) {
      this.element.empty();
      this.element.remove();
    }

    // preventing memory leaks
    this.config = null;
    this.context = null;
    this.formConfig = null;
    this.newWidgetScope.$destroy();
  }
}