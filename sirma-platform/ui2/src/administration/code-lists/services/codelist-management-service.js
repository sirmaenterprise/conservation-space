import {Inject, Injectable} from 'app/app';
import {CodelistRestService} from 'services/rest/codelist-service';
import {Configuration} from 'common/application-config';
import {TranslateService} from 'services/i18n/translate-service';
import _ from 'lodash';

const DEFAULT_LANGUAGE = 'EN';
const LANGUAGE_CODE_LIST = '13';

/**
 * Administration service for managing controlled vocabularies in the system.
 *
 * Responsible for converting code lists into proper structure for the web application.
 *
 * @author Mihail Radkov
 */
@Injectable()
@Inject(CodelistRestService, Configuration, TranslateService)
export class CodelistManagementService {

  constructor(codelistRestService, configuration, translateService) {
    this.codelistRestService = codelistRestService;
    this.configuration = configuration;
    this.translateService = translateService;

    this.languagesCache = [];
  }

  getCodeLists() {
    return this.codelistRestService.getCodeLists().then((response) => {
      let codeLists = response.data.map(codeList => this.transformCodeList(codeList));
      this.updateLanguagesCache(codeLists);
      this.assignDescription(codeLists);
      this.sortCodeLists(codeLists);
      return codeLists;
    });
  }

  saveCodeList(oldCodeList, newCodeList) {
    let transformed = this.transformCodeBack(newCodeList);

    let updatedValues = this.getUpdatedValues(oldCodeList, newCodeList);
    transformed.values = updatedValues.map(value => this.transformCodeBack(value));

    return this.codelistRestService.saveCodeList(transformed);
  }

  exportCodeLists() {
    return this.codelistRestService.exportCodeLists().then(response => {
      return {
        data: response.data,
        status: response.status
      };
    });
  }

  createCodeList() {
    let codeList = this.createCode();
    codeList.values = [];
    return codeList;
  }

  createCodeValue(codeList) {
    let codeValue = this.createCode();
    codeValue.codeListValue = codeList.id;
    codeValue.active = true;
    return codeValue;
  }

  transformCodeList(codeList) {
    let transformed = this.transformCode(codeList);
    transformed.values = codeList.values || [];
    transformed.values = transformed.values.map(codeValue => this.transformCode(codeValue));
    return transformed;
  }

  transformCode(code) {
    let transformed = {
      id: code.value,
      descriptions: this.mapDescriptions(code),
      extras: this.mapExtras(code)
    };
    this.assignProperty(transformed, code, 'active');
    this.assignProperty(transformed, code, 'codeListValue');
    return transformed;
  }

  transformCodeBack(code) {
    let transformed = {
      value: code.id,
      descriptions: Object.values(code.descriptions).map(descr => this.transformDescriptionBack(descr)),
      extra1: code.extras['1'],
      extra2: code.extras['2'],
      extra3: code.extras['3']
    };
    this.assignProperty(transformed, code, 'active');
    this.assignProperty(transformed, code, 'codeListValue');
    return transformed;
  }

  transformLanguage(lang) {
    return lang.toUpperCase();
  }

  mapDescriptions(code) {
    let transformedDescriptions = {};
    code.descriptions.forEach((description) => {
      let language = this.transformLanguage(description.language);
      transformedDescriptions[language] = this.createDescription(language,
        description.name, description.comment);
    });
    return transformedDescriptions;
  }

  transformDescriptionBack(description) {
    return {
      name: description.name,
      comment: description.comment,
      language: description.language
    };
  }

  mapExtras(code) {
    let extras = {};
    extras['1'] = code.extra1 || '';
    extras['2'] = code.extra2 || '';
    extras['3'] = code.extra3 || '';
    return extras;
  }

  /**
   * Assigns property value from source to destination object
   * only if the given property exists in the source object
   *
   * @param destination - object to be assigned to
   * @param source - source object for extraction
   * @param property - property to be assigned
   */
  assignProperty(destination, source, property) {
    if (source[property] !== undefined) {
      destination[property] = source[property];
    }
  }

  updateLanguagesCache(codeLists) {
    let languagesCodeList = _.find(codeLists, list => this.isLanguageCodeList(list));
    if (languagesCodeList) {
      this.languagesCache = languagesCodeList.values.map(v => this.transformLanguage(v.id));
    }
  }

  isLanguageCodeList(codeList) {
    return codeList.id === LANGUAGE_CODE_LIST;
  }

  assignDescription(codeLists) {
    let languages = this.languagesCache;
    let userLanguage = this.transformLanguage(this.translateService.getCurrentLanguage());
    let systemLanguage = this.transformLanguage(this.configuration.get(Configuration.SYSTEM_LANGUAGE));

    codeLists.forEach(codeList => {
      this.assignLanguages(codeList.descriptions, languages);
      codeList.description = this.getSuitableDescription(codeList, userLanguage, systemLanguage);
      if (codeList.values) {
        codeList.values.forEach(value => {
          this.assignLanguages(value.descriptions, languages);
          value.description = this.getSuitableDescription(value, userLanguage, systemLanguage);
        });
      }
    });
  }

  /**
   * Constrains all language entries inside provided code list
   * descriptions based on a list of languages
   *
   * When a language is not present inside the current description
   * an empty description is created for the said language
   *
   * @param descriptions - descriptions for which to assign languages
   * @param languages - list of constraining languages
   */
  assignLanguages(descriptions, languages) {
    let descriptionLanguages = Object.keys(descriptions);
    let addedLanguages = _.difference(languages, descriptionLanguages);
    addedLanguages.forEach(lang => descriptions[lang] = this.createDescription(lang));
  }

  /**
   * Retrieves the most suitable code description based on provided languages
   * When no suitable description for the given languages if found a default
   * language lookup is used {DEFAULT_LANGUAGE}.
   *
   * @param code - from where to extract description
   * @param userLanguage - the user defined language
   * @param systemLanguage - the system defined language
   */
  getSuitableDescription(code, userLanguage, systemLanguage) {
    return this.getDescription(code, userLanguage) || this.getDescription(code, systemLanguage) ||
      this.getDescription(code, DEFAULT_LANGUAGE) || code.descriptions[userLanguage];
  }

  /**
   * Returns the description for a given language if valid
   *
   * @param code - from where to extract description
   * @param language - language of the description
   */
  getDescription(code, language) {
    let description = code.descriptions[language];
    // a valid description is such that exists and has a valid name and name contents
    return description && description.name && description.name.length ? description : undefined;
  }

  createDescription(language, name = '', comment = '') {
    return {
      name,
      comment,
      language
    };
  }

  getUpdatedValues(oldCodeList, newCodeList) {
    if (!oldCodeList) {
      return newCodeList.values;
    }

    return newCodeList.values.filter(newValue => {
      let oldValue = _.find(oldCodeList.values, oldValue => {
        return oldValue.id === newValue.id;
      });
      return !oldValue || !this.compareValues(oldValue, newValue);
    });
  }

  createCode() {
    let code = {
      id: '',
      descriptions: {},
      extras: {
        '1': '',
        '2': '',
        '3': ''
      }
    };
    // Assign empty descriptions & set description reference
    this.assignDescription([code]);
    return code;
  }

  /**
   * Sorts the set of code lists and their values by a code's ID.
   *
   * @param codeLists - the set of codes to sort
   */
  sortCodeLists(codeLists) {
    this.sortCodes(codeLists);
    codeLists.forEach(list => this.sortCodes(list.values));
  }

  /**
   * Sorts codes based on their ID. It properly handles IDs which are numbers and those who are not.
   *
   * @param codes - the codes to sort
   */
  sortCodes(codes) {
    codes.sort((code1, code2) => {
      let firstId = parseInt(code1.id);
      let secondId = parseInt(code2.id);

      if (isNaN(firstId) || isNaN(secondId)) {
        return code1.id.localeCompare(code2.id);
      }
      return firstId - secondId;
    });
  }

  areCodeListsEqual(sourceCodeList, targetCodeList) {
    return this.areCodeFieldsEqual(sourceCodeList, targetCodeList) && this.areValuesEqual(sourceCodeList, targetCodeList);
  }

  areCodeFieldsEqual(sourceCode, targetCode) {
    return sourceCode.id === targetCode.id
      && _.isEqual(sourceCode.descriptions, targetCode.descriptions)
      && _.isEqual(sourceCode.extras, targetCode.extras);
  }

  areValuesEqual(sourceCodeList, targetCodeList) {
    if (sourceCodeList.values.length !== targetCodeList.values.length) {
      return false;
    }

    // Find at least one pair that is not equal for early break
    return !sourceCodeList.values.some((v, index) => {
      let sourceValue = sourceCodeList.values[index];
      let targetValue = targetCodeList.values[index];
      return !this.compareValues(sourceValue, targetValue);
    });
  }

  compareValues(sourceValue, targetValue) {
    return this.areCodeFieldsEqual(sourceValue, targetValue) && sourceValue.active === targetValue.active;
  }

}
