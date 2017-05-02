import {Component, View, Inject, NgTimeout} from 'app/app';
import {Configurable} from 'components/configurable';
import {Select} from 'components/select/select';
import {InstanceSelect} from 'components/select/instance/instance-select';
import {FormWrapper} from 'form-builder/form-wrapper';
import {TranslateService} from 'services/i18n/translate-service';
import {ContextSelector} from 'components/contextselector/context-selector';
import {DefinitionService} from 'services/rest/definition-service';
import {InstanceRestService} from 'services/rest/instance-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {InstanceObject} from 'idoc/idoc-context';
import {ModelsService} from 'services/rest/models-service';
import {PropertiesRestService} from 'services/rest/properties-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {AfterFormValidationEvent} from 'form-builder/validation/after-form-validation-event';
import {InstanceCreateConfigurationEvent} from './instance-create-configuration-event';
import _ from 'lodash';
import template from './instance-create-configuration.html!text';

@Component({
  selector: 'instance-create-configuration',
  properties: {
    'config': 'config'
  },
  events: ['onFormLoaded']
})

@View({
  template: template
})
@Inject(DefinitionService, InstanceRestService, PromiseAdapter, TranslateService, ModelsService, NgTimeout, Eventbus, PropertiesRestService)
export class InstanceCreateConfiguration extends Configurable {

  constructor(definitionService, instanceRestService, promiseAdapter, translateService, modelsService, $timeout, eventbus, propertiesService) {
    super({});
    this.definitionService = definitionService;
    this.instanceRestService = instanceRestService;
    this.promiseAdapter = promiseAdapter;
    this.translateService = translateService;
    this.modelsService = modelsService;
    this.$timeout = $timeout;
    this.eventbus = eventbus;
    this.propertiesService = propertiesService;
    this.validationEvents = [];
    this.processedProperties = [];
  }

  ngOnInit() {
    this.config.formConfig.models.onContextSelected = (contextId) => {
      this.onContextSelected(contextId);
    };
    this.onContextSelected(this.config.formConfig.models.parentId);
  }

  /**
   * Auto-selects either the default type option if present or assigns predefined object type. The latter is with higher priority.
   */
  assignType() {
    if (this.config.instanceType) {
      this.instanceType = this.config.instanceType;
      this.onTypeSelected(this.instanceType);
    } else {
      this.types.some((type) => {
        if (type.default) {
          this.instanceType = type.id;
          this.onTypeSelected(type.id);
          return true;
        }
        return false;
      });
    }
  }

  onTypeSelected(type) {
    if (type && type.length > 0) {
      var option = _.find(this.types, function (current) {
        return current.id === type;
      });

      this.type = option;

      this.config.formConfig.models.definitionId = null;
      this.config.formConfig.models.viewModel = null;

      if (option.subtypes) {
        this.loadSubTypes(option.subtypes);
      }

      if (option.definitionId) {
        this.loadDefinitionModel(option.definitionId);
      }
    }
    this.clearData();
  }

  onContextSelected(contextId) {
    this.modelsService.getModels(this.config.purpose, contextId, this.config.mimetype, this.config.fileExtension, this.config.classFilter, this.config.definitionFilter).then((data)=> {
      this.config.formConfig.models.errorMessage = data.errorMessage;
      this.models = data.models;
      this.types = this.convertModelsToOptions(this.models);

      var options = this.types.slice();
      options.unshift({id: '', text: ''});

      this.instanceTypeConfig = {
        width: '100%',
        placeholder: this.translateService.translateInstant('select.newobjecttype.placeholder'),
        data: options,
        disabled: !!this.config.instanceType
      };

      // timeout is added because instance-select recreates the element after the data is loaded so setting default value must be done on the next digest cycle
      this.$timeout(() => {
        this.assignType();
      });

      // Publish instance create configuration event after the main types have been loaded
      this.eventbus.publish(new InstanceCreateConfigurationEvent({
        models: this.models
      }));
    });
  }

  onSubTypeSelected(subType) {
    if (subType && subType.length > 0) {
      this.loadDefinitionModel(subType);
    }
    this.clearData();
  }

  loadSubTypes(subtypes) {
    var options = subtypes.slice();
    options.unshift({id: '', text: ''});

    this.instanceSubTypeConfig = {
      width: '100%',
      placeholder: this.translateService.translateInstant('select.newobjecttype.placeholder'),
      data: options,
      disabled: !!this.config.instanceSubType
    };
    //Clear selected sub type before loading. There is scenario where it is populated.
    //Scenario is:
    //1. Select select type and subtype.
    //2. Select type without subtype.
    //3. Select type from 1.
    //see  CMF-23522
    this.instanceSubType = null;
    this.$timeout(() => {
      if (this.config.instanceSubType) {
        this.instanceSubType = this.config.instanceSubType;
        this.loadDefinitionModel(this.instanceSubType);
      }
    });
  }

  loadDefinitionModel(definitionId) {
    if (this.config.suggestedProperties) {
      this.config.suggestedProperties.clear();
    }

    let requests = [];
    requests.push(this.instanceRestService.loadDefaults(definitionId, this.config.formConfig.models.parentId));
    requests.push(this.definitionService.getDefinitions(definitionId));
    this.promiseAdapter.all(requests).then((responses) => {
      this.onDefinitionModelLoaded(responses, definitionId);
    });
  }

  onDefinitionModelLoaded(responses, definitionId) {
    let instanceData = responses[0].data;
    let definitionData = responses[1].data[definitionId];

    definitionData.headers = instanceData.headers;

    let instanceObject = new InstanceObject(definitionId, definitionData);
    instanceObject.mergePropertiesIntoModel(instanceData.properties);

    if (this.config.instanceData) {
      // Overriding default properties values with ones from predefined instance data
      instanceObject.setPropertiesValue(this.config.instanceData.properties);
    }

    // Models is passed by reference from the outside so assigning it directly to instanceObject.getModels() will produce an error
    this.config.formConfig.models.headers = instanceData.headers;
    this.config.formConfig.models.instanceType = instanceData.instanceType;
    this.config.formConfig.models.validationModel = instanceObject.getModels().validationModel;
    this.config.formConfig.models.viewModel = instanceObject.getModels().viewModel;
    this.config.formConfig.models.definitionId = definitionId;
    this.config.formConfig.models.definitionLabel = instanceObject.getModels().definitionLabel;
    this.config.formConfig.models.instanceId = instanceData.id;
    this.config.formViewMode = FormWrapper.FORM_VIEW_MODE_EDIT;

    let event = this.eventbus.subscribe(AfterFormValidationEvent, (data) => this.populateMandatoryObjectsValues(data, instanceData.parentId));
    this.validationEvents.push(event);

    this.onFormLoaded({
      event: {
        models: this.config.formConfig.models,
        type: this.instanceType
      }
    });
  }

  populateMandatoryObjectsValues(eventData, parentId) {
    if (!parentId) {
      return;
    }
    let data = eventData[0];
    let properties = data.viewModel;
    if (properties) {
      Object.keys(properties).forEach((key) => {
        let localProperty = properties[key];
        if (this.isMandatoryObjectProperty(localProperty) && this.isNotProcessing(localProperty.identifier)) {
          this.processedProperties.push(localProperty.identifier);

          this.propertiesService.loadObjectPropertiesSuggest(parentId, this.getPropertyRange(localProperty), localProperty.multivalue).then((response) => {
            let result = response.data;
            if (data.validationModel[localProperty.identifier].value instanceof Array) {
              data.validationModel[localProperty.identifier].value = data.validationModel[localProperty.identifier].value.concat(result);
            } else {
              data.validationModel[localProperty.identifier].value = result;
            }

            if (this.config.suggestedProperties) {
              this.config.suggestedProperties.set(localProperty.identifier, result);
            }
          });
        }
      });
    }
  }

  isMandatoryObjectProperty(property) {
    return property.isMandatory && property.control && property.control.controlParams && property.control.controlParams.range;
  }

  getPropertyRange(property) {
    return property.control.controlParams.range;
  }

  convertModelsToOptions(models) {
    var options = [];

    var classesMap = {};

    // locate the classes
    models.forEach(function (model) {
      if (model.type === ModelsService.TYPE_CLASS) {
        var option = {
          id: model.id,
          text: model.label,
          default: model.default
        };

        options.push(option);
        classesMap[model.id] = option;
      }
    });

    this.attachChildrenDefinitions(models, classesMap);

    this.attachTopLevelDefinitions(options);

    return this.removeClassesWithoutDefinition(options);
  }

  attachChildrenDefinitions(models, classesMap) {
    models.forEach(function (model) {
      if (model.type === ModelsService.TYPE_DEFINITION) {
        var clazz = classesMap[model.parent];
        if (!clazz.subtypes) {
          clazz.subtypes = [];
        }

        clazz.subtypes.push({
          id: model.id,
          text: model.label,
          parent: model.parent
        });
      }
    });
  }

  /*
   * Find classes with single definition and attach it to the class directly
   */
  attachTopLevelDefinitions(options) {
    options.forEach(function (option) {
      if (option.subtypes && option.subtypes.length === 1) {
        option.definitionId = option.subtypes[0].id;
        option.subtypes = undefined;
      }
    });
  }

  removeClassesWithoutDefinition(options) {
    var result = [];
    options.forEach(function (option) {
      if (option.definitionId || option.subtypes) {
        result.push(option);
      }
    });

    return result;
  }

  isSubTypeDropdownVisible() {
    return !!this.type && this.type.subtypes !== undefined;
  }

  ngOnDestroy() {
    this.clearData();
  }

  clearData() {
    for (let event of this.validationEvents) {
      event.unsubscribe();
    }
    this.validationEvents = [];
    this.processedProperties = [];
  }

  isNotProcessing(id) {
    return this.processedProperties.indexOf(id) === -1;
  }

  toggleShowAllProperties() {
    this.config.showAllProperties = !this.config.showAllProperties;
    this.config.renderMandatory = !this.config.renderMandatory;
  }
}

