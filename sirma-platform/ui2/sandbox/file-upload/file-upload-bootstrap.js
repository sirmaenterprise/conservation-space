import {Component, View, Inject} from 'app/app';
import 'create/file-upload-panel';
import {UrlUtils} from 'common/url-utils';
import {FileUploadIntegration} from 'file-upload/file-upload-integration';
import {InstanceRestService} from 'services/rest/instance-service';
import template from './template.html!text';

@Component({
  selector: 'file-upload-bootstrap'
})
@View({
  template: template
})
@Inject(FileUploadIntegration, InstanceRestService)
class FileUploadBootstrap {

  constructor(fileUploadIntegration, instanceRestService) {
    var hash = '?' + window.location.hash.substring(2);
    var fail = UrlUtils.getParameter(hash, 'fail');
    var multiple = UrlUtils.getParameter(hash, 'multiple');
    var timeout = parseInt(UrlUtils.getParameter(hash, 'timeout'));
    var id = UrlUtils.getParameter(hash, 'id');
    var instanceType = UrlUtils.getParameter(hash, 'instanceType');
    var predefined = UrlUtils.getParameter(hash, 'predefined');

    if (multiple === undefined) {
      multiple = false;
    }

    if (timeout) {
      fileUploadIntegration.setTimeout(timeout);
    } else {
      fileUploadIntegration.setTimeout(1000);
    }

    if (fail === 'true') {
      fileUploadIntegration.switchToFail();
    } else {
      fileUploadIntegration.switchToSuccess();
    }

    this.config = {
      maxFileSize: 2000,
      single: !multiple,
      onValidityChange: function () {

      },
      formConfig: {
        models: {
          instanceType: instanceType
        }
      }
    };

    if (predefined) {
      var data = new Blob(['123456'], {type: 'text/plain'});
      var file = new File([data], 'test.txt');
      this.config.fileObject = file;
    }

    this.config.id = id;
    this.config.header = '<span>Uploaded file.txt</span>';
  }
}