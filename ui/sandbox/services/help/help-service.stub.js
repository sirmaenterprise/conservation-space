import {Injectable, Inject} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';

@Injectable()
@Inject(PromiseAdapter)
export class HelpService {
  constructor(promiseAdapter) {
    this.promiseAdapter = promiseAdapter;
    this.mapping = {
      'existing-target': 'helpInstanceId'
    };
  }

  initialize() {
    return this.promiseAdapter.resolve();
  }

  getHelpInstanceId(helpTarget) {
    return this.mapping[helpTarget];
  }
}