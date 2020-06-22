import {ModelManagementRestService, SERVICE_URL} from 'administration/model-management/services/model-management-rest-service';
import {RestClient} from 'services/rest-client';

import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';

describe('ModelManagementRestService', () => {

  let restClientStub;
  let modelManagementRestService;

  beforeEach(() => {
    restClientStub = stub(RestClient);
    restClientStub.get.returns(PromiseStub.resolve({}));
    restClientStub.post.returns(PromiseStub.resolve({}));
    modelManagementRestService = new ModelManagementRestService(restClientStub);
  });

  it('should fetch the models hierarchy', () => {
    modelManagementRestService.getModelsHierarchy();
    expect(restClientStub.get.calledOnce).to.be.true;
  });

  it('should fetch models meta data', () => {
    modelManagementRestService.getModelsMetaData();
    expect(restClientStub.get.calledOnce).to.be.true;
  });

  it('should fetch models data', () => {
    modelManagementRestService.getModelData();
    expect(restClientStub.get.calledOnce).to.be.true;
  });

  it('should fetch models for deployment', () => {
    modelManagementRestService.getModelsForDeploy();
    expect(restClientStub.get.calledOnce).to.be.true;
  });

  it('should save provided models', () => {
    modelManagementRestService.saveModelData();
    expect(restClientStub.post.calledOnce).to.be.true;
  });

  it('should deploy provided models', () => {
    modelManagementRestService.deployModels(['1', '2'], 33);
    expect(restClientStub.post.calledOnce).to.be.true;
    let expectedPayload = {modelsToDeploy: ['1', '2'], version: 33};
    expect(restClientStub.post.calledWith(`${SERVICE_URL}/deploy`, expectedPayload)).to.be.true;
  });
});