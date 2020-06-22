import {KeycloakAuthenticator} from 'security/keycloak/keycloak-authenticator';
import {Logger} from 'services/logging/logger';

import {KeycloakAdapter} from './keycloak-adapter';
import {stub} from 'test/test-utils';
import {PromiseStub} from 'test/promise-stub';

describe('KeycloakAuthenticator', function () {

  let authenticator;
  let windowAdapter;
  let keycloakAdapter;

  beforeEach(() => {
    windowAdapter = stubWindowAdapter();
    keycloakAdapter = stubKeycloakAdapter();

    authenticator = new KeycloakAuthenticator(windowAdapter, stub(Logger));
  });

  describe('init()', () => {
    it('should init adapter', () => {
      expect(authenticator.keycloakAdapter).to.be.undefined;
      authenticator.init({});
      expect(authenticator.keycloakAdapter).to.deep.equal({});
    });
  });

  describe('Initialized', () => {

    beforeEach(() => {
      authenticator.init(keycloakAdapter);
    });

    describe('authenticate()', () => {
      it('should return true when authenticated', () => {
        expect(authenticator.authenticate()).to.equal(true);
        expect(authenticator.keycloakAdapter.updateToken.called).to.be.false;
      });

      it('should trigger update token when not authenticated', () => {
        authenticator.keycloakAdapter.isTokenExpired.returns(true);

        expect(authenticator.authenticate()).to.equal(true);
        expect(authenticator.keycloakAdapter.updateToken.calledOnce).to.be.true;
      });
    });

    describe('logout()', () => {
      it('should invoke adapter logout', () => {
        authenticator.logout();

        let expected = {
          redirectUri: 'https://sep-test'
        };
        expect(authenticator.keycloakAdapter.logout.calledWith(expected)).to.be.true;
      });

      it('should redirect to given uri after logout', () => {
        authenticator.logout('https://sep.com/#/idoc/emf:47e08b1a-0b96-4a98-ad2e-99f91d3487bf?mode=preview');

        let expected = {
          redirectUri: 'https://sep.com/#/idoc/emf:47e08b1a-0b96-4a98-ad2e-99f91d3487bf?mode=preview'
        };
        expect(authenticator.keycloakAdapter.logout.calledWith(expected)).to.be.true;
      });
    });

    describe('isAuthenticated()', () => {
      it('should return false when adapter not initialized', () => {
        authenticator.keycloakAdapter = undefined;
        expect(authenticator.isAuthenticated()).to.be.false;
      });

      it('should return false when authenticated flag is false', () => {
        authenticator.keycloakAdapter.authenticated = false;

        expect(authenticator.isAuthenticated()).to.be.false;
      });

      it('should return false when authenticated flag is true but token expired', () => {
        authenticator.keycloakAdapter.isTokenExpired.returns(true);

        expect(authenticator.isAuthenticated()).to.be.false;
      });

      it('should return true when authenticated flag is true and token not expired', () => {
        expect(authenticator.isAuthenticated()).to.be.true;
      });
    });

    describe('getToken()', () => {
      it('should get token from adapter', () => {
        expect(authenticator.getToken()).to.eventually.equal('token');
      });

      it('should invoke login when token update fails', () => {
        authenticator.keycloakAdapter.updateToken.returns(PromiseStub.reject());

        authenticator.getToken();

        expect(authenticator.keycloakAdapter.login.calledOnce).to.be.true;
      });
    });

    describe('getUsername()', () => {
      it('should build correct username', () => {
        authenticator.keycloakAdapter.realm = 'sep.test';
        authenticator.keycloakAdapter.tokenParsed = {
          'preferred_username': 'regularuser'
        };

        expect(authenticator.getUsername()).to.be.equal('regularuser@sep.test');
      });
    });

    describe('buildAuthHeader()', () => {
      it('should build correct header', () => {
        expect(authenticator.buildAuthHeader()).to.eventually.equal('Bearer token');
      });
    });

  });

  function stubWindowAdapter() {
    return {
      location: {
        origin: 'https://sep-test'
      }
    };
  }

  function stubKeycloakAdapter() {
    let adapter = new KeycloakAdapter();
    adapter.login = sinon.stub();
    adapter.logout = sinon.stub();
    adapter.isTokenExpired = sinon.stub().returns(false);
    adapter.updateToken = sinon.stub().returns(PromiseStub.resolve());
    Object.seal(adapter);
    return adapter;
  }

});