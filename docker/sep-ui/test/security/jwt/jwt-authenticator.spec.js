import {JwtAuthenticator} from 'security/jwt/jwt-authenticator';
import {AuthenticationService} from 'security/authentication-service';
import {PromiseStub} from 'test/promise-stub';

describe('JwtAuthenticator', () => {

  let authenticator;
  let windowAdapter;
  let localStorageService;

  beforeEach(() => {
    windowAdapter = stubWindowAdapter();
    localStorageService = stubLocalStorageService();

    authenticator = new JwtAuthenticator(windowAdapter, localStorageService, PromiseStub);
  });

  describe('authenticate', () => {
    it('should properly extract the authentication token from the browser URL', () => {
      windowAdapter.location = 'https://localhost/#idoc/?test=123&jwt=test-token';

      let result = authenticator.authenticate();

      expect(result).to.equal(true);
      expect(authenticator.isAuthenticated()).to.be.true;
      expect(authenticator.getToken()).to.eventually.equal('test-token');
    });

    it('should favour URL token over local storage', () => {
      windowAdapter.location = 'https://localhost/#idoc/?test=123&jwt=url-token';
      localStorageService.set(AuthenticationService.JWT_TOKEN_KEY, 'old-token');

      authenticator.authenticate();

      expect(authenticator.getToken()).to.eventually.equal('url-token');
    });
  });

  describe('logout', () => {
    const JWT_TOKEN = 'jwt-token';

    beforeEach(() => {
      localStorageService.set(AuthenticationService.JWT_TOKEN_KEY, JWT_TOKEN);
    });

    it('should delete jwt token and username on logout', () => {
      authenticator.logout();

      expect(localStorageService.get(AuthenticationService.JWT_TOKEN_KEY)).to.be.undefined;
      expect(authenticator.getUsername()).to.be.undefined;
    });
  });

  describe('isAuthenticated', () => {
    it('should return false when the authentication token is NOT set', () => {
      expect(authenticator.isAuthenticated()).to.be.false;
    });

    it('should return true when the authentication token is set', () => {
      localStorageService.set(AuthenticationService.JWT_TOKEN_KEY, 'token');
      expect(authenticator.isAuthenticated()).to.be.true;
    });
  });

  describe('getToken', () => {
    it('should return undefined when no token set', () => {
      expect(authenticator.getToken()).to.eventually.be.undefined;
    });

    it('should properly fetch and return the authentication token', () => {
      localStorageService.set(AuthenticationService.JWT_TOKEN_KEY, 'token');
      expect(authenticator.getToken()).to.eventually.equal('token');
    });
  });

  describe('setToken', () => {
    it('should properly store the authentication token', () => {
      authenticator.setToken('token');
      expect(authenticator.getToken()).to.eventually.equal('token');
    });
  });

  describe('removeToken', () => {
    it('should properly remove authentication token', () => {
      authenticator.setToken('token');
      expect(authenticator.getToken()).to.eventually.equal('token');

      authenticator.removeToken();
      expect(authenticator.getToken()).to.eventually.be.undefined;
    });
  });

  describe('getUsername', () => {
    it('should return undefined when user is not authenticated', () => {
      expect(authenticator.getUsername()).to.be.undefined;
    });

    it('should properly extract username from token', () => {
      let token = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJpc3N1ZXJJZCIsImp0aSI6ImFiY2QiLCJpYXQiOiIxMjM0NTY3ODkwIiwic3ViIjoiSm9obiJ9.N-fnXpQDGZARBnK-KC50uurdh8p8VBGs5tRr_W6Zwxs';
      authenticator.setToken(token);

      expect(authenticator.getUsername()).to.equal('John');
    });

    it('should throw an exception when token is invalid', () => {
      let token = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJpc3N1ZXJJZCIsImp0aSI6ImFiY2QiLCJpYXQiOiIxMjM0NTY3ODkwIn0.B2mPRHEpDZxNjzflduCfY3fUuZjeeR8Pl4JsxYsUNo0';
      authenticator.setToken(token);

      expect(() => authenticator.getUsername()).to.throw(Error);
    });
  });

  describe('buildAuthHeader', () => {
    it('should properly build header', () => {
      authenticator.setToken('token');
      expect(authenticator.buildAuthHeader()).to.eventually.equal('Jwt token');
    });
  });

  function stubWindowAdapter() {
    return {
      navigate: sinon.spy(),
      location: sinon.spy()
    };
  }

  function stubLocalStorageService(values) {
    let store = values || {};
    return {
      get: (name) => {
        return store[name];
      },
      set: (name, value) => {
        store[name] = value;
      },
      remove: (name) => {
        delete store[name];
      }
    };
  }

});
