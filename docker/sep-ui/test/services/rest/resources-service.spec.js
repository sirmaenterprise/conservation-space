import {ResourceRestService, USER, GROUP} from 'services/rest/resources-service';
import {RestClient} from 'services/rest-client';
import {TranslateService} from 'services/i18n/translate-service';
import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';

describe('ResourceRestService', () => {

  let restClient;
  let translateService;
  let resourceService;

  beforeEach(() => {
    restClient = stub(RestClient);
    translateService = stub(TranslateService);

    resourceService = new ResourceRestService(restClient, translateService, PromiseStub);
  });

  describe('getResources', () => {
    it('should fetch resources by a given criteria with correct path parameters.', () => {
      let opts = {
        param: 'admin'
      };
      resourceService.getResources(opts);
      expect(restClient.get.calledOnce).to.be.true;
      expect(restClient.get.getCall(0).args[0]).to.equal('/users');
      expect(restClient.get.getCall(0).args[1].params.param).to.equal('admin');
    });
  });

  describe('getResource', () => {
    it('should call the rest client with correct path parameter if an ID is provided.', () => {
      resourceService.getResource('administrator');
      expect(restClient.get.calledOnce).to.be.true;
      expect(restClient.get.getCall(0).args[0]).to.equal('/users/administrator');
    });

    it('should reject requests if called without an ID', () => {
      expect(resourceService.getResource()).to.be.rejected;
    });
  });

  describe('changePassword()', () => {
    it('should reject request if there is missing parameter.', () => {
      expect(resourceService.changePassword('john')).to.be.rejected;
      expect(translateService.translateInstant.calledOnce).to.be.true;
    });

    it('should call the rest client with correct parameters', () => {
      let expected = {
        username: 'john',
        oldPassword: '123',
        newPassword: '321'
      };

      resourceService.changePassword(expected.username, expected.oldPassword, expected.newPassword);

      expect(restClient.post.calledOnce).to.be.true;
      expect(restClient.post.getCall(0).args[0]).to.equal('/user/change-password');
      expect(restClient.post.getCall(0).args[1]).to.deep.equal(expected);
    });
  });

  describe('getAllUsers()', () => {
    it('should invoke rest service for users', () => {
      resourceService.getAllUsers(1, 10);

      let expectedParams = {
        params: {
          pageNumber: 1,
          pageSize: 10,
          properties: undefined
        }
      };

      expect(restClient.get.calledWith('/administration/users', expectedParams)).to.be.true;
    });
  });

  describe('getAllGroups()', () => {
    it('should invoke rest service for groups', () => {
      resourceService.getAllGroups(1, 10);

      let expectedParams = {
        params: {
          pageNumber: 1,
          pageSize: 10,
          properties: undefined
        }
      };

      expect(restClient.get.calledWith('/administration/groups', expectedParams)).to.be.true;
    });
  });

  describe('getAllResources()', () => {
    it('should construct correct parameters', () => {
      resourceService.getAllResources(USER, 1, 10, ['id']);

      let expectedParams = {
        params: {
          pageNumber: 1,
          pageSize: 10,
          properties: ['id']
        }
      };

      expect(restClient.get.calledWith('/administration/users', expectedParams)).to.be.true;
    });

    it('should add resource to select param if passed', () => {
      resourceService.getAllResources(USER, 1, 10, ['id'], 'emf:resource');

      let expectedParams = {
        params: {
          pageNumber: 1,
          pageSize: 10,
          properties: ['id'],
          highlight: 'emf:resource'
        }
      };

      expect(restClient.get.calledWith('/administration/users', expectedParams)).to.be.true;
    });

    it('should invoke group rest service when resource type is group', () => {
      resourceService.getAllResources(GROUP, 1, 10, ['id']);

      let expectedParams = {
        params: {
          pageNumber: 1,
          pageSize: 10,
          properties: ['id']
        }
      };

      expect(restClient.get.calledWith('/administration/groups', expectedParams)).to.be.true;
    });
  });

  describe('getCaptcha()', () => {
    it('should call rest client with correct params', () => {
      let code = 'code';
      let tenant = 'tenant';
      let expectedConfig = {
        params: {
          code,
          tenant
        }
      };

      resourceService.getCaptcha(code, tenant);

      expect(restClient.get.calledOnce).to.be.true;
      expect(restClient.get.getCall(0).args[0]).to.equal('/account/captcha');
      expect(restClient.get.getCall(0).args[1]).to.deep.equal(expectedConfig);
    });
  });

  describe('confirmAccount()', () => {
    it('should call rest client with correct params', () => {
      let username = 'user';
      let password = 'pass';
      let code = 'code';
      let captchaAnswer = 'answer';
      let tenant = 'tenant';
      let expectedPayload = {
        code,
        captchaAnswer,
        username,
        password,
        tenant
      };

      resourceService.confirmAccount(username, password, code, captchaAnswer, tenant);

      expect(restClient.post.calledOnce).to.be.true;
      expect(restClient.post.getCall(0).args[0]).to.equal('/account/confirm?tenant=' + tenant);
      expect(restClient.post.getCall(0).args[1]).to.deep.equal(expectedPayload);
    });
  });

});