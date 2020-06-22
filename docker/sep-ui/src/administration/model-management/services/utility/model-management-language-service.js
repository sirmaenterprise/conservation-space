import {Inject, Injectable} from 'app/app';
import {Eventbus} from 'services/eventbus/eventbus';
import {Configuration} from 'common/application-config';
import {CodelistRestService} from 'services/rest/codelist-service';
import {TranslateService} from 'services/i18n/translate-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';

import {ConfigurationsUpdateEvent} from 'common/configuration-events';
import {LanguageChangeSuccessEvent} from 'services/i18n/language-change-success-event';
import {CodeListSavedEvent} from 'administration/code-lists/services/events/code-list-saved-event';

import {CODELISTS} from 'administration/code-lists/codelist-constants';
import _ from 'lodash';

const DEFAULT_LANGUAGE = 'en';

/**
 * Utility service aiming at providing services related to managing and accessing languages and
 * language settings. This service provides means of extracting current user, system or default
 * languages.
 *
 * @author Svetlozar Iliev
 */
@Injectable()
@Inject(Eventbus, TranslateService, Configuration, CodelistRestService, PromiseAdapter)
export class ModelManagementLanguageService {

  constructor(eventbus, translateService, configuration, codelistRestService, promiseAdapter) {
    this.subscriptions = [];

    this.eventbus = eventbus;
    this.configuration = configuration;
    this.promiseAdapter = promiseAdapter;
    this.translateService = translateService;
    this.codelistRestService = codelistRestService;

    this.initializeUserLanguages();
    this.initializeSystemLanguages();

    this.subscribeToLanguageAddition();
    this.subscribeToUserLanguageChange();
    this.subscribeToSystemLanguageChange();
  }

  initializeUserLanguages() {
    delete this.availableLanguages;
    this.userLanguage = ModelManagementLanguageService.transformLanguage(this.translateService.getCurrentLanguage());
  }

  initializeSystemLanguages() {
    delete this.availableLanguages;
    this.systemLanguage = ModelManagementLanguageService.transformLanguage(this.configuration.get(Configuration.SYSTEM_LANGUAGE));
  }

  initializeAvailableLanguages(codeList) {
    let languages = codeList.values.map(value => this.getLanguageEntry(value));
    // merge the incoming transformed code list values with the existing languages to create final mapping
    this.availableLanguages = _.uniq(languages.concat(this.availableLanguages), false, lang => lang.value);
  }

  subscribeToUserLanguageChange() {
    this.subscriptions.push(this.eventbus.subscribe(LanguageChangeSuccessEvent, () => this.initializeUserLanguages()));
  }

  subscribeToSystemLanguageChange() {
    this.subscriptions.push(this.eventbus.subscribe(ConfigurationsUpdateEvent, () => this.initializeSystemLanguages()));
  }

  subscribeToLanguageAddition() {
    this.subscriptions.push(this.eventbus.subscribe(CodeListSavedEvent, args => {
      let codeList = args[0];
      // code list should be of proper type and languages were already pre-loaded
      if (codeList.value === CODELISTS.CL_LANGUAGES && this.availableLanguages) {
        this.initializeAvailableLanguages(codeList);
      }
    }));
  }

  getUserLanguage() {
    return this.userLanguage;
  }

  getSystemLanguage() {
    return this.systemLanguage;
  }

  getDefaultLanguage() {
    return DEFAULT_LANGUAGE;
  }

  getLanguages() {
    if (!this.availableLanguages) {
      return this.codelistRestService.getCodeList(CODELISTS.CL_LANGUAGES).then(response => {
        this.availableLanguages = response.data.values.map(value => this.getLanguageEntry(value));
        return this.availableLanguages;
      });
    }
    return this.promiseAdapter.resolve(this.availableLanguages);
  }

  getLanguageEntry(code) {
    // compute the code of the entry and it's label based on preset languages
    let lang = ModelManagementLanguageService.transformLanguage(code.value);
    let descriptions = _.transform(code.descriptions, (result, val) => {
      let codeLang = ModelManagementLanguageService.transformLanguage(val.language);
      result[codeLang] = val.name;
    }, {});
    let label = this.getApplicableDescription((locale) => descriptions[locale]);

    // create a language entry from id and label
    return {value: lang, label: label || lang};
  }

  getApplicableDescription(extractor) {
    return extractor(this.getSystemLanguage()) || extractor(this.getUserLanguage()) || extractor(this.getDefaultLanguage());
  }

  static transformLanguage(lang) {
    return lang && lang.toLowerCase();
  }

  /**
   * Transform an array of languages from the internal format represented by {@link getLanguageEntry} or a simple string
   * entry representing the language key to a unified map format consisting of the language code as a key and language
   * label as a value mapped to this key. The method also removes all duplicates based on the language keys if any are
   * present.
   *
   * @param languages - languages array
   * @returns - map of languages mapped by language code and label as value
   */
  static transformLanguages(languages) {
    languages = _.uniq(languages, lang => lang.value || lang);
    return _.transform(languages, (result, lang) => result[lang.value || lang] = lang.label || lang, {});
  }
}