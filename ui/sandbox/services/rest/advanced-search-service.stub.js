import { Inject, Injectable } from 'app/app';
import { RestClient } from 'services/rest-client';
import uuid from  'common/uuid';
import { PromiseAdapter } from 'adapters/angular/promise-adapter';
import data from 'sandbox/services/rest/advanced-search-service.data.json!';

@Injectable()
@Inject(PromiseAdapter)
export class AdvancedSearchService {

  constructor(promiseAdapter){
    this.promiseAdapter = promiseAdapter;
  }

  search(searchTree) {
    return {
      promise: this.promiseAdapter.promise((resolve) => {
        this.search = searchTree;
        resolve(data.searchTree);
      }),
      timeout: this.promiseAdapter.resolve()
    }
  }

  getRegisteredSystems(){
     return this.promiseAdapter.promise((resolve) => {
       resolve(data.systems);
     });
  }

}
