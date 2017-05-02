import {PropertiesSelector} from 'idoc/widget/properties-selector/properties-selector';
import {PropertiesSelectorHelper, COMMON_PROPERTIES} from 'idoc/widget/properties-selector/properties-selector-helper';
import {PromiseAdapterMock} from '../../../adapters/angular/promise-adapter-mock';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';
import {CONTROL_TYPE} from 'models/model-utils';

describe('PropertiesSelector', () => {
  it('should properly initialize selected properties when instantiated with previous configuration', () => {
    let propertiesSelector = mockPropertiesSelector();
    let definition = findDefinitionByIdentifier(propertiesSelector.definitions, 'GEP111111');
    let property = findPropertyByName(definition, 'property2');
    expect(property.selected).to.be.true;
  });

  it('should properly generate common properties', () => {
    let propertiesSelector = mockPropertiesSelector();
    let definition = findDefinitionByIdentifier(propertiesSelector.definitions, COMMON_PROPERTIES);
    expect(definition.fields).to.have.length(2);
  });

  describe('extractCommonProperties()', () => {
    it('should return an array with common properties', () => {
      let propertiesSelector = mockPropertiesSelector();
      let commonProperties = propertiesSelector.extractCommonProperties(mockMultipleDefinitions());
      expect(commonProperties).to.have.length(2);
      expect(commonProperties[0]).to.have.property('name', 'property5')
      expect(commonProperties[1]).to.have.property('name', 'property3')
    });
  });

  describe('hasProperty()', () => {
    it('should return true if property exists', () => {
      let propertiesSelector = mockPropertiesSelector();
      expect(propertiesSelector.hasProperty(mockMultipleDefinitions()[0], 'property5')).to.be.true;
    });

    it('should return false if property does not exist', () => {
      let propertiesSelector = mockPropertiesSelector();
      expect(propertiesSelector.hasProperty(mockMultipleDefinitions()[0], 'not existing property')).to.be.false;
    });
  });

  describe('onPropertyChange()', () => {
    it('should select all properties with the same name when a common property is changed', () => {
      let commonPropertyName = 'property5';
      let propertiesSelector = mockPropertiesSelector();
      let commonDefinition = findDefinitionByIdentifier(propertiesSelector.definitions, COMMON_PROPERTIES);
      let commonProperty = findPropertyByName(commonDefinition, commonPropertyName);
      commonProperty.selected = true;
      propertiesSelector.onPropertyChange(commonDefinition, commonProperty);
      propertiesSelector.definitions.forEach((definition) => {
        let property = findPropertyByName(definition, commonPropertyName);
        expect(property.selected).to.be.true;
      });
    });

    it('should deselect all properties with the same name when a common property is changed', () => {
      let commonPropertyName = 'property5';
      let propertiesSelector = mockPropertiesSelector();
      // Select all properties with name commonPropertyName in all definitions
      propertiesSelector.definitions.forEach((definition) => {
        let property = findPropertyByName(definition, commonPropertyName);
        property.selected = true;
      });
      let commonDefinition = findDefinitionByIdentifier(propertiesSelector.definitions, COMMON_PROPERTIES);
      let commonProperty = findPropertyByName(commonDefinition, commonPropertyName);
      commonProperty.selected = false;
      propertiesSelector.onPropertyChange(commonDefinition, commonProperty);
      propertiesSelector.definitions.forEach((definition) => {
        let property = findPropertyByName(definition, commonPropertyName);
        expect(property.selected).to.be.false;
      });
    });

    it('should deselect common property if normal property with the same name is deselected', () => {
      let commonPropertyName = 'property5';
      let propertiesSelector = mockPropertiesSelector();
      // Select all properties with name commonPropertyName in all definitions
      propertiesSelector.definitions.forEach((definition) => {
        let property = findPropertyByName(definition, commonPropertyName);
        property.selected = true;
      });
      let commonProperty = findPropertyInDefinition(propertiesSelector.definitions, COMMON_PROPERTIES, commonPropertyName);
      expect(commonProperty.selected).to.be.true;
      let definition = findDefinitionByIdentifier(propertiesSelector.definitions, 'GEP111111');
      let property = findPropertyByName(definition, commonPropertyName);
      property.selected = false;
      propertiesSelector.onPropertyChange(definition, property);
      expect(commonProperty.selected).to.be.false;
    });
  });

  describe('selectProperty()', () => {
    it('should update property selected flag', () => {
      let propertiesSelector = mockPropertiesSelector();
      propertiesSelector.updateSelectedProperties = sinon.spy();

      let property = findPropertyInDefinition(propertiesSelector.definitions, 'GEP100002', 'property1');
      expect(property.selected).to.be.false;
      propertiesSelector.selectProperty(findDefinitionByIdentifier(propertiesSelector.definitions, 'GEP100002'), 'property1', true);
      expect(propertiesSelector.updateSelectedProperties.calledOnce);
      expect(property.selected).to.be.true;
      propertiesSelector.selectProperty(findDefinitionByIdentifier(propertiesSelector.definitions, 'GEP100002'), 'property1', false);
      expect(propertiesSelector.updateSelectedProperties.calledTwice);
      expect(property.selected).to.be.false;
    });

    it('should update selected properties array on property select/deselect', () => {
      let propertiesSelector = mockPropertiesSelector();

      let property = findPropertyInDefinition(propertiesSelector.definitions, 'GEP100002', 'property1');
      propertiesSelector.selectProperty(findDefinitionByIdentifier(propertiesSelector.definitions, 'GEP100002'), 'property1', true);
      expect(propertiesSelector.config.selectedProperties['GEP100002']).to.eql(['property1']);
      propertiesSelector.selectProperty(findDefinitionByIdentifier(propertiesSelector.definitions, 'GEP100002'), 'property1', false);
      expect(propertiesSelector.config.selectedProperties['GEP100002']).to.have.length(0);
    });
  });

  describe('selectAll()', () => {
    it('should select all properties', () => {
      let propertiesSelector = mockPropertiesSelector();
      let definition = findDefinitionByIdentifier(propertiesSelector.definitions, 'GEP111111');
      propertiesSelector.selectAll(definition, true);
      flattenProperties(definition).forEach((property) => {
        expect(property.selected).to.be.true;
      });
    });

    it('should deselect all properties', () => {
      let propertiesSelector = mockPropertiesSelector();
      let definition = findDefinitionByIdentifier(propertiesSelector.definitions, 'GEP111111');
      propertiesSelector.selectAll(definition, false);
      flattenProperties(definition).forEach((property) => {
        expect(property.selected).to.be.false;
      });
    });
  });

  describe('isCommonDefinition()', () => {
    it('should properly return true or false depending on the definition', () => {
      let propertiesSelector = mockPropertiesSelector();
      let definition = findDefinitionByIdentifier(propertiesSelector.definitions, COMMON_PROPERTIES);
      expect(propertiesSelector.isCommonDefinition(definition)).to.be.true;
      definition = findDefinitionByIdentifier(propertiesSelector.definitions, 'GEP111111');
      expect(propertiesSelector.isCommonDefinition(definition)).to.be.false;
    });
  });

  describe('showProperty()', () => {
    it('should return proper result depending on property label and given filter', () => {
      let propertiesSelector = mockPropertiesSelector();
      let property = findPropertyInDefinition(propertiesSelector.definitions, 'GEP111111', 'property2');
      expect(propertiesSelector.showProperty(property, 'Property 2')).to.be.true;
      expect(propertiesSelector.showProperty(property, 'Non existing property')).to.be.false;
    });
  });

  describe('showRegion()', () => {
    it('should return proper result depending on region properties labels and given filter', () => {
      let propertiesSelector = mockPropertiesSelector();
      let definition = findDefinitionByIdentifier(propertiesSelector.definitions, 'GEP111111');
      expect(propertiesSelector.showRegion(definition.fields[0], 'Property 5')).to.be.true;
      expect(propertiesSelector.showRegion(definition.fields[0], 'Non existing property')).to.be.false;
    });
  });
  describe('hasMultipleDefinitions()', () => {
    it('should return true if there is more than one definition', () => {
      let propertiesSelector = mockPropertiesSelector();
      expect(propertiesSelector.hasMultipleDefinitions()).to.be.true;
    });
  });

  describe('initSelectedProperties()', () => {
    it('should remove common selected properties if they are no longer common properties', () => {
      let propertiesSelector = mockPropertiesSelector();
      propertiesSelector.config.selectedProperties[COMMON_PROPERTIES] = ['property1', 'property2', 'property3'];
      let commonProperties = [{
        name: 'property1'
      }, {
        name: 'property3'
      }];
      propertiesSelector.initSelectedProperties([], commonProperties);
      expect(propertiesSelector.config.selectedProperties[COMMON_PROPERTIES]).to.eql(['property1', 'property3']);
    });

    it('should remove selected properties for definitions that are no longer selected', () => {
      let propertiesSelector = mockPropertiesSelector();
      expect(propertiesSelector.config.selectedProperties['GEP111111']).to.not.be.undefined;
      propertiesSelector.initSelectedProperties(['GEP100002']);
      expect(propertiesSelector.config.selectedProperties['GEP111111']).to.be.undefined;
    });

    it('should initialize selected properties for newly added definitions with common selected properties', () => {
      let propertiesSelector = mockPropertiesSelector();
      propertiesSelector.config.selectedProperties[COMMON_PROPERTIES] = ['property1', 'property2', 'property3'];
      let commonProperties = [{
        name: 'property1'
      }, {
        name: 'property2'
      }, {
        name: 'property3'
      }];
      expect(propertiesSelector.config.selectedProperties['NEW_DEFINITION_ID']).to.be.undefined;
      propertiesSelector.initSelectedProperties(['GEP111111', 'NEW_DEFINITION_ID'], commonProperties);
      expect(propertiesSelector.config.selectedProperties['NEW_DEFINITION_ID']).to.eql(['property1', 'property2', 'property3']);
    });
  });

  describe('buildPropConfig()', () => {
    it('should build appropriate controls', () => {
      let expectedResult = [
        {
          controlType: CONTROL_TYPE.CODELIST,
          label: 'label'
        },
        {
          controlType: CONTROL_TYPE.CODELIST_LIST,
          label: 'label'
        }
      ];
      let propertiesSelector = mockPropertiesSelector();
      expect(propertiesSelector.buildPropConfig()).to.deep.equal(expectedResult);
    });
  });

  describe('addPropertySettings()', () => {
    it('should not add property config if codelist is not present', () => {
      let propertiesSelector = mockPropertiesSelector();
      let property = {};
      propertiesSelector.addPropertySettings('id', property);
      expect(property.config).to.be.undefined;
    });

    it('should not add property config if property data is not present', () => {
      let propertiesSelector = mockPropertiesSelector();
      let property = {codelist: 1};
      propertiesSelector.addPropertySettings('id', property);
      expect(property.config).to.be.undefined;
    });

    it('should add default control type', () => {
      let propertiesSelector = mockPropertiesSelector();
      let property = {
        name: "name",
        codelist: 100,
      };
      propertiesSelector.config.selectedPropertiesData = {};
      propertiesSelector.addPropertySettings('id', property);
      expect(property.config.selected).to.be.equal(CONTROL_TYPE.CODELIST);
    });

    it('should add control type from selected properties data', () => {
      let propertiesSelector = mockPropertiesSelector();
      let property = {
        name: "name",
        codelist: 1
      };
      propertiesSelector.config.selectedPropertiesData = {
        id: {
          "name": CONTROL_TYPE.CODELIST_LIST
        }
      };
      propertiesSelector.addPropertySettings('id', property);
      expect(property.config.selected).to.be.equal(CONTROL_TYPE.CODELIST_LIST);
    });
  });

  describe('updatePropertySettings()', () => {
    it('should not update properties select data if control type is codelist', () => {
      let property = {
        name: "name",
        codelist: 1,
        config: {
          selected: CONTROL_TYPE.CODELIST
        }
      };
      let propertiesSelector = mockPropertiesSelector();
      propertiesSelector.config.selectedPropertiesData = {'id': {}};
      propertiesSelector.updatePropertySettings('id', property);
      expect(propertiesSelector.config.selectedPropertiesData).to.be.empty;
    });

    it('should not update properties select data', () => {
      let property = {
        name: "name",
        codelist: 1,
        config: {
          selected: CONTROL_TYPE.CODELIST_LIST
        }
      };
      let propertiesSelector = mockPropertiesSelector();
      propertiesSelector.config.selectedPropertiesData = {};
      propertiesSelector.updatePropertySettings('id', property);
      expect(propertiesSelector.config.selectedPropertiesData['id'][property.name]).to.be.equal(CONTROL_TYPE.CODELIST_LIST);
    });
  });

  describe('removePropertySettings()', () => {
    it('should remove property settings ', () => {
      var property = {
        name: 'name',
        config: {}
      };
      let propertiesSelector = mockPropertiesSelector();
      propertiesSelector.config.selectedPropertiesData = {
        'id': {
          'name': {}
        }
      };
      propertiesSelector.removePropertySettings('id', property);
      expect(property.config).to.be.undefined;
      expect(propertiesSelector.config.selectedPropertiesData['id'][property.name]).to.be.undefined;
    });
  });

  describe('isPropertyVisible()', () => {
    it('should be visible if property is selected and selected properties data is available', () => {
      let property = {
        selected: true
      };
      let propertiesSelector = mockPropertiesSelector();
      propertiesSelector.config.selectedPropertiesData = {};
      expect(propertiesSelector.isPropertyVisible(property)).to.be.true;
    });

    it('should not be visible if property is not selected', () => {
      let property = {
        selected: false
      };
      let propertiesSelector = mockPropertiesSelector();
      propertiesSelector.config.selectedPropertiesData = {};
      expect(propertiesSelector.isPropertyVisible(property)).to.be.false;
    });

    it('should not be visible if properties select data is no present', () => {
      let property = {
        selected: true
      };
      let propertiesSelector = mockPropertiesSelector();
      expect(propertiesSelector.isPropertyVisible(property)).to.be.false;
    });
  });
});

function findDefinitionByIdentifier(definitions, identifier) {
  return definitions.find((definition) => {
    return definition.identifier === identifier;
  });
}

function findPropertyByName(definition, propertyName) {
  let properties = flattenProperties(definition);
  return properties.find((property) => {
    return property.name === propertyName;
  });
}

function findPropertyInDefinition(definitions, definitionIdentifier, propertyName) {
  let definition = findDefinitionByIdentifier(definitions, definitionIdentifier);
  return findPropertyByName(definition, propertyName);
}

function flattenProperties(definition) {
  let properties = [];
  for (let i in definition.fields) {
    let property = definition.fields[i];
    if (isRegion(property)) {
      properties.push(...flattenProperties(property));
    } else {
      properties.push(property);
    }
  }
  return properties;
}

function isRegion(property) {
  return property.fields && property.fields.length > 0;
}

function mockPropertiesSelector() {
  let scope = mock$scope();
  let propertiesSelectorHelper = new PropertiesSelectorHelper(PromiseAdapterMock.mockAdapter(), mockDefinitionService());

  let propertiesSelector = new PropertiesSelector(scope, propertiesSelectorHelper, mockTranslateService());
  propertiesSelector.config = {
    definitions: mockMultipleDefinitions(),
    selectedProperties: {
      GEP111111: ['property2']
    }
  };
  scope.$digest();
  return propertiesSelector;
}

function mockDefinitionService() {
  return {
    getDefinitions: () => {
      return Promise.resolve({data: mockMultipleDefinitions()});
    }
  };
}

function mockTranslateService() {
  return {
    translateInstant: sinon.spy(() => {
      return 'label';
    })
  };
}

function mockMultipleDefinitions() {
  return [
    {
      identifier: 'GEP111111',
      label: 'Project for testing',
      fields: [
        {
          name: 'property1',
          label: 'Property 1',
          fields: [
            {
              name: 'property5',
              label: 'Property 5',
            },
            {
              name: 'property6',
              label: 'Property 6',
            }
          ]
        },
        {
          name: 'property2',
          label: 'Property 2',
        },
        {
          name: 'property3',
          label: 'Property 3',
        }
      ]
    },
    {
      identifier: 'GEP100002',
      label: 'Test Project',
      fields: [
        {
          name: 'property1',
          label: 'Property 1',
        },
        {
          name: 'property3',
          label: 'Property 3',
        },
        {
          name: 'property5',
          label: 'Property 5',
        }
      ]
    }
  ];
}
