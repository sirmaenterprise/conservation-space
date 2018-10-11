import {Injectable, Inject} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';

@Injectable()
@Inject(PromiseAdapter)
export class MailboxInfoService {

  constructor(promiseAdapter) {
    this.promiseAdapter = promiseAdapter;
  }

  getUnreadMessagesCount(accountName, config) {
    return this.promiseAdapter.promise((resolve) => {
        resolve({data: 7});
    });
  }
}