import {Inject, Injectable} from 'app/app';
import {RestClient, HEADER_V2_JSON} from 'services/rest-client';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {TranslateService} from 'services/i18n/translate-service';
import {UrlUtils} from 'common/url-utils';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import {MODE_PRINT, STATE_PARAM_TAB} from 'idoc/idoc-constants';
import {AuthenticationService} from 'security/authentication-service';
import {InstanceResponse} from 'services/rest/response/instance-response';

import _ from 'lodash';

const serviceUrl = '/instances';

@Injectable()
@Inject(RestClient, PromiseAdapter, TranslateService, AuthenticationService, WindowAdapter)
export class ActionsService {

  constructor(restClient, promiseAdapter, translateService, authenticationService, windowAdapter) {
    this.restClient = restClient;
    this.promiseAdapter = promiseAdapter;
    this.translateService = translateService;
    this.config = {};
    this.config.headers = {
      'Accept': HEADER_V2_JSON,
      'Content-Type': HEADER_V2_JSON
    };
    this.authenticationService = authenticationService;
    this.windowAdapter = windowAdapter;
  }

  /**
   * Calls the service to load actions evaluated according to provided context data, grouped in JSON tree form
   * based on the definition model.
   *
   * @param id The instance id for which to load actions
   * @param data
   *  {
   *    // current instance id for which the actions should be evaluated
   *    id: 'instanceId',
   *    // current context in which the instance is opened
   *    contextId: 'contextInstanceId',
   *    // a placeholder is the target from where the actions were requested
   *    placeholder: 'placeholder',
   *    path: ['instanceid1', 'instanceid2', ...]
   *  }
   * @returns {Promise.<*[]>}
   */
  getActions(id, data) {
    let requestConfig = _.defaults({
      params: data
    }, this.config);

    return this.restClient.get(`${serviceUrl}/${id}/actions`, requestConfig);
  }

  /**
   * Calls the service to load actions evaluated according to provided context data.
   *
   * @param id The instance id for which to load actions
   * @param data
   *  {
   *    // current instance id for which the actions should be evaluated
   *    id: 'instanceId',
   *    // current context in which the instance is opened
   *    contextId: 'contextInstanceId',
   *    // a placeholder is the target from where the actions were requested
   *    placeholder: 'placeholder',
   *    path: ['instanceid1', 'instanceid2', ...]
   *  }
   * @returns {Promise.<*[]>}
   */
  getFlatActions(id, data) {
    let requestConfig = _.defaults({
      params: data
    }, this.config);
    return this.restClient.get(`${serviceUrl}/${id}/actions/flat`, requestConfig);
  }

  executeTransition(id, data) {
    return this.restClient.post(`${serviceUrl}/${id}/actions/transition`, data, this.config);
  }

  download(id) {
    return this.restClient.post(`${serviceUrl}/${id}/actions/download`, null, this.config);
  }

  move(id, destination, config) {
    config = _.defaultsDeep(config || {}, this.config);
    return this.restClient.post(`${serviceUrl}/${id}/actions/move`, destination, config);
  }

  lock(id, data) {
    return this.restClient.post(`${serviceUrl}/${id}/actions/lock`, data, this.config);
  }

  unlock(id, data) {
    return this.restClient.post(`${serviceUrl}/${id}/actions/unlock`, data, this.config);
  }

  addIcons(id, icons) {
    return this.restClient.post(`${serviceUrl}/${id}/actions/addicons`, {icons}, this.config);
  }

  addThumbnail(request) {
    if (!request || !request.instanceId || !request.thumbnailObjectId) {
      var message = this.translateService.translateInstant('action.add.thumbnail.missing.id');
      return this.promiseAdapter.reject(message);
    }
    var data = {
      thumbnailObjectId: request.thumbnailObjectId
    };
    return this.restClient.post(`${serviceUrl}/${request.instanceId}/actions/thumbnail`, data, this.config);
  }

  addRelation(id, data) {
    return this.restClient.post(`${serviceUrl}/${id}/actions/addRelation`, data, this.config);
  }

  updateRelations(data) {
    return this.restClient.post(`${serviceUrl}/actions/updateRelations`, data, this.config);
  }

  createOrUpdate(id, data) {
    return this.restClient.patch(`${serviceUrl}/${id}/actions/createOrUpdate`, data, this.config);
  }

  getChangeTypeInstance(id, asType) {
    return this.restClient.get(`${serviceUrl}/${id}/actions/changeType?asType=${asType}`).then((response) => {
      return new InstanceResponse(response);
    });
  }

  changeType(id, data) {
    return this.restClient.post(`${serviceUrl}/${id}/actions/changeType`, data, this.config);
  }

  exportPDF(id, tabId, data, config) {
    config = _.defaults(config || {}, this.config);
    config.responseType = 'arraybuffer';
    this.buildURL(id, tabId, data, true);
    return this.restClient.post('/export/pdf', data, config);
  }

  exportWord(id, tabId, data, config) {
    config = _.defaults(config || {}, this.config);
    this.buildURL(id, tabId, data);
    return this.restClient.post(`${serviceUrl}/${id}/actions/export-word`, data, config);
  }

  buildURL(id, tabId, data, absoluteURL) {
    let params = {
      'mode': MODE_PRINT
    };
    if (tabId) {
      params[STATE_PARAM_TAB] = tabId;
    }

    data.url = `${absoluteURL ? this.windowAdapter.location.origin : ''}${UrlUtils.buildIdocUrl(id, undefined, params)}`;
  }

  publish(id, data) {
    return this.restClient.post(`${serviceUrl}/${id}/actions/publish`, data, this.config);
  }

  publishAsPdf(id, data) {
    return this.restClient.post(`${serviceUrl}/${id}/actions/publishAsPdf`, data, this.config);
  }

  /**
   * Calls server operation for revert on version. The operation will handle the processing of the instance and
   * its content.
   * Note that all of the object properties and some system will remain the same.
   * Note that the current object which data will be replaced will be locked while the revert process is executed.
   *
   * @param id - the id of the version which data should replace the data in the current object
   * @param data - the payload of the request, containing information about the executed action like user operation, etc.
   * @return instance which data is replaced with the data from the version
   */
  revertVersion(id, data) {
    return this.restClient.post(`${serviceUrl}/${id}/actions/revert-version`, data, this.config);
  }

  /**
   * Activates template instance.
   *
   * @param id of the template instance.
   * @param data the data needed for the request.
   * @return the activated template instance.
   */
  activateTemplate(id, data) {
    return this.restClient.post(`${serviceUrl}/${id}/actions/activate-template`, data, this.config);
  }

  downloadForEditOffline(id) {
    let tokenParam = AuthenticationService.TOKEN_REQUEST_PARAM;
    return this.authenticationService.getToken().then(token => {
      return this.restClient.getUrl(`${serviceUrl}/${id}/actions/edit-offline-check-out?${tokenParam}=${token}`);
    });
  }

  delete(id, userOperation) {
    return this.restClient.post(`${serviceUrl}/${id}/actions/delete`, {userOperation}, this.config);
  }
}
