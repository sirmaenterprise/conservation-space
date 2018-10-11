import {Component, View, Inject, NgTimeout} from 'app/app';
import {Configurable} from 'components/configurable';
import {HEADER_COMPACT} from 'instance-header/header-constants';
import {FormWrapper} from 'form-builder/form-wrapper';
import {ModelUtils} from 'models/model-utils';
import {TranslateService} from 'services/i18n/translate-service';
import {DefinitionService} from 'services/rest/definition-service';
import {InstanceRestService} from 'services/rest/instance-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {InstanceObject} from 'models/instance-object';
import {ModelsService} from 'services/rest/models-service';
import {PropertiesRestService} from 'services/rest/properties-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {AfterFormValidationEvent} from 'form-builder/validation/after-form-validation-event';
import {CONTEXT_VALIDATED} from 'create/instance-create-panel';
import {TEMPLATE_ID} from 'idoc/template/template-constants';
import 'components/select/instance/instance-select';
import {CONTEXT_CHANGED_EVENT, ADD_CONTEXT_ERROR_MESSAGE_COMMAND} from 'components/contextselector/context-selector';

import 'idoc/template/idoc-template-selector';
import _ from 'lodash';
import template from './instance-create-configuration.html!text';
import './instance-create-configuration.css!';

@Component({
  selector: 'instance-create-configuration',
  properties: {
    'config': 'config'
  },
  events: ['onFormLoaded', 'onConfigurationCompleted']
})

@View({
  template
})
/**
 *  Component used for selection of type, sub type, template and etc.
 *
 *  Interaction with component:
 *  The component is configured to listening for {@link CONTEXT_CHANGED_EVENT} when event occurred component will reload
 *  models available for selected context.
 */
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
    this.validationEventsSubscriptions = [];
  }

  ngOnInit() {
    this.onContextSelected(this.config.formConfig.models.parentId).then(() => {
      this.contextChangedHandler = this.config.eventEmitter.subscribe(CONTEXT_CHANGED_EVENT, this.onContextSelected.bind(this));
    });
  }

  /**
   * Auto-selects either the default type option if present or assigns predefined object type. The latter is with higher priority.
   */
  assignType() {
    // if clone instance or create instance with predefined value load selected type
    if (this.config.instanceType) {
      this.instanceType = this.config.instanceType;
      this.onTypeSelected(this.instanceType);
      // if there's value already selected by user don't reload the form
    } else if (this.config.formConfig.models.instanceType) {
      // refresh the type selector only if current selected type is found in the refreshed models
      let isAllowed = InstanceCreateConfiguration.isPresentType(this.types, this.config.formConfig.models.definitionId);
      if (!isAllowed) {
        this.onTypeSelected(this.instanceType);
      } else {
        // Apply the last selected subtype. This is needed in current scenario:
        // A subtype OT210001 is selected, then a context with restricted subtypes (allowed children filtering) is
        // chosen and the only possible subtype ET110002 is rendered in the form (the subtype field should be hidden in
        // this stage). Then the context is cleared. It is expected then the rendered subtype ET110002 and the form to
        // remain unchanged. If we don't do this, the subtype combo would show the previous selection OT210001 but the
        // form would render ET110002 which is wrong.
        this.instanceSubType = this.config.formConfig.models.definitionId;
      }
      // load default types
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

  static isPresentType(types, definitionId) {
    return types.some((type) => {
      if (type.subtypes && type.subtypes.length) {
        return type.subtypes.some((subtype) => {
          return subtype.id === definitionId;
        });
      } else {
        if (type.definitionId === definitionId) {
          return true;
        }
      }
    });
  }

  onTypeSelected(type) {
    if (type && type.length > 0 && this.types && this.types.length > 0) {

      this.refreshCurrentType(type);

      // Fix for scroll issue. See CMF-25168
      this.$timeout(() => {
        this.config.formConfig.models.definitionId = null;
        this.config.formConfig.models.viewModel = null;
      });

      if (this.type && this.type.subtypes) {
        this.loadSubTypes(this.type.subtypes);
      }

      if (this.type && this.type.definitionId) {
        this.loadDefinitionModel(this.type.definitionId);
      }
    } else {
      this.type = null;
      this.instanceSubType = null;
      this.config.formConfig.models.definitionId = null;
      this.config.formConfig.models.viewModel = null;
    }
    this.clearData();
  }

  onContextSelected(contextId) {
    return this.modelsService.getModels(this.config.purpose, contextId, this.config.mimetype, this.config.fileExtension, this.config.classFilter, this.config.definitionFilter).then((data) => {
      this.config.eventEmitter.publish(CONTEXT_VALIDATED, data);
      if (data.errorMessage) {
        this.config.eventEmitter.publish(ADD_CONTEXT_ERROR_MESSAGE_COMMAND, data.errorMessage);
      }
      this.models = data.models;
      this.types = this.convertModelsToOptions(this.models);

      if (this.types && this.types.length === 0) {
        this.config.eventEmitter.publish(ADD_CONTEXT_ERROR_MESSAGE_COMMAND, 'idoc.dialog.no.permissions');
      }

      let options = this.types.slice();
      options.unshift({id: '', text: ''});

      // when only one option is allowed, set it as default CMF-27724.
      if (this.types.length === 1) {
        this.types[0].default = true;
      }
      // when context is touched, then models are reloaded and the current type and sub types needs to be refreshed
      // with the actual data (subtypes)
      if (this.type) {
        this.refreshCurrentType(this.type.id);
        // type have to be checked again, because it can be undefined after refresh of current type.
        this.type && this.type.subtypes && this.loadSubTypes(this.type.subtypes);
      }

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

      this.onConfigurationCompleted({
        event: {
          models: this.models
        }
      });
    });
  }

  onSubTypeSelected(subType) {
    if (subType && subType.length > 0) {
      this.loadDefinitionModel(subType);
    }
    this.clearData();
  }

  refreshCurrentType(type) {
    this.type = _.find(this.types, function (current) {
      return current.id === type;
    });
  }

  loadSubTypes(subtypes) {
    let options = subtypes.slice();
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
    if (this.config.instanceSubType) {
      this.instanceSubType = this.config.instanceSubType;
      this.loadDefinitionModel(this.instanceSubType);
    }
  }

  loadDefinitionModel(definitionId) {
    if (this.config.suggestedProperties) {
      this.config.suggestedProperties.clear();
    }

    let requests = [];
    requests.push(this.instanceRestService.loadDefaults(definitionId, this.config.formConfig.models.parentId));
    requests.push(this.definitionService.getDefinitions(definitionId));
    this.promiseAdapter.all(requests).then((responses) => {
      // Timeout is needed to queue method execution to let the rendering threads catch up.
      // This help some browsers (FF, IE) to show more than one form correct.
      this.$timeout(() => {
        this.onDefinitionModelLoaded(responses, definitionId);
      });
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

    this.config.renderMenu = () => {
      return true;
    };

    let eventSubscription = this.eventbus.subscribe(AfterFormValidationEvent, (data) => {
      this.populateMandatoryObjectsValues(data, instanceData.parentId);
    });
    this.validationEventsSubscriptions.push(eventSubscription);

    this.onFormLoaded({
      event: {
        models: this.config.formConfig.models,
        type: this.instanceType
      }
    });

    this.instanceObject = instanceObject;
  }

  initViewFromTemplate(event) {
    // Added specific check for the default(blank) template id. If it is
    // selected when the instance is created or it is changed by executing an
    // action, the logic will detect that and handle it properly, instead of
    // setting the primary template to the instance.
    if (event.template.templateInstanceId) {
      this.selectedTemplate = event.template.id;
      ModelUtils.updateObjectPropertyValue(this.config.formConfig.models.validationModel[TEMPLATE_ID], true, [event.template.templateInstanceId]);
    } else {
      ModelUtils.updateObjectPropertyValue(this.config.formConfig.models.validationModel[TEMPLATE_ID], true, [event.template.id]);
    }
  }

  populateMandatoryObjectsValues([data], parentId) {
    if (!parentId) {
      return;
    }
    let properties = data.viewModel || {};
    Object.keys(properties).forEach((key) => {
      let localProperty = properties[key];
      let control = ModelUtils.getControl(localProperty.control, localProperty.controlId);

      let isMandatory = InstanceCreateConfiguration.isMandatoryObjectProperty(localProperty, control);
      if (isMandatory) {
        let shouldBeProcessed = InstanceCreateConfiguration.shouldProcessProperty(localProperty, data);
        let isSuggested = data.validationModel[localProperty.identifier].isSuggested;
        if (shouldBeProcessed && !isSuggested) {
          // isSuggested flag is saved in validation model because there are situation (multiple upload for example) when
          // more than one item with same definition and fields are loaded and we need reliable way to detect which
          // property is already processed
          data.validationModel[localProperty.identifier].isSuggested = true;

          this.propertiesService.loadObjectPropertiesSuggest(parentId, InstanceCreateConfiguration.getPropertyRange(control), localProperty.multivalue).then((response) => {
            let result = response.data;
            result.forEach((instance) => {
              // add the suggested relation and its header in the object property value
              ModelUtils.updateObjectProperty(data.validationModel, localProperty.identifier, instance.id);
              ModelUtils.setObjectPropertyHeader(data.validationModel, localProperty.identifier, instance.id, instance.headers && instance.headers[HEADER_COMPACT], HEADER_COMPACT);
            });
            if (this.config.suggestedProperties) {
              this.config.suggestedProperties.set(localProperty.identifier, result);
            }
          });
        }
      }
    });
  }

  static isMandatoryObjectProperty(property, control) {
    return !!(property.isMandatory && control && control.controlParams && control.controlParams.range);
  }

  static getPropertyRange(control) {
    return control.controlParams.range;
  }

  convertModelsToOptions(models) {
    let options = [];

    let classesMap = {};

    // locate the classes
    models.forEach(function (model) {
      if (model.type === ModelsService.TYPE_CLASS) {
        let option = {
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
        let clazz = classesMap[model.parent];
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
    let result = [];
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
    this.contextChangedHandler.unsubscribe();
  }

  clearData() {
    _.forEach(this.validationEventsSubscriptions, function (subscription) {
      subscription.unsubscribe();
    });

    this.validationEventsSubscriptions = [];
  }

  /**
   * Validates whether the property is applicable for processing.
   *  For example: single select object properties who have a value already,
   *  are not eligible for further property suggest
   *
   * @param localProperty
   * @param data
   * @returns {boolean}
   */
  static shouldProcessProperty(localProperty, data) {
    let propertyValue = data.validationModel[localProperty.identifier].value;
    let hasValue = propertyValue && propertyValue.results && propertyValue.results.length;
    return localProperty.multivalue || !hasValue;
  }

  toggleShowAllProperties() {
    this.config.showAllProperties = !this.config.showAllProperties;
    this.config.renderMandatory = !this.config.renderMandatory;
  }
}