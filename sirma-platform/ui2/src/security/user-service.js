import {Injectable, Inject} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {AuthenticationService} from 'security/authentication-service';
import {ResourceRestService} from 'services/rest/resources-service';
import {ModelsService} from 'services/rest/models-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {Event} from 'app/app';

@Injectable()
@Inject(AuthenticationService, ResourceRestService, ModelsService, PromiseAdapter, Eventbus)
export class UserService {

  constructor(authenticationService, resourceRestService, modelsService, promiseAdapter, eventbus) {
    this.authenticationService = authenticationService;
    this.resourceRestService = resourceRestService;
    this.modelsService = modelsService;
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
        // some important user information needs to be taken from the models service.
        this.promiseAdapter.all([this.resourceRestService.getResource(username), this.modelsService.getClassInfo(UserService.EMF_USER)]).then((response) => {
          let [userResource, userClassInfo] = [response[0].data, response[1].data];

          this.currentUser = {};
          this.currentUser.username = userResource.value;
          this.currentUser.name = userResource.label;
          this.currentUser.id = userResource.id;
          this.currentUser.isAdmin = userResource.isAdmin;
          this.currentUser.language = userResource.language;
          this.currentUser.tenantId = userResource.tenantId;
          this.currentUser.emailAddress = userResource.emailAddress;
          this.currentUser.mailboxSupportable = userClassInfo.mailboxSupportable;
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

UserService.EMF_USER = 'emf:User';
