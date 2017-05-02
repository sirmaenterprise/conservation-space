import {Component, View, Inject} from 'app/app';
import 'create/file-upload-panel';
import template from './template.html!text';
import {UrlUtils} from 'common/url-utils';
import {FileUploadIntegration} from 'file-upload/file-upload-integration';

@Component({
  selector: 'file-upload-bootstrap'
})
@View({
  template: template
})
@Inject(FileUploadIntegration)
class FileUploadBootstrap {

  constructor(fileUploadIntegration) {
    var hash = '?' + window.location.hash.substring(2);
    var fail = UrlUtils.getParameter(hash, 'fail');
    var multiple = UrlUtils.getParameter(hash, 'multiple');
    var timeout = parseInt(UrlUtils.getParameter(hash, 'timeout'));
    var id = UrlUtils.getParameter(hash, 'id');

    if (multiple === undefined) {
      multiple = false;
    }

    if (fail === 'true') {
      fileUploadIntegration.swithToFail();
    } else {
      fileUploadIntegration.switchToSuccess();
    }

    if (timeout) {
      fileUploadIntegration.setTimeout(timeout);
    } else {
      fileUploadIntegration.setTimeout(1000);
    }

    this.config = {
      maxFileSize: 2000,
      single: !multiple,
      onValidityChange: function () {

      }
    };

    this.config.id = id;
    this.config.header = '<span>Uploaded file.txt</span>';
  }
}