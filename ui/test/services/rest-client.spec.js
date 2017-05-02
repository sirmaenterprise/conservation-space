import {RestClient, BASE_PATH} from 'services/rest-client';

var expect = chai.expect;

describe('RestClient', function () {

  describe('configure', function () {
    it('should put the passed headers in the underlying $http service', function () {
      var $http = {};
      $http.defaults = {};
      $http.defaults.headers = {};
      $http.defaults.headers.common = {};

      var restClient = new RestClient($http);

      var config = {
        headers: {
          'X-CSRF-Token': 'myToken'
        }
      };

      restClient.configure(config);

      expect($http.defaults.headers.common['X-CSRF-Token']).to.equal(config.headers['X-CSRF-Token']);
    });

    it('should store the provided basePath', function () {
      var $http = {};
      const BASE_PATH = '/test/123';

      var restClient = new RestClient($http);

      var config = {
        basePath: BASE_PATH
      };

      restClient.configure(config);

      expect(restClient.basePath).to.equal(BASE_PATH);
    });
  });

  describe('get', function () {
    it('should call the get() method of the underlying $http with correct url and params', function () {
      var $http = {};
      $http.get = sinon.spy();

      const URL = '/users';

      var params = {
        name: 'something'
      };

      var restClient = new RestClient($http);

      restClient.get(URL, params);

      expect($http.get.getCall(0).args[0]).to.equal(restClient.basePath + URL);
      expect($http.get.getCall(0).args[1]).to.equal(params);

      restClient.get(URL);
      expect($http.get.getCall(1).args[1]).to.be.undefined;
    });
  });

  describe('post', function () {
    it('should call the post() method of the underlying $http with correct url and post data', function () {
      var $http = {};
      $http.post = sinon.spy();

      const URL = '/users';

      var user = {
        name: 'Someone',
        password: 'Something'
      };

      var restClient = new RestClient($http);

      restClient.post(URL, user);

      expect($http.post.getCall(0).args[0]).to.equal(restClient.basePath + URL);
      expect($http.post.getCall(0).args[1]).to.equal(user);
    });
  });

  describe('deleteResource', () => {
    it('should call the delete() method from $http with correct arguments', () => {
      let $http = {};
      $http.delete = sinon.spy();
      const URL = '/instances/emf:123456';
      let restClient = new RestClient($http);
      let params = {
        name: 'something'
      };
      restClient.deleteResource(URL, params);

      expect($http.delete.getCall(0).args[0]).to.equal(restClient.basePath + URL);
      expect($http.delete.getCall(0).args[1]).to.eql(params);
    });
  });

  describe('getUrl', () => {
    it('should construct urls using the base path as prefix', () => {
      let restClient = new RestClient({});

      expect(restClient.getUrl('/test123')).to.equal(BASE_PATH + '/test123');
    });
  });

});