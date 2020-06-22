import {Injectable, Inject} from 'app/app';
import {SELECT_OBJECT_CURRENT, SELECT_OBJECT_MANUALLY, SELECT_OBJECT_AUTOMATICALLY} from 'idoc/widget/object-selector/object-selector';
import {ANY_OBJECT, SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {SINGLE_SELECTION} from 'search/search-selection-modes';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {DefinitionService} from 'services/rest/definition-service';
import {NamespaceService} from 'services/rest/namespace-service';
import uuid from 'common/uuid';
import _ from 'lodash';

const PROPERTY_DISPLAY_TYPE_SYSTEM = 'SYSTEM';

export const COMMON_PROPERTIES = 'COMMON_PROPERTIES';

/**
 * This service can be used to obtain definition properties for one or more instances
 */
@Injectable()
@Inject(PromiseAdapter, DefinitionService, NamespaceService)
export class PropertiesSelectorHelper {
  constructor(promiseAdapter, definitionService, namespaceService) {
    this.promiseAdapter = promiseAdapter;
    this.definitionService = definitionService;
    this.namespaceService = namespaceService;
  }

  // TODO: This method is a copy-paste from DatatableWidget.generateHeaders method with some minor changes.
  // It belongs here IMO, so I'm planning a task for replacing it with this one.
  collectPropertiesLabels(selectedProperties) {
    let labels = [];
    if (!selectedProperties) {
      return this.promiseAdapter.resolve(labels);
    }

    let definitionsIdentifiers = Object.keys(selectedProperties);
    // avoid unnecessary load of all definitions.
    if (!this.hasSelectedProperties(selectedProperties, definitionsIdentifiers)) {
      return this.promiseAdapter.resolve(labels);
    }
    let commonPropertiesIndex = definitionsIdentifiers.indexOf(COMMON_PROPERTIES);
    if (commonPropertiesIndex !== -1) {
      definitionsIdentifiers.splice(commonPropertiesIndex, 1);
    }

    return this.definitionService.getFields(definitionsIdentifiers).then((definitions) => {
      let headers = new Map();
      definitions.data.forEach((definition) => {
        let propertiesForDefinition = selectedProperties[definition.identifier] || [];
        let flatDefinitionProperties = this.flattenDefinitionProperties(definition);

        propertiesForDefinition.forEach((propertyIdentifier) => {
          let definitionField = _.find(flatDefinitionProperties, (field) => {
            return field.name === propertyIdentifier;
          });

          let fieldLabel = definitionField ? definitionField.label : propertyIdentifier;
          let fieldHeader = headers.get(propertyIdentifier);
          if (fieldHeader) {
            if (fieldHeader.labels.indexOf(fieldLabel) === -1) {
              fieldHeader.labels.push(fieldLabel);
            }
          } else {
            headers.set(propertyIdentifier, {name: propertyIdentifier, labels: [fieldLabel]});
          }

        });
      });
      return Array.from(headers.values());
    });
  }

  hasSelectedProperties(properties, definitions) {
    return definitions.some((definition) => {
      return !!properties[definition].length;
    });
  }

  /**
   * Updates selected definitions directly into propertiesSelectorConfig object
   * @param config widget config object
   * @param propertiesSelectorConfig properties selector configuration. Used to update selected definitions
   * @param context idoc context
   * @returns {*}
   */
  getDefinitionsArray(config, propertiesSelectorConfig, context) {
    let loaderUid = uuid();
    propertiesSelectorConfig.loaderUid = loaderUid;
    let definitionsLoader = this.loadDefinitions(context, config);
    return definitionsLoader.then((definitions) => {
      let uniqueDefinitions = this.removeDuplicatedDefinitions(definitions);
      return this.afterDefinitionsLoaded(loaderUid, uniqueDefinitions, config, propertiesSelectorConfig);
    });
  }

  /**
   * Ensures that the provided array of definitions contains only unique elements (based on the definition identifier).
   * @param definitions - array of definition objects
   * @returns {Array} of non duplicates
   */
  removeDuplicatedDefinitions(definitions) {
    return definitions.filter((definition, index, self) => _.findIndex(self, (traversed) => {
      return definition.identifier === traversed.identifier;
    }) === index);
  }

  /**
   * Process loaded definitions. Updates properties selector definitions
   * @param loaderUid
   * @param definitions
   * @param config
   * @param propertiesSelectorConfig
   * @returns {Array}
   */
  afterDefinitionsLoaded(loaderUid, definitions, config, propertiesSelectorConfig) {
    // check that this is the last started loader to avoid overriding data by late loaders
    if (propertiesSelectorConfig.loaderUid === loaderUid) {
      propertiesSelectorConfig.definitions = propertiesSelectorConfig.definitions || [];
      this.mergeDefinitions(propertiesSelectorConfig.definitions, definitions);
      this.removeSelectedProperties(propertiesSelectorConfig, config);
      return propertiesSelectorConfig.definitions;
    }
  }

  /**
   * Merge newly selected definitions into currently selected definitions.
   * This method ensures that references to currently selected definitions are kept.
   * All current definitions which does not exist in new definitions array are removed.
   * @param currentDefinitions
   * @param newDefinitions
   */
  mergeDefinitions(currentDefinitions, newDefinitions) {
    let definitions = newDefinitions.map((newDefinition) => {
      let currentDefinitionIndex = _.findIndex(currentDefinitions, (currentDefinition) => {
        return currentDefinition.identifier === newDefinition.identifier;
      });
      if (currentDefinitionIndex !== -1) {
        return currentDefinitions[currentDefinitionIndex];
      } else {
        return newDefinition;
      }
    });
    currentDefinitions.splice(0);
    currentDefinitions.push(...definitions);
  }

  /**
   * Load definitions depending on what is chosen in select object tab
   * @param context
   * @param config Selection mode is taken from config.selectObjectMode and can be current, manually, automatically.
   */
  loadDefinitions(context, config) {
    let selectObjectMode = PropertiesSelectorHelper.getSelectObjectMode(config);

    if (selectObjectMode === SELECT_OBJECT_CURRENT || selectObjectMode === SELECT_OBJECT_MANUALLY) {
      let selectedObjects = this.getSelectedObjects(context, config);
      return this.extractObjectsDefinitions(selectedObjects, context);
    } else if (selectObjectMode === SELECT_OBJECT_AUTOMATICALLY) {
      let selectedTypes = SearchCriteriaUtils.getTypesFromCriteria(config.criteria);
      return this.extractDefinitionsByIdentifiers(selectedTypes);
    }
    if (config.selectedTypes) {
      return this.extractDefinitionsByIdentifiers(config.selectedTypes);
    }
    return this.promiseAdapter.reject('Select object mode is undefined');
  }

  /**
   * Return correct select object mode. If opened object is a version its selectObjectMode is always "manually".
   * This method ensures that the original selectObjectMode is returned in order to properly load definitions.
   * @param config Selection mode is taken from config.selectObjectMode and can be current, manually, automatically.
   * @returns {*}
   */
  static getSelectObjectMode(config) {
    if (config.originalConfigurationDiff && config.originalConfigurationDiff.selectObjectMode) {
      return config.originalConfigurationDiff.selectObjectMode;
    }
    return config.selectObjectMode;
  }

  getSelectedObjects(context, config) {
    if (config.selectObjectMode === SELECT_OBJECT_CURRENT) {
      return [context.getCurrentObjectId()];
    }

    let selectedObjectsArray = [];
    if (config.selection === SINGLE_SELECTION && config.selectedObject !== undefined) {
      selectedObjectsArray.push(config.selectedObject);
    } else if (config.selectedObjects !== undefined) {
      selectedObjectsArray.push(...config.selectedObjects);
    }

    if (config.selectObjectMode === SELECT_OBJECT_MANUALLY && selectedObjectsArray.length > 0) {
      return selectedObjectsArray;
    }
  }

  /**
   * Remove selected properties for definitions which are no longer selected.
   * Remove selected properties if they have been removed from the definition.
   */
  removeSelectedProperties(propertiesSelectorConfig, config) {
    Object.keys(config.selectedProperties).forEach((definitionId) => {
      let definitionIndex = _.findIndex(propertiesSelectorConfig.definitions, (definition) => {
        return definition.identifier === definitionId;
      });
      // if selected properties definition is no longer present in newly loaded definitions
      if (definitionId !== COMMON_PROPERTIES && definitionIndex === -1) {
        delete config.selectedProperties[definitionId];
      }
    });

    config.selectedProperties = this.removeMissingSelectedProperties(propertiesSelectorConfig.definitions, config.selectedProperties);
  }

  /**
   * Extract definition by object ids and converts it to the internal format
   * @param objects of object ids
   * @param context of object ids
   * @returns {*}
   */
  extractObjectsDefinitions(objects, context) {
    if (!objects) {
      return this.promiseAdapter.resolve([]);
    } else {
      let definitionsIdentifiers = [];
      return context.getSharedObjects(objects, null).then((sharedObjects) => {
        sharedObjects.data.forEach((object) => {
          if (definitionsIdentifiers.indexOf(object.getModels().definitionId) === -1) {
            definitionsIdentifiers.push(object.getModels().definitionId);
          }
        });
        return this.extractDefinitionsByIdentifiers(definitionsIdentifiers);
      });
    }
  }

  /**
   * Extract definitions by their identifiers (ex. ['emf:Case', 'GEP11111']) and convert them to internal format
   * @param identifiers
   * @returns {*}
   */
  extractDefinitionsByIdentifiers(identifiers) {
    if (identifiers === undefined) {
      return this.promiseAdapter.resolve([]);
    } else {
      if (_.includes(identifiers, ANY_OBJECT)) {
        identifiers = [];
      }
      let definitionsLoader = this.definitionService.getFields(identifiers);
      let definitionsFullURILoader;
      // if there are no selected definitions the definitions loaded load all definitions, so URI loader should wait for definitions to be loaded to get the URIs
      if (identifiers.length > 0) {
        definitionsFullURILoader = this.getDefinitionsURIsMap(identifiers);
      } else {
        definitionsFullURILoader = definitionsLoader.then((definitions) => {
          let allDefinitionsIdentifiers = definitions.data.map((definition) => {
            return definition.identifier;
          });
          return this.getDefinitionsURIsMap(allDefinitionsIdentifiers);
        });
      }

      return this.promiseAdapter.all([definitionsLoader, definitionsFullURILoader]).then((results) => {
        let definitions = results[0];
        let definitionsURIsMap = results[1];
        return definitions.data.map((definition) => {
          definition.fields = this.filterDefinitionFields(definition);
          // if definition identifier is rdf type it is converted to full URI so it can be easily compared against InstanceObject's properties
          definition.identifier = definitionsURIsMap[definition.identifier] || definition.identifier;
          definition.id = definition.identifier;
          return definition;
        });
      });
    }
  }

  /**
   * Filter definition fields recursively based on display type
   * @param definition
   * @returns {Array.<T>} of fields which are not of display type SYSTEM
   */
  filterDefinitionFields(definition) {
    return definition.fields.filter((field) => {
      let keepField = PROPERTY_DISPLAY_TYPE_SYSTEM !== field.displayType;
      if (keepField && field.fields && field.fields.length > 0) {
        field.fields = this.filterDefinitionFields(field);
      }
      return keepField;
    });
  }

  /**
   * Return all properties for a definition as flat array (region properties are flatten)
   * @param definition
   * @returns {Array}
   */
  flattenDefinitionProperties(definition) {
    let properties = [];
    definition.fields.forEach((property) => {
      if (this.isRegion(property)) {
        properties.push(...this.flattenDefinitionProperties(property));
      } else {
        properties.push(property);
      }
    });
    return properties;
  }

  isRegion(property) {
    return !!property.fields;
  }

  /**
   * Takes all semantic URIs and convert them to full URIs
   * @param definitionIdentifiers
   * @returns {*} map with short URIs as keys and full URIs as values
   */
  getDefinitionsURIsMap(definitionIdentifiers) {
    let semanticIdentifiers = definitionIdentifiers.filter((definitionIdentifier) => {
      return definitionIdentifier.indexOf(':') !== -1;
    });
    if (semanticIdentifiers.length > 0) {
      return this.namespaceService.toFullURI(semanticIdentifiers).then((response) => {
        return response.data || {};
      });
    } else {
      return this.promiseAdapter.resolve({});
    }
  }

  /**
   * Removes selected properties from selected properties array if these properties are missing in the definition (removed from the definition)
   * @param definitions all loaded definitions
   * @param selectedProperties map
   * @returns filtered selected properties map
   */
  removeMissingSelectedProperties(definitions, selectedProperties) {
    Object.keys(selectedProperties).forEach((definitionIdentifier) => {
      let selectedPropertiesForDefinition = Object.keys(selectedProperties[definitionIdentifier]);
      let definitionIndex = _.findIndex(definitions, (definition) => {
        return definition.identifier === definitionIdentifier;
      });
      if (definitionIndex !== -1 && selectedPropertiesForDefinition.length) {
        let definitionPropertiesSet = this.generateDefinitionPropertiesSet(definitions[definitionIndex]);
        selectedPropertiesForDefinition.forEach((selectedPropertyName) => {
          if (!definitionPropertiesSet.has(selectedPropertyName)) {
            delete selectedProperties[definitionIdentifier][selectedPropertyName];
          }
        });
      }
    });
    return selectedProperties;
  }

  /**
   * Get selected properties to be displayed either by definition id (higher priority) or by semantic (rdf) type
   * @returns {*} object with selected properties names or undefined
   */
  static getSelectedProperties(models, selectedPropertiesMap) {
    if (!selectedPropertiesMap) {
      return {};
    }
    let rdfType = models.validationModel[PropertiesSelectorHelper.RDF_TYPE] ? models.validationModel[PropertiesSelectorHelper.RDF_TYPE].value : undefined;
    let selectedProperties = selectedPropertiesMap[models.definitionId] || selectedPropertiesMap[rdfType];
    let semanticHierarchy = models.validationModel[PropertiesSelectorHelper.SEMANTIC_HIERARCHY];
    if (selectedProperties === undefined && semanticHierarchy) {
      //in case selectedProperties couldn't be directly accessed by definitionId, and rdfType is not available, iterate the
      //semantic tree, lookup the selected properties by their URIs and accumulate the result into selectedProperties
      semanticHierarchy.value.forEach((semanticClass) => {
        selectedProperties = $.extend(selectedProperties, selectedPropertiesMap[semanticClass]);
      });
    }
    return selectedProperties || {};
  }

  /**
   * Extract all definition field names into a set
   * @param definition
   * @param propertiesSet used for recursion. Just ignore it when calling the method.
   * @returns {Set|*} with all definition property names
   */
  generateDefinitionPropertiesSet(definition, propertiesSet) {
    propertiesSet = propertiesSet instanceof Set ? propertiesSet : new Set();
    definition.fields.forEach((property) => {
      if (this.isRegion(property)) {
        propertiesSet = this.generateDefinitionPropertiesSet(property, propertiesSet);
      } else {
        propertiesSet.add(property.name);
      }
    });
    return propertiesSet;
  }

  static transformSelectedProperies(properties) {
    let selectedProperties = {};
    if (properties instanceof Array) {
      properties.forEach((property) => {
        selectedProperties[property] = {'name': property};
      });
    } else {
      selectedProperties = properties;
    }
    return selectedProperties;
  }
}

PropertiesSelectorHelper.RDF_TYPE = 'rdf:type';
PropertiesSelectorHelper.SEMANTIC_HIERARCHY = 'semanticHierarchy';
