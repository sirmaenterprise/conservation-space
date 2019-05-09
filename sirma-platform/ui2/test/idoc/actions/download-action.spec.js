import {DownloadAction} from 'idoc/actions/download-action';
import {ActionsService} from 'services/rest/actions-service';
import {AuthenticationService} from 'security/authentication-service';
import {BASE_PATH} from 'services/rest-client';

import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';

describe('DownloadAction', () => {

  let downloadAction;
  let actionsService;
  let authenticationService;

  beforeEach(() => {
    actionsService = mockActionsService();
    authenticationService = mockAuthenticationService();

    downloadAction = new DownloadAction(actionsService, authenticationService, {}, PromiseStub);
  });

  it('execute should call actionsService.download with object id', () => {
    let context = {
      currentObject: {
        getId: () => 'emf:123456'
      }
    };

    downloadAction.execute(undefined, context);

    expect(actionsService.download.callCount).to.equal(1);
    expect(actionsService.download.calledWith('emf:123456')).to.be.true;
  });

  it('decorateDownloadURI should properly decorate the URL returned from the server', () => {
    expect(downloadAction.decorateDownloadURI('/testURI1')).to.eventually.equal(BASE_PATH + '/testURI1?jwt=testJWTToken');
  });

  it('decorateDownloadURI should properly decorate the URL with query param', () => {
    expect(downloadAction.decorateDownloadURI('/testURI2?download')).to.eventually.equal(BASE_PATH + '/testURI2?download&jwt=testJWTToken');
  });

  function mockActionsService() {
    let service = stub(ActionsService);
    service.download.returns(PromiseStub.resolve({}));
    return service;
  }

  function mockAuthenticationService() {
    let service = stub(AuthenticationService);
    service.getToken.returns(PromiseStub.resolve('testJWTToken'));
    return service;
  }

});
