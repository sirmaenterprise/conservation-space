import {Component, View, Inject, NgScope} from 'app/app';
import {Collapsible} from 'components/collapsible/collapsible';
import {PropertiesSelectorHelper, COMMON_PROPERTIES} from 'idoc/widget/properties-selector/properties-selector-helper';
import {CONTROL_TYPE} from 'models/model-utils';
import {TranslateService} from 'services/i18n/translate-service';
import _ from 'lodash';
import propertiesSelectorTemplate from 'idoc/widget/properties-selector/properties-selector.html!text';
import 'idoc/widget/properties-selector/properties-selector.css!';

@Component({
  selector: 'seip-properties-selector',
  properties: {
    config: 'config'
  }
})
@View({
  template: propertiesSelectorTemplate
})
@Inject(NgScope, PropertiesSelectorHelper, TranslateService)
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
  constructor($scope, propertiesSelectorHelper, translateService) {
    this.propertiesSelectorHelper = propertiesSelectorHelper;
    this.translateService = translateService;
    $scope.$watch(() => {
      if (this.config && this.config.definitions) {
        return this.config.definitions.map((definition) => {
          return definition.identifier;
        });
      }
      return null;
    }, (definitionsIdentifiers) => {
      let definitions = this.config.definitions;
      if (definitions) {
        let commonProperties = this.extractCommonProperties(definitions);
        this.initSelectedProperties(definitionsIdentifiers, commonProperties);
        if (commonProperties && commonProperties.length > 0) {
          let commonDefinition = {
            identifier: COMMON_PROPERTIES,
            label: 'odw.config.common.properties',
            fields: commonProperties
          };
          this.definitions = [commonDefinition, ...definitions];
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
        property.selected = selectedProperties.indexOf(property.name) !== -1;
        if (property.selected) {
          this.addPropertySettings(definitionId, property);
        }
      });
    });
  }

  removePropertySettings(definitionId, property) {
    if (this.config.selectedPropertiesData && this.config.selectedPropertiesData[definitionId]) {
      delete this.config.selectedPropertiesData[definitionId][property.name];
      delete property.config;
    }
  }

  isPropertyVisible(property) {
    return property.selected && !!this.config.selectedPropertiesData;
  }

  addPropertySettings(definitionId, property) {
    if (!property.codelist || !this.config.selectedPropertiesData) {
      return;
    }
    // default visualization type
    var controlType = CONTROL_TYPE.CODELIST;
    var controlTypesByDefinition = this.config.selectedPropertiesData[definitionId];
    // check for new visualization type
    if (controlTypesByDefinition && controlTypesByDefinition[property.name]) {
      controlType = controlTypesByDefinition[property.name];
    }
    property.config = {
      selected: controlType,
      options: this.buildPropConfig(property.multivalue)
    };
    this.updatePropertySettings(definitionId, property);
  }

  updatePropertySettings(definitionId, property) {
    var controlTypesByDefinition = this.config.selectedPropertiesData[definitionId];
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
  }

  buildPropConfig(isMultiValue) {
    var bundle = isMultiValue ? 'widget.property.option.checkbox' : 'widget.property.option.radio';
    return [
      {
        controlType: CONTROL_TYPE.CODELIST,
        label: this.translateService.translateInstant('widget.property.option.dropdown')
      },
      {
        controlType: CONTROL_TYPE.CODELIST_LIST,
        label: this.translateService.translateInstant(bundle)
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
    let currentCommonSelectedProperties = this.config.selectedProperties[COMMON_PROPERTIES] || [];
    let updatedCommonSelectedProperties = [];
    if (commonProperties && currentCommonSelectedProperties.length > 0) {
      commonProperties.forEach((commonProperty) => {
        if (currentCommonSelectedProperties.indexOf(commonProperty.name) !== -1) {
          updatedCommonSelectedProperties.push(commonProperty.name);
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

    // Update selected properties of newly added definitions with common selected properties
    definitionsIdentifiers.forEach((definitionIdentifier) => {
      if (!this.config.selectedProperties[definitionIdentifier]) {
        this.config.selectedProperties[definitionIdentifier] = _.clone(this.config.selectedProperties[COMMON_PROPERTIES]);
      }
    });
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
      this.addPropertySettings(definition.identifier, property);
      this.config.selectedProperties[definition.identifier].push(property.name);
    } else {
      this.removePropertySettings(definition.identifier, property);
      let index = this.config.selectedProperties[definition.identifier].indexOf(property.name);
      if (index !== -1) {
        this.config.selectedProperties[definition.identifier].splice(index, 1);
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
}
