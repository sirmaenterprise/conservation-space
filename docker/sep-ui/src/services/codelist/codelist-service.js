import {Inject, Injectable} from 'app/app';
import _ from 'lodash';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {CodelistRestService} from 'services/rest/codelist-service';

/**
 * Service for manipulating codelists and codevalues
 */
@Injectable()
@Inject(PromiseAdapter, CodelistRestService)
export class CodelistService {
  constructor(promiseAdapter, codelistRestService) {
    this.promiseAdapter = promiseAdapter;
    this.codelistRestService = codelistRestService;
  }

  /**
   * Aggregates codevalues for the given codelists into single array
   * @param codelists
   * @param filterFunc - optional function to filter codevalues. Filtering is performed on the original values before conversion.
   * @param convertFunc - optional function to convert codevalues into different format.
   * @returns {*} promise which resolves with array of codevalues
   */
  aggregateCodelists(codelists, filterFunc, convertFunc) {
    let codelistLoaders = codelists.map((codelist) => {
      return this.codelistRestService.getCodelist({
        codelistNumber: codelist
      });
    });
    return this.promiseAdapter.all(codelistLoaders).then((responses) => {
      return this.mergeCodeValues(responses, filterFunc, convertFunc);
    });
  }

  mergeCodeValues(responses, filterFunc, convertFunc) {
    let values = [];
    let uniqueValuesSet = new Set();

    responses.forEach((response) => {
      if (response.data) {
        response.data.forEach((codeValue) => {
          let value = codeValue.value;
          if (!uniqueValuesSet.has(value) && (!_.isFunction(filterFunc) || filterFunc(codeValue))) {
            values.push(_.isFunction(convertFunc) ? convertFunc(codeValue) : codeValue);
            uniqueValuesSet.add(value);
          }
        });
      }
    });

    return values;
  }

  /**
   * Converts codevalue into format suitable for Select component
   * @param codeValue
   * @returns {{id: *, text: *}}
   */
  static convertToSelectValue(codeValue) {
    return {
      id: codeValue.value,
      text: codeValue.label
    };
  }
}
