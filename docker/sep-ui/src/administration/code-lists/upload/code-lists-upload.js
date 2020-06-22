import {Component, Inject, NgElement, NgScope, View} from 'app/app';
import {CodelistRestService} from 'services/rest/codelist-service';
import {RestClient} from 'services/rest-client';
import {AuthenticationService} from 'security/authentication-service';
import {AUTHORIZATION} from 'services/rest/http-headers';
import {DialogService} from 'components/dialog/dialog-service';
import {TranslateService} from 'services/i18n/translate-service';
import {FileUploadIntegration} from 'file-upload/file-upload-integration';
import 'jquery-file-upload/js/vendor/jquery.ui.widget';
import 'jquery-file-upload';

import './code-lists-upload.css!css';
import template from './code-lists-upload.html!text';

/**
 * Administration component to choose a file with code lists and to overwrite or update the existing set in the system.
 *
 * In successful upload invokes the <code>onUpload</code> component event.
 *
 * @author Mihail Radkov
 */
@Component({
  selector: 'seip-code-lists-upload',
  events: ['onUpload']
})
@View({
  template
})
@Inject(NgScope, NgElement, DialogService, TranslateService, CodelistRestService, RestClient, FileUploadIntegration, AuthenticationService)
export class CodeListsUpload {

  constructor($scope, $element, dialogService, translateService, codelistRestService, restClient, fileUploadIntegration, authenticationService) {
    this.$scope = $scope;
    this.$element = $element;
    this.dialogService = dialogService;
    this.translateService = translateService;
    this.codelistRestService = codelistRestService;
    this.restClient = restClient;
    this.fileUploadIntegration = fileUploadIntegration;
    this.authenticationService = authenticationService;
  }

  ngOnInit() {
    this.headers = this.restClient.config.headers;
    this.urls = {
      update: this.codelistRestService.getUpdateServiceUrl(),
      overwrite: this.codelistRestService.getOverwriteServiceUrl()
    };
    this.messages = {
      update: this.translateService.translateInstant('code.lists.upload.update.confirm'),
      overwrite: this.translateService.translateInstant('code.lists.upload.overwrite.confirm'),
      success: this.translateService.translateInstant('code.lists.upload.success')
    };
  }

  ngAfterViewInit() {
    this.$element.fileupload({
      add: (event, data) => {
        this.uploadControl = data;
        // Fileupload is not within angular's scope so we need to call the digest explicitly
        this.$scope.$digest();
      },
      type: 'POST',
      url: this.urls.overwrite,
      headers: this.headers
    });

    this.$element.find('.select-file-btn').click(() => {
      this.$element.find('.file-upload-field').click();
    });
  }

  overwrite() {
    this.setUploadUrl(this.urls.overwrite);
    this.upload(this.messages.overwrite);
  }

  update() {
    this.setUploadUrl(this.urls.update);
    this.upload(this.messages.update);
  }

  upload(message) {
    this.dialogService.confirmation(message, undefined, {
      buttons: [
        this.dialogService.createButton(DialogService.YES, 'dialog.button.yes', true),
        this.dialogService.createButton(DialogService.NO, 'dialog.button.no')
      ],
      onButtonClick: (buttonId, componentScope, dialogConfig) => {
        if (buttonId === DialogService.YES) {
          this.startUpload();
        }
        dialogConfig.dismiss();
      }
    });
  }

  clearUploadState() {
    this.uploading = false;
    if (this.uploadControl) {
      this.uploadControl.files = [];
    }
  }

  startUpload() {
    this.uploading = true;
    delete this.uploadMessage;

    this.addAuthHeader().then(() => {
      this.fileUploadIntegration.submit(this.uploadControl).done(() => {
        this.error = false;
        this.uploadMessage = this.messages.success;
        this.notifyOnUpload();
      }).fail((error) => {
        this.error = true;
        let message = error.responseJSON.message;
        this.uploadMessage = Array.isArray(message) ? message : [message];
      }).always(() => {
        this.clearUploadState();
        // Fileupload is not within angular's scope so we need to call the digest explicitly
        this.$scope.$digest();
      });
    });
  }

  addAuthHeader() {
    return this.authenticationService.buildAuthHeader().then(authHeaderValue => {
      this.uploadControl.headers = this.uploadControl.headers || {};
      this.uploadControl.headers[AUTHORIZATION] = authHeaderValue;
      return true;
    });
  }

  notifyOnUpload() {
    if (this.onUpload) {
      this.onUpload();
    }
  }

  setUploadUrl(url) {
    this.$element.fileupload('option', 'url', url);
  }

  hasSelectedFiles() {
    return !!this.uploadControl && this.uploadControl.files.length > 0;
  }

  getSelectedFile() {
    return this.uploadControl.files[0].name;
  }

  ngOnDestroy() {
    if (this.$element.data('blueimp-fileupload')) {
      this.$element.fileupload('destroy');
    }
  }
}