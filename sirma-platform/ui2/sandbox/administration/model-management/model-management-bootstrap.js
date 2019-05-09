import {Component, View, Inject} from 'app/app';
import {UrlUtils} from 'common/url-utils';
import {Configuration} from 'common/application-config';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {TranslateService} from 'services/i18n/translate-service';

import {ModelManagementRestService} from './model-management-rest-service.stub';
import {ModelActionFactory} from 'administration/model-management/actions/model-action-factory';
import {ModelActionProcessor} from 'administration/model-management/actions/model-action-processor';

import 'administration/model-management/model-management';
import template from './model-management-bootstrap.html!text';

@Component({
  selector: 'model-management-bootstrap'
})
@View({
  template
})
@Inject(PromiseAdapter, TranslateService, Configuration, ModelManagementRestService, ModelActionFactory, ModelActionProcessor)
export class ModelManagementBootstrap {

  constructor(promiseAdapter, translateService, configuration, modelManagementRestService, modelActionFactory, modelActionProcessor) {
    this.configuration = configuration;
    this.promiseAdapter = promiseAdapter;
    this.translateService = translateService;

    this.modelActionFactory = modelActionFactory;
    this.modelActionProcessor = modelActionProcessor;
    this.modelManagementRestService = modelManagementRestService;
  }

  ngOnInit() {
    this.loadActionProcessorsAndFactories();
    let hash = '?' + window.location.hash.substring(1);

    let userLang = UrlUtils.getParameter(hash, 'userLang');
    if (userLang) {
      this.translateService.changeLanguage(userLang);
    }

    let systemLang = UrlUtils.getParameter(hash, 'systemLang');
    if (systemLang) {
      this.configuration.configs[Configuration.SYSTEM_LANGUAGE] = systemLang;
    }

    let saveStatus = UrlUtils.getParameter(hash, 'saveStatus');
    this.modelManagementRestService.setSaveStatus(saveStatus);

    let publishStatus = UrlUtils.getParameter(hash, 'publishStatus');
    this.modelManagementRestService.setPublishStatus(publishStatus);
  }

  loadActionProcessorsAndFactories() {
    return this.promiseAdapter.all([
      this.modelActionFactory.loadActionFactories(),
      this.modelActionProcessor.loadActionProcessors()
    ]).then(() => this.render = true);
  }
}
