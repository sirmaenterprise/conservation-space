import {Inject, Injectable} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import _ from 'lodash';
import data from 'sandbox/services/rest/concept-service.data.json!';

@Injectable()
@Inject(PromiseAdapter)
export class ConceptService {

  constructor(promiseAdapter) {
    this.promiseAdapter = promiseAdapter;
  }

  getConceptHierarchy(scheme, broader) {
    return this.promiseAdapter.resolve(_.cloneDeep(data[scheme]));
  }

}