import {Inject, Injectable} from 'app/app';
import {RestClient} from 'services/rest-client';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {Labels} from 'services/rest/labels';

const serviceUrl = '/label';

@Injectable()
@Inject(RestClient, PromiseAdapter)
export class LabelRestService {

  constructor(restClient, promiseAdapter) {
    this.restClient = restClient;
    this.promiseAdapter = promiseAdapter;
    this.prepareTranslationLabels(Labels.DATA);
  }

  prepareTranslationLabels(languages) {
    Object.keys(languages).forEach(language => this.clearEmptyTranslationLabels(languages[language]));
  }

  clearEmptyTranslationLabels(language) {
    Object.keys(language).forEach(label => !language[label].length && delete language[label]);
  }

  getLabels(language) {
    return this.promiseAdapter.promise((resolve) => {
      resolve(Labels.DATA[language]);
    });
  }

  getDefinitionLabels(data) {
    return this.restClient.post(serviceUrl + '/multi', data);
  }

}