/**
 * Abstract class which defines an authenticator used for securing the application.
 *
 * NOTE: remove this class after full migration to keycloak
 */
export class Authenticator {

  constructor() {
    let requiredMethods = [this.authenticate, this.logout, this.isAuthenticated, this.getToken, this.setToken, this.removeToken,
      this.getUsername, this.buildAuthHeader];

    for (let i in requiredMethods) {
      if (typeof requiredMethods[i] !== 'function') {
        throw new TypeError('Must override required authenticator method');
      }
    }
  }

}