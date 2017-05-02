import {ServletClient} from 'services/servlet-client';

describe('ServletClient', () => {

  var servletClient;
  beforeEach(() => {
    servletClient = new ServletClient(mockHttp());
  });

  describe('get()', () => {
    it('should make requests to the right url with correct config', () => {
      servletClient.get('/my-rules', {params: []});
      expect(servletClient.$http.get.calledOnce).to.be.true;
      expect(servletClient.$http.get.getCall(0).args[0]).to.equal('/remote/my-rules');
      expect(servletClient.$http.get.getCall(0).args[1]).to.deep.equal({params: []});
    });
  });

});

function mockHttp() {
  return {
    get: sinon.spy()
  }
}