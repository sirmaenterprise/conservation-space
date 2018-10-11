import {HeadersService} from 'instance-header/headers-service';
import {InstanceRestService} from 'services/rest/instance-service';
import {HEADER_COMPACT} from 'instance-header/header-constants';
import {Logger} from 'services/logging/logger';
import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';

describe('HeadersService', () => {

  let headersService;

  describe('loadHeaders', () => {

    it('should not return headers if no ids are provided', (done) => {
      headersService = getServiceInstance();

      headersService.loadHeaders([], HEADER_COMPACT).then((headers) => {
        expect(headers).to.eql({});
        done();
      });
    });

    it('should return object with loaded headers mapped to instance ids', (done) => {
      headersService = getServiceInstance();
      let instanceServiceResponse = {
        data: [
          buildInstanceWithHeaders('id:0'),
          buildInstanceWithHeaders('id:2')
        ]
      };
      headersService.instanceRestService.loadBatch.returns(PromiseStub.resolve(instanceServiceResponse));

      headersService.loadHeaders(['id:0', 'id:2'], HEADER_COMPACT).then((headers) => {
        expect(headers).to.eql({
          'id:0': {
            id: 'id:0',
            compact_header: 'compact-header-id:0'
          },
          'id:2': {
            id: 'id:2',
            compact_header: 'compact-header-id:2'
          }
        });
        done();
      });
    });

    it('should log an error service breaks somehow', (done) => {
      headersService = getServiceInstance();
      headersService.instanceRestService.loadBatch = () => {
        return PromiseStub.reject('server error');
      };
      headersService.headersLoaded = sinon.spy();

      return headersService.loadHeaders(['id:0', 'id:2'], HEADER_COMPACT).then(() => {
        expect(headersService.headersLoaded.called).to.be.false;
        expect(headersService.logger.error.calledOnce).to.be.true;
        expect(headersService.logger.error.getCall(0).args).to.eql(['server error']);
        done();
      }).catch(done);
    });
  });

});

function getServiceInstance() {
  let instanceServiceStub = stub(InstanceRestService);
  let loggerStub = stub(Logger);
  return new HeadersService(instanceServiceStub, loggerStub, PromiseStub);
}

function buildInstanceWithHeaders(id) {
  return {
    id: id,
    headers: {
      default_header: `default-header-${id}`,
      compact_header: `compact-header-${id}`,
      breadcrumb_header: `breadcrumb-header-${id}`
    }
  }
}