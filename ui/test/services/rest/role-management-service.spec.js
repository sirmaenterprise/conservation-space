import {RoleManagementService, SERVICE_BASE_URL} from 'services/rest/role-management-service';
import {HEADER_V2_JSON} from 'services/rest-client';

describe('RoleManagementService', function () {
  let restClient = {basePath: 'basepath'};
  let service = new RoleManagementService(restClient);

  it('should build http headers config', () => {
    let headers = service.config.headers;

    expect(service.config).to.exist;
    expect(service.config.headers).to.exist;

    Object.keys(headers).forEach((header) => {
      expect(headers[header]).to.equal(HEADER_V2_JSON);
    });
  });

  describe('getRoles', function () {

    it('should call rest client with correct parameters', () => {
      restClient.get = sinon.spy();
      service.getRoles();

      expect(restClient.get.calledOnce).to.be.true;
      expect(restClient.get.getCall(0).args[0]).to.equal(SERVICE_BASE_URL + '/roles');
      expect(restClient.get.getCall(0).args[1]).to.exist;
    });

  });

  describe('getActions', function () {

    it('should call rest client with correct parameters', () => {
      restClient.get = sinon.spy();
      service.getActions();

      expect(restClient.get.calledOnce).to.be.true;
      expect(restClient.get.getCall(0).args[0]).to.equal(SERVICE_BASE_URL + '/actions');
      expect(restClient.get.getCall(0).args[1]).to.exist;
    });

  });

  describe('getRoleActions', function () {

    it('should call rest client with correct parameters', () => {
      restClient.get = sinon.spy();
      service.getRoleActions();

      expect(restClient.get.calledOnce).to.be.true;
      expect(restClient.get.getCall(0).args[0]).to.equal(SERVICE_BASE_URL + '/roleActions');
      expect(restClient.get.getCall(0).args[1]).to.exist;
    });

  });

  describe('getFilters', function () {

    it('should call rest client with correct parameters', () => {
      restClient.get = sinon.spy();
      service.getFilters();

      expect(restClient.get.calledOnce).to.be.true;
      expect(restClient.get.getCall(0).args[0]).to.equal(SERVICE_BASE_URL + '/filters');
      expect(restClient.get.getCall(0).args[1]).to.exist;
    });

  });

  describe('saveRoleActions', function () {

    it('should call rest client with correct parameters', () => {
      restClient.post = sinon.spy();
      service.saveRoleActions([{
        role: 'roleId',
        action: 'actionId',
        enabled: true,
        filters: []
      }]);

      expect(restClient.post.calledOnce).to.be.true;
      expect(restClient.post.getCall(0).args[0]).to.equal(SERVICE_BASE_URL + '/roleActions');
      expect(restClient.post.getCall(0).args[1]).to.exist;
    });

  });

});