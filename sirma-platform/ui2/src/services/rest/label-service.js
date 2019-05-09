import {Inject, Injectable} from 'app/app';
import {RestClient} from 'services/rest-client';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import labels from 'services/rest/labels';

const serviceUrl = '/label';

@Injectable()
@Inject(RestClient, PromiseAdapter)
export class LabelRestService {

  constructor(restClient, promiseAdapter) {
    this.restClient = restClient;
    this.promiseAdapter = promiseAdapter;
    this.prepareTranslationLabels();
  }

  prepareTranslationLabels() {
    Object.keys(labels).forEach(language => this.clearEmptyTranslationLabels(labels[language]));
  }

  clearEmptyTranslationLabels(language) {
    Object.keys(language).forEach(label => !language[label].length && delete language[label]);
  }

  getLabels(language) {
    return this.promiseAdapter.promise((resolve) => {
      resolve(labels[language]);
    });
  }

  getDefinitionLabels(data) {
    return this.restClient.post(serviceUrl + '/multi', data);
  }
}
