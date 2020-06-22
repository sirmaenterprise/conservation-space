import {Component, View, Inject} from 'app/app';
import 'administration/model-import/model-import';
import {FileUploadIntegration} from 'file-upload/file-upload-integration';
import {UrlUtils} from 'common/url-utils';

import template from './model-import-bootstrap.html!text';

@Component({
  selector: 'model-import-bootstrap'
})
@View({
  template
})
@Inject(FileUploadIntegration)
export class ModelImportBootstrap {

  constructor(fileUploadIntegration) {
    this.fileUploadIntegration = fileUploadIntegration;

    let hash = '?' + window.location.hash.substring(2);
    let shouldFail = UrlUtils.getParameter(hash, 'fail');
    let hasErrors = UrlUtils.getParameter(hash, 'hasErrors');

    if (shouldFail === 'true') {
      let errors = hasErrors === 'true' ? ['Something went wrong', 'Another thing went wrong'] : undefined;

      this.fileUploadIntegration.switchToFail({
        messages: errors
      });
    } else {
      this.fileUploadIntegration.switchToSuccess();
    }
  }

}