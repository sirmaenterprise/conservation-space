import {Injectable, Inject} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import config from 'sandbox/services/rest/services.config.json!';

@Injectable()
@Inject(PromiseAdapter)
export class RelationshipsService {

  constructor(promiseAdapter) {
    this.promiseAdapter = promiseAdapter;
    this.config = config['relationships'];
  }

  find() {
    return this.promiseAdapter.promise((resolve) => {
      setTimeout(() => {
        resolve({
          data: [
            {id: 'rel:this', title: 'This Relationship'},
            {id: 'rel:that', title: 'That Relationship'}
          ]
        });
      }, this.config.timeout);
    });
  }

  getRelationInfo() {
    return this.promiseAdapter.resolve({data: {}});
  }
}