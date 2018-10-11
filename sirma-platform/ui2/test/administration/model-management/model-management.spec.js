import {
  ModelManagement,
  MODEL_FIELDS_TAB,
  MODEL_GENERAL_TAB,
  MODEL_MANAGEMENT_QUERY_PARAMETER,
  MODEL_MANAGEMENT_EXTENSION_POINT
} from 'administration/model-management/model-management';

import {EventEmitter} from 'common/event-emitter';
import {ModelClass} from 'administration/model-management/model/model-class';
import {ModelClassHierarchy} from 'administration/model-management/model/model-hierarchy';

import {ModelManagementService} from 'administration/model-management/services/model-management-service';
import {StateParamsAdapter} from 'adapters/router/state-params-adapter';
import {Router} from 'adapters/router/router';

import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';

describe('ModelManagement', () => {

  let routerStub;
  let stateParamsAdapterStub;
  let modelManagementServiceStub;

  let model;
  let hierarchy;
  let modelManagement;

  beforeEach(() => {
    routerStub = stub(Router);
    stateParamsAdapterStub = stub(StateParamsAdapter);
    modelManagementServiceStub = stub(ModelManagementService);

    model = new ModelClass('object', 'entity');
    hierarchy = new ModelClassHierarchy(model);

    modelManagementServiceStub.getModel.returns(PromiseStub.resolve(model));
    modelManagementServiceStub.getProperties.returns(PromiseStub.resolve({}));
    modelManagementServiceStub.getHierarchy.returns(PromiseStub.resolve({tree: hierarchy, flat: hierarchy}));
    modelManagement = new ModelManagement(modelManagementServiceStub, stateParamsAdapterStub, routerStub, PromiseStub);
  });

  it('should initialize the properties model', () => {
    modelManagement.ngOnInit();
    expect(modelManagement.properties).to.exist;
  });

  it('should initialize the model and configuration for the tree', () => {
    stateParamsAdapterStub.getStateParam.returns('collection');
    modelManagement.ngOnInit();
    expect(modelManagement.treeModel).to.deep.eq(hierarchy);
    expect(modelManagement.treeConfig).to.deep.eq({node: 'collection'});
    expect(modelManagement.flatTreeModel).to.deep.eq(hierarchy);
  });

  it('should initialize the model and configuration for the tabs', () => {
    modelManagement.ngOnInit();

    expect(modelManagement.modelSectionsConfig.tabs.length).to.eq(2);
    expect(modelManagement.modelSectionsConfig.tabs[0].id).to.eq(MODEL_GENERAL_TAB);
    expect(modelManagement.modelSectionsConfig.tabs[1].id).to.eq(MODEL_FIELDS_TAB);
  });

  it('should publish event when the model is changed or reloaded', () => {
    modelManagement.emitter = stub(EventEmitter);

    modelManagement.loadModel();
    expect(modelManagement.emitter.publish.calledOnce).to.be.true;
  });

  it('should re-link the model when it is changed or reloaded', () => {
    modelManagement.emitter = stub(EventEmitter);

    modelManagement.loadModel();
    expect(modelManagementServiceStub.linkModel.calledOnce).to.be.true;
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
    modelManagement.loadModel('object');

    expect(modelManagement.model).to.equal(model);
    expect(modelManagement.isModelMissing()).to.be.false;
  });

  it('should flag if the requested model is missing', () => {
    modelManagementServiceStub.getModel.returns(PromiseStub.reject());
    modelManagement.loadModel('object');
    expect(modelManagement.isModelMissing()).to.be.true;
  });

  it('should get a model from a provided hierarchy', () => {
    expect(modelManagement.getModel('object', {object: hierarchy})).to.equal(model);
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

  it('should un-subscribe to model change or reload', () => {
    modelManagement.emitter = stub(EventEmitter);

    modelManagement.ngOnDestroy();
    expect(modelManagement.emitter.unsubscribeAll.calledOnce).to.be.true;
  });
});