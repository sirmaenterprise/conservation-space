import {InstanceCreateConfiguration} from 'create/instance-create-configuration';
import {PromiseStub} from 'test/promise-stub';
import {EventEmitter} from 'common/event-emitter';
import {stub} from 'test/test-utils';
import {ADD_CONTEXT_ERROR_MESSAGE_COMMAND} from 'components/contextselector/context-selector';
import {Eventbus} from 'services/eventbus/eventbus';
import {PropertiesRestService} from 'services/rest/properties-service';
import {DefinitionService} from 'services/rest/definition-service';
import {InstanceRestService} from 'services/rest/instance-service';
import {TranslateService} from 'services/i18n/translate-service';
import {ModelsService} from 'services/rest/models-service';

describe('InstanceCreateConfiguration', function () {

  let instanceCreateConfiguration;
  let eventbus;
  let propertiesService;
  let definitionService;
  let instanceService;
  let modelService;

  beforeEach(() => {
    eventbus = stub(Eventbus);
    propertiesService = mockPropertiesService();
    definitionService = mockDefinitionService();
    instanceService = mockInstanceService();
    modelService = mockModelsService();
    instanceCreateConfiguration = new InstanceCreateConfiguration(definitionService, instanceService, PromiseStub,
      mockTranslateService(), modelService, mockTimeout(), eventbus, propertiesService);
    instanceCreateConfiguration.config = {
      eventEmitter: stub(EventEmitter)
    };
  });

  describe('ngOnInit()', () => {

    it('should emit onConfigurationCompleted event when onContextSelected() ', () => {
      instanceCreateConfiguration.onConfigurationCompleted = sinon.spy();
      instanceCreateConfiguration.assignType = sinon.spy();

      instanceCreateConfiguration.convertModelsToOptions = sinon.spy(() => {
        return [{id: 'emf:Document', text: 'Document'}];
      });

      instanceCreateConfiguration.onContextSelected();
      expect(instanceCreateConfiguration.onConfigurationCompleted.calledOnce).to.be.true;

      let args = instanceCreateConfiguration.onConfigurationCompleted.getCall(0).args;
      expect(args[0].event).to.deep.equal({models: []});
    });

    describe('isPresentType', () => {
      let types = [
        {
          default: undefined,
          id: 'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Case',
          text: 'Case',
          subtypes: [
            {
              id: 'CS0002',
              parent: 'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Case',
              text: 'Case with autocreated WF'
            },
            {
              id: 'CS0003',
              parent: 'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Case',
              text: 'Case with create Painting action'
            }
          ]
        },
        {
          default: undefined,
          id: 'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document',
          text: 'Document',
          subtypes: [
            {
              id: 'DT0003',
              parent: 'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document',
              text: 'Analysis report (for WF)'
            },
            {
              id: 'DT0004',
              parent: 'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document',
              text: 'Condition Report (for WF)'
            }
          ]
        },
        {
          default: undefined,
          definitionId: 'TASKST01',
          id: 'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Task',
          text: 'Task',
          subtypes: []
        }
      ];

      it('should return true if selected definitionId is found in the types list', () => {
        expect(InstanceCreateConfiguration.isPresentType(types, 'TASKST01')).to.be.true;
      });

      it('should return false if selected definitionId is not found in the types list', () => {
        expect(InstanceCreateConfiguration.isPresentType(types, 'TASKST02')).to.be.false;
      });

      it('should return true if selected definitionId is found as subtype in the types list', () => {
        expect(InstanceCreateConfiguration.isPresentType(types, 'CS0003')).to.be.true;
      });

      it('should return false if selected definitionId is not found as subtype in the types list', () => {
        expect(InstanceCreateConfiguration.isPresentType(types, 'CS0004')).to.be.false;
      });
    });

    it('should construct instance type select configuration', () => {
      instanceCreateConfiguration.onConfigurationCompleted = sinon.spy();
      instanceCreateConfiguration.assignType = sinon.spy();
      instanceCreateConfiguration.config.formConfig = {
        models: {}
      };
      modelService.getModels.returns(PromiseStub.resolve({'errorMessage': 'error'}));
      instanceCreateConfiguration.convertModelsToOptions = sinon.spy(() => {
        return [{id: 'emf:Document', text: 'Document'}];
      });
      instanceCreateConfiguration.ngOnInit();
      expect(instanceCreateConfiguration.instanceTypeConfig).to.exist;
      expect(instanceCreateConfiguration.instanceTypeConfig.disabled).to.be.false;
      // if there is only one type present, default tag should be set,
      expect(instanceCreateConfiguration.instanceTypeConfig.data).to.deep.equal([{
        id: '', text: ''
      }, {
        id: 'emf:Document', text: 'Document', default: true
      }]);
      expect(instanceCreateConfiguration.assignType.calledOnce).to.be.true;
    });

    it('should disable instance type select if there is a predefined instance type', () => {
      instanceCreateConfiguration.onConfigurationCompleted = sinon.spy();
      instanceCreateConfiguration.assignType = sinon.spy();
      modelService.getModels.returns(PromiseStub.resolve({}));
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

    it('should set error message when there is no model', () => {
      instanceCreateConfiguration.types = [];
      instanceCreateConfiguration.onConfigurationCompleted = function onConfigurationCompleted() {
      };
      instanceCreateConfiguration.config.formConfig = {
        models: {}
      };

      instanceCreateConfiguration.onContextSelected('contextId');

      expect(instanceCreateConfiguration.config.eventEmitter.publish.withArgs(ADD_CONTEXT_ERROR_MESSAGE_COMMAND, 'idoc.dialog.no.permissions').calledOnce).to.be.true;
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
          'id': 'http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain#Book',
          'label': 'Book',
          'type': 'class',
          'parent': 'http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain#CulturalObject'
        },
        {
          'id': 'EO007005',
          'label': 'Book',
          'type': 'definition',
          'parent': 'http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain#Book'
        },
        {
          'id': 'EO007009',
          'label': 'Bound manuscript',
          'type': 'definition',
          'parent': 'http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain#BoundManuscript'
        },
        {
          'id': 'http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain#BoundManuscript',
          'label': 'Bound Manuscript',
          'type': 'class',
          'parent': 'http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain#CulturalObject'
        },
        {
          'id': 'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document',
          'label': 'Document',
          'type': 'class'
        },
        {
          'id': 'MS210001',
          'label': 'Approval document',
          'type': 'definition',
          'parent': 'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document'
        },
        {
          'id': 'OT210027',
          'label': 'Common document',
          'type': 'definition',
          'parent': 'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document'
        },
        {
          'id': 'http://www.sirma.com/ontologies/2013/10/definitionLess',
          'label': 'Definitionless Type',
          'type': 'class'
        },
        {
          'id': 'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Tag',
          'label': 'Таг',
          'type': 'class'
        },
        {
          'id': 'tag',
          'label': 'Таг',
          'type': 'definition',
          'parent': 'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Tag'
        }];
    }
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
      instanceCreateConfiguration.config = {
        formConfig: {
          models: {}
        }
      };
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
      instanceCreateConfiguration.config.formConfig = {
        models: {}
      };
      instanceCreateConfiguration.types = [{
        id: 'type1',
        default: true
      }];
      instanceCreateConfiguration.assignType();
      expect(instanceCreateConfiguration.instanceType).to.equal('predefinedType');
      expect(instanceCreateConfiguration.onTypeSelected.calledOnce).to.be.true;
      expect(instanceCreateConfiguration.onTypeSelected.getCall(0).args[0]).to.equals('predefinedType');
    });

    it('should not change selected type', () => {
      instanceCreateConfiguration.onTypeSelected = sinon.spy();
      instanceCreateConfiguration.config = {
        formConfig: {
          models: {
            instanceType: 'predefinedType'
          }
        }
      };
      instanceCreateConfiguration.types = [{
        id: 'type1',
        default: true
      }];
      instanceCreateConfiguration.assignType();
      expect(instanceCreateConfiguration.onTypeSelected.called).to.be.false;
    });
  });

  describe('onTypeSelected()', () => {
    it('should clear data for loading properties', () => {
      var unsubscribeMock = {
        unsubscribe: () => {
        }
      };
      instanceCreateConfiguration.config = {
        formConfig: {
          models: {}
        }
      };
      instanceCreateConfiguration.validationEventsSubscriptions = [unsubscribeMock];
      instanceCreateConfiguration.onTypeSelected();
      expect(instanceCreateConfiguration.validationEventsSubscriptions.length).to.equal(0);
    });

    it('should reset subtype and models if no type is selected', () => {
      instanceCreateConfiguration.config = {
        formConfig: {
          models: {
            definitionId: 'definitionId',
            viewModel: {}
          }
        }
      };
      instanceCreateConfiguration.type = {};
      instanceCreateConfiguration.instanceSubType = 'subType';
      instanceCreateConfiguration.onTypeSelected();
      expect(instanceCreateConfiguration.type).to.be.null;
      expect(instanceCreateConfiguration.instanceSubType).to.be.null;
      expect(instanceCreateConfiguration.config.formConfig.models.definitionId).to.be.null;
      expect(instanceCreateConfiguration.config.formConfig.models.viewModel).to.be.null;
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
      instanceCreateConfiguration.getEligibleFields = sinon.spy(() => {
        return [];
      });
      let suggestedProperties = new Map();
      suggestedProperties.set('references', []);
      instanceCreateConfiguration.config = {
        suggestedProperties,
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
      instanceCreateConfiguration.getEligibleFields = sinon.spy(() => {
        return [];
      });
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
      expect(instanceCreateConfiguration.validationEventsSubscriptions.length).to.equal(1);
    });
  });

  describe('populateMandatoryObjectsValues()', () => {

    it('should not populate model with suggested values if parentId is undefined', () => {
      var data = [{
        viewModel: {
          'emf:references': {
            identifier: 'emf:references',
            isMandatory: true,
            multivalue: false,
            controlId: 'control-identifier',
            control: [{
              identifier: 'control-identifier',
              controlParams: {
                range: 'chd:CulturalObject'
              }
            }]
          }
        },
        validationModel: {
          'emf:references': {
            value: {}
          }
        }
      }];

      instanceCreateConfiguration.populateMandatoryObjectsValues(data);

      expect(data[0].validationModel['emf:references'].value).to.eql({});
      expect(data[0].validationModel['emf:references'].isSuggested).to.equal(undefined);
    });

    it('should not populate model with suggested values on missing viewModel', () => {
      var data = [{
        validationModel: {
          'emf:references': {
            value: {}
          }
        }
      }];

      instanceCreateConfiguration.populateMandatoryObjectsValues(data);

      let model = data[0].validationModel['emf:references'];
      expect(model.value.results).to.be.undefined;
      expect(model.isSuggested).to.equal(undefined);
    });

    it('should populate suggested properties config if passed', () => {
      var parentId = 'emf:parentId';
      var data = [{
        viewModel: {
          'emf:references': {
            identifier: 'emf:references',
            isMandatory: true,
            multivalue: false,
            controlId: 'control-identifier',
            control: [{
              identifier: 'control-identifier',
              controlParams: {
                range: 'chd:CulturalObject'
              }
            }]
          }
        },
        validationModel: {
          'emf:references': {
            value: {}
          }
        }
      }];
      var suggestedProperties = new Map();
      instanceCreateConfiguration.config = {
        suggestedProperties
      };

      instanceCreateConfiguration.populateMandatoryObjectsValues(data, parentId);

      expect(suggestedProperties.size).to.equal(1);
      expect(data[0].validationModel['emf:references'].isSuggested).to.be.true;
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
    let translateService = stub(TranslateService);
    translateService.translateInstant.returns('');
    return translateService;
  }

  function mockModelsService() {
    let modelsService = stub(ModelsService);
    modelsService.getModels.returns(PromiseStub.resolve({
      models: []
    }));
    return modelsService;
  }

  function mockPropertiesService() {
    let propertiesService = stub(PropertiesRestService);
    propertiesService.loadObjectPropertiesSuggest.returns(PromiseStub.resolve({
      data: [
        {
          id: 'emf:123',
          headers: {
            compact_header: 'Compact header'
          }
        }
      ]
    }));

    return propertiesService;
  }

  function mockInstanceService() {
    let instanceService = stub(InstanceRestService);
    instanceService.loadDefaults.returns(PromiseStub.resolve({
      data: {
        headers: {}
      }
    }));
    return instanceService;
  }

  function mockDefinitionService() {
    let definitionService = stub(DefinitionService);
    definitionService.getDefinitions.returns(PromiseStub.resolve({
      data: {
        'definitionId': {}
      }
    }));
    return definitionService;
  }

  function mockTimeout() {
    return (callbackFunction) => {
      callbackFunction();
    };
  }
});
