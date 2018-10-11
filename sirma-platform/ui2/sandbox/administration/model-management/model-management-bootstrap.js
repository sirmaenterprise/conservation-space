import {Component, View, Inject} from 'app/app';
import {UrlUtils} from 'common/url-utils';
import {TranslateService} from 'services/i18n/translate-service';
import {Configuration} from 'common/application-config';

import 'administration/model-management/model-management';

import template from './model-management-bootstrap.html!text';

@Component({
  selector: 'model-management-bootstrap'
})
@View({
  template
})
@Inject(TranslateService, Configuration)
export class ModelManagementBootstrap {

  constructor(translateService, configuration) {
    this.configuration = configuration;
    this.translateService = translateService;
  }

  ngOnInit() {
    this.render = true;
    let hash = '?' + window.location.hash.substring(1);

    let userLang = UrlUtils.getParameter(hash, 'userLang');
    if (userLang) {
      this.translateService.changeLanguage(userLang);
    }

    let systemLang = UrlUtils.getParameter(hash, 'systemLang');
    if (systemLang) {
      this.configuration.configs[Configuration.SYSTEM_LANGUAGE] = systemLang;
    }
  }
}