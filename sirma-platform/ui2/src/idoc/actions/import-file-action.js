import {Injectable, Inject} from 'app/app';
import {TranslateService} from 'services/i18n/translate-service';
import {NotificationService} from 'services/notification/notification-service';
import {ActionHandler} from 'services/actions/action-handler';
import {DialogService} from 'components/dialog/dialog-service';
import {ImportService} from 'services/rest/import-service';
import {InstanceRestService} from 'services/rest/instance-service';
import {ContextSelector} from 'components/contextselector/context-selector';
import {Configuration} from 'common/application-config';
import {BASE_PATH} from 'services/rest-client';
import {AuthenticationService} from 'services/security/authentication-service';

@Injectable()
@Inject(TranslateService, NotificationService, DialogService, ImportService, InstanceRestService, Configuration, AuthenticationService)
export class ImportFileAction extends ActionHandler {
  constructor(translateService, notificationService, dialogService, importService, instanceRestService, configurationService, authenticationService) {
    super();
    this.translateService = translateService;
    this.notificationService = notificationService;
    this.dialogService = dialogService;
    this.importService = importService;
    this.instanceRestService = instanceRestService;
    this.configurationService = configurationService;
    this.authenticationService = authenticationService;
  }

  execute(action, context) {
    this.disabled = true;
    this.context = context;
    let config = { params: { properties: ['hasParent'] } };
    return this.instanceRestService.load(context.currentObject.id, config).then((data) => {
      this.confirmFileValidation(this.provideContext(data.data));
    });
  }

  confirmFileValidation(currentObjectId) {
    this.loadDTJavaDynamically();
    var buttons = [
      { id: DialogService.OK, label: 'upload.content', cls: 'btn-warning', dismiss: false, disabled: false },
      { id: DialogService.CONFIRM, label: 'dialog.button.validatefile', cls: 'btn-primary', dismiss: true, disabled: false },
      { id: DialogService.CANCEL, label: 'dialog.button.cancel', dismiss: true }
    ];

    var componentProperties = {
      config: {
        parentId: currentObjectId
      }
    };

    var dialogConfig = {
      modalCls: 'file-import-dialog',
      header: 'dialog.header.select.context',
      largeModal: false,
      buttons: buttons,
      onButtonClick: (buttonId, componentScope, dialogConfig) => {
        if (buttonId === DialogService.OK) {
          this.launchApplication();
        }
        else if (buttonId === DialogService.CONFIRM) {
          this.contextId = componentScope.contextSelector.config.parentId;
          this.validateFile();
        }

      }
    };
    this.dialogService.create(ContextSelector, componentProperties, dialogConfig);
  }

  /**
   * Load dtjava.js. We load dtjava.js this way because it is crappy and system.js can't load it.
   */
  loadDTJavaDynamically() {
    let script = document.createElement('script');
    script.src = 'https://java.com/js/dtjava.js';
    document.head.appendChild(script);
  }

  validateFile() {
    this.notificationService.info(this.translateService.translateInstant('import.validation.message'));
    return this.importService.readFile(this.context.currentObject.id, this.contextId).then((response) => {
      if (response.data.data.length > 0) {
        this.disabled = false;
      }
      this.confirmImport(response);
    });
  }

  confirmImport(response) {
    let confirmationMessage = this.translateService.translateInstant('import.file.confirmation.message');
    this.dialogService.confirmation(confirmationMessage + response.data.report.headers.breadcrumb_header, null, {
      buttons: [
        { id: DialogService.CONFIRM, label: this.translateService.translateInstant('dialog.button.importfile'), cls: 'btn-primary', disabled: this.disabled },
        { id: DialogService.CANCEL, label: this.translateService.translateInstant('dialog.button.cancel') }
      ],
      onButtonClick: (buttonID, componentScope, dialogConfig) => {
        if (buttonID === DialogService.CONFIRM) {
          this.notificationService.info(this.translateService.translateInstant('idoc.info.data.import.triggered'));
          this.importService.importFile(this.context.currentObject.id, this.contextId, response.data).then(() => {
            this.notificationService.success(this.translateService.translateInstant('external.operation.success'));
          });
        }
        dialogConfig.dismiss();
      }
    });
  }

  /**
  * Check if the current object has parent and provide it as context.
  */
  provideContext(currentObject) {
    let hasParent = currentObject.properties.hasParent;
    if (hasParent && hasParent.results && hasParent.results.length > 0) {
      return hasParent.results[0];
    }
    return null;
  }

  /**
   * Launches java application through web start (jnlp).
   */
  launchApplication() {
    // let eaiContentTool = this.configurationService.get('eai.spreadsheet.content.tool.path');
    let apiUrl = this.configurationService.get('ui2.url') + BASE_PATH;
    let jwtToken = this.authenticationService.getToken();
    let namedParameters = {};
    let eaiContentTool = apiUrl + '/integration/content/tool/descriptor?jwt=' + jwtToken + '&id=' + this.context.currentObject.id + '&apiUrl=' + apiUrl;
    namedParameters.apiUrl = apiUrl;
    namedParameters.uri = this.context.currentObject.id;
    namedParameters.authorization = jwtToken;
    this.triggerJnlp(namedParameters, eaiContentTool);
  }

  triggerJnlp(params, url) {
    // we launch eai-content-tool through jnlp
    dtjava.launch({
      url: url
    }, {
        // we specify jvm options for eai-content-tool
        javafx: '8.0+'
    },
    {});
  }

}