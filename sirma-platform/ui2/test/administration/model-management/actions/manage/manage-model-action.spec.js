import {ManageModelAction} from 'administration/model-management/actions/manage/manage-model-action';
import {Router} from 'adapters/router/router';
import {NamespaceService} from 'services/rest/namespace-service';
import {InstanceObject} from 'models/instance-object';

import {
  MODEL_MANAGEMENT_QUERY_PARAMETER,
  MODEL_MANAGEMENT_EXTENSION_POINT
} from 'administration/model-management/model-management';

import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';
import {StateParamsAdapterMock} from 'test/adapters/angular/state-params-adapter-mock';

describe('ManageModelAction', () => {

  let action;
  beforeEach(() => {
    action = new ManageModelAction(stub(Router), StateParamsAdapterMock.mockAdapter(), stubNamespaceService(true), PromiseStub);
  });

  it('should navigate to the model management page', () => {
    action.namespaceService = stubNamespaceService(true);
    action.execute(undefined, getActionContext('emf:Document'));

    let expectedParams = {};
    expectedParams[MODEL_MANAGEMENT_QUERY_PARAMETER] = 'emf:Document';
    expect(action.router.navigate.calledOnce).to.be.true;
    expect(action.router.navigate.calledWith(MODEL_MANAGEMENT_EXTENSION_POINT, expectedParams)).to.be.true;
  });

  it('should resolve full URI before navigating to the model management page', () => {
    action.namespaceService = stubNamespaceService(false);
    action.execute(undefined, getActionContext('emf:Document'));

    expect(action.namespaceService.convertToFullURI.calledOnce).to.be.true;
    expect(action.namespaceService.convertToFullURI.calledWith(['emf:Document'])).to.be.true;

    let expectedParams = {};
    expectedParams[MODEL_MANAGEMENT_QUERY_PARAMETER] = 'emf:Document#converted';
    expect(action.router.navigate.calledOnce).to.be.true;
    expect(action.router.navigate.calledWith(MODEL_MANAGEMENT_EXTENSION_POINT, expectedParams)).to.be.true;
  });

  function getActionContext(id) {
    return {
      currentObject: new InstanceObject(id)
    };
  }

  function stubNamespaceService(isFull) {
    let stubbed = stub(NamespaceService);
    stubbed.isFullUri.returns(isFull);
    stubbed.convertToFullURI = sinon.spy(uris => {
      let converted = uris.map(uri => uri + '#converted');
      return PromiseStub.resolve(converted);
    });
    return stubbed;
  }
});
