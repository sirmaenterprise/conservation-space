import {ConfigurationRestService} from 'services/rest/configurations-service';
import {PromiseStub} from 'test/promise-stub';
import {StatusCodes} from 'services/rest/status-codes';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';

const SERVICE_BASE_URL = '/configurations/';
const TENANT_CONFIGURATION = SERVICE_BASE_URL + 'tenant';
const SERVICE_RELOAD = SERVICE_BASE_URL + 'reload';

describe('ConfigurationRestService', () => {

  var configService;
  beforeEach(() => {
    configService = new ConfigurationRestService(mockRestClient());
  });

  it('should return proper data and status when loading configurations', () => {
    var configUri = configService.loadConfigurations();
    expect(configUri).to.eventually.equal(TENANT_CONFIGURATION);
    expect(configService.restClient.get.calledOnce).to.be.true;
  });

  it('should return proper data and status when updating configurations', () => {
    var postUri = configService.updateConfigurations({test: 'test'});
    expect(postUri).to.eventually.equal(TENANT_CONFIGURATION);
    expect(configService.restClient.post.calledOnce).to.be.true;
  });

  it('should return proper data and status when reloading configurations', () => {
    var reloadUri = configService.reloadConfigurations();
    expect(reloadUri).to.eventually.equal(SERVICE_RELOAD);
    expect(configService.restClient.get.calledOnce).to.be.true;
  });

  function mockRestClient() {
    return {
      get: sinon.spy((params) => {
        return PromiseStub.resolve(params);
      }),
      post: sinon.spy((params) => {
        return PromiseStub.resolve(params);
      })
    };
  }

});
