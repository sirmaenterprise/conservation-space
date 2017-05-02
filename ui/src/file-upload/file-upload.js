import {View, Component, Inject, NgElement, NgScope} from 'app/app';
import {Configurable} from 'components/configurable';
import 'jquery-file-upload/js/vendor/jquery.ui.widget';
import 'jquery-file-upload';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import {ContentRestService} from 'services/rest/content-service';
import {InstanceRestService} from 'services/rest/instance-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {RestClient} from 'services/rest-client';
import uuid from 'common/uuid';
import {UserActivityEvent} from 'layout/session/user-activity-event';
import fileUploadTemplate from './file-upload.html!text';
import './file-upload.css!';
import './file-item';

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
@Inject(NgScope, NgElement, ContentRestService, InstanceRestService, RestClient, Eventbus, WindowAdapter)
export class FileUpload extends Configurable {

  constructor($scope, element, contentRestService, instanceRestService, restClient, eventbus, windowAdapter) {
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
      this.config.url = instanceRestService.getContentUploadUrl(this.config.id);
      this.existingEntity = true;
    }

    this.element = element;
    this.entries = [];
    this.$scope = $scope;
    this.eventbus = eventbus;
    this.windowAdapter = windowAdapter;
  }

  ngAfterViewInit() {
    var _this = this;
    var element = this.element;
    this.fileCount = 0;

    element.fileupload({
      add: function (e, data) {
        if (_this.config.single) {
          _this.entries = [];
        }

        var file = data.files[0];
        // used to identify entries in the callback functions
        file.id = uuid();

        _this.entries.push({
          file: file,
          uploadControl: data
        });

        _this.$scope.$digest();
        _this.fileCount = _this.entries.length;
      },
      start: _this.onUploadStarted.bind(_this),
      always: _this.onUploadCompleted.bind(_this),
      progress: function (e, data) {
        var progress = parseInt(data.loaded / data.total * 90, 10);
        var id = data.files[0].id;

        _this.updateProgressBar(id, progress)
      },
      url: this.config.url,
      type: _this.config.method,
      headers: this.config.headers,
      dataType: 'json'
    });

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
   * @param file the file which validity is changed
   * @param valid true if the file is valid
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
    this.onValidityChange({
      event: {
        valid: this.validEntries.count > 0
      }
    });
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
  }

  updateProgressBar(fileId, progress) {
    var progressBar = $('#' + fileId).find('.progress-bar');
    var percentage = progress + '%';

    progressBar.css('width', percentage);
    progressBar.text(percentage);
  }

  remove(entry) {
    var index = this.entries.indexOf(entry);
    this.entries.splice(index, 1);
    this.fileCount = this.entries.length;
  }

  ngOnDestroy() {
    if (this.intervalId) {
      this.windowAdapter.window.clearInterval(this.intervalId);
    }
  }
}