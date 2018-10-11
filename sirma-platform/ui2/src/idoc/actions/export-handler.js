import {InstanceAction} from 'idoc/actions/instance-action';
import {BASE_PATH} from 'services/rest-client';
import {AuthenticationService} from 'services/security/authentication-service';
import {UrlUtils} from 'common/url-utils';
import {ErrorCodes} from 'services/rest/error-codes';
import FileSaver from 'file-saver';
import _ from 'lodash';

export const EXPORT_RESTRICT_NOTIFICATION = 'action.export.restricted.notification';

/**
 * Base class for actions that requires the idoc to be exported first.
 * Implementations have to implement 'getNotificationLabelId' method where to return the id of the label which will be
 * used for the notification which will be shown to the user before the operation to start.
 * Implementations have to implement 'afterExportHandler' method where to implement the additional logic that should be
 * executed after the export is completed.
 */
export class ExportHandler extends InstanceAction {

  constructor(logger, actionsService, translateService, notificationService, authenticationService, instanceRestService, promiseAdapter) {
    super(logger);
    if (typeof this.getNotificationLabelId !== 'function') {
      throw new TypeError('ExportHandler handlers must override the \'getNotificationLabelId\' function!');
    }
    if (typeof this.afterExportHandler !== 'function') {
      throw new TypeError('ExportHandler handlers must override the \'afterExportHandler\' function!');
    }
    if (!actionsService || !translateService || !notificationService) {
      throw Error('Missing mandatory argument: actionsService, translateService or notificationService');
    }
    this.actionsService = actionsService;
    this.translateService = translateService;
    this.notificationService = notificationService;
    this.authenticationService = authenticationService;
    this.instanceRestService = instanceRestService;
    this.promiseAdapter = promiseAdapter;
  }

  execute(action, context) {
    let notification = this.translateService.translateInstant(this.getNotificationLabelId());
    this.notificationService.info(notification);
  }

  exportPDF(action, context) {
    let actionPayload = this.buildActionPayload(action, context.currentObject, action.action);
    return this.getInstanceObjectTitle(context.currentObject).then((title) => {
      actionPayload['fileName'] = title;
      return this.actionsService.exportPDF(context.currentObject.getId(), context.activeTabId, actionPayload, {skipInterceptor: this.skipHttpInterceptor}).then((result) => {
        return this.afterExportHandler(result);
      }).catch((rejection) => {
        if (this.skipHttpInterceptor(rejection)) {
          this.notificationService.error(this.translateService.translateInstant('export.timeout.error.message'));
        }
      });
    });
  }

  exportWord(action, context) {
    let actionPayload = this.buildActionPayload(action, context.currentObject, action.action);
    return this.getInstanceObjectTitle(context.currentObject).then((title) => {
      actionPayload['file-name'] = title;
      return this.actionsService.exportWord(context.currentObject.getId(), context.activeTabId, actionPayload, {skipInterceptor: this.skipHttpInterceptor}).then((result) => {
        return this.afterExportHandler(result);
      }).catch((rejection) => {
        if (this.skipHttpInterceptor(rejection)) {
          this.notificationService.error(this.translateService.translateInstant('export.timeout.error.message'));
        }
      });
    });
  }

  getInstanceObjectTitle(instanceObject) {
    let title = _.get(instanceObject.getModels(), 'validationModel.title.value');
    if (title) {
      return this.promiseAdapter.resolve(title);
    } else {
      return this.instanceRestService.load(instanceObject.id).then((response) => {
        return _.get(response.data, 'properties.title');
      });
    }
  }

  skipHttpInterceptor(rejection) {
    return _.get(rejection, 'data.code') === ErrorCodes.TIMEOUT;
  }

  /**
   * Get the active tab from UI because of the case where the idoc is opened trough a link and there is no url fragment
   * pointing to the default opened tab. In this case the printTab action would print the whole document instead of the
   * default opened tab. If sometimes we decide to set the tab id fragment in the url once document is loaded we can
   * then get the active tab id from the TabsConfig from the IdocPage in context attribute.
   * @returns the id of the current active tab
   */
  getActiveTabId() {
    let activeTabId = $('.idoc-tabs .active .tab-elements-wrapper').attr('data-target');
    if (activeTabId) {
      activeTabId = activeTabId.slice('#tab-'.length);
      return activeTabId;
    }
  }

  downloadFile(response) {
    let iframe = document.createElement('iframe');
    iframe.id = 'downloadDocumentFrame';
    iframe.style.display = 'none';
    document.body.appendChild(iframe);
    iframe.src = this.createDownloadURL(response.data);
  }

  downloadBlob(response, fileName) {
    let blob = new Blob([response.data], {
      type: 'application/pdf'
    });
    FileSaver.saveAs(blob, fileName);
  }

  createDownloadURL(fileName) {
    let downloadURI = BASE_PATH + fileName;
    downloadURI += UrlUtils.getParamSeparator(downloadURI);
    downloadURI += AuthenticationService.TOKEN_REQUEST_PARAM + '=' + this.authenticationService.getToken();
    return downloadURI;
  }

  /**
   * Checks for system content by currently selected tab. In cases when some operation(under tab context) need to be
   * restricted for system content.
   * @param context the context on which the operation will be performed
   * @returns {boolean} true if selected tab has system content
   */
  hasSystemContent(context) {
    let config = context.idocPageController.tabsConfig;
    if (config) {
      let tab = config.getActiveTab();
      return tab && tab.system;
    }
    return false;
  }
}