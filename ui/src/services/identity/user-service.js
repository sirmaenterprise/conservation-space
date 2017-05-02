import {Injectable, Inject} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {AuthenticationService} from 'services/security/authentication-service';
import {ResourceRestService} from 'services/rest/resources-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {Event} from 'app/app';

@Injectable()
@Inject(AuthenticationService, ResourceRestService, PromiseAdapter, Eventbus)
export class UserService {

  constructor(authenticationService, resourceRestService, promiseAdapter, eventbus) {
    this.authenticationService = authenticationService;
    this.resourceRestService = resourceRestService;
    this.promiseAdapter = promiseAdapter;
    this.eventbus = eventbus;
  }

  /**
   * Returns promise with info about currently logged user.
   *
   * @param reload If the user should be reloaded even its already loaded once. This can be used if the user details are changed.
   * @returns {Promise} promise with the user info
   */
  getCurrentUser(reload = false) {
    return this.promiseAdapter.promise((resolve) => {
      if (this.currentUser && !reload) {
        resolve(this.currentUser);
      } else {
        let username = this.authenticationService.getUsername();

        this.resourceRestService.getResource(username).then((response) => {
          let data = response.data;

          this.currentUser = {};
          this.currentUser.username = data.value;
          this.currentUser.name = data.label;
          this.currentUser.id = data.id;
          this.currentUser.isAdmin = data.isAdmin;
          this.currentUser.language = data.language;
          this.currentUser.tenantId = data.tenantId;

          this.eventbus.publish(new UserLoadedEvent(this.currentUser));

          resolve(this.currentUser);
        });
      }
    });
  }

  changePassword(oldPassword, newPassword) {
    return this.resourceRestService.changePassword(this.authenticationService.getUsername(), oldPassword, newPassword);
  }

  getCurrentUserId() {
    return this.currentUser.id;
  }

}

/**
 * An event fired after current user gets loaded.
 */
@Event()
export class UserLoadedEvent {
  constructor() {
    this.args = arguments;
  }

  getData() {
    return this.args;
  }
}

