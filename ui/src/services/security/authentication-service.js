import {Injectable, Inject} from 'app/app';
import {LocalStorageService} from 'services/storage/local-storage-service';
import _ from 'lodash';
import {UrlUtils} from 'common/url-utils';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import base64 from 'common/lib/base64';

export const LOGOUT_URL='/remote/ServiceLogout';

/**
 * Operations with user identity like authentication and sign-out.
 *
 * On authentication checks if the security token is presented in the url, otherwise redirects to the identity
 * provider passing a return url.
 */
@Injectable()
@Inject(WindowAdapter, LocalStorageService)
export class AuthenticationService {

  constructor(windowAdapter, localStorageService) {
    this.windowAdapter = windowAdapter;
    this.localStorageService = localStorageService;
    this.isProcessing = false;
  }

  /**
   * Checks if a security token is presented in the url. If presented, authenticates the user using this token.
   *
   * @returns true if the authentication is successful, false otherwise.
   */
  authenticate() {
    if (!this.isAuthenticated() && !this.isProcessing) {
      this.windowAdapter.navigate('/remote/auth?url=' + encodeURIComponent(this.windowAdapter.location.href));
      return false;
    }
    return true;
  }

  logout(relayTo) {
    this.isProcessing = true;

    var url = LOGOUT_URL + `?${AuthenticationService.TOKEN_REQUEST_PARAM}=${this.getToken()}`;
    if (relayTo) {
      url += `&RelayState=${encodeURIComponent(relayTo)}`;
    }
    this.removeToken();
    delete this.username;

    this.windowAdapter.navigate(url);
  }

  isAuthenticated() {
    var token = UrlUtils.getParameter(this.windowAdapter.location, AuthenticationService.TOKEN_REQUEST_PARAM);
    if (token != null) {
      this.setToken(token);
      return true;
    } else if (this.getToken()) {
      return true;
    }
    return false;
  }

  getToken() {
    return this.localStorageService.get(AuthenticationService.JWT_TOKEN_KEY);
  }

  setToken(token) {
    this.localStorageService.set(AuthenticationService.JWT_TOKEN_KEY, token);
  }

  removeToken() {
    this.localStorageService.remove(AuthenticationService.JWT_TOKEN_KEY);
  }

  /**
   * Returns username extracted from jwt token.
   *
   * @returns the username if user is logged in, otherwise undefined
   */
  getUsername() {
    if (!this.username) {
      let token = this.getToken();
      if (token) {
        var decoded = base64.decode(token.split('.')[1]);
        this.username = JSON.parse(decoded).sub;
        if (!this.username) {
          throw new Error('Username cannot be extracted from token');
        }
      }
    }

    return this.username;
  }

}

AuthenticationService.JWT_TOKEN_KEY = 'JWT';
AuthenticationService.TOKEN_REQUEST_PARAM = 'jwt';