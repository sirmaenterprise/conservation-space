import {AuthenticationService} from 'security/authentication-service';
import {JwtAuthenticator} from 'security/jwt/jwt-authenticator';
import {stub} from 'test/test-utils';

describe('AuthenticationService', function () {

  let authService;
  let authenticator;

  beforeEach(() => {
    authService = new AuthenticationService();

    authenticator = stub(JwtAuthenticator);
    authService.init(authenticator);
  });

  describe('authenticate()', () => {
    it('should invoke authenticator method', () => {
      authService.authenticate();
      expect(authenticator.authenticate.calledOnce).to.be.true;
    });
  });

  describe('logout()', () => {
    it('should invoke authenticator method', () => {
      authService.logout('test');
      expect(authenticator.logout.calledWith('test')).to.be.true;
    });
  });

  describe('isAuthenticated()', () => {
    it('should invoke authenticator method', () => {
      authService.isAuthenticated();
      expect(authenticator.isAuthenticated.calledOnce).to.be.true;
    });
  });

  describe('getToken()', () => {
    it('should invoke authenticator method', () => {
      authService.getToken();
      expect(authenticator.getToken.calledOnce).to.be.true;
    });
  });

  describe('setToken()', () => {
    it('should invoke authenticator method', () => {
      authService.setToken('token');
      expect(authenticator.setToken.calledWith('token')).to.be.true;
    });
  });

  describe('removeToken()', () => {
    it('should invoke authenticator method', () => {
      authService.removeToken();
      expect(authenticator.removeToken.calledOnce).to.be.true;
    });
  });

  describe('getUsername()', () => {
    it('should invoke authenticator method', () => {
      authService.getUsername();
      expect(authenticator.getUsername.calledOnce).to.be.true;
    });
  });

  describe('buildAuthHeader()', () => {
    it('should invoke authenticator method', () => {
      authService.buildAuthHeader();
      expect(authenticator.buildAuthHeader.calledOnce).to.be.true;
    });
  });

});