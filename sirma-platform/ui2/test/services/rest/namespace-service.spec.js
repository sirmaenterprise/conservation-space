import {NamespaceService} from 'services/rest/namespace-service';
import {HEADER_V2_JSON} from 'services/rest-client';
import {PromiseStub} from 'test/promise-stub';

const FULL_URI = 'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document';
const SHORT_URI = 'emf:Document';
const DEFINITION_TYPE = 'commonDocument';

describe('NamespaceService', function () {
  let restClient = {};
  let namespaceService;

  beforeEach(() => {
    namespaceService = new NamespaceService(restClient, PromiseStub);
  });

  it('should recognize short uris', () => {
    expect(namespaceService.isShortUri(SHORT_URI)).to.be.true;
    expect(namespaceService.isShortUri(FULL_URI)).to.be.false;
    expect(namespaceService.isShortUri(DEFINITION_TYPE)).to.be.false;
  });

  it('should recognize full uris', () => {
    expect(namespaceService.isFullUri(FULL_URI)).to.be.true;
    expect(namespaceService.isFullUri(SHORT_URI)).to.be.false;
    expect(namespaceService.isFullUri(DEFINITION_TYPE)).to.be.false;
  });

  describe('convertToFullURI', () => {
    it('should convert all uris and ignore all non uri values', () => {
      let types = ['short:uri', 'DEF_TYPE', 'short:uri2'];
      restClient.post = sinon.spy(() => {
        return PromiseStub.resolve({
          data: {
            'short:uri': 'http://full/uri',
            'short:uri2': 'http://full/uri2'
          }
        });
      });
      namespaceService.convertToFullURI(types).then((converted) => {
        expect(converted).to.deep.equal(['http://full/uri', 'DEF_TYPE', 'http://full/uri2']);
        expect(restClient.post.calledOnce).to.be.true;
        expect(restClient.post.getCall(0).args[1]).to.deep.equal(['short:uri', 'short:uri2']);
      });
    });
  });

  describe('toFullURI()', () => {
    it('should post request with proper arguments if there is at least one short uri that is not in the cache', (done) => {
      restClient.post = sinon.stub().returns(PromiseStub.resolve({
        data: {
          'shortURI:2': 'http://full/uri2'
        }
      }));

      namespaceService.toFullURI(['http://uri.com#1', 'shortURI:2']).then(function (result) {
        expect(restClient.post.calledOnce);
        expect(restClient.post.getCall(0).args[0]).to.equal('/semantic/uri/conversion/to-full');
        expect(restClient.post.getCall(0).args[2].headers.Accept).to.equal(HEADER_V2_JSON);

        expect(namespaceService.cache['shortURI:2']).to.equal('http://full/uri2');

        expect(result.data).not.to.be.undefined;

        done();
      });
    });

    it('should use cached uris if available', (done) => {
      restClient.post = sinon.spy();

      const URIS = ['short:uri', 'short:uri2'];

      namespaceService.cache = {
        'short:uri': 'http://full/uri',
        'short:uri2': 'http://full/uri2'
      };

      let result = namespaceService.toFullURI(URIS);
      result.then(function (result) {
        expect(restClient.post.called).to.be.false;

        // expect an object with the same key and value === the long uris
        expect(result.data[URIS[0]]).to.equal(namespaceService.cache[URIS[0]]);
        expect(result.data[URIS[1]]).to.equal(namespaceService.cache[URIS[1]]);

        done();
      });
    });

    it('should not post if all uris are full', (done) => {
      restClient.post = sinon.spy();

      const URIS = ['http://uri.com#1', 'http://uri.com#2'];

      let result = namespaceService.toFullURI(URIS);
      result.then(function (result) {
        expect(restClient.post.called).to.be.false;

        // expect an object with the same key and value === the long uris
        expect(result.data[URIS[0]]).to.equal(URIS[0]);
        expect(result.data[URIS[1]]).to.equal(URIS[1]);

        done();
      });
    });
  });
});
