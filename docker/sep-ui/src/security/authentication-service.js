import {Injectable} from 'app/app';

/**
 * Authentication service which acts as proxy for using concrete authenticator implementation.
 * The authenticator should be passed to the init method before using the service.
 */
@Injectable()
export class AuthenticationService {

  init(auth) {
    this.authenticator = auth;
  }

  authenticate() {
    return this.authenticator.authenticate();
  }

  logout(relayTo) {
    this.authenticator.logout(relayTo);
  }

  isAuthenticated() {
    return this.authenticator.isAuthenticated();
  }

  getToken() {
    return this.authenticator.getToken();
  }

  setToken(token) {
    this.authenticator.setToken(token);
  }

  removeToken() {
    this.authenticator.removeToken();
  }

  getUsername() {
    return this.authenticator.getUsername();
  }

  buildAuthHeader() {
    return this.authenticator.buildAuthHeader();
  }

}

AuthenticationService.JWT_TOKEN_KEY = 'JWT';
AuthenticationService.TOKEN_REQUEST_PARAM = 'jwt';