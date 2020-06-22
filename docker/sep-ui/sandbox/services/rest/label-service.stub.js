import _ from 'lodash';
import {Injectable, Inject} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import allLabels from 'services/rest/labels.json!';
import data from 'sandbox/services/rest/label-service.data.json!';

@Injectable()
@Inject(PromiseAdapter)
export class LabelRestService {

  constructor(promiseAdapter) {
    this.promiseAdapter = promiseAdapter;
    this.labels = _.clone(allLabels);

    // Prevent errors for missing labels for info and debug widgets as they don't have labels defined and are used only
    // in e2e tests.
    this.labels.en['hello-widget'] = 'Hello Widget';
    this.labels.en['debug-widget'] = 'Debug Widget';
    this.labels.bg['hello-widget'] = 'Hello Widget';
    this.labels.bg['debug-widget'] = 'Debug Widget';
    this.labels.de['hello-widget'] = 'Hello Widget';
    this.labels.de['debug-widget'] = 'Debug Widget';
  }

  getLabels(language) {
    return this.promiseAdapter.resolve(this.labels[language]);
  }

  getDefinitionLabels() {
    return this.promiseAdapter.resolve(_.clone(data.set1));
  }
}
