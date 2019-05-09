import {Inject, Injectable} from 'app/app';
import {Eventbus} from 'services/eventbus/eventbus';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {CodelistRestService} from 'services/rest/codelist-service';

import {ModelList} from 'administration/model-management/model/model-list';
import {ModelManagementLanguageService} from 'administration/model-management/services/utility/model-management-language-service';

import {CodeListSavedEvent} from 'administration/code-lists/services/events/code-list-saved-event';

import _ from 'lodash';

/**
 * Utility service extracting all available code lists. Once extracted code lists are stored in cache.
 * Service is listening for changes in code lists and if needed cache is updated with new value. The
 * service also stores all code values related to a given code list. For each code list and code value
 * labels for the currently configured language are resolved and provided with the payload.
 *
 * @author Stella Djulgerova
 * @author Svetlozar Iliev
 */
@Injectable()
@Inject(Eventbus, CodelistRestService, ModelManagementLanguageService, PromiseAdapter)
export class ModelManagementCodelistService {

  constructor(eventbus, codelistRestService, modelManagementLanguageService, promiseAdapter) {
    this.eventbus = eventbus;
    this.promiseAdapter = promiseAdapter;
    this.codelistRestService = codelistRestService;
    this.modelManagementLanguageService = modelManagementLanguageService;

    this.codeLists = new ModelList();
    this.subscribeForCodelistUpdates();
  }

  subscribeForCodelistUpdates() {
    this.eventbus.subscribe(CodeListSavedEvent, args => {
      this.updateCodeList(args[0]);
    });
  }

  updateCodeList(codeList) {
    // transform the incoming updated list & refresh
    let transformed = this.transformCodeList(codeList);
    this.codeLists.insert(transformed, codeList.value);
  }

  getCodeLists() {
    if (this.codeLists.size() === 0) {
      this.codeLists = new ModelList();
      return this.codelistRestService.getCodeLists().then((response) => {
        let transformed = response.data.map(list => this.transformCodeList(list));
        transformed.forEach(list => this.codeLists.insert(list, list.value));
        return this.codeLists.getModels();
      });
    }
    return this.promiseAdapter.resolve(this.codeLists.getModels());
  }

  getCodeList(id) {
    let existing = this.codeLists && this.codeLists.getModel(id);
    if (existing) {
      // get the existing code list from the cache
      return this.promiseAdapter.resolve(existing);
    } else {
      // have to fetch internally a single list when the cache or the list item is not present
      return this.codelistRestService.getCodeList(id).then(res => this.transformCodeList(res.data));
    }
  }

  transformCodeList(list) {
    let transformed = this.transformCodeEntry(list);
    transformed.values = list.values.map(value => this.transformCodeEntry(value));
    return transformed;
  }

  transformCodeEntry(code) {
    let descriptions = _.transform(code.descriptions, (result, val) => {
      // map description transformation by the provided languages by the code description
      result[ModelManagementLanguageService.transformLanguage(val.language)] = val.name;
    }, {});
    // resolve the current label based on configured language
    let label = this.getApplicableDescription(descriptions);

    // create a code entry from the resolved id and label
    return {value: code.value, label: label || code.value};
  }

  getApplicableDescription(descriptions) {
    return this.modelManagementLanguageService.getApplicableDescription((locale) => descriptions[locale]);
  }
}