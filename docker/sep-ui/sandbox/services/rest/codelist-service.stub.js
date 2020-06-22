import {Injectable, Inject, NgTimeout} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import _ from 'lodash';
import data from 'sandbox/services/rest/codelist-service.data.json!';
import config from 'sandbox/services/rest/services.config.json!';
import managementData from 'sandbox/services/rest/codelist-management.data.json!';

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
        let values = _.cloneDeep(data[opts.codelistNumber] || {data: []});

        if (opts.filterBy) {
          let filteredValues = [];
          let filtered = data[opts.codelistNumber].filtered[opts.filterBy];
          if (filtered) {
            filtered.forEach((key) => {
              let found = _.find(values.data, (cv) => {
                return cv.value === key;
              });
              filteredValues.push(found);
            });
            values.data = filteredValues;
          }
        }

        resolve(values);
      }, this.config.timeout);
    });
  }

  getCodeList(codeList) {
    return this.promiseAdapter.resolve({
      data: managementData[codeList]
    });
  }

  getCodeLists() {
    return this.promiseAdapter.resolve({
      data: Object.values(managementData)
    });
  }

  /**
   * Simulate backend persistence.
   */
  saveCodeList(updatedCodeList) {
    let codeList = managementData[updatedCodeList.value];

    if (codeList) {
      let codeListValues = codeList.values;
      Object.assign(codeList, updatedCodeList);
      codeList.values = codeListValues;

      if (updatedCodeList.values.length > 0) {
        codeList.values = codeList.values.filter(value => {
          return !updatedCodeList.values.some(updatedValue => updatedValue.value === value.value);
        });
        codeList.values.push(...updatedCodeList.values);
      }
    } else {
      managementData[updatedCodeList.value] = updatedCodeList;
    }

    return this.promiseAdapter.resolve();
  }

  /**
   * Simulate backend encoded data payload
   */
  exportCodeLists() {
    return this.promiseAdapter.resolve({
      status: 'ok',
      //data content characters [a, b, c, d]
      data: new Uint8Array([97, 98, 99, 100])
    });
  }

  getUpdateServiceUrl() {
    return '/update';
  }

  getOverwriteServiceUrl() {
    return '/overwrite';
  }
}