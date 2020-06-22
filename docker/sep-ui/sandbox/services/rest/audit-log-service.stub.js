import {Inject, Injectable, NgTimeout} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import data from 'sandbox/services/rest/audit-log.service.data.json!';

@Injectable()
@Inject(PromiseAdapter, NgTimeout)
export class AuditLogService {

  constructor(promiseAdapter, $timeout) {
    this.promiseAdapter = promiseAdapter;
    this.$timeout = $timeout;
  }

  search() {
    return {
      promise: this.promiseAdapter.promise((resolve) => {
        this.$timeout(() => {
          resolve(data);
        }, 100);
      }),
      timeout: this.promiseAdapter.resolve()
    };
  }
}