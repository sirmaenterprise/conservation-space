import { Injectable, Inject, NgTimeout } from 'app/app';
import { PromiseAdapter } from 'adapters/angular/promise-adapter';
import data from 'sandbox/services/rest/codelist-service.data.json!';
import config from 'sandbox/services/rest/services.config.json!';
import _ from 'lodash';

@Injectable()
@Inject(PromiseAdapter, NgTimeout)
export class CodelistRestService {
  constructor(promiseAdapter, $timeout) {
    this.promiseAdapter = promiseAdapter;
    this.$timeout = $timeout;
    this.config = config['codelists'];
  }

  getCodelist(opts) {
    opts = opts || {};
    return this.promiseAdapter.promise((resolve) => {
      this.$timeout(() => {
        var codeValues = data[opts.codelistNumber] || {data: []};
        resolve(_.cloneDeep(codeValues));
      }, this.config.timeout);
    });
  }
}