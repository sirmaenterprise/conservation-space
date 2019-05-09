import {
  ModelManagement,
  MODEL_FIELDS_TAB,
  MODEL_GENERAL_TAB,
  MODEL_HEADERS_TAB,
  MODEL_ACTIONS_TAB,
  MODIFIED_SECTION_CLASS,
  MODEL_MANAGEMENT_QUERY_PARAMETER,
  MODEL_MANAGEMENT_SECTION_QUERY_PARAMETER,
  MODEL_MANAGEMENT_EXTENSION_POINT
} from 'administration/model-management/model-management';

import {EventEmitter} from 'common/event-emitter';
import {ModelEvents} from 'administration/model-management/model/model-events';
import {ModelClass} from 'administration/model-management/model/model-class';
import {ModelList} from 'administration/model-management/model/model-list';
import {ModelResponse} from 'administration/model-management/model/model-response';
import {ModelClassHierarchy} from 'administration/model-management/model/model-hierarchy';
import {ModelDeployRequest} from 'administration/model-management/model/request/model-deploy-request';
import {ModelDefinition} from 'administration/model-management/model/model-definition';
import {ModelSingleAttribute} from 'administration/model-management/model/attributes/model-single-attribute';
import {ModelValidationReport} from 'administration/model-management/model/validation/model-validation-report';

import {ModelManagementModelsService} from 'administration/model-management/services/utility/model-management-models-service';
import {ModelManagementStateRegistry} from 'administration/model-management/services/model-management-state-registry';

import {ModelActionFactory} from 'administration/model-management/actions/model-action-factory';
import {ModelActionProcessor} from 'administration/model-management/actions/model-action-processor';

import {StateParamsAdapter} from 'adapters/router/state-params-adapter';
import {TranslateService} from 'services/i18n/translate-service';
import {Router} from 'adapters/router/router';

import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';

describe('ModelManagement', () => {

  let routerStub;
  let translateServiceStub;
  let stateParamsAdapterStub;

  let modelActionFactoryStub;
  let modelActionProcessorStub;

  let modelManagementModelsServiceStub;
  let modelManagementStateRegistryStub;

  let model;
  let hierarchy;
  let response;
  let modelManagement;

  beforeEach(() => {
    routerStub = stub(Router);
    translateServiceStub = stub(TranslateService);
    stateParamsAdapterStub = stub(StateParamsAdapter);

    modelActionFactoryStub = stub(ModelActionFactory);
    modelActionProcessorStub = stub(ModelActionProcessor);

    modelManagementModelsServiceStub = stub(ModelManagementModelsService);
    modelManagementStateRegistryStub = stub(ModelManagementStateRegistry);

    model = new ModelClass('object', 'entity');
    hierarchy = new ModelClassHierarchy(model);
    response = new ModelResponse(model, 0);

    modelActionFactoryStub.create.returns([]);
    modelActionProcessorStub.changeset.returns([]);
    modelActionProcessorStub.execute.returns([]);
    modelActionProcessorStub.restore.returns([]);
    modelActionProcessorStub.on.returns([]);

    modelManagementModelsServiceStub.getModels.returns(PromiseStub.resolve({}));
    modelManagementModelsServiceStub.getMetaData.returns(PromiseStub.resolve({}));
    modelManagementModelsServiceStub.getModel.returns(PromiseStub.resolve(response));
    modelManagementModelsServiceStub.getHierarchy.returns(PromiseStub.resolve(hierarchy));
    modelManagementModelsServiceStub.getDeploymentModels.returns(PromiseStub.resolve(new ModelDeployRequest()));

    modelManagementModelsServiceStub.deploy.returns(PromiseStub.resolve());
    modelManagementModelsServiceStub.save.returns(PromiseStub.resolve({modelVersion: 1}));

    modelManagement = new ModelManagement(modelManagementModelsServiceStub, modelActionFactoryStub, modelActionProcessorStub,
      modelManagementStateRegistryStub, stateParamsAdapterStub, translateServiceStub, PromiseStub, routerStub);
    modelManagement.emitter = stub(EventEmitter);
  });

  it('should initialize component related action handlers', () => {
    modelManagement.ngOnInit();
    expect(modelManagement.actionHandlers).to.exist;
  });

  it('should initialize the model and configuration for the tree', () => {
    stateParamsAdapterStub.getStateParam.returns('collection');
    modelManagement.ngOnInit();
    expect(modelManagement.treeModel).to.deep.eq(hierarchy);
    expect(modelManagement.treeConfig).to.deep.eq({id: 'collection'});
  });

  it('should initialize the model and configuration for the tabs', () => {
    modelManagement.ngOnInit();

    expect(modelManagement.modelSectionsConfig.tabs.length).to.eq(4);
    expect(modelManagement.modelSectionsConfig.tabs[0].id).to.eq(MODEL_GENERAL_TAB);
    expect(modelManagement.modelSectionsConfig.tabs[1].id).to.eq(MODEL_FIELDS_TAB);
    expect(modelManagement.modelSectionsConfig.tabs[2].id).to.eq(MODEL_ACTIONS_TAB);
    expect(modelManagement.modelSectionsConfig.tabs[3].id).to.eq(MODEL_HEADERS_TAB);

    // should create a default mapping for each section where direct references are used, for fast access
    expect(modelManagement.modelSectionsConfig.mapping[MODEL_GENERAL_TAB]).to.eq(modelManagement.modelSectionsConfig.tabs[0]);
    expect(modelManagement.modelSectionsConfig.mapping[MODEL_FIELDS_TAB]).to.eq(modelManagement.modelSectionsConfig.tabs[1]);
    expect(modelManagement.modelSectionsConfig.mapping[MODEL_ACTIONS_TAB]).to.eq(modelManagement.modelSectionsConfig.tabs[2]);
    expect(modelManagement.modelSectionsConfig.mapping[MODEL_HEADERS_TAB]).to.eq(modelManagement.modelSectionsConfig.tabs[3]);

    // Should have cleared any section states in the registry
    expect(modelManagementStateRegistryStub.clearSectionStates.calledOnce).to.be.true;
  });

  it('should provide proper deployment response along with the currently selected model', () => {
    let type = new ModelClass('class');
    let model = new ModelDefinition('definition').setType(type);

    // stub currently selected model
    modelManagement.model = model;

    let models = new ModelList().insert(model).insert(type).insert(new ModelClass('class2'));
    let deploymentRequest = new ModelDeployRequest().setModels(models).setValidationReport(new ModelValidationReport());
    modelManagementModelsServiceStub.getDeploymentModels.returns(PromiseStub.resolve(deploymentRequest));

    // trigger deployment request & check for the proper models
    modelManagement.onModelDeployRequested().then(request => {
      expect(request instanceof ModelDeployRequest).to.be.true;
      let selected = new ModelList().insert(model).insert(type);
      expect(request.getSelectedModels()).to.deep.eq(selected);
      expect(request.getModels()).to.deep.eq(models);
      expect(modelManagementModelsServiceStub.getDeploymentModels.calledOnce).to.be.true;
    });
  });

  it('should provide empty list of selected models when invalid model is requested for deploy', () => {
    let type = new ModelClass('class');
    let model = new ModelDefinition('definition').setType(type);

    // stub currently selected model
    modelManagement.model = model;

    let models = new ModelList().insert(model);

    // stub the validation response
    let validationResponse = getValidationReport();
    let deploymentRequest = new ModelDeployRequest().setModels(models)
      .setValidationReport(new ModelValidationReport(validationResponse));
    modelManagementModelsServiceStub.getDeploymentModels.returns(PromiseStub.resolve(deploymentRequest));

    // trigger deployment request
    modelManagement.onModelDeployRequested().then(request => {
      // the selected models should be an empty model list
      let selected = new ModelList();
      expect(request.getSelectedModels()).to.deep.eq(selected);
      expect(request.getModels()).to.deep.eq(models);
      expect(modelManagementModelsServiceStub.getDeploymentModels.calledOnce).to.be.true;
    });
  });

  it('should directly call deployment from the management service and reload entire component', () => {
    let deploymentRequest = new ModelDeployRequest();
    modelManagement.onModelDeployConfirmed(deploymentRequest);
    expect(modelManagementModelsServiceStub.deploy.calledOnce).to.be.true;
    expect(modelManagementModelsServiceStub.deploy.calledWith(deploymentRequest)).to.be.true;

    expect(routerStub.navigate.calledOnce).to.be.true;
    expect(routerStub.navigate.calledWith(MODEL_MANAGEMENT_EXTENSION_POINT, stateParamsAdapterStub.getStateParams(), {reload: true})).to.be.true;
  });

  it('should set the active section if present as router state', () => {
    stateParamsAdapterStub.getStateParam.returns('fields');
    modelManagement.ngOnInit();
    expect(modelManagement.modelSectionsConfig.activeTab).to.equal('fields');
  });

  it('should prepare model when the model is changed or reloaded', () => {
    modelManagement.notifyForModelChange = sinon.spy();
    modelManagement.onSelectedNode({id: 'model'});

    expect(modelManagement.isModelMissing()).to.be.false;
    expect(modelManagement.notifyForModelChange.calledOnce).to.be.true;
  });

  it('should set the current model on selected node', () => {
    modelManagement.onSelectedNode({id: 'model'});
    expect(routerStub.navigate.calledWith(MODEL_MANAGEMENT_EXTENSION_POINT)).to.be.true;
    expect(stateParamsAdapterStub.setStateParam.calledWith(MODEL_MANAGEMENT_QUERY_PARAMETER, 'model')).to.be.true;
  });

  it('should set the current model as a state parameter', () => {
    modelManagement.setCurrentModel('model');
    expect(stateParamsAdapterStub.setStateParam.calledWith(MODEL_MANAGEMENT_QUERY_PARAMETER, 'model')).to.be.true;
  });

  it('should get the current model as a state parameter', () => {
    stateParamsAdapterStub.getStateParam.returns('model');
    expect(modelManagement.getCurrentModel()).to.eq('model');
    expect(stateParamsAdapterStub.getStateParam.calledWith(MODEL_MANAGEMENT_QUERY_PARAMETER)).to.be.true;
  });

  it('should load & store the current model', () => {
    expect(modelManagement.model).to.not.exist;
    modelManagement.onSelectedNode('object');

    expect(modelManagement.model).to.equal(model);
    expect(modelManagement.isModelMissing()).to.be.false;
  });

  it('should flag if the requested model is missing', () => {
    modelManagementModelsServiceStub.getModel.returns(PromiseStub.reject());
    modelManagement.onSelectedNode('object');
    expect(modelManagement.isModelMissing()).to.be.true;
  });

  it('should properly resolve if a model is still loading', () => {
    modelManagement.model = null;
    stateParamsAdapterStub.getStateParam.returns('model');
    expect(modelManagement.isModelLoading()).to.be.true;
  });

  it('should properly resolve if a model is selected', () => {
    stateParamsAdapterStub.getStateParam.returns('model');
    expect(modelManagement.isModelSelected()).to.be.true;
  });

  it('should resolve the current tab visibility', () => {
    modelManagement.ngOnInit();

    modelManagement.modelSectionsConfig.activeTab = 'some-active-tab';
    expect(modelManagement.isSectionVisible(MODEL_FIELDS_TAB)).to.be.false;

    modelManagement.modelSectionsConfig.activeTab = MODEL_GENERAL_TAB;
    expect(modelManagement.isSectionVisible(MODEL_GENERAL_TAB)).to.be.true;
  });

  it('should navigate to model when requested to do so', () => {
    modelManagement.onSelectedNode = sinon.spy();

    modelManagement.onModelLoad(new ModelDefinition('id'), true);
    expect(modelManagement.onSelectedNode.calledOnce).to.be.true;
    expect(modelManagement.onSelectedNode.calledWith({id: 'id'})).to.be.true;
  });

  it('should only load a model without navigating to it', () => {
    modelManagement.loadModel = sinon.spy();

    modelManagement.onModelLoad(new ModelDefinition('id'), false);
    expect(modelManagement.loadModel.calledOnce).to.be.true;
    expect(modelManagement.loadModel.calledWith('id')).to.be.true;
  });

  it('should un-subscribe to model change or reload', () => {
    modelManagement.emitter = stub(EventEmitter);

    modelManagement.ngOnDestroy();
    expect(modelManagement.emitter.unsubscribeAll.calledOnce).to.be.true;
  });

  it('should properly handle state change for a given tab and reflect that', () => {
    modelManagement.ngOnInit();
    modelManagement.onSectionStateChange(true, MODEL_GENERAL_TAB);

    let general = modelManagement.getSectionTabById(MODEL_GENERAL_TAB);
    expect(general.postfix).to.exist;
    expect(general.classes).to.eq(MODIFIED_SECTION_CLASS);

    // Should register the section state in the registry
    expect(modelManagementStateRegistryStub.setSectionState.calledOnce).to.be.true;
    expect(modelManagementStateRegistryStub.setSectionState.calledWith(general.id, true)).to.be.true;
  });

  it('should get section tab by identifier', () => {
    modelManagement.ngOnInit();
    let general = modelManagement.getSectionTabById(MODEL_GENERAL_TAB);
    expect(general).to.eq(modelManagement.modelSectionsConfig.tabs[0]);
  });

  it('should properly set the state of a tab', () => {
    modelManagement.ngOnInit();
    let general = modelManagement.getSectionTabById(MODEL_GENERAL_TAB);

    expect(general.classes).to.not.exist;
    expect(general.postfix).to.not.exist;

    modelManagement.setTabModifiedState(general, true);

    expect(general.classes).to.exist;
    expect(general.postfix).to.exist;
  });

  it('should update the router state when a section is changed', () => {
    modelManagement.ngOnInit();
    modelManagement.onSectionChange({id: 'fields'});
    expect(stateParamsAdapterStub.setStateParam.calledOnce).to.be.true;
    expect(stateParamsAdapterStub.setStateParam.calledWith(MODEL_MANAGEMENT_SECTION_QUERY_PARAMETER, 'fields')).to.be.true;
    expect(routerStub.navigate.calledOnce).to.be.true;
    expect(routerStub.navigate.calledWith(MODEL_MANAGEMENT_EXTENSION_POINT)).to.be.true;
  });

  it('should notify for model change', () => {
    let model = new ModelClass('clazz');
    modelManagement.notifyForModelChange(model);
    expect(modelManagement.emitter.publish.calledWith(ModelEvents.MODEL_CHANGED_EVENT, model)).to.be.true;
  });

  it('should notify for model state change', () => {
    let state = false;
    let model = new ModelClass('clazz');

    modelManagement.notifyForModelStateChange(model, false);
    expect(modelManagement.emitter.publish.calledWith(ModelEvents.MODEL_STATE_CHANGED_EVENT, {
      model,
      state
    })).to.be.true;
  });

  it('should notify for model name change', () => {
    let label = new ModelSingleAttribute();

    modelManagement.notifyForModelAttributeChange(label);
    expect(modelManagement.emitter.publish.calledWith(ModelEvents.MODEL_ATTRIBUTE_CHANGED_EVENT, label)).to.be.true;
  });

  function getValidationReport() {
    return {
      nodes: [
        {
          id: "definition",
          messages: [
            {
              severity: "ERROR",
              message: "model is invalid, there is an error"
            }
          ]
        }
      ]
    };
  }
});