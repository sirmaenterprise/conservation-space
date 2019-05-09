import {Component, View, Inject, NgScope, NgCompile, NgElement} from 'app/app';
import 'components/collapsible/collapsible';
import {DEFINITION_RANGE_FIELD} from 'services/rest/properties-service';
import {PropertiesSelectorHelper, COMMON_PROPERTIES} from 'idoc/widget/properties-selector/properties-selector-helper';
import {CONTROL_TYPE} from 'models/model-utils';
import {TranslateService} from 'services/i18n/translate-service';
import _ from 'lodash';
import propertiesSelectorTemplate from 'idoc/widget/properties-selector/properties-selector.html!text';
import 'idoc/widget/properties-selector/properties-selector.css!';

export const RELATED_OBJECT_HEADER = 'related-object-header';
export const RELATED_OBJECT_PROPERTIES = 'related-object-properties';
export const SELECTED_PROPERTIES = 'selectedProperties';

@Component({
  selector: 'seip-properties-selector',
  properties: {
    config: 'config',
    parent: 'parent'
  }
})
@View({
  template: propertiesSelectorTemplate
})
@Inject(NgScope, NgCompile, NgElement, PropertiesSelectorHelper, TranslateService)
/**
 * Component for selecting properties. If there are more than one definitions this component extract common properties and display additional group if there are any.
 * It accepts following parameters via config object:
 * @param config.definitions - array of type:
 * [{
 *    identifier: 'DEFINITION ID',
 *    label: 'DEFINITION LABEL',
 *    fields: [
 *      {
 *        name: 'FIELD NAME',
 *        label: 'FIELD LABEL',
 *        fields: [...] // For regions
 *      }, ...
 *    ]
 * }, ...]
 * @param config.selectedProperties - map of type:
 * {
 *    DEFINITION_IDENTIFIER: [FIELD_NAME_1, FIELD_NAME_2, ...], ...
 * }
 * When selecting properties config.selectedProperties map is updated.
 */
export class PropertiesSelector {
  constructor($scope, $compile, $element, propertiesSelectorHelper, translateService) {
    this.propertiesSelectorHelper = propertiesSelectorHelper;
    this.translateService = translateService;
    this.$scope = $scope;
    this.$compile = $compile;
    this.$element = $element;
    this.subPropertiesConfig = {};

    $scope.$watch(() => {
      if (this.config && this.config.definitions) {
        return this.config.definitions.map((definition) => {
          return definition.identifier;
        });
      }
      return null;
    }, (definitionsIdentifiers) => {
      if (this.config && this.config.definitions) {
        let definitions = this.config.definitions;
        let commonProperties = this.extractCommonProperties(definitions);
        this.initSelectedProperties(definitionsIdentifiers, commonProperties);
        if (commonProperties && commonProperties.length > 0) {
          let commonDefinition = {
            identifier: COMMON_PROPERTIES,
            id: this.config.commonPropertiesLabel || COMMON_PROPERTIES,
            label: this.config.commonPropertiesLabel || 'odw.config.common.properties',
            fields: commonProperties
          };
          // For sub properties only common definition should be visible
          if (this.config.commonPropertiesLabel) {
            this.definitions = [commonDefinition];
          } else {
            this.definitions = [commonDefinition, ...definitions];
          }
        } else {
          this.definitions = definitions;
        }
        this.initSelection();
      }
    }, true);
  }

  /**
   * Initialize selected flag of the model used for visualization based on this.config.selectedProperties map
   */
  initSelection() {
    this.definitions.forEach((definition) => {
      let definitionId = definition.identifier;
      let selectedProperties = this.config.selectedProperties[definitionId];
      let properties = this.propertiesSelectorHelper.flattenDefinitionProperties(definition);
      properties.forEach((property) => {
        property.selected = Object.keys(selectedProperties).indexOf(property.name) !== -1;
        if (property.selected) {
          this.addCodelistPropertySettings(definitionId, property);
          this.addObjectPropertySettings(definitionId, property);
        }
      });
    });
  }

  removePropertySettings(definitionId, property) {
    if (this.config.selectedPropertiesData && this.config.selectedPropertiesData[definitionId]) {
      delete this.config.selectedPropertiesData[definitionId][property.name];
      delete property.config;
      if (this.config.showObjectPropertyOptions) {
        this.removeSubPropertySelector(property.name);
      }
    }
  }

  isPropertyVisible(property) {
    return property.selected && !!this.config.selectedPropertiesData;
  }

  addCodelistPropertySettings(definitionId, property) {
    if (!this.showCodelistPropertyOptions(property)) {
      return;
    }
    property.config = {
      selected: this.getSelectedOption(CONTROL_TYPE.CODELIST, definitionId, property),
      options: this.buildCodelistPropConfig(property.multivalue)
    };
    this.updatePropertySettings(definitionId, property);
  }

  showCodelistPropertyOptions(property) {
    return this.config.showCodelistPropertyOptions && property.codelist;
  }

  showObjectPropertyOptions(property) {
    return this.config.showObjectPropertyOptions && property.controlDefinition && property.controlDefinition.identifier === CONTROL_TYPE.PICKER;
  }

  getSelectedOption(defaultOption, definitionId, property) {
    // default option selection
    let selectedOption = defaultOption;
    let selectedPropertiesByDefinition = this.config.selectedPropertiesData[definitionId];
    // check for new selected option
    if (selectedPropertiesByDefinition && selectedPropertiesByDefinition[property.name]) {
      selectedOption = selectedPropertiesByDefinition[property.name];
    }
    return selectedOption;
  }

  addObjectPropertySettings(definitionId, property) {
    if (!this.showObjectPropertyOptions(property)) {
      return;
    }
    property.config = {
      selected: this.getSelectedOption(RELATED_OBJECT_HEADER, definitionId, property),
      options: this.buildObjectPropConfig()
    };
    this.updatePropertySettings(definitionId, property);
  }

  updatePropertySettings(definitionId, property) {

    // Apply selection given from config
    this.definitions.forEach((definition) => {
      let definitionProperties = this.propertiesSelectorHelper.flattenDefinitionProperties(definition);
      definitionProperties.forEach((definitionProperty) => {
        if (definitionProperty.name === property.name && definitionProperty.config) {
          definitionProperty.config.selected = property.config.selected;
        }
      });
      if (this.config.selectedPropertiesData[definition.identifier] && this.config.selectedPropertiesData[definition.identifier][property.name]) {
        this.config.selectedPropertiesData[definition.identifier][property.name] = property.config.selected;
      }
    });

    let controlTypesByDefinition = this.config.selectedPropertiesData[definitionId];
    if (!controlTypesByDefinition) {
      this.config.selectedPropertiesData[definitionId] = {};
    }
    if (property.config.selected === CONTROL_TYPE.CODELIST) {
      delete this.config.selectedPropertiesData[definitionId][property.name];
      if (_.isEmpty(this.config.selectedPropertiesData[definitionId])) {
        delete this.config.selectedPropertiesData[definitionId];
      }
    } else {
      this.config.selectedPropertiesData[definitionId][property.name] = property.config.selected;
    }

    if (property.config.selected === RELATED_OBJECT_PROPERTIES) {
      this.addSubPropertySelector(property);
    } else if (property.config.selected === RELATED_OBJECT_HEADER) {
      Object.keys(this.config.selectedProperties).forEach((definitionKey) => {
        if (this.config.selectedProperties[definitionKey] && this.config.selectedProperties[definitionKey][property.name]) {
          delete this.config.selectedProperties[definitionKey][property.name].selectedProperties;
        }
      });
      this.removeSubPropertySelector(property.name);
    }
  }

  buildCodelistPropConfig(isMultiValue) {
    let bundle = isMultiValue ? 'widget.property.option.checkbox' : 'widget.property.option.radio';
    return [
      {
        value: CONTROL_TYPE.CODELIST,
        label: this.translateService.translateInstant('widget.property.option.dropdown')
      },
      {
        value: CONTROL_TYPE.CODELIST_LIST,
        label: this.translateService.translateInstant(bundle)
      }
    ];
  }

  buildObjectPropConfig() {
    return [
      {
        value: RELATED_OBJECT_HEADER,
        label: this.translateService.translateInstant('widget.property.option.header')
      },
      {
        value: RELATED_OBJECT_PROPERTIES,
        label: this.translateService.translateInstant('widget.property.option.properties')
      }
    ];
  }

  /**
   * Extract common properties (with the same property.name) for given definitions
   * @param definitions
   * @returns {*} an array with common properties or undefined
   */
  extractCommonProperties(definitions) {
    let commonProperties;
    if (definitions && definitions.length > 1) {
      let firstDefinitionProperties = this.propertiesSelectorHelper.flattenDefinitionProperties(definitions[0]);
      commonProperties = firstDefinitionProperties.filter((property) => {
        let isCommonProperty = true;
        for (let i = 1; i < definitions.length && isCommonProperty; i++) {
          isCommonProperty = this.hasProperty(definitions[i], property.name);
        }
        return isCommonProperty;
      }).map((property) => {
        return _.clone(property);
      });
    }
    return commonProperties;
  }

  /**
   * Initialize selected properties map based on selected definitions
   * @param definitionsIdentifiers of all selected definitions
   * @param commonProperties
   */
  initSelectedProperties(definitionsIdentifiers, commonProperties) {
    // If a common property was selected but it is no longer a common property it is removed from (not added to) common selected properties
    let currentCommonSelectedProperties = this.config.selectedProperties[COMMON_PROPERTIES] || {};
    let updatedCommonSelectedProperties = {};

    if (commonProperties && Object.keys(currentCommonSelectedProperties).length > 0) {
      commonProperties.forEach((commonProperty) => {
        if (Object.keys(currentCommonSelectedProperties).indexOf(commonProperty.name) !== -1) {
          updatedCommonSelectedProperties[commonProperty.name] = currentCommonSelectedProperties[commonProperty.name] || {name: commonProperty.name};
        }
      });
    }
    this.config.selectedProperties[COMMON_PROPERTIES] = updatedCommonSelectedProperties;

    // Remove selected properties for definitions that are no longer selected
    Object.keys(this.config.selectedProperties).forEach((selectedPropertiesDefinitionIdentifier) => {
      if (selectedPropertiesDefinitionIdentifier !== COMMON_PROPERTIES && definitionsIdentifiers.indexOf(selectedPropertiesDefinitionIdentifier) === -1) {
        delete this.config.selectedProperties[selectedPropertiesDefinitionIdentifier];
      }
    });

    // Remove selected properties data for definitions that are no longer selected
    if (this.config.selectedPropertiesData) {
      Object.keys(this.config.selectedPropertiesData).forEach((definitionKey) => {
        if (definitionKey !== COMMON_PROPERTIES && definitionsIdentifiers.indexOf(definitionKey) === -1) {
          delete this.config.selectedPropertiesData[definitionKey];
        }
      });
    }

    // Update selected properties of newly added definitions with common selected properties
    definitionsIdentifiers.forEach((definitionIdentifier) => {
      if (!this.config.selectedProperties[definitionIdentifier]) {
        this.config.selectedProperties[definitionIdentifier] = _.clone(this.config.selectedProperties[COMMON_PROPERTIES]);
      }
    });

    // Remove sub properties selectors for definitions that are no longer selected
    if (this.config.showObjectPropertyOptions) {
      let renderedSubSelectors = this.$element.find('.sub-properties-selector > div');
      let properties = [];
      Object.keys(this.config.selectedProperties).forEach((definitionKey) => {
        properties = _.union(properties, Object.keys(this.config.selectedProperties[definitionKey]));
      });

      renderedSubSelectors.each((index, element) => {
        let subSelector = $(element);
        if (!this.parent && !commonProperties) {
          this.config.selectedPropertiesData[COMMON_PROPERTIES] = {};
        }
        if (properties.indexOf(subSelector.attr('id')) === -1 && !this.parent) {
          subSelector.remove();
        }
      });

      if (definitionsIdentifiers.length === 0) {
        renderedSubSelectors.remove();
      }
      this.updateSubPropertiesData(this.config);
    }
  }

  hasProperty(definition, propertyName) {
    let properties = this.propertiesSelectorHelper.flattenDefinitionProperties(definition);
    return properties.some((property) => {
      return property.name === propertyName;
    });
  }

  /**
   * Should be called when property selected field is changed
   * @param propertyDefinition
   * @param property
   */
  onPropertyChange(propertyDefinition, property) {
    this.updateSelectedProperties(propertyDefinition, property);
    this.definitions.forEach((definition) => {
      // If selected property definition is common definition then update all properties with same name in all definitions
      // or if selected property definition is not common definition and property is deselected then deselect same property in the common definition
      if (propertyDefinition.identifier !== definition.identifier &&
        (this.isCommonDefinition(propertyDefinition) || (this.isCommonDefinition(definition) && !property.selected))) {
        this.selectProperty(definition, property.name, property.selected);
      }
    });
  }

  selectProperty(definition, propertyName, selected) {
    let definitionProperties = this.propertiesSelectorHelper.flattenDefinitionProperties(definition);
    definitionProperties.some((definitionProperty) => {
      if (definitionProperty.name === propertyName) {
        definitionProperty.selected = selected;
        this.updateSelectedProperties(definition, definitionProperty);
      }
    });
  }

  /**
   * Update selected properties map when the model is changed
   * @param definition
   * @param property
   */
  updateSelectedProperties(definition, property) {
    if (property.selected) {
      this.addCodelistPropertySettings(definition.identifier, property);
      this.addObjectPropertySettings(definition.identifier, property);
      if (this.config.subPropertyOf) {
        this.addSubProperty(property);
      } else {
        this.config.selectedProperties[definition.identifier][property.name] = {name: property.name};
      }
    } else {
      if (this.config.subPropertyOf) {
        this.removeSubProperty(property);
      } else {
        this.removePropertySettings(definition.identifier, property);
        delete this.config.selectedProperties[definition.identifier][property.name];
      }
    }
  }

  /**
   * Select/deselect all properties for given definition
   * @param definition
   * @param selectAll
   */
  selectAll(definition, selectAll) {
    let properties = this.propertiesSelectorHelper.flattenDefinitionProperties(definition);
    properties.forEach((property) => {
      if (this.showProperty(property, definition.filter)) {
        let throwOnChange = property.selected !== selectAll;
        property.selected = selectAll;
        if (throwOnChange) {
          this.onPropertyChange(definition, property);
        }
      }
    });
  }

  isCommonDefinition(definition) {
    return definition.identifier === COMMON_PROPERTIES;
  }

  showProperty(property, filter) {
    return !filter || property.label.toLowerCase().indexOf(filter.toLowerCase()) > -1;
  }

  /**
   * Show region property if it contains at least one not filtered field
   * @param property
   * @param filter
   * @returns {boolean}
   */
  showRegion(property, filter) {
    if (this.propertiesSelectorHelper.isRegion(property)) {
      for (let i = 0; i < property.fields.length; i++) {
        if (this.showProperty(property.fields[i], filter)) {
          return true;
        }
      }
    }
    return false;
  }

  hasMultipleDefinitions() {
    return this.definitions.length > 1;
  }

  /**
   * Adds sub properties selector to form
   * @param property - the property for which sub properties should be displayed
   */
  addSubPropertySelector(property) {
    if (this.subPropertiesConfig[property.name]) {
      return;
    }

    this.subPropertiesConfig[property.name] = {
      selectedProperties: this.extractSelectedSubProperties(this.config.selectedProperties, property.name),
      subPropertyOf: property.name
    };

    let definitionsTypeConfig = {
      selectedTypes: this.getDefinitionsRange(property.controlDefinition),
      selectedProperties: {}
    };

    this.propertiesSelectorHelper.getDefinitionsArray(definitionsTypeConfig, this.subPropertiesConfig[property.name]).then((definitions) => {
      if (definitions.length === 1) {
        definitions[0].id = definitions[0].label + ': ' + property.label;
        definitions[0].label = definitions[0].label + ': ' + property.label;
        this.subPropertiesConfig[property.name].selectedProperties = this.extractSelectedSubProperties(this.config.selectedProperties, property.name, definitions[0].identifier);
      } else {
        this.subPropertiesConfig[property.name].commonPropertiesLabel = this.getDefinitionsLabel(definitions) + ': ' + property.label;
      }

      if (!this.innerScope) {
        this.innerScope = this.$scope.$new();
      }
      let subPropertiesSelector = this.$element.find('.sub-properties-selector');
      let subSelector = this.$compile(`<seip-properties-selector id="${property.name}" parent="propertiesSelector.config" config="propertiesSelector.subPropertiesConfig['${property.name}']"></seip-properties-selector>`)(this.innerScope);
      subPropertiesSelector.append(subSelector);
    });
  }

  /**
   * Removes sub properties selector from form
   * @param deselectedPropertyName - deselected property name
   */
  removeSubPropertySelector(deselectedPropertyName) {

    // Detect if sub property selector should be removed. One property should be selected in different definition group.
    // It's sub properties selector should be removed only if main property is deselected from all groups
    let shouldBeRemoved = true;
    Object.keys(this.config.selectedPropertiesData).some((definitionKey) => {
      if (!shouldBeRemoved) {
        return false;
      }
      Object.keys(this.config.selectedPropertiesData[definitionKey]).some((propertyName) => {
        if (this.config.selectedPropertiesData[definitionKey][propertyName] === RELATED_OBJECT_PROPERTIES && propertyName === deselectedPropertyName) {
          shouldBeRemoved = false;
          return false;
        }
      });
    });

    if (shouldBeRemoved) {
      this.$element.find('#' + deselectedPropertyName.replace(/:/i, '\\:')).remove();
      delete this.subPropertiesConfig[deselectedPropertyName];
    }

    this.updateSubPropertiesData(this.config);
  }

  /**
   * Adds property to config when it's selected in sub properties selector
   *      - If main property is selected from COMMON_PROPERTIES then it appear in all definition groups so
   *        sub property is added to all
   *      - If main property is selected from specific definition it appear only in this definition group
   * @param property - sub property which name and label should be add to model
   */
  addSubProperty(property) {
    Object.keys(this.parent.selectedProperties).forEach((definitionKey) => {
      let selectedMainProperty = this.parent.selectedProperties[definitionKey][this.config.subPropertyOf];
      if (!selectedMainProperty) {
        return;
      }
      selectedMainProperty[SELECTED_PROPERTIES] = selectedMainProperty[SELECTED_PROPERTIES] || [];
      selectedMainProperty[SELECTED_PROPERTIES].push({name: property.name, label: property.label});
    });
    this.updateSubPropertiesData(this.parent);
  }

  /**
   * Removes property from config when it's deselected in sub properties selector
   *      - If main property is selected from COMMON_PROPERTIES then it appear in all definition groups so
   *        sub property is removed from all
   *      - If main property is selected from specific definition it appear only in this definition group
   * @param property - sub property which should be removed from model
   */
  removeSubProperty(property) {
    Object.keys(this.parent.selectedProperties).forEach((definitionKey) => {
      let selectedMainProperty = this.parent.selectedProperties[definitionKey][this.config.subPropertyOf];
      if (!selectedMainProperty) {
        return;
      }
      selectedMainProperty[SELECTED_PROPERTIES] = selectedMainProperty[SELECTED_PROPERTIES].filter((selectedProperty) => {
        return selectedProperty.name !== property.name;
      });
    });
    this.updateSubPropertiesData(this.parent);
  }

  /**
   * Detect if any property is checked in sub properties selector and if yes updates show related proerties flag
   * @param config - properties selector config
   */
  updateSubPropertiesData(config) {
    let subPropertyIsSelected = false;
    Object.keys(config.selectedProperties).some((definitionKey) => {
      if (subPropertyIsSelected) {
        return false;
      }
      Object.keys(config.selectedProperties[definitionKey]).some((propertyName) => {
        let selectedProperties = config.selectedProperties[definitionKey][propertyName].selectedProperties;
        if (selectedProperties && selectedProperties.length > 0) {
          subPropertyIsSelected = true;
          return false;
        }
      });
    });
    config.selectedSubPropertiesData.showRelatedProperties = subPropertyIsSelected;
  }

  /**
   * Get defined definition range of given property
   * @param controlDefinition - property control definition
   * @returns {Array} array with definition identifiers (emf:Case, emf:Project etc.) or empty array if no range is available
   */
  getDefinitionsRange(controlDefinition) {
    if (!controlDefinition || !controlDefinition.controlParams) {
      return [];
    } else {
      let range = [];
      Object.keys(controlDefinition.controlParams).forEach((param) => {
        let controlParam = controlDefinition.controlParams[param];
        if (controlParam.name === DEFINITION_RANGE_FIELD && controlParam.value) {
          range = controlParam.value.replace(/\s/g,'').split(',');
        }
      });
      return range;
    }
  }

  /**
   * Get all definitions labels
   * @param definitions - given definitions
   * @returns {string} - all labels as comma separated string
   */
  getDefinitionsLabel(definitions) {
    let commonPropertiesLabel = '';
    definitions.forEach((definition) => {
      commonPropertiesLabel = commonPropertiesLabel + definition.label + ', ';
    });
    return commonPropertiesLabel.replace(/,\s*$/, '');
  }

  extractSelectedSubProperties(selectedProperties, propertyName, forDefinition) {
    forDefinition = forDefinition || COMMON_PROPERTIES;
    let properties = {};
    properties[forDefinition] = {};
    Object.keys(selectedProperties).forEach((definitionKey) => {
      if (selectedProperties[definitionKey][propertyName] && selectedProperties[definitionKey][propertyName].selectedProperties) {
        selectedProperties[definitionKey][propertyName].selectedProperties.forEach((property) => {
          properties[forDefinition][property.name] = {name: property.name};
        });
      }
    });
    return properties;
  }
}
