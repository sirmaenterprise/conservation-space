import {ResourceRestService} from 'services/rest/resources-service';
import {PromiseAdapterMock} from 'test/adapters/angular/promise-adapter-mock';

describe('ResourceRestService', () => {

  var restClient = getRestClientMock();
  var service = new ResourceRestService(restClient, getTranslateServiceMock(), PromiseAdapterMock.mockAdapter());

  it('should fetch resources by a given criteria with correct path parameters.', () => {
    restClient.get = sinon.spy();
    let opts = {
      param: 'admin'
    };
    service.getResources(opts);
    expect(restClient.get.calledOnce);
    expect(restClient.get.getCall(0).args[0]).to.equal('/users');
    expect(restClient.get.getCall(0).args[1].params.param).to.equal('admin');
  });

  it('should call the rest client with correct path parameter if an ID is provided.', () => {
    restClient.get = sinon.spy();
    service.getResource('administrator');
    expect(restClient.get.calledOnce);
    expect(restClient.get.getCall(0).args[0]).to.equal('/users/administrator');
  });

  it('should reject requests if called without an ID.', (done) => {
    Promise.resolve(service.getResource()).catch((error) => {
      expect(error).to.equal('Cannot load resource without an ID.');
      done();
    });
  });

  describe('changePassword()', () => {
    it('should reject request if there is missing parameter.', (done) => {
      service.changePassword('john').catch((error) => {
        expect(error).to.equal('label');
        done();
      });
    });

    it('should call the rest client with correct parameters', () => {
      restClient.post = sinon.spy();
      let expected = {
        username: 'john',
        oldPassword: '123',
        newPassword: '321'
      };

      service.changePassword(expected.username, expected.oldPassword, expected.newPassword);

      expect(restClient.post.calledOnce);
      expect(restClient.post.getCall(0).args[0]).to.equal('/user/change-password');
      expect(restClient.post.getCall(0).args[1]).to.deep.equal(expected);
    });
  });

});

function getTranslateServiceMock() {
  return {
    translateInstant: sinon.spy(()=>{return 'label'})
  }
}

function getRestClientMock() {
  return {
    get: sinon.spy(),
    post: sinon.spy()
  }
}