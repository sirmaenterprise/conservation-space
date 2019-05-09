import {View, Component, Inject, NgElement, NgScope, NgTimeout} from 'app/app';
import application from 'app/app';
import {Configurable} from 'components/configurable';
import 'jquery-file-upload/js/vendor/jquery.ui.widget';
import 'jquery-file-upload';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import {ContentRestService} from 'services/rest/content-service';
import {TranslateService} from 'services/i18n/translate-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {RestClient} from 'services/rest-client';
import {Configuration} from 'common/application-config';
import uuid from 'common/uuid';
import {UserActivityEvent} from 'layout/session/user-activity-event';
import {RefreshWidgetsCommand} from 'idoc/actions/events/refresh-widgets-command';
import './file-item';
import {ADD_UPLOAD_REMAINING_MESSAGE_COMMAND, REMOVE_UPLOAD_REMAINING_MESSAGE_COMMAND} from 'create/file-upload-panel';

import './file-upload.css!';
import fileUploadTemplate from './file-upload.html!text';

/**
 * Provides a form for creating instances with uploaded content.
 * Only document definitions are provided as options (emf:Document).
 *
 * Config:
 *  - single - if true only single file upload is allowed. Default: true
 *  - method - HTTP method. Default: POST
 *  - url - url where to post the data.
 *  - headers - list of headers to be passed to the file upload HTTP request
 *  - maxFileSize - maximum size of the uploaded file (in bytes).
 *  - parentId - id of the entity where the documents will be uploaded as children.
 *  - id - id of the instance to which the file is uploaded. I.e. when uploading a new version.
 *  - header - header of the instance to which the file is uploaded. Mandatory if the "id" config property is provided.
 *  - fileUploadedCallback - callback triggered when a new file has been uploaded
 */
@Component({
  selector: 'seip-file-upload',
  properties: {
    'config': 'config'
  },
  events: ['onValidityChange']
})
@View({
  template: fileUploadTemplate
})
@Inject(NgScope, NgElement, ContentRestService, TranslateService, RestClient, Eventbus, WindowAdapter, Configuration, NgTimeout)
export class FileUpload extends Configurable {

  constructor($scope, element, contentRestService, translateService, restClient, eventbus, windowAdapter, configuration, $timeout) {
    super({
      method: 'POST',
      url: contentRestService.getServiceUrl(),
      headers: restClient.config.headers
    });

    // Handles all the entries' current state and the count of the valid entries.
    this.validEntries = {
      count: 0
    };

    // force single upload when entity id is provided
    if (this.config.id) {
      this.config.single = true;
      this.existingEntity = true;
    }

    this.element = element;
    this.entries = [];
    this.$scope = $scope;
    this.eventbus = eventbus;
    this.windowAdapter = windowAdapter;
    this.configuration = configuration;
    this.translateService = translateService;
    this.filesToUploadLater = [];
    this.$timeout = $timeout;
  }

  ngAfterViewInit() {
    let _this = this;
    let element = this.element;
    this.fileCount = 0;
    this.currentlyUploading = 0;
    this.filesUploaded = false;
    this.maxNumberOfFiles = this.configuration.get(Configuration.UPLOAD_MAX_SIMULTANEOUS_NUMBER_FILES);

    element.fileupload({
      add(e, data) {
        if (_this.currentlyUploading >= _this.maxNumberOfFiles) {
          _this.filesToUploadLater.push(data.files[0]);

          // timeout is used to notify angular about the messages was changed outside of it
          _this.$timeout(() => {
            _this.refreshRemainingFilesMessage();
          });
          return;
        }
        if (_this.config.single) {
          _this.entries = [];
        }

        let file = data.files[0];
        // used to identify entries in the callback functions
        file.id = uuid();

        _this.entries.unshift({
          file,
          uploadControl: data
        });

        if (!application.$rootScope.$$phase) {
          _this.$scope.$digest();
        }

        _this.fileCount = _this.entries.length;
        _this.currentlyUploading++;
      },
      start: _this.onUploadStarted.bind(_this),
      always: _this.onUploadCompleted.bind(_this),
      progress(e, data) {
        let progress = parseInt(data.loaded / data.total * 90, 10);
        let id = data.files[0].id;

        _this.updateProgressBar(id, progress);
      },
      url: _this.config.url,
      type: _this.config.method,
      headers: _this.config.headers,
      dataType: 'json',
      limitConcurrentUploads: _this.configuration.get(Configuration.UPLOAD_MAX_CONCURRENT_REQUESTS),
      singleFileUploads: true
    });

    if (this.config.files) {
      this.element.fileupload('add', {files: this.config.files});
    }

    if (this.config.fileObject) {
      let data = {};
      data.files = [this.config.fileObject];
      element.fileupload('add', data);
      $('.select-files-button').remove();
      $('.remove-button').remove();
    }

    element.on('remove', () => {
      if (element.data('blueimp-fileupload')) {
        element.fileupload('destroy');
      }
    });

    if (!this.config.single) {
      element.find('.file-upload-field').attr('multiple', '');
    }

    element.find('.select-files-button').click(function () {
      element.find('.file-upload-field').click();
    });

  }

  /**
   * If the entry had been valid before the change and now isn't reduces the count of the valid entries.
   * If it had been invalid and now is valid increases the count of the entries.
   *
   * @param event that holds the file which validity is changed and a flag which is true if the file is valid
   */
  onValidityChanged(event) {
    let file = event.file;
    let valid = event.valid;
    if (valid) {
      if (!this.validEntries[file.id]) {
        this.validEntries[file.id] = true;
        this.validEntries.count++;
      }
    } else {
      if (this.validEntries[file.id]) {
        this.validEntries[file.id] = false;
        this.validEntries.count--;
      }
    }

    this.refreshRemainingFilesMessage();

    this.onValidityChange({
      event: {
        valid: this.validEntries.count > 0
      }
    });
  }

  callOnFileUploadedCallback(event) {
    let uploadedCallback = this.config.fileUploadedCallback;

    if (uploadedCallback) {
      uploadedCallback(event.instance);
    }
  }

  onUploadStarted() {
    if (!this.intervalId) {
      this.intervalId = this.windowAdapter.window.setInterval(() => {
        this.eventbus.publish(new UserActivityEvent());
      }, 1000);
    }
  }

  onUploadCompleted() {
    if (--this.fileCount === 0) {
      this.windowAdapter.window.clearInterval(this.intervalId);
    }

    --this.currentlyUploading;
    this.filesUploaded = true;

    this.processWithFilesToUploadLater(() => {
      let remainingFiles = this.filesToUploadLater;
      this.filesToUploadLater = [];
      this.element.fileupload('add', {files: remainingFiles});
    });

    this.refreshRemainingFilesMessage();
  }

  updateProgressBar(fileId, progress) {
    let progressBar = $('#' + fileId).find('.progress-bar');
    let percentage = progress + '%';

    progressBar.css('width', percentage);
    progressBar.text(percentage);
  }

  remove(entry) {
    let index = this.entries.indexOf(entry);
    this.entries.splice(index, 1);
    this.fileCount = this.entries.length;
    this.currentlyUploading--;
    this.processWithFilesToUploadLater(() => {
      this.element.fileupload('add', {files: this.filesToUploadLater.shift()});
    });
    this.refreshRemainingFilesMessage();
  }

  refreshRemainingFilesMessage() {
    this.config.eventEmitter.publish(REMOVE_UPLOAD_REMAINING_MESSAGE_COMMAND, this.remainingFilesMessage);

    this.processWithFilesToUploadLater(() => {
      this.remainingFilesMessage = this.translateService.translateInstantWithInterpolation('fileupload.maxNumberOfFiles', {
        limit: this.maxNumberOfFiles,
        remaining: this.filesToUploadLater.length
      });

      this.config.eventEmitter.publish(ADD_UPLOAD_REMAINING_MESSAGE_COMMAND, this.remainingFilesMessage);
    });
  }

  processWithFilesToUploadLater(callback) {
    if (this.filesToUploadLater.length > 0) {
      callback();
    }
  }

  ngOnDestroy() {
    if (this.intervalId) {
      this.windowAdapter.window.clearInterval(this.intervalId);
    }
    if (this.filesUploaded) {
      if (this.config.purpose && this.config.purpose.indexOf('upload') !== -1) {
        this.eventbus.publish(new RefreshWidgetsCommand());
      }

      if (this.config.userOperation && (this.config.userOperation === 'uploadNewVersion' || this.config.userOperation === 'uploadRevision')) {
        this.config.onClosed();
      }
    }
  }
}
