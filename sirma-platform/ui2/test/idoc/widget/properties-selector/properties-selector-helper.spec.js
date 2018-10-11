import {PropertiesSelectorHelper, COMMON_PROPERTIES} from 'idoc/widget/properties-selector/properties-selector-helper';
import {CommonMocks} from './common-mocks';
import {ANY_OBJECT} from 'search/utils/search-criteria-utils';
import {SELECT_OBJECT_CURRENT, SELECT_OBJECT_MANUALLY, SELECT_OBJECT_AUTOMATICALLY} from 'idoc/widget/object-selector/object-selector';
import {SINGLE_SELECTION, MULTIPLE_SELECTION} from 'search/search-selection-modes';
import {LABEL_POSITION_LEFT, LABEL_POSITION_HIDE, LABEL_TEXT_LEFT, LABEL_TEXT_RIGHT} from 'form-builder/form-wrapper';
import {PromiseAdapterMock} from '../../../adapters/angular/promise-adapter-mock';
import {PromiseStub} from 'test/promise-stub';

describe('PropertiesSelectorHelper', () => {
  // Tests race conditions while updating definitions variable.
  // If two loadings are started one after another and second load finish first its result will be overriden by the first loader unless properly synchronized.
  // This test ensures that this would not happen
  it('getDefinitionsArray should update definitions with the result of latest, not last finished, call', (done) => {
    let propertiesSelectorHelper = initSelectorHelper();

    propertiesSelectorHelper.mergeDefinitions = sinon.spy();
    propertiesSelectorHelper.removeSelectedProperties = () => {};

    // shared object between the two calls
    let propertiesSelectorConfig = {};

    sinon.stub(propertiesSelectorHelper, 'loadDefinitions', () => {
      return new Promise((resolve) => {
        setTimeout(() => { resolve(['call1']); }, 0);
      });
    });
    // because of the timeout this loader will finish second but it will not call propertiesSelectorHelper.mergeDefinitions and override definitions
    let loader1 = propertiesSelectorHelper.getDefinitionsArray({}, propertiesSelectorConfig, {});

    propertiesSelectorHelper.loadDefinitions.restore();
    sinon.stub(propertiesSelectorHelper, 'loadDefinitions', () => {
      return Promise.resolve(['call2']);
    });
    let loader2 = propertiesSelectorHelper.getDefinitionsArray({}, propertiesSelectorConfig, {});

    Promise.all([loader1, loader2]).then(() => {
      expect(propertiesSelectorHelper.mergeDefinitions.callCount).to.equal(1);
      expect(propertiesSelectorHelper.mergeDefinitions.getCall(0).args[1][0]).to.equal('call2');
      done();
    }).catch(done);
  });

  it('getDefinitionsArray should remove any duplicates when the definitions are loaded', () => {
    let propertiesSelectorHelper = initSelectorHelper();
    propertiesSelectorHelper.loadDefinitions = () => {
      return PromiseStub.resolve([{identifier: '1'}, {identifier: '2'}, {identifier: '1'}]);
    };
    propertiesSelectorHelper.afterDefinitionsLoaded = sinon.spy();
    propertiesSelectorHelper.getDefinitionsArray({}, {});
    expect(propertiesSelectorHelper.afterDefinitionsLoaded.calledOnce).to.be.true;
    let definitions = propertiesSelectorHelper.afterDefinitionsLoaded.getCall(0).args[1];
    expect(definitions).to.deep.equal([{identifier: '1'}, {identifier: '2'}]);
  });

  it('mergeDefinitions should replace current definitions with new definitions preserving references', () => {
    let propertiesSelectorHelper = initSelectorHelper();
    let currentDefinitions = [{
      identifier: 'definition1',
      value: 'value1'
    }, {
      identifier: 'definition2',
      value: 'value2'
    }, {
      identifier: 'definition3',
      value: 'value3'
    }];
    let newDefinitions = [{
      identifier: 'definition1',
      value: 'newValue1'
    }, {
      identifier: 'definition4',
      value: 'newValue4'
    }];
    propertiesSelectorHelper.mergeDefinitions(currentDefinitions, newDefinitions);
    let expectedDefinitions = [{
      identifier: 'definition1',
      value: 'value1'
    }, {
      identifier: 'definition4',
      value: 'newValue4'
    }];
    expect(currentDefinitions).to.eql(expectedDefinitions);
  });

  it('should load correct definition depending on select object mode', (done) => {
    let propertiesSelectorHelper = initSelectorHelper();
    let config = {};
    config.selectObjectMode = SELECT_OBJECT_AUTOMATICALLY;
    config.criteria = CommonMocks.mockCriteria();
    propertiesSelectorHelper.loadDefinitions(CommonMocks.mockContext(), config).then((definition) => {
      expect(definition[0].identifier).to.equal('full#emf:Case');
      expect(definition[0].label).to.equal('Case');
      expect(definition[0].fields).to.have.length(1);

      expect(definition[1].identifier).to.equal('GEP11111');
      expect(definition[1].label).to.equal('Project for testing');
      expect(definition[1].fields).to.have.length(2);
      done();
    }).catch(done);
  });

  describe('getSelectObjectMode', () => {
    it('should return original selectObjectMode if object is version', () => {
      let config = {
        selectObjectMode: SELECT_OBJECT_MANUALLY,
        originalConfigurationDiff: {
          selectObjectMode: SELECT_OBJECT_AUTOMATICALLY
        }
      };
      expect(PropertiesSelectorHelper.getSelectObjectMode(config)).to.equals(SELECT_OBJECT_AUTOMATICALLY);
    });

    it('should return original selectObjectMode if object is not version', () => {
      let config = {
        selectObjectMode: SELECT_OBJECT_AUTOMATICALLY
      };
      expect(PropertiesSelectorHelper.getSelectObjectMode(config)).to.equals(SELECT_OBJECT_AUTOMATICALLY);
    });
  });

  it('getSelectedObjects should return correct object depending on select object mode', () => {
    let propertiesSelectorHelper = initSelectorHelper();
    let config = {
      selectObjectMode: SELECT_OBJECT_CURRENT
    };
    expect(propertiesSelectorHelper.getSelectedObjects(CommonMocks.mockContext(), config)).to.eql(['currentObjectId']);
    config.selectObjectMode = SELECT_OBJECT_MANUALLY;
    config.selectedObject = 'sharedObjectId';
    config.selection = SINGLE_SELECTION;
    expect(propertiesSelectorHelper.getSelectedObjects(CommonMocks.mockContext(), config)).to.eql(['sharedObjectId']);
  });

  it('removeSelectedProperties should remove missing definitions from selectedProperties map', () => {
    let propertiesSelectorHelper = initSelectorHelper();
    let propertiesSelectorConfig = mockPropertiesSelectorConfig();
    let config = {};
    config.selectedProperties = {
      definitionId1: [],
      definitionId2: [],
      definitionId3: []
    };
    config.selectedProperties[COMMON_PROPERTIES] = [];
    propertiesSelectorConfig.definitions = [{identifier: 'definitionId1', fields: []}, {identifier: 'definitionId3', fields: []}];
    propertiesSelectorHelper.removeSelectedProperties(propertiesSelectorConfig, config);
    expect(config.selectedProperties).to.have.property(COMMON_PROPERTIES);
    expect(config.selectedProperties).to.have.property('definitionId1');
    expect(config.selectedProperties).to.not.have.property('definitionId2');
    expect(config.selectedProperties).to.have.property('definitionId3');
  });

  it('extractObjectsDefinitions should return definition converted to internal format', (done) => {
    let propertiesSelectorHelper = initSelectorHelper();
    propertiesSelectorHelper.extractObjectsDefinitions(['currentObjectId'], CommonMocks.mockContext()).then((definition) => {
      expect(definition[0].identifier).to.equal('definitionId');
      expect(definition[0].label).to.equal('definitionLabel');
      expect(definition[0].fields).to.have.length(1);
      done();
    }).catch(done);
  });

  it('extractObjectsDefinitions should resolve with empty array if object ids array is undefined', (done) => {
    let propertiesSelectorHelper = initSelectorHelper();
    propertiesSelectorHelper.extractObjectsDefinitions(undefined, CommonMocks.mockContext()).then((definitions) => {
      expect(definitions).to.have.length(0);
      done();
    }).catch(done);
  });

  it('filterDefinitionFields should filter out all properties with display type SYSTEM', () => {
    let definition = {
      fields: [
        {
          identifier: 'field1',
          displayType: 'EDITABLE'
        }, {
          identifier: 'field2',
          displayType: 'SYSTEM'
        }, {
          identifier: 'field3',
          displayType: 'EDITABLE',
          fields: [{
            identifier: 'field3_field1',
            displayType: 'EDITABLE'
          }, {
            identifier: 'field3_field2',
            displayType: 'SYSTEM'
          }]
        }
      ]
    };
    let propertiesSelectorHelper = initSelectorHelper();
    let result = propertiesSelectorHelper.filterDefinitionFields(definition);
    expect(result).to.have.lengthOf(2);
    expect(result[1].fields).to.have.lengthOf(1);
  });

  it('extractDefinitionsByIdentifiers should return array with definitions converted to internal format', (done) => {
    let propertiesSelectorHelper = initSelectorHelper();
    propertiesSelectorHelper.definitionService = mockDefinitionService();
    propertiesSelectorHelper.extractDefinitionsByIdentifiers(['emf:Case', 'GEP11111']).then((definitions) => {
      expect(definitions).to.have.length(2);
      expect(definitions[0]).to.have.property('identifier', 'full#emf:Case');
      expect(definitions[1].fields).to.have.length(2);
      done();
    }).catch(done);
  });

  it('extractDefinitionsByIdentifiers should return all fields if any object is selected', (done) => {
    let propertiesSelectorHelper = initSelectorHelper();
    propertiesSelectorHelper.definitionService = mockDefinitionService();
    propertiesSelectorHelper.extractDefinitionsByIdentifiers([ANY_OBJECT]).then((definitions) => {
      expect(definitions).to.have.length(3);
      done();
    }).catch(done);
  });

  it('extractDefinitionsByIdentifiers should call getDefinitionsURIsMap with all definition ids returned by definitionsLoader if identifiers parameter is empty', (done) => {
    let propertiesSelectorHelper = initSelectorHelper();
    propertiesSelectorHelper.definitionService = mockDefinitionService();
    propertiesSelectorHelper.getDefinitionsURIsMap = sinon.spy(() => Promise.resolve({}));
    propertiesSelectorHelper.extractDefinitionsByIdentifiers([]).then((definitions) => {
      expect(propertiesSelectorHelper.getDefinitionsURIsMap.getCall(0).args[0]).to.eql(['emf:Case', 'GEP11111', 'definitionId']);
      propertiesSelectorHelper.getDefinitionsURIsMap.reset();
      done();
    }).catch(done);
  });

  it('flattenDefinitionProperties should extract properties from regions and return a flat array', () => {
    let propertiesSelectorHelper = initSelectorHelper();
    let result = propertiesSelectorHelper.flattenDefinitionProperties(mockDefinitionsServiceResponse().data[1]);
    expect(result).to.have.length(3);
  });

  it('isRegion should return proper result depending on the property type (regions have fields own property)', () => {
    let propertiesSelectorHelper = initSelectorHelper();
    let definition = mockDefinitionsServiceResponse().data[1];
    expect(propertiesSelectorHelper.isRegion(definition.fields[0])).to.be.false;
    expect(!!propertiesSelectorHelper.isRegion(definition.fields[1])).to.be.true;
  });

  it('getDefinitionsURIsMap should resolve with transformed URIs', (done) => {
    let propertiesSelectorHelper = initSelectorHelper();
    propertiesSelectorHelper.getDefinitionsURIsMap(['shortURI:1', 'shortURI:2']).then((result) => {
      expect(result).to.have.all.keys(['shortURI:1', 'shortURI:2']);
      expect(result['shortURI:1']).to.equal('full#shortURI:1');
      expect(result['shortURI:2']).to.equal('full#shortURI:2');
      done();
    }).catch(done);
  });

  describe('getSelectedProperties', () => {
    let models = {
      definitionId: 'ET220000',
      validationModel: {
        'rdf:type': {
          'dataType': 'any',
          'value': 'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Project',
          'messages': []
        },
        'semanticHierarchy': {
          'value': ['http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Activity']
        }
      }
    };

    it('should return selected properties by definitionId', () => {
      let selectedPropertiesMap = {
        'ET220000': {'inputTextEdit': {'name': 'inputTextEdit'} },
        'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Project': {'inputTextPreview': {'name': 'inputTextPreview'} }
      };
      expect(PropertiesSelectorHelper.getSelectedProperties(models, selectedPropertiesMap)).to.eql({'inputTextEdit': {'name': 'inputTextEdit'} });
    });

    it('should return selected properties by rdf type', () => {
      let selectedPropertiesMap = {
        'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Project': {'inputTextPreview': {'name': 'inputTextPreview'} }
      };
      expect(PropertiesSelectorHelper.getSelectedProperties(models, selectedPropertiesMap)).to.eql({'inputTextPreview': {'name': 'inputTextPreview'} });
    });

    it('should return selected properties by semantic hierarchy', () => {
      let selectedPropertiesMap = {
        'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Activity': {'inputTextPreview': {'name': 'inputTextPreview'} }
      };
      expect(PropertiesSelectorHelper.getSelectedProperties(models, selectedPropertiesMap)).to.eql({'inputTextPreview': {'name': 'inputTextPreview'} });
    });
  });

  it('generateDefinitionPropertiesSet should return a set with all properties name in the definition', () => {
    let definitions = mockDefinitionsServiceResponse();
    let propertiesSelectorHelper = initSelectorHelper();
    let propertiesSet = propertiesSelectorHelper.generateDefinitionPropertiesSet(definitions.data[1]);
    expect(propertiesSet.size).to.equals(3);
    expect(propertiesSet.has('projectField1')).to.be.true;
    expect(propertiesSet.has('projectField2SubField2')).to.be.true;
  });

  it('removeMissingSelectedProperties should remove all properties missing in the definitions from the selected properties', () => {
    let definitions = mockDefinitionsServiceResponse();
    let propertiesSelectorHelper = initSelectorHelper();
    let selectedProperties = {
      'GEP11111': {
        'projectField1': {'name': 'projectField1'},
        'unexistingProperty': {'name': 'unexistingProperty'},
        'projectField2SubField2': {'name': 'projectField2SubField2'},
        'anotherUnexistingProperty': {'name': 'anotherUnexistingProperty'}
      }
    };
    let expected = {
      'projectField1': {'name': 'projectField1'},
      'projectField2SubField2': {'name': 'projectField2SubField2'}
    };
    let filteredSelectedProperties = propertiesSelectorHelper.removeMissingSelectedProperties(definitions.data, selectedProperties);
    expect(filteredSelectedProperties['GEP11111']).to.eql(expected);
  });

  describe('transformSelectedProperies', () => {
    it('should convert array to map', () => {
      let expected = {
        'property1': {'name': 'property1'},
        'property2': {'name': 'property2'},
        'property3': {'name': 'property3'}
      };
      expect(PropertiesSelectorHelper.transformSelectedProperies(['property1', 'property2', 'property3'])).to.eql(expected);
    });

    it('should not change properties if format is correct', () => {
      let properies = {
        'property1': {'name': 'property1'},
        'property2': {'name': 'property2'},
        'property3': {'name': 'property3'}
      };
      expect(PropertiesSelectorHelper.transformSelectedProperies(properies)).to.eql(properies);
    });
  });
});

function initSelectorHelper() {
  return new PropertiesSelectorHelper(PromiseAdapterMock.mockAdapter(), mockDefinitionService(), mockNamespaceService());
}

function mockDefinitionService() {
  return {
    getFields: (identifiers) => {
      let allDefinitions = mockDefinitionsServiceResponse();
      if (identifiers && identifiers.length > 0) {
        let definitions = [];
        identifiers.forEach((definitionIdentifier) => {
          let definition = allDefinitions.data.find((definition) => {
            return definition.identifier === definitionIdentifier;
          });
          if (definition) {
            definitions.push(definition);
          }
        });
        return Promise.resolve({data: definitions});
      } else {
        return Promise.resolve(allDefinitions);
      }
    }
  };
}

function mockDefinitionsServiceResponse() {
  return {
    data: [{
      'identifier': 'emf:Case',
      'label': 'Case',
      'fields': [{
        name: 'caseField1',
        label: 'Case field 1',
        fields: [{
          name: 'caseField1SubField1',
          label: 'Case field 1 sub field 1'
        }]
      }, {
        name: 'caseField2',
        label: 'Case field 2',
        displayType: 'SYSTEM'
      }]
    }, {
      'identifier': 'GEP11111',
      'label': 'Project for testing',
      'fields': [{
        name: 'projectField1',
        label: 'Project field 1'
      }, {
        name: 'projectField2',
        label: 'Project field 2',
        fields: [{
          name: 'projectField2SubField1',
          label: 'Project field 2 sub field 1',
          displayType: 'SYSTEM'
        }, {
          name: 'projectField2SubField2',
          label: 'Project field 2 sub field 2'
        }]
      }]
    }, {
      'identifier': 'definitionId',
      'label': 'definitionLabel',
      'fields': [{
        name: 'field1',
        label: 'Field 1',
        fields: [{
          name: 'field1SubField1',
          label: 'Field 1 sub field 1'
        }]
      }, {
        name: 'field2',
        label: 'Field 2',
        displayType: 'SYSTEM'
      }]
    }]
  };
}

function mockPropertiesSelectorConfig() {
  return {
    definitions: [],
    selectedProperties: []
  };
}

function mockNamespaceService() {
  return {
    toFullURI: (shortURIs) => {
      let urisMap = shortURIs.reduce((result, uri) => {
        result[uri] = `full#${uri}`;
        return result;
      }, {});
      return Promise.resolve({data: urisMap});
    }
  }
}

function getAdvancedSearchCriteria() {
  return {
    condition: 'OR',
    rules: [{
      field: 'types',
      value: ['emf:Case', 'emf:Document', 'GEP11111']
    }, {
      field: 'types',
      value: undefined
    }]
  };
}