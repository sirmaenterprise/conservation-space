import {View, Component, Inject} from 'app/app';
import {ModelManagementLanguageService} from 'administration/model-management/services/utility/model-management-language-service';

import 'filters/to-trusted-html';

import template from './model-language-select.html!text';

/**
 * Component responsible for displaying available system languages and allowing to change between them. It fires
 * languageChanged event with the selected language as payload.
 *
 * @author svelikov
 */
@Component({
  selector: 'model-language-select',
  properties: {
    'language': 'language',
    'languages': 'languages'
  },
  events: ['onLanguageChanged']
})
@View({
  template
})
@Inject(ModelManagementLanguageService)
export class ModelLanguageSelect {

  constructor(modelManagementLanguageService) {
    this.modelManagementLanguageService = modelManagementLanguageService;
  }

  ngOnInit() {
    this.availableLanguages = [];
    return this.modelManagementLanguageService.getLanguages().then((languages) => {
      languages = languages.concat(this.languages || []);
      this.availableLanguages = ModelManagementLanguageService.transformLanguages(languages);
    });
  }

  onLanguageSelected(language) {
    return this.onLanguageChanged({language});
  }

  getSelectedLanguage() {
    return this.availableLanguages[this.language];
  }
}