import {PermissionsRestService} from 'services/rest/permissions-service';
import {HEADER_V2_JSON} from 'services/rest-client';

describe('Tests for permissions service rest client', function () {
  let restClient = {basePath: 'basepath'};
  let permissionsService = new PermissionsRestService(restClient);
  let instanceId = 'instanceId';

  it('Test if permisions service performs get request with proper arguments ', () => {
    restClient.get = sinon.spy();
    let includеInherited = true;
    permissionsService.load(instanceId, includеInherited);
    expect(restClient.get.calledOnce);
    expect(restClient.get.getCall(0).args[0]).to.equal('/instances/' + instanceId + '/permissions');
    let headers = restClient.get.getCall(0).args[1].headers;
    let params = restClient.get.getCall(0).args[1].params;
    expect(headers[Object.keys(headers)[0]]).to.equal(HEADER_V2_JSON);
    expect(params[Object.keys(params)[0]]).to.be.true;
  });

  it('Test if permissions service performs save request with proper arguments ', () => {
    restClient.post = sinon.spy();
    let permissions = {
      id: 'userId',
      special: 'Consumer'
    };
    permissionsService.save(instanceId, permissions);
    expect(restClient.post.calledOnce);
    expect(restClient.post.getCall(0).args[0]).to.equal('/instances/' + instanceId + '/permissions');
    expect(restClient.post.getCall(0).args[1].id).to.equal('userId');
    let headers = restClient.post.getCall(0).args[2].headers;
    expect(headers[Object.keys(headers)[0]]).to.equal(HEADER_V2_JSON);
  });

  it('Test if permissions service calls the backend ', () => {
    restClient.post = sinon.spy();

    permissionsService.restoreChildrenPermissions(instanceId);
    expect(restClient.post.calledOnce);
    expect(restClient.post.getCall(0).args[0]).to.equal('/instances/' + instanceId + '/permissions/restore-from-parent');
    let headers = restClient.post.getCall(0).args[1].headers;
    expect(headers[Object.keys(headers)[0]]).to.equal(HEADER_V2_JSON);
  });

  it('Test if permisions service performs get request for the roles with proper arguments ', () => {
    restClient.get = sinon.spy();
    permissionsService.getRoles();
    expect(restClient.get.calledOnce);
    expect(restClient.get.getCall(0).args[0]).to.equal('/instances/permissions/roles');
  });

});