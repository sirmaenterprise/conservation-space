import {AuthenticationService} from 'services/security/authentication-service';

describe('AuthenticationService', function () {

  describe('logout()', () => {
    it('should call windowAdapter.navigate using logout url and pass the authentication token', () => {
      const JWT_TOKEN = 'jwt-token';
      var windowAdapter = {navigate: sinon.spy()};

      var service = new AuthenticationService(windowAdapter, stubLocalStorageService({[AuthenticationService.JWT_TOKEN_KEY]: JWT_TOKEN}));

      service.logout();
      expect(windowAdapter.navigate.called).to.be.true;
      expect(windowAdapter.navigate.getCall(0).args[0]).to.eq(`/remote/ServiceLogout?${AuthenticationService.TOKEN_REQUEST_PARAM}=${JWT_TOKEN}`);
    });

    it('should call windowAdapter.navigate using logout url with provided relay state', () => {
      const JWT_TOKEN = 'jwt-token';
      var windowAdapter = {navigate: sinon.spy()};

      var service = new AuthenticationService(windowAdapter, stubLocalStorageService({[AuthenticationService.JWT_TOKEN_KEY]: JWT_TOKEN}));

      service.logout('test/#/search');
      expect(windowAdapter.navigate.called).to.be.true;
      expect(windowAdapter.navigate.getCall(0).args[0]).to.eq(`/remote/ServiceLogout?${AuthenticationService.TOKEN_REQUEST_PARAM}=${JWT_TOKEN}&RelayState=test%2F%23%2Fsearch`);
    });

    it('should delete jwt token and username on logout', () => {
      const JWT_TOKEN = 'jwt-token';
      var windowAdapter = {navigate: sinon.spy()};
      var localStorageService = stubLocalStorageService({[AuthenticationService.JWT_TOKEN_KEY]: JWT_TOKEN});

      var service = new AuthenticationService(windowAdapter, localStorageService);
      service.logout('test/#/search');

      expect(localStorageService.get(AuthenticationService.JWT_TOKEN_KEY)).to.be.undefined;
      expect(service.getUsername()).to.be.undefined;
    });
  });

  it('should properly store the authentication token', function () {
    var token = 'test123';

    var localStorageServiceMock = {};
    localStorageServiceMock.set = sinon.spy();

    var authenticationService = new AuthenticationService(null, localStorageServiceMock);

    authenticationService.setToken(token);

    expect(localStorageServiceMock.set.calledWith(AuthenticationService.JWT_TOKEN_KEY, token)).to.be.true;
  });

  it('should properly fetch and return the authentication token', function () {
    var token = 'test123';

    var authenticationService = new AuthenticationService(null, stubLocalStorageService({[AuthenticationService.JWT_TOKEN_KEY]: token}));

    expect(authenticationService.getToken()).to.equal(token);
  });

  it('should tell that the user is NOT authenticated when the authentication token is NOT set', function () {
    var authenticationService = new AuthenticationService(stubWindowAdapter(), stubLocalStorageService());

    expect(authenticationService.isAuthenticated()).to.be.false;
  });

  it('should tell that the user is authenticated when the authentication token is already set', function () {
    var authenticationService = new AuthenticationService(stubWindowAdapter(), stubLocalStorageService({[AuthenticationService.JWT_TOKEN_KEY]: 'test123'}));

    expect(authenticationService.isAuthenticated()).to.be.true;
  });

  it('should properly extract the authentication token from the browser URL', function () {
    var windowAdapter = {
      location: 'https://localhost/#idoc/?test=123&jwt=test-token'
    };

    var authenticationService = new AuthenticationService(windowAdapter, stubLocalStorageService());

    var result = authenticationService.authenticate();

    expect(result).to.be.true;
    expect(authenticationService.isAuthenticated()).to.be.true;
    expect(authenticationService.getToken()).to.be.equals('test-token');
  });

  it('should navigate the browser to the authentication URL when the user is not logged in', function () {
    var windowAdapter = {
      location: new String('?test=123'),
      navigate: sinon.spy()
    };

    var authenticationService = new AuthenticationService(windowAdapter, stubLocalStorageService());

    var result = authenticationService.authenticate();

    expect(result).to.be.false;
    expect(authenticationService.isAuthenticated()).to.be.false;
    expect(windowAdapter.navigate.called).to.be.true;
    expect(windowAdapter.navigate.getCall(0).args[0]).to.contain('url=');
  });

  it('should properly remove authentication token', function () {
    var authenticationService = new AuthenticationService(null, stubLocalStorageService({[AuthenticationService.JWT_TOKEN_KEY]: 'test123'}));
    authenticationService.removeToken();

    expect(authenticationService.getToken()).to.be.undefined;
  });

  it('should properly extract username from token', function () {
    var token = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJpc3N1ZXJJZCIsImp0aSI6ImFiY2QiLCJpYXQiOiIxMjM0NTY3ODkwIiwic3ViIjoiSm9obiJ9.N-fnXpQDGZARBnK-KC50uurdh8p8VBGs5tRr_W6Zwxs';

    var authenticationService = new AuthenticationService(null, stubLocalStorageService({[AuthenticationService.JWT_TOKEN_KEY]: token}));

    expect(authenticationService.getUsername()).to.equal('John');
  });

  it('should not return username when user is not authenticated', function () {
    var token = undefined;

    var authenticationService = new AuthenticationService(null, stubLocalStorageService({[AuthenticationService.JWT_TOKEN_KEY]: token}));

    expect(authenticationService.getUsername()).to.be.undefined;
  });

  it('should throw an exception when token is invalid', function () {
    var token = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJpc3N1ZXJJZCIsImp0aSI6ImFiY2QiLCJpYXQiOiIxMjM0NTY3ODkwIn0.B2mPRHEpDZxNjzflduCfY3fUuZjeeR8Pl4JsxYsUNo0';

    var authenticationService = new AuthenticationService(null, stubLocalStorageService({[AuthenticationService.JWT_TOKEN_KEY]: token}));

    expect(function () {
      authenticationService.getUsername();
    }).to.throw(Error);
  });

  it('should not be in processing state by default', function () {
    var authenticationService = new AuthenticationService(null, stubLocalStorageService({[AuthenticationService.JWT_TOKEN_KEY]: 'jwt'}));

    expect(authenticationService.isProcessing).to.be.false;
  });

  it('should be in processing state if logout is triggered', function () {
    var authenticationService = new AuthenticationService(stubWindowAdapter(), stubLocalStorageService({[AuthenticationService.JWT_TOKEN_KEY]: 'jwt'}));
    authenticationService.logout();

    expect(authenticationService.isProcessing).to.be.true;
  });

  it('should not redirect if logout is in progress', function () {
    var windowAdapter = stubWindowAdapter();
    var authenticationService = new AuthenticationService(windowAdapter, stubLocalStorageService());
    authenticationService.isProcessing = true;

    authenticationService.authenticate();

    expect(windowAdapter.navigate.called).to.be.false;
  });

  it('should redirect if logout is not in progress', function () {
    var windowAdapter = stubWindowAdapter();
    var authenticationService = new AuthenticationService(windowAdapter, stubLocalStorageService());

    authenticationService.authenticate();

    expect(windowAdapter.navigate.called).to.be.true;
  });

  it('should favour URL token over local storage', function () {
    var windowAdapter = {
      location: 'https://localhost/#idoc/?test=123&jwt=url-token'
    };
    var authenticationService = new AuthenticationService(windowAdapter, stubLocalStorageService({[AuthenticationService.JWT_TOKEN_KEY]: 'local-storate-token'}));

    authenticationService.authenticate();

    expect(authenticationService.getToken()).to.equal('url-token');
  });
});

function stubLocalStorageService(values) {
  var stub = {
    get: function (name) {
      return this.values[name];
    },
    set: function (name, value) {
      this.values[name] = value;
    },
    remove: function (name) {
      delete this.values[name];
    }
  };
  stub.values = values || {};
  return stub;
}

function stubWindowAdapter() {
  return {
    navigate: sinon.spy(),
    location: sinon.spy()
  }
}
