import {Component, Inject, View} from 'app/app';
import 'administration/code-lists/code-lists';
import {FileUploadIntegration} from 'file-upload/file-upload-integration';
import {UrlUtils} from 'common/url-utils';
import {TranslateService} from 'services/i18n/translate-service';
import {Configuration} from 'common/application-config';

import template from './code-lists-bootstrap.html!text';

@Component({
  selector: 'code-lists-bootstrap'
})
@View({
  template
})
@Inject(FileUploadIntegration, TranslateService, Configuration)
export class CodeListsBootstrap {

  constructor(fileUploadIntegration, translateService, configuration) {
    this.fileUploadIntegration = fileUploadIntegration;
    this.translateService = translateService;
    this.configuration = configuration;
  }

  ngOnInit() {
    this.render = true;
    let hash = '?' + window.location.hash.substring(1);
    let shouldFail = UrlUtils.getParameter(hash, 'fail');

    if (shouldFail === 'true') {
      this.fileUploadIntegration.switchToFail({
        message: 'Error!'
      });
    } else {
      this.fileUploadIntegration.switchToSuccess({
        message: 'Success!'
      });
    }

    let userLang = UrlUtils.getParameter(hash, 'userLang');
    if (userLang) {
      this.translateService.changeLanguage(userLang);
    }

    let systemLang = UrlUtils.getParameter(hash, 'systemLang');
    if (systemLang) {
      this.configuration.configs[Configuration.SYSTEM_LANGUAGE] = systemLang;
    }

    // Force paging
    this.configuration.configs[Configuration.SEARCH_PAGE_SIZE] = 3;
  }
}
