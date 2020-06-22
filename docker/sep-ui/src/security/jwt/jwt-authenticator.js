import {Inject, Injectable} from 'app/app';
import {Authenticator} from 'security/authenticator';
import {AuthenticationService} from 'security/authentication-service';
import {LocalStorageService} from 'services/storage/local-storage-service';
import {UrlUtils} from 'common/url-utils';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import base64 from 'common/lib/base64';

/**
 * This authenticator is not related to any IDP. It works with JWT tokens issued by SEP.
 * Mainly used for export & print as pdf.
 */
@Injectable()
@Inject(WindowAdapter, LocalStorageService, PromiseAdapter)
export class JwtAuthenticator extends Authenticator {

  constructor(windowAdapter, localStorageService, promiseAdapter) {
    super();
    this.windowAdapter = windowAdapter;
    this.localStorageService = localStorageService;
    this.promiseAdapter = promiseAdapter;
  }

  authenticate() {
    return this.isAuthenticated();
  }

  logout() {
    this.removeToken();
    delete this.username;
  }

  isAuthenticated() {
    let token = UrlUtils.getParameter(this.windowAdapter.location, AuthenticationService.TOKEN_REQUEST_PARAM);
    if (token != null) {
      this.setToken(token);
      return true;
    } else if (this.getTokenInstant()) {
      return true;
    }
    return false;
  }

  getToken() {
    return this.promiseAdapter.resolve(this.getTokenInstant());
  }

  getTokenInstant() {
    return this.localStorageService.get(AuthenticationService.JWT_TOKEN_KEY);
  }

  setToken(token) {
    this.localStorageService.set(AuthenticationService.JWT_TOKEN_KEY, token);
  }

  removeToken() {
    this.localStorageService.remove(AuthenticationService.JWT_TOKEN_KEY);
  }

  getUsername() {
    if (!this.username) {
      let token = this.getTokenInstant();
      if (token) {
        let decoded = base64.decode(token.split('.')[1]);
        this.username = JSON.parse(decoded).sub;
        if (!this.username) {
          throw new Error('Username cannot be extracted from token');
        }
      }
    }

    return this.username;
  }

  buildAuthHeader() {
    return this.getToken().then(token => `Jwt ${token}`);
  }

}