import {Inject, Injectable, NgTimeout} from 'app/app';
import $ from 'jquery';
import {FieldValidator} from 'form-builder/validation/field-validator';
import {PropertiesRestService} from 'services/rest/properties-service';
import {LabelRestService} from 'services/rest/label-service';
import {Configuration} from 'common/application-config';
import {BeforeIdocSaveEvent} from 'idoc/actions/events/before-idoc-save-event';
import {MomentAdapter} from 'adapters/moment-adapter';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {ModelUtils} from 'models/model-utils';
import {Eventbus} from 'services/eventbus/eventbus';
import {HEADER_BREADCRUMB} from 'instance-header/header-constants';
import {HeadersService} from 'instance-header/headers-service';
import _ from 'lodash';
import {InstanceObject} from 'models/instance-object';
import {CodelistFilterProvider} from 'form-builder/validation/related-codelist-filter/codelist-filter-provider';
import {CodelistRestService} from 'services/rest/codelist-service';
import {Logger} from 'services/logging/logger';

export const DEFAULT_VALUE_PATTERN = 'default_value_pattern';

const DELIMITER = '|';

/**
 *  Calculation validator can be used for value calculation according to different rules.
 *  Calculation validator can be used ONLY if instance is not persisted.
 *  This validator doesn't modify view model properties in any way but only the property values.
 *  It allows many types of calculation rules to be applied. Rules can be synchronous or asynchronous.
 *  Validator get bindings from the rule context and resolve as many bindings as possible from the already loaded model. For the unresolved values (most of which will be object properties),
 *  call rest service with the bindings to evaluate the values. When values are evaluated, for each binding from the response find the placeholder in template passed as control-param in the view model
 *  and replace it with the respective value.
 *  Bindings can contain functions (expressions evaluated on server before instance persist). When template is evaluated for the first time this functions are replaced with labels. This labels are
 *  user friendly explanation of what the function can do.
 *  At the end when user saves the instance, in the field value template may still have unresolved placeholders (such as sequences) which will be resolved and populated before the actual persist.
 */
@Injectable()
@Inject(PropertiesRestService, Configuration, MomentAdapter, PromiseAdapter, LabelRestService, NgTimeout, Eventbus, HeadersService, Logger, CodelistFilterProvider, CodelistRestService)
export class Calculation extends FieldValidator {

  constructor(propertiesRestService, configuration, momentAdapter, promiseAdapter, labelRestService, $timeout, eventbus, headersService, logger, codelistFilterProvider, codelistRestService) {
    super();
    this.propertiesRestService = propertiesRestService;
    this.datePattern = configuration.get(Configuration.UI_DATE_FORMAT);
    this.momentAdapter = momentAdapter;
    this.promiseAdapter = promiseAdapter;
    this.labelRestService = labelRestService;
    this.$timeout = $timeout;
    this.eventbus = eventbus;
    this.headersService = headersService;
    this.eventsMap = new Map();
    this.functionsLabels = new Map();
    this.logger = logger;
    this.codelistFilterProvider = codelistFilterProvider;
    this.codelistRestService = codelistRestService;
  }

  // TODO: refactor this method
  validate(validatedFieldName, validatorDef, validationModel, flatModel, formControl, definitionId, instanceId) {

    return this.$timeout(() => {
      let bindings = validatorDef.rules[0].context.bindings;
      let definitionId = validatorDef.rules[0].context.definitionId;
      // If object is persisted, or field value is edited by user, or field is trying to suggest from itself we don't need suggestions
      if ((validationModel['identifier'] ? validationModel['identifier'].value !== 'NO_ID' : true) || flatModel[validatedFieldName].editedByUser || this.isLinkedToItself(bindings, flatModel[validatedFieldName].uri)) {
        return this.promiseAdapter.resolve(true);
      }

      this.clearEventsMap(definitionId);
      this.definitionId = definitionId;

      this.subscribeBeforeIdocSaveEvent(validatedFieldName, flatModel);
      let unresolvedBindings = bindings.slice();
      let template = ModelUtils.getControl(flatModel[validatedFieldName].control, DEFAULT_VALUE_PATTERN).controlParams.template;
      let fieldModel = Calculation.getEmptyPropertyValue(flatModel[validatedFieldName]);
      let headerLoaders = [];

      // Find out if some property from the model is bound to template, then calculate its value and replace its binding
      // in the template.
      Object.keys(flatModel).forEach((propertyName) => {
        let property = flatModel[propertyName];
        if (bindings.indexOf(property.uri) > -1) {
          // The property is bound to the template. Let's calculate its visible value.
          let uri = property.uri;
          fieldModel = validationModel[propertyName].value;

          // If binding is in dropdown (codelist) we need to get value label
          let propertyValue = validationModel[propertyName].valueLabel || fieldModel || '';
          let calculatedPropertyValue = propertyValue;

          if (ModelUtils.isPicker(property)) {
            let ids = ModelUtils.getObjectPropertyValue(validationModel[propertyName]);
            headerLoaders.push(this.headersService.loadHeaders(ids, HEADER_BREADCRUMB).then((headers) => {
              let headersString = Object.keys(headers).map((id) => {
                return $(headers[id][HEADER_BREADCRUMB]).text();
              }).join(', ');
              template = this.replaceInTemplate(template, uri, headersString || '');
              unresolvedBindings.splice(unresolvedBindings.indexOf(uri), 1);
            }));
          } else if (ModelUtils.isDatetime(property)) {
            // If binding is date/datetime we need to format value using configuration pattern
            calculatedPropertyValue = this.momentAdapter.format(propertyValue, this.datePattern) || '';
          } else if (ModelUtils.isCheckbox(property)) {
            calculatedPropertyValue = validationModel[propertyName].valueLabel || validationModel[propertyName].value;
          } else if (ModelUtils.isRadiobuttonGroup(property)) {
            let controlField = _.find(ModelUtils.getControl(flatModel[propertyName].control, flatModel[propertyName].controlId).controlFields, (field) => {
              return field.identifier === propertyValue;
            });
            calculatedPropertyValue = controlField.label;
          }

          if (!ModelUtils.isPicker(property)) {
            template = this.replaceInTemplate(template, uri, calculatedPropertyValue);
            unresolvedBindings.splice(unresolvedBindings.indexOf(uri), 1);
          }
        }
      });

      this.promiseAdapter.all(headerLoaders).then(() => {
        let payload = this.buildPayload(unresolvedBindings, flatModel, validationModel, validatedFieldName);
        let bindingsPromise;
        let functionsPromise;
        let filteredCodelist;

        if (payload.length !== 0) {
          bindingsPromise = this.propertiesRestService.evaluateValues(this.definitionId, payload);
        }
        if (validatorDef.rules[0].context.functions.length !== 0) {
          functionsPromise = this.labelRestService.getDefinitionLabels(validatorDef.rules[0].context.functions);
        }

        if (InstanceObject.isCodelistProperty(flatModel[validatedFieldName])) {
          const codelistFilters = this.codelistFilterProvider.getFilterConfig(instanceId || definitionId, validatedFieldName);
          let opts = {
            codelistNumber: flatModel[validatedFieldName].codelist
          };
          if (codelistFilters) {
            opts.filterBy = codelistFilters.filterBy;
            opts.inclusive = codelistFilters.inclusive;
            opts.filterSource = codelistFilters.filterSource;
            filteredCodelist = this.codelistRestService.getCodelist(opts);
          }
        }
        return this.promiseAdapter.all([bindingsPromise, functionsPromise, filteredCodelist]);
      }).then(([bindingValues, functionsLabels, codelistValues]) => {
        let data;

        if (bindingValues) {
          template = this.applyServerValues(bindingValues.data.data, template);
          data = bindingValues.data.data[0];
        }
        if (functionsLabels) {
          this.functionsLabels.set(validatedFieldName, functionsLabels.data);
        }

        if (ModelUtils.isText(flatModel[validatedFieldName])) {
          template = this.replaceFunctionsInTemplate(template, validatorDef.rules[0].context.functions, ModelUtils.getControl(flatModel[validatedFieldName].control, DEFAULT_VALUE_PATTERN).controlParams, this.functionsLabels.get(validatedFieldName));
          template = this.replaceUnresolvedBindings(template, unresolvedBindings);
          validationModel[validatedFieldName].value = template;
        } else if (ModelUtils.isRichtext(flatModel[validatedFieldName])) {
          template = this.replaceFunctionsInTemplate(template, validatorDef.rules[0].context.functions, ModelUtils.getControl(flatModel[validatedFieldName].control, DEFAULT_VALUE_PATTERN).controlParams, this.functionsLabels.get(validatedFieldName));
          template = this.replaceUnresolvedBindings(template, unresolvedBindings);
          validationModel[validatedFieldName].richtextValue = template;
          validationModel[validatedFieldName].value = template;
        } else {
          let hasValue = Calculation.hasValue(fieldModel);
          if (!hasValue && data) {
            let responseData = data.properties[0].propertyValue;
            // Parse value in model if suggest should be made from one object picked to another and object property is not part of current object
            if (InstanceObject.isObjectProperty(flatModel[validatedFieldName])) {
              fieldModel = this.parseObjectsBindingResponseToObjectValue(responseData);
            } else if (InstanceObject.isCodelistProperty(flatModel[validatedFieldName]) && codelistValues) {
              let sharedCodelistData = {};
              fieldModel = null;
              codelistValues.data.forEach((codelistItem) => {
                sharedCodelistData[codelistItem.value] = codelistItem.label;
                if (codelistItem.value === responseData) {
                  fieldModel = responseData;
                }
              });
              validationModel[validatedFieldName].sharedCodelistData = sharedCodelistData;
            } else {
              fieldModel = responseData;
            }
          }
          validationModel[validatedFieldName].value = _.cloneDeep(fieldModel);
        }
        return true;
      });
    }, 0);
  }

  static hasValue(fieldModel) {
    if (fieldModel && fieldModel.results && fieldModel.results.length) {
      return true;
    }
    return !!(fieldModel && !fieldModel.results);
  }

  static getEmptyPropertyValue(propertyViewModel) {
    if (ModelUtils.isPicker(propertyViewModel)) {
      return ModelUtils.getEmptyObjectPropertyValue();
    }
    return null;
  }

  /**
   * Parses response with objects.<br>
   * Example of <code>objectsBindingResponse</code>:
   * <pre>
   *     [{
   *        id: 'emf:0001',
   *        headers: {
   *          compact_header: '<span>.......</span>'
   *        }
   *     }
   *     ...
   *     ]
   * </pre>
   *
   * Example of result:
   * <pre>
   *     {
   *        add: ['emf:0001', ...],
   *        results: ['emf:0001', ...],
   *        headers: {
   *            'emf:0001': {
   *                id: 'emf:0001',
   *                compact_header: '<span>.......</span>'
   *            }
   *            ...
   *        }
   *     }
   * </pre>
   */
  parseObjectsBindingResponseToObjectValue(objectsBindingResponse) {
    let json = JSON.parse(objectsBindingResponse);
    let objectValue = ModelUtils.getEmptyObjectPropertyValue();
    if (json) {
      json.forEach((object) => {
        let id = object.id;
        objectValue.results.push(id);
        objectValue.add.push(id);
        objectValue.headers[id] = _.defaultsDeep(object.headers, {id});
        objectValue.total = objectValue.results.length;
      });
    }
    return objectValue;
  }

  isLinkedToItself(bindings, uri) {
    return bindings.indexOf(uri) > -1;
  }

  replaceUnresolvedBindings(template, unresolvedBindings) {
    unresolvedBindings.forEach((binding) => {
      template = this.replaceInTemplate(template, binding, '');
    });
    return template;
  }

  replaceFunctionsInTemplate(template, functions = [], controlParams, data) {
    functions.forEach((functionValue) => {
      let paramValue = '{' + controlParams[functionValue.toLowerCase()] + '}';
      let position = paramValue.length - 1;
      let toBeReplaced = [paramValue.slice(0, position), DELIMITER + functionValue, paramValue.slice(position)].join('');
      template = template.split(toBeReplaced).join(data[functionValue]);
    });
    return template;
  }

  applyServerValues(data, template) {
    data.forEach((element) => {
      element.properties.forEach((property) => {
        let value = property.propertyValue === false ? false : property.propertyValue || '';
        template = this.replaceInTemplate(template, property.propertyName, value);
      });
    });
    return template;
  }

  restoreOriginalFunctionsPlaceholders(template, controlParams, fieldName) {
    if (!this.functionsLabels.get(fieldName)) {
      return template;
    }
    Object.keys(this.functionsLabels.get(fieldName)).forEach((functionValue) => {
      template = template.split(this.functionsLabels.get(fieldName)[functionValue]).join(controlParams[functionValue.toLowerCase()]);
    });
    return template;
  }

  replaceInTemplate(template, oldValue, newValue) {
    return template.split('$[' + oldValue + ']').join(newValue);
  }

  /**
   * Builds payload for a request to backend with all unresolved properties bindings
   *
   * @param unresolvedBindings
   *          All bindings which values can not be resolved from current model
   * @param flatModel
   *          The current object view model which is flattened
   * @param validationModel
   *          The current field validation model.
   * @param fieldName
   *          The field name to which validator is attached.
   * @returns payload with following format:
   *          [
   *            {
   *              id: 'selected-report-type-instance-id',
   *              target: 'uri of related field',
   *              source: 'identifier of field to which validator is attached'
   *            },
   *            {
   *              id: 'emf:123456',
   *              target: 'emf:type',
   *              source: 'generatedField'
   *            }
   *          ]
   */
  buildPayload(unresolvedBindings, flatModel, validationModel, fieldName) {
    let bindings = [];
    let payload = [];
    let index = 0;

    unresolvedBindings.forEach((binding) => {
      let objectPropertyUri = binding.split('.')[0];
      bindings.push(objectPropertyUri);
    });

    bindings.forEach((binding) => {
      Object.keys(flatModel).forEach((propertyName) => {
        let property = flatModel[propertyName];
        if (binding === property.uri) {
          let key = property.identifier;
          let value = ModelUtils.getObjectPropertyValue(validationModel[key]);
          if (value && value[0] && value.length == 1) {
            payload.push({id: value[0], target: unresolvedBindings[index], source: flatModel[fieldName].identifier});
          }
        }
      });
      index++;
    });
    return payload;
  }

  subscribeBeforeIdocSaveEvent(fieldName, flatModel) {
    if (!ModelUtils.isText(flatModel[fieldName]) || this.eventsMap.has(fieldName)) {
      return;
    }

    let beforeIdocSaveEvent = this.eventbus.subscribe(BeforeIdocSaveEvent, (data) => {
      flatModel[fieldName].editedByUser = false;
      if (data[0] && data[0].properties[fieldName] && !flatModel[fieldName].editedByUser) {
        data[0].properties[fieldName] = this.restoreOriginalFunctionsPlaceholders(data[0].properties[fieldName], ModelUtils.getControl(flatModel[fieldName].control, DEFAULT_VALUE_PATTERN).controlParams, fieldName);
      }
    });
    this.eventsMap.set(fieldName, beforeIdocSaveEvent);
  }

  clearEventsMap(definitionId) {
    if (this.definitionId !== definitionId) {
      this.eventsMap.forEach((event) => {
        event.unsubscribe();
      });
      this.eventsMap.clear();
      this.functionsLabels.clear();
    }
  }

}