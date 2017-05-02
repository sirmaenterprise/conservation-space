import {InstanceCreateConfiguration} from 'create/instance-create-configuration';
import {PromiseStub} from 'test/promise-stub';
import {PromiseAdapterMock} from 'test/adapters/angular/promise-adapter-mock';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';

describe('InstanceCreateConfiguration', function () {

  var instanceCreateConfiguration;
  var eventbus;
  var propertiesService;
  var definitionService;
  var instanceService;

  beforeEach(() => {
    eventbus = mockEventbus();
    propertiesService = mockPropertiesService();
    definitionService = mockDefinitionService();
    instanceService = mockInstanceService();
    instanceCreateConfiguration = new InstanceCreateConfiguration(definitionService, instanceService, PromiseAdapterMock.mockImmediateAdapter(),
      mockTranslateService(), mockModelsService(), mockTimeout(), eventbus, propertiesService);
  });

  describe('ngOnInit()', () => {

    it('should publish InstanceCreateConfigurationEvent when onContextSelected() ', () => {
      instanceCreateConfiguration.assignType = sinon.spy();
      instanceCreateConfiguration.config.formConfig = {
        models: {}
      };
      instanceCreateConfiguration.modelsService = {
        getModels: sinon.spy(() => {
          return PromiseStub.resolve({
            models: {}
          });
        })
      };
      instanceCreateConfiguration.convertModelsToOptions = sinon.spy(() => {
        return [{id: 'emf:Document', text: 'Document'}];
      });

      instanceCreateConfiguration.onContextSelected();
      let args = instanceCreateConfiguration.eventbus.publish.getCall(0).args;
      expect(args[0].getData()[0]).to.deep.equal({models: {}});
    });

    it('should construct instance type select configuration', () => {
      instanceCreateConfiguration.assignType = sinon.spy();
      instanceCreateConfiguration.config.formConfig = {
        models: {}
      };
      instanceCreateConfiguration.modelsService = {
        getModels: sinon.spy(() => {
          return PromiseStub.resolve({
            "errorMessage": "error"
          });
        })
      };
      instanceCreateConfiguration.convertModelsToOptions = sinon.spy(() => {
        return [{id: 'emf:Document', text: 'Document'}];
      });
      instanceCreateConfiguration.ngOnInit();
      expect(instanceCreateConfiguration.instanceTypeConfig).to.exist;
      expect(instanceCreateConfiguration.instanceTypeConfig.disabled).to.be.false;
      expect(instanceCreateConfiguration.instanceTypeConfig.data).to.deep.equal([{
        id: '', text: ''
      }, {
        id: 'emf:Document', text: 'Document'
      }]);
      expect(instanceCreateConfiguration.assignType.calledOnce).to.be.true;
      expect(instanceCreateConfiguration.config.formConfig.models.onContextSelected).to.exist;
      expect(instanceCreateConfiguration.config.formConfig.models.errorMessage).to.be.equal("error");
    });

    it('should disable instance type select if there is a predefined instance type', () => {
      instanceCreateConfiguration.assignType = sinon.spy();
      instanceCreateConfiguration.modelsService = {
        getModels: sinon.spy(() => {
          return PromiseStub.resolve({});
        })
      };
      instanceCreateConfiguration.config.formConfig = {
        models: {}
      };
      instanceCreateConfiguration.convertModelsToOptions = sinon.spy(() => {
        return [{id: 'emf:Document', text: 'Document'}];
      });
      instanceCreateConfiguration.config.instanceType = 'instanceType';
      instanceCreateConfiguration.ngOnInit();
      expect(instanceCreateConfiguration.instanceTypeConfig).to.exist;
      expect(instanceCreateConfiguration.instanceTypeConfig.disabled).to.be.true;
    });
  });

  describe('convertDefinitionsToOptions', () => {
    var options;
    beforeEach(function () {
      options = instanceCreateConfiguration.convertModelsToOptions(createModel());
    });

    it('should show top level classes', function () {
      expect(options.length).to.equal(4);
    });

    it('should set subdefinitions as children', function () {
      var option = options[2];

      expect(option.id).to.equal('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document');
      expect(option.subtypes.length).to.be.equal(2);

      expect(option.subtypes[0].id).to.equal('MS210001');
      expect(option.subtypes[1].id).to.equal('OT210027');
    });

    it('should set definitionId on top level class that have only one definition as a child', function () {
      expect(options[3].id).to.equal('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Tag');
      expect(options[3].definitionId).to.be.equal('tag');
    });

    function createModel() {
      return [
        {
          "id": "http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain#Book",
          "label": "Book",
          "type": "class",
          "parent": "http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain#CulturalObject"
        },
        {
          "id": "EO007005",
          "label": "Book",
          "type": "definition",
          "parent": "http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain#Book"
        },
        {
          "id": "EO007009",
          "label": "Bound manuscript",
          "type": "definition",
          "parent": "http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain#BoundManuscript"
        },
        {
          "id": "http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain#BoundManuscript",
          "label": "Bound Manuscript",
          "type": "class",
          "parent": "http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain#CulturalObject"
        },
        {
          "id": "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document",
          "label": "Document",
          "type": "class"
        },
        {
          "id": "MS210001",
          "label": "Approval document",
          "type": "definition",
          "parent": "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document"
        },
        {
          "id": "OT210027",
          "label": "Common document",
          "type": "definition",
          "parent": "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document"
        },
        {
          "id": "http://www.sirma.com/ontologies/2013/10/definitionLess",
          "label": "Definitionless Type",
          "type": "class"
        },
        {
          "id": "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Tag",
          "label": "Таг",
          "type": "class"
        },
        {
          "id": "tag",
          "label": "Таг",
          "type": "definition",
          "parent": "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Tag"
        }];
    };
  });

  describe('isSubTypeDropdownVisible()', () => {
    it('should return true if there are subtypes to display', () => {
      expect(instanceCreateConfiguration.isSubTypeDropdownVisible()).to.be.false;
      instanceCreateConfiguration.type = {
        subtypes: []
      };
      expect(instanceCreateConfiguration.isSubTypeDropdownVisible()).to.be.true;
    });
  });

  describe('assignType()', () => {
    it('should assign default type', () => {
      instanceCreateConfiguration.onTypeSelected = sinon.spy();
      instanceCreateConfiguration.types = [{
        id: 'type1',
        default: false
      }, {
        id: 'type2',
        default: true
      }, {
        id: 'type3'
      }, {
        id: 'type4',
        default: true
      }];
      instanceCreateConfiguration.assignType();
      expect(instanceCreateConfiguration.instanceType).to.equal('type2');
      expect(instanceCreateConfiguration.onTypeSelected.calledOnce).to.be.true;
      expect(instanceCreateConfiguration.onTypeSelected.getCall(0).args[0]).to.equals('type2');
    });

    it('should assign configured predefined type', () => {
      instanceCreateConfiguration.onTypeSelected = sinon.spy();
      instanceCreateConfiguration.config.instanceType = 'predefinedType';
      instanceCreateConfiguration.types = [{
        id: 'type1',
        default: true
      }];
      instanceCreateConfiguration.assignType();
      expect(instanceCreateConfiguration.instanceType).to.equal('predefinedType');
      expect(instanceCreateConfiguration.onTypeSelected.calledOnce).to.be.true;
      expect(instanceCreateConfiguration.onTypeSelected.getCall(0).args[0]).to.equals('predefinedType');
    });
  });

  describe('onTypeSelected()', () => {
    it('should clear data for loading properties', () => {
      var unsubscribeMock = {
        unsubscribe: () => {
        }
      };
      instanceCreateConfiguration.validationEvents = [unsubscribeMock];
      instanceCreateConfiguration.processedProperties = ['property1'];
      instanceCreateConfiguration.onTypeSelected();
      expect(instanceCreateConfiguration.validationEvents.length).to.equal(0);
      expect(instanceCreateConfiguration.processedProperties.length).to.equal(0);
    });
  });

  describe('loadSubTypes()', () => {
    it('should construct instance sub type select configuration', () => {
      instanceCreateConfiguration.loadSubTypes([{id: '1', text: 'One'}]);
      expect(instanceCreateConfiguration.instanceSubTypeConfig).to.exist;
      expect(instanceCreateConfiguration.instanceSubTypeConfig.data).to.exist;
      expect(instanceCreateConfiguration.instanceSubTypeConfig.data).to.deep.equal([{
        id: '', text: ''
      }, {
        id: '1', text: 'One'
      }]);
      expect(instanceCreateConfiguration.instanceSubTypeConfig.disabled).to.be.false;
    });

    it('should disable instance sub type select if there is predefined sub type', () => {
      instanceCreateConfiguration.loadDefinitionModel = sinon.spy();
      instanceCreateConfiguration.config.instanceSubType = 'subType';
      instanceCreateConfiguration.loadSubTypes([]);
      expect(instanceCreateConfiguration.instanceSubTypeConfig).to.exist;
      expect(instanceCreateConfiguration.instanceSubTypeConfig.disabled).to.be.true;
    });

    it('should assign predefined instance sub type if configured', () => {
      instanceCreateConfiguration.loadDefinitionModel = sinon.spy();
      instanceCreateConfiguration.config.instanceSubType = 'subType';
      instanceCreateConfiguration.loadSubTypes([]);
      expect(instanceCreateConfiguration.instanceSubType).to.equal('subType');
      expect(instanceCreateConfiguration.loadDefinitionModel.calledOnce).to.be.true;
      expect(instanceCreateConfiguration.loadDefinitionModel.getCall(0).args[0]).to.equal('subType');
    });
  });

  describe('loadDefinitionModel()', () => {
    it('should clear suggested properties', () => {
      let suggestedProperties = new Map();
      suggestedProperties.set('references', []);
      instanceCreateConfiguration.config = {
        suggestedProperties: suggestedProperties,
        formConfig: {
          models: {}
        }
      };
      instanceCreateConfiguration.onFormLoaded = () => {
      };

      instanceCreateConfiguration.loadDefinitionModel('definitionId');
      expect(suggestedProperties.size).to.equal(0);
    });

    it('should load default properties', () => {
      instanceCreateConfiguration.config.formConfig = {
        models: {
          parentId: 'emf:123'
        }
      };
      instanceCreateConfiguration.onDefinitionModelLoaded = sinon.spy();

      instanceCreateConfiguration.loadDefinitionModel('definition-id');
      expect(instanceCreateConfiguration.instanceRestService.loadDefaults.calledOnce).to.be.true;
      expect(instanceCreateConfiguration.instanceRestService.loadDefaults.getCall(0).args[0]).to.equal('definition-id');
      expect(instanceCreateConfiguration.instanceRestService.loadDefaults.getCall(0).args[1]).to.equal('emf:123');
      expect(instanceCreateConfiguration.onDefinitionModelLoaded.calledOnce).to.be.true;
      expect(instanceCreateConfiguration.onDefinitionModelLoaded.getCall(0).args[1]).to.equal('definition-id');
    });
  });

  describe('onDefinitionModelLoaded()', () => {
    it('should subscribe to AfterFormValidationEvent', () => {
      var definitionId = 'ET10001';
      var responses = [{
        data: {
          headers: {},
          properties: {},
          instanceType: 'documentinstance',
          parentId: 'emf:parentId'
        }
      }, {
        data: {
          'ET10001': {
            instanceType: 'objectinstance'
          }
        }
      }];
      instanceCreateConfiguration.config = {
        formConfig: {
          models: {}
        }
      };
      instanceCreateConfiguration.onFormLoaded = () => {
      };

      instanceCreateConfiguration.onDefinitionModelLoaded(responses, definitionId);

      expect(eventbus.subscribe.called).to.be.true;
      expect(instanceCreateConfiguration.validationEvents.length).to.equal(1);
    });
  });

  describe('populateMandatoryObjectsValues()', () => {
    it('should populate model with suggested values if its array', () => {
      var parentId = 'emf:parentId';
      var data = [{
        viewModel: {
          'emf:references': {
            identifier: 'emf:references',
            isMandatory: true,
            multivalue: false,
            control: {
              controlParams: {
                range: 'chd:CulturalObject'
              }
            }
          }
        },
        validationModel: {
          'emf:references': {
            value: [{id: 'emf:321'}]
          }
        }
      }];
      instanceCreateConfiguration.config = {};

      instanceCreateConfiguration.populateMandatoryObjectsValues(data, parentId);

      expect(data[0].validationModel['emf:references'].value).to.deep.equal([{id: 'emf:321'}, {id: 'emf:123'}]);
    });

    it('should assign property value with suggested values if its undefined', () => {
      var parentId = 'emf:parentId';
      var data = [{
        viewModel: {
          'emf:references': {
            identifier: 'emf:references',
            isMandatory: true,
            multivalue: false,
            control: {
              controlParams: {
                range: 'chd:CulturalObject'
              }
            }
          }
        },
        validationModel: {
          'emf:references': {
            value: undefined
          }
        }
      }];
      instanceCreateConfiguration.config = {};

      instanceCreateConfiguration.populateMandatoryObjectsValues(data, parentId);

      expect(data[0].validationModel['emf:references'].value).to.deep.equal([{id: 'emf:123'}]);
    });

    it('should not populate model with suggested values if parentId is undefined', () => {
      var data = [{
        viewModel: {
          'emf:references': {
            identifier: 'emf:references',
            isMandatory: true,
            multivalue: false,
            control: {
              controlParams: {
                range: 'chd:CulturalObject'
              }
            }
          }
        },
        validationModel: {
          'emf:references': {
            value: []
          }
        }
      }];

      instanceCreateConfiguration.populateMandatoryObjectsValues(data);

      expect(data[0].validationModel['emf:references'].value.length).to.equal(0);
    });

    it('should not populate model with suggested values on missing viewModel', () => {
      var data = [{
        validationModel: {
          'emf:references': {
            value: []
          }
        }
      }];

      instanceCreateConfiguration.populateMandatoryObjectsValues(data);

      expect(data[0].validationModel['emf:references'].value.length).to.equal(0);
    });

    it('should populate suggested properties config if passed', () => {
      var parentId = 'emf:parentId';
      var data = [{
        viewModel: {
          'emf:references': {
            identifier: 'emf:references',
            isMandatory: true,
            multivalue: false,
            control: {
              controlParams: {
                range: 'chd:CulturalObject'
              }
            }
          }
        },
        validationModel: {
          'emf:references': {
            value: undefined
          }
        }
      }];
      var suggestedProperties = new Map();
      instanceCreateConfiguration.config = {
        suggestedProperties: suggestedProperties
      };

      instanceCreateConfiguration.populateMandatoryObjectsValues(data, parentId);

      expect(suggestedProperties.size).to.equal(1);
    });
  });

  describe('toggleShowAllProperties()', () => {
    it('should toggle renderMandatory and showAllProperties', () => {
      instanceCreateConfiguration.config.renderMandatory = true;
      instanceCreateConfiguration.config.showAllProperties = false;
      instanceCreateConfiguration.toggleShowAllProperties();
      expect(instanceCreateConfiguration.config.renderMandatory).to.be.false;
      expect(instanceCreateConfiguration.config.showAllProperties).to.be.true;
    });
  });

  function mockTranslateService() {
    return {
      translateInstant: () => {
        return ""
      }
    }
  }

  function mockModelsService() {
    return {
      getModels: sinon.spy(() => {
        return PromiseStub.resolve({});
      })
    };
  }

  function mockEventbus() {
    return {
      subscribe: sinon.spy(),
      publish: sinon.spy()
    };
  }

  function mockPropertiesService() {
    return {
      loadObjectPropertiesSuggest: sinon.spy(() => {
        return PromiseStub.resolve({
          data: [
            {id: 'emf:123'}
          ]
        });
      })
    };
  }

  function mockInstanceService() {
    return {
      loadDefaults: sinon.spy(() => {
        return PromiseStub.resolve({
          data: {
            headers: {}
          }
        });
      })
    };
  }

  function mockDefinitionService() {
    return {
      getDefinitions: sinon.spy(() => {
        return PromiseStub.resolve({
          data: {
            'definitionId': {}
          }
        });
      })
    };
  }

  function mockTimeout() {
    return (callbackFunction) => {
      callbackFunction();
    };
  }
});
