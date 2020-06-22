import {Inject, Injectable} from 'app/app';
import {HEADER_V2_JSON, RestClient} from 'services/rest-client';
import {CommandChain} from 'common/command-chain/command-chain';
import {
  BooleanTypeConverter,
  DateTimeTypeConverter,
  DefaultTypeConverter,
  NumericTypeConverter,
  ObjectTypeConverter,
  StringTypeConverter
} from 'common/convert/property-converter';
import {RequestsCacheService} from 'services/rest/requests-cache-service';
import {ValidationService} from 'form-builder/validation/validation-service';
import {DefinitionService} from 'services/rest/definition-service';
import {TranslateService} from 'services/i18n/translate-service';
import {FORBIDDEN} from 'idoc/idoc-constants';
import {CONTROL_TYPE} from 'models/model-utils';
import {CRITERIA_FTS_RULE_FIELD} from 'search/utils/search-criteria-utils';
import _ from 'lodash';

export const SERVICE_URL = '/properties';

export const DEFINITION_RANGE_FIELD = 'range';
export const DEFINITION_RANGE_SEPARATOR = ',';

@Injectable()
@Inject(RestClient, TranslateService, DefinitionService, RequestsCacheService)
export class PropertiesRestService {

  constructor(restClient, translateService, definitionService, requestsCacheService) {
    this.restClient = restClient;
    this.translateService = translateService;
    this.requestsMap = new Map();
    this.requestsCacheService = requestsCacheService;

    this.config = {};
    this.config.headers = {
      'Accept': HEADER_V2_JSON,
      'Content-Type': HEADER_V2_JSON
    };
    this.definitionService = definitionService;

    this.converter = new CommandChain([new BooleanTypeConverter(),
      new NumericTypeConverter(),
      new StringTypeConverter(),
      new DateTimeTypeConverter(),
      new ObjectTypeConverter(),
      new DefaultTypeConverter()]);
  }

  /**
   * Retrieves the searchable properties for multiple or single type. The properties are converted for use of UI2.
   *
   * @param types - The given types. could be an array or string
   * @returns http promise
   */
  getSearchableProperties(types) {
    return this.definitionService.getFields(types).then((response) => {
      let convertedProperties = this.convertProperties(response);
      convertedProperties.unshift(this.getFreeTextCriteria(), this.getAnyFieldCriteria(), this.getAnyRelationCriteria(), this.getAnyObjectCriteria());
      return convertedProperties;
    });
  }

  /**
   * Checks if the given value is not already in use.
   *
   * @param definitionId the definition id of the current object
   * @param instanceId the id of the instance
   * @param fieldName the unique field name
   * @param value the current value of the field
   */
  checkFieldUniqueness(definitionId, instanceId, fieldName, value) {
    let url = `${SERVICE_URL}/unique`;
    let data = {
      definitionId,
      instanceId,
      propertyName: fieldName,
      value
    };

    return this.requestsCacheService.cache(url, data, this.requestsMap, () => {
      return this.restClient.post(url, data, this.config);
    });
  }

  convertProperties(response) {
    let mappedProperties = {};

    response.data.forEach((type) => {
      // build & map properties for each type
      this.mapProperties(type, mappedProperties);
    });
    // dump mapped properties as an array of it's values
    return _.values(mappedProperties);
  }

  mapProperties(field, mappedProperties) {
    if (!this.isRegion(field)) {
      // skip processing all forbidden fields
      if (field.uri === FORBIDDEN || field.displayType === ValidationService.DISPLAY_TYPE_SYSTEM) {
        return;
      }

      if (!mappedProperties[field.uri]) {
        // ensure that properties are unique and hold references for latter use
        if (this.isCodeListProperty(field)) {
          mappedProperties[field.uri] = this.buildCodeListProperty(field);
        } else if (this.isObjectTypeProperty(field)) {
          mappedProperties[field.uri] = this.buildObjectTypeProperty(field);
        } else {
          mappedProperties[field.uri] = this.buildProperty(field);
        }
      }

      // connect all code list properties
      if (this.isCodeListProperty(field)) {
        let codeListId = field.codelist;
        let codeLists = mappedProperties[field.uri].codeLists;
        if (codeLists && !_.includes(codeLists, codeListId)) {
          codeLists.push(codeListId);
        }
      }
    } else {
      // loop over the fields contained inside the property
      _.forEach(field.fields, (property) => {
        this.mapProperties(property, mappedProperties);
      });
    }
  }

  buildProperty(property) {
    let primitive = this.buildBaseProperty(property);
    primitive.type = this.converter.execute(property.dataType.name);
    // extract property ranges & assign only if present
    let range = this.extractRangeValues(property);
    if (range && range.length > 0) {
      primitive.range = range;
    }
    return primitive;
  }

  buildBaseProperty(property) {
    return {
      id: property.uri,
      text: property.label
    };
  }

  buildObjectTypeProperty(property) {
    let result = this.buildBaseProperty(property);
    result.type = 'objectType';
    return result;
  }

  buildCodeListProperty(property) {
    let codeList = this.buildBaseProperty(property);
    codeList.type = 'codeList';
    codeList.codeLists = [];
    return codeList;
  }

  extractRangeValues(property) {
    let ranges = [];
    let definition = property.controlDefinition;

    if (definition && definition.controlParams) {
      let rangeField = _.find(definition.controlParams, (params) => {
        return params.name === DEFINITION_RANGE_FIELD;
      });
      if (rangeField) {
        ranges = rangeField.value.split(DEFINITION_RANGE_SEPARATOR).filter((range) => {
          return range && range.length > 0;
        }).map(range => range.trim());
      }
    }

    return ranges;
  }

  isObjectTypeProperty(property) {
    return property.controlDefinition && property.controlDefinition.identifier === CONTROL_TYPE.OBJECT_TYPE_SELECT;
  }

  isCodeListProperty(property) {
    return property.codelist;
  }

  isRegion(property) {
    return property.fields;
  }

  getAnyRelationCriteria() {
    return {
      id: 'anyRelation',
      text: this.translateService.translateInstant('search.advanced.property.anyRelation'),
      type: 'object'
    };
  }

  getAnyFieldCriteria() {
    return {
      id: 'anyField',
      text: this.translateService.translateInstant('search.advanced.property.anyField'),
      type: 'string'
    };
  }

  getFreeTextCriteria() {
    return {
      id: CRITERIA_FTS_RULE_FIELD,
      text: this.translateService.translateInstant('search.advanced.property.freeText'),
      type: 'fts'
    };
  }

  getAnyObjectCriteria() {
    return {
      id: 'types',
      text: this.translateService.translateInstant('search.advanced.property.type'),
      type: ''
    };
  }

  loadObjectPropertiesSuggest(parentId, type, multivalued) {
    let url = `${SERVICE_URL}/suggest?targetId=${parentId}&type=${type}&multivalued=${multivalued}`;

    return this.requestsCacheService.cache(url, '', this.requestsMap, () => {
      return this.restClient.get(url);
    });
  }

  evaluateValues(definitionId, bindings) {
    let url = `${SERVICE_URL}/value/eval`;

    let data = {
      definitionId,
      bindings
    };

    return this.requestsCacheService.cache(url, data, this.requestsMap, () => {
      return this.restClient.post(url, data, this.config);
    });
  }
}