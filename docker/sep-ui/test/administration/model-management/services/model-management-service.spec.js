import {ModelManagementService} from 'administration/model-management/services/model-management-service';
import {ModelManagementRestService} from 'administration/model-management/services/model-management-rest-service';
import {ModelManagementCopyService} from 'administration/model-management/services/model-management-copy-service';

import {ModelDataBuilder} from 'administration/model-management/services/builders/model-data-builder';
import {ModelPathBuilder} from 'administration/model-management/services/builders/model-path-builder';
import {ModelStoreBuilder} from 'administration/model-management/services/builders/model-store-builder';
import {ModelMetaDataBuilder} from 'administration/model-management/services/builders/model-meta-builder';
import {ModelPropertyBuilder} from 'administration/model-management/services/builders/model-property-builder';
import {ModelHierarchyBuilder} from 'administration/model-management/services/builders/model-hierarchy-builder';

import {ModelStore} from 'administration/model-management/model/models-store';
import {ModelClass} from 'administration/model-management/model/model-class';
import {ModelDefinition} from 'administration/model-management/model/model-definition';
import {ModelList} from 'administration/model-management/model/model-list';
import {ModelPath} from 'administration/model-management/model/model-path';
import {ModelsMetaData} from 'administration/model-management/meta/models-meta';
import {ModelChangeSet} from 'administration/model-management/model/model-changeset';
import {ModelOperation} from 'administration/model-management/model/model-operation';
import {ModelSingleAttribute} from 'administration/model-management/model/attributes/model-single-attribute';
import {ModelDeployRequest} from 'administration/model-management/model/request/model-deploy-request';

import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';

describe('ModelManagementService', () => {

  let modelManagementService;
  let modelManagementRestServiceStub;
  let modelManagementCopyServiceStub;

  let modelDataBuilderStub;
  let modelPathBuilderStub;
  let modelStoreBuilderStub;
  let modelPropertyBuilderStub;
  let modelHierarchyBuilderStub;
  let modelMetaDataBuilderStub;

  beforeEach(() => {
    modelDataBuilderStub = stub(ModelDataBuilder);
    modelPathBuilderStub = stub(ModelPathBuilder);
    modelStoreBuilderStub = stub(ModelStoreBuilder);
    modelPropertyBuilderStub = stub(ModelPropertyBuilder);
    modelMetaDataBuilderStub = stub(ModelMetaDataBuilder);
    modelHierarchyBuilderStub = stub(ModelHierarchyBuilder);

    modelManagementRestServiceStub = stub(ModelManagementRestService);
    modelManagementCopyServiceStub = stub(ModelManagementCopyService);

    modelPropertyBuilderStub.buildProperties.returns(new ModelList());
    modelMetaDataBuilderStub.buildMetaData.returns(new ModelsMetaData());

    modelStoreBuilderStub.buildStoreFromHierarchy.returns(PromiseStub.resolve([]));
    modelStoreBuilderStub.buildStoreFromProperties.returns(PromiseStub.resolve([]));

    modelPathBuilderStub.buildPathFromModel.returns(new ModelPath());
    modelManagementCopyServiceStub.copyFromPath.returns(new ModelSingleAttribute());
    modelManagementCopyServiceStub.restoreFromPath.returns(new ModelSingleAttribute());

    modelManagementRestServiceStub.saveModelData.returns(PromiseStub.resolve({}));
    modelManagementRestServiceStub.getModelData.returns(PromiseStub.resolve({}));
    modelManagementRestServiceStub.getModelsHierarchy.returns(PromiseStub.resolve({}));
    modelManagementRestServiceStub.getModelsMetaData.returns(PromiseStub.resolve());
    modelManagementRestServiceStub.getModelProperties.returns(PromiseStub.resolve({}));
    modelManagementRestServiceStub.getModelsForDeploy.returns(PromiseStub.resolve(getDeploymentResponse()));

    modelManagementService = new ModelManagementService(modelManagementRestServiceStub, modelManagementCopyServiceStub,
      PromiseStub, modelDataBuilderStub, modelHierarchyBuilderStub, modelMetaDataBuilderStub,
      modelPropertyBuilderStub, modelStoreBuilderStub, modelPathBuilderStub);
  });

  it('should reject if the requested model is missing', (done) => {
    modelManagementService.getModel('emf:missing', new ModelStore()).catch(() => done());
  });

  it('should directly resolve any loaded models', () => {
    let emfEntity = new ModelClass('emf:Entity').setLoaded(true).setParent(null);
    let emfEvent = new ModelClass('emf:Event').setLoaded(true).setParent(emfEntity);
    let models = new ModelStore().addModel(emfEntity).addModel(emfEvent);

    modelManagementService.getModel('emf:Entity', models);
    expect(modelDataBuilderStub.buildModels.called).to.be.false;
    // all models in the chain are loaded so no service request is made
    expect(modelManagementRestServiceStub.getModelData.called).to.be.false;
    expect(modelDataBuilderStub.buildModelLinks.called).to.be.true;
  });

  it('should try to build models which are not loaded', () => {
    let models = new ModelStore().addModel(new ModelClass('emf:Entity').setLoaded(false));
    modelManagementService.getModel('emf:Entity', models);
    expect(modelDataBuilderStub.buildModels.called).to.be.true;
    // should call the service with the first encountered model that is not loaded
    expect(modelManagementRestServiceStub.getModelData.calledWith('emf:Entity')).to.be.true;
    expect(modelDataBuilderStub.buildModelLinks.called).to.be.true;
  });

  it('should try to build parent models which are not loaded', () => {
    let emfEntity = new ModelClass('emf:Entity').setLoaded(false).setParent(null);
    let emfEvent = new ModelClass('emf:Event').setLoaded(true).setParent(emfEntity);
    let models = new ModelStore().addModel(emfEvent);

    modelManagementService.getModel('emf:Event', models);
    expect(modelDataBuilderStub.buildModels.called).to.be.true;
    // should call the service with the first encountered model that is not loaded
    expect(modelManagementRestServiceStub.getModelData.calledWith('emf:Entity')).to.be.true;
    expect(modelDataBuilderStub.buildModelLinks.called).to.be.true;
  });

  it('should properly call and build the models meta data', () => {
    modelManagementService.getMetaData();
    expect(modelMetaDataBuilderStub.buildMetaData.called).to.be.true;
  });

  it('should properly get the models for deployment', () => {
    let clazz = new ModelClass('clazz');
    let definition = new ModelDefinition('definition');
    let store = new ModelStore().addModel(clazz).addModel(definition);

    modelManagementService.getModelsForDeploy(store).then(models => {
      expect(models instanceof ModelDeployRequest).to.be.true;
      // should prepare a proper model list with the both models
      expect(models.getModels().getModels()).to.deep.eq([clazz, definition]);
      expect(models.getVersion()).to.equal(33);
    });
  });

  it('should properly call and build model store', () => {
    modelManagementService.getModelStore();
    expect(modelStoreBuilderStub.buildStoreFromHierarchy.calledOnce).to.be.true;
    expect(modelStoreBuilderStub.buildStoreFromProperties.calledOnce).to.be.true;
  });

  it('should properly call and build the models hierarchy', () => {
    modelManagementService.getHierarchy();
    expect(modelHierarchyBuilderStub.buildHierarchy.called).to.be.true;
  });

  it('should properly call and build the semantic model properties', () => {
    modelManagementService.getProperties(new ModelsMetaData());
    expect(modelPropertyBuilderStub.buildProperties.called).to.be.true;
  });

  it('should properly call procedure for saving models and transform them to the required structure', () => {
    let model = new ModelSingleAttribute();
    let change = new ModelChangeSet();
    change.setOperation(ModelOperation.MODIFY)
      .setSelector('path-to-model')
      .setNewValue('new-value')
      .setOldValue('old-value')
      .setModel(model);

    modelManagementService.saveModels([change]);
    expect(modelManagementRestServiceStub.saveModelData.calledOnce).to.be.true;

    let changes = modelManagementRestServiceStub.saveModelData.getCall(0).args[0].changes;
    expect(changes).to.deep.equal([{
      operation: 'modifyAttribute',
      selector: 'path-to-model',
      newValue: 'new-value',
      oldValue: 'old-value',
    }]);
  });

  it('should properly call procedure for deploying models', () => {
    let clazz = new ModelClass('clazz');
    let definition = new ModelDefinition('definition');
    let list = new ModelList().insert(clazz).insert(definition);

    let deploymentRequest = new ModelDeployRequest();
    deploymentRequest.setSelectedModels(list);
    deploymentRequest.setVersion(33);

    modelManagementService.deployModels(deploymentRequest);
    expect(modelManagementRestServiceStub.deployModels.calledOnce).to.be.true;
    expect(modelManagementRestServiceStub.deployModels.calledWith(['clazz', 'definition'], 33)).to.be.true;
  });

  function getDeploymentResponse() {
    return {
      nodes: [{
        id: 'clazz'
      }, {
        id: 'definition'
      }],
      version: 33
    };
  }
});