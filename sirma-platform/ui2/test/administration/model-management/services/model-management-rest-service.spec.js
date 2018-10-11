import {ModelManagementRestService} from 'administration/model-management/services/model-management-rest-service';
import {RestClient} from 'services/rest-client';

import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';

describe('ModelManagementRestService', () => {

  let restClientStub;
  let modelManagementRestService;

  beforeEach(() => {
    restClientStub = stub(RestClient);
    restClientStub.get.returns(PromiseStub.resolve({}));
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
});