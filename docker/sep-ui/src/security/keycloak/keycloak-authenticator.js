import {Inject, Injectable} from 'app/app';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import {Authenticator} from 'security/authenticator';
import {Logger} from 'services/logging/logger';

const THIRTY_SECONDS = 30;

/**
 * Authenticator for Keycloak IdP, which uses Keycloak JS adapter for securing the application.
 * The adapter is using OpenID Connect 1.0 + OAuth 2.0 protocols with Authorization Code Flow.
 *
 * The Authorization Code flow redirects the user agent to Keycloak. Once the user has successfully authenticated with
 * Keycloak an Authorization Code is created and the user agent is redirected back to the application. The application
 * then uses the authorization code along with its credentials to obtain an Access Token, Refresh Token and ID Token
 * from Keycloak. For more details see the spec: http://openid.net/specs/openid-connect-core-1_0.html#CodeFlowAuth
 *
 * The init method should be invoked with instance of the adapter that to be used.
 *
 * For better security, the tokens are not stored in any storage. They are only contained in the memory while the
 * application is open in the user's browser.
 *
 * The access token have short lifespan (5 min by default). The token retrieval methods make sure that an active token
 * is returned by requesting new one from keycloak if necessary.
 *
 * The keycloak adapter creates a hidden iframe that is used to detect if a Single-Sign Out has occurred or if the user
 * session is expired. This does not require any network traffic, instead the status is retrieved by looking at a
 * special status cookie which have keycloak's origin.
 *
 * @see AuthenticationInterceptor
 */
@Injectable()
@Inject(WindowAdapter, Logger)
export class KeycloakAuthenticator extends Authenticator {

  constructor(windowAdapter, logger) {
    super();
    this.windowAdapter = windowAdapter;
    this.logger = logger;
  }

  init(adapter) {
    this.keycloakAdapter = adapter;
  }

  authenticate() {
    if (!this.isAuthenticated()) {
      // if session is expired login page will be loaded, otherwise will try to get new access token
      this.getToken();
    }
    // already authenticated in main.js
    return true;
  }

  /**
   * Logouts the authenticated user. Sets the given redirect uri for logout or if uri not provided makes sure that the
   * next consecutive login will redirect to logged in user's dashboard.
   */
  logout(redirectUri) {
    let options = {
      redirectUri: redirectUri || this.windowAdapter.location.origin
    };
    this.keycloakAdapter.logout(options);
  }

  isAuthenticated() {
    return this.keycloakAdapter !== undefined && this.keycloakAdapter.authenticated && !this.keycloakAdapter.isTokenExpired();
  }

  /**
   * Returns an active access token. If the current token is about to expire within the next 30 seconds, a new one is
   * retrieved from keycloak.
   *
   * @returns {promise.Promise<any> | * | Promise<T | never>}
   */
  getToken() {
    return this.keycloakAdapter.updateToken(THIRTY_SECONDS).then(() => {
      return this.keycloakAdapter.token;
    }).catch(error => {
      this.logger.error('Failed to update token: ' + error);
      this.keycloakAdapter.login();
    });
  }

  getUsername() {
    return this.keycloakAdapter.tokenParsed['preferred_username'] + '@' + this.getTenant();
  }

  getTenant() {
    return this.keycloakAdapter.realm;
  }

  buildAuthHeader() {
    return this.getToken().then(token => `Bearer ${token}`);
  }

  setToken() {
    // not needed
  }

  removeToken() {
    // not needed
  }

}

KeycloakAuthenticator.CLIENT_ID = 'sep-ui';
KeycloakAuthenticator.TENANT = 'tenant';
KeycloakAuthenticator.MASTER_TENANT = 'master';
KeycloakAuthenticator.LOGIN_REQUIRED = 'login-required';