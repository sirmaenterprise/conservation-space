import {DownloadAction} from 'idoc/actions/download-action';
import {PromiseStub} from 'test/promise-stub';
import {PromiseAdapterMock} from 'test/adapters/angular/promise-adapter-mock';
import {BASE_PATH} from 'services/rest-client';

describe('DownloadAction', () => {
  let actionsService, authenticationService;
  beforeEach(() => {
    actionsService = {
      download: () => {
        return PromiseStub.resolve({})
      }
    };

    authenticationService = {
      getToken: () => {
        return 'testJWTToken';
      }
    };
  });

  it('execute should call actionsService.download with object id', () => {
    let actionsServiceDownloadSpy = sinon.spy(actionsService, 'download');
    let downloadAction = new DownloadAction(actionsService, authenticationService, {}, PromiseAdapterMock.mockImmediateAdapter());
    let context = {
      currentObject: {
        getId: () => 'emf:123456'
      }
    };
    downloadAction.execute(undefined, context);
    expect(actionsServiceDownloadSpy.callCount).to.equal(1);
    expect(actionsServiceDownloadSpy.args[0][0]).to.equal('emf:123456');
  });

  it('decorateDownloadURI should properly decorate the URL returned from the server', () => {
    let downloadAction = new DownloadAction(actionsService, authenticationService);
    expect(downloadAction.decorateDownloadURI('/testURI1')).to.equal(BASE_PATH + '/testURI1?jwt=testJWTToken');
    expect(downloadAction.decorateDownloadURI('/testURI2?download')).to.equal(BASE_PATH + '/testURI2?download&jwt=testJWTToken');
  });
});
