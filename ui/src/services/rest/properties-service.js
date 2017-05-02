import {Inject, Injectable} from 'app/app';
import {RestClient} from 'services/rest-client';
import {TranslateService} from 'services/i18n/translate-service';

export const SERVICE_URL = '/properties';
export const NUMERIC_PROPERTIES = ['int', 'long', 'float', 'double'];

@Injectable()
@Inject(RestClient, TranslateService)
export class PropertiesRestService {

  constructor(restClient, translateService) {
    this.restClient = restClient;
    this.translateService = translateService;
  }

  /**
   * Retrieves the searchable properties for multiple or single type. The properties are converted for use of UI2.
   *
   * @param types - The given types. could be an array or string
   * @returns http promise
   */
  getSearchableProperties(types) {
    var config = {
      params: {}
    };
    if (types instanceof Array) {
      config.params['forType'] = types.join(',');
    } else if (typeof types === 'string') {
      config.params['forType'] = types;
    }

    return this.restClient.get(`${SERVICE_URL}/searchable/semantic`, config).then((response) => {
      let convertedProperties = this.convertProperties(response);
      //TODO: Those two should be returned from the backend with the new service.
      convertedProperties.unshift(this.getAnyFieldCriteria(), this.getAnyRelationCriteria());
      return convertedProperties;
    });
  }

  //TODO: Remove the converting after new service is implemented.
  convertProperties(response) {
    var mappedProperties = [];
    var uniqueUriSet = new Set();
    response.data.forEach((property) => {
      if (!uniqueUriSet.has(property.uri)) {
        mappedProperties.push(this.convertProperty(property));
        // Ensuring only unique properties will be returned
        uniqueUriSet.add(property.uri);
      }
    });
    return mappedProperties;
  }

  convertProperty(property) {
    var convertedProperty = {
      id: property.uri,
      text: property.text,
      type: property.rangeClass
    };

    if (property.codeLists && property.codeLists.length > 0) {
      convertedProperty.type = 'codeList';
      convertedProperty.codeLists = property.codeLists;
    } else if (this.isNumericProperty(property)) {
      convertedProperty.type = 'numeric';
    } else if (property.propertyType === 'object') {
      convertedProperty.type = 'object';
    }
    return convertedProperty;
  }

  isNumericProperty(property) {
    return NUMERIC_PROPERTIES.indexOf(property.rangeClass) > -1;
  }

  getAnyRelationCriteria() {
    return {
      id: "anyRelation",
      text: this.translateService.translateInstant('search.advanced.property.anyRelation'),
      type: "object"
    }
  }

  getAnyFieldCriteria() {
    return {
      id: "anyField",
      text: this.translateService.translateInstant('search.advanced.property.anyField'),
      type: "string"
    }
  }

  loadObjectPropertiesSuggest(parentId, type, multivalued) {
    return this.restClient.get(`/properties/suggest?targetId=${parentId}&type=${type}&multivalued=${multivalued}`);
  }

}