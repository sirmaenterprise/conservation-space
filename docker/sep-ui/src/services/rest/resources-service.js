import {Inject, Injectable} from 'app/app';
import {RestClient, HEADER_V2_JSON} from 'services/rest-client';
import {TranslateService} from 'services/i18n/translate-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';

const serviceUrl = '/users';
const usersAdministrationServiceUrl = '/administration/users';
const groupsAdministrationServiceUrl = '/administration/groups';

export const USER = 'user';
export const GROUP = 'group';

@Injectable()
@Inject(RestClient, TranslateService, PromiseAdapter)
export class ResourceRestService {

  constructor(restClient, translateService, promiseAdapter) {
    this.restClient = restClient;
    this.translateService = translateService;
    this.promiseAdapter = promiseAdapter;
  }

  getResources(opts) {
    let config = {
      params: opts
    };
    return this.restClient.get(serviceUrl, config);
  }

  getResource(id) {
    if (!id) {
      return this.promiseAdapter.reject('Cannot load resource without an ID');
    }
    return this.restClient.get(`${serviceUrl}/${id}`);
  }

  changePassword(username, oldPassword, newPassword) {
    if (!username || !oldPassword || !newPassword) {
      let message = this.translateService.translateInstant('change.password.dialog.missing.args');
      return this.promiseAdapter.reject(message);
    }
    let payload = {
      username,
      oldPassword,
      newPassword
    };
    return this.restClient.post('/user/change-password', payload, {
      headers: {
        'Accept': HEADER_V2_JSON,
        'Content-Type': HEADER_V2_JSON
      },
      // TODO: remove skipping when messages in notifications are added
      'skipInterceptor': true
    });
  }

  /**
   * Returns all users, including the inactive ones with options for pagination and which properties to load.
   * This method should be accessed by admin users, otherwise the request will be rejected with 403 forbidden.
   *
   * @param pageNumber number of the current page
   * @param pageSize size of the users in a page
   * @param propertiesToLoad array of user properties which to load
   * @param userToHighlight id of a user to highlight, results of the page in which it belongs will be returned,
   *        ignoring the pageNumber param
   */
  getAllUsers(pageNumber, pageSize, propertiesToLoad, userToHighlight) {
    return this.getAllResources(USER, pageNumber, pageSize, propertiesToLoad, userToHighlight);
  }

  /**
   * Returns all groups, including the inactive ones with options for pagination and which properties to load.
   * This method should be accessed by admin users, otherwise the request will be rejected with 403 forbidden.
   *
   * @param pageNumber number of the current page
   * @param pageSize size of the users in a page
   * @param propertiesToLoad array of user properties which to load
   * @param groupToHighlight id of a group to highlight, results of the page in which it belongs will be returned,
   *        ignoring the pageNumber param
   */
  getAllGroups(pageNumber, pageSize, propertiesToLoad, groupToHighlight) {
    return this.getAllResources(GROUP, pageNumber, pageSize, propertiesToLoad, groupToHighlight);
  }

  /**
   * Returns all resources, including the inactive ones with options for pagination and which properties to load.
   * The type of the resources depends on the resourceType param, which can be USER or GROUP.
   * This method should be accessed by admin users, otherwise the request will be rejected with 403 forbidden.
   *
   * @param resourceType type of the resource to load, possible values: USER or GROUP
   * @param pageNumber number of the current page
   * @param pageSize size of the users in a page
   * @param propertiesToLoad array of user properties which to load
   * @param resourceToHighlight id of a group to highlight, results of the page in which it belongs will be returned,
   *        ignoring the pageNumber param
   */
  getAllResources(resourceType, pageNumber, pageSize, propertiesToLoad, resourceToHighlight) {
    let requestConfig = {
      params: {
        pageNumber,
        pageSize,
        properties: propertiesToLoad
      }
    };
    if (resourceToHighlight) {
      requestConfig.params.highlight = resourceToHighlight;
    }

    let requestUrl = usersAdministrationServiceUrl;
    if (resourceType === GROUP) {
      requestUrl = groupsAdministrationServiceUrl;
    }

    return this.restClient.get(requestUrl, requestConfig);
  }

  /**
   * Retrieves captcha link used for account confirmation process.
   *
   * @param code the confirmation code, if the confirmation code is not valid no link will be returned
   * @param tenant id of the tenant
   * @returns {*}
   */
  getCaptcha(code, tenant) {
    let config = {
      params: {
        code,
        tenant
      }
    };

    return this.restClient.get('/account/captcha', config);
  }

  confirmAccount(username, password, code, captchaAnswer, tenant) {
    let payload = {
      code,
      captchaAnswer,
      username,
      password,
      tenant
    };

    return this.restClient.post('/account/confirm?tenant=' + tenant, payload, {
      headers: {
        'Accept': HEADER_V2_JSON,
        'Content-Type': HEADER_V2_JSON
      }
    });
  }

}