import {Injectable, Inject} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';

/**
 * This action handler is assigned to not allowed action, when the user has no available actions over an object.
 */
@Injectable()
@Inject(PromiseAdapter)
export class NoAllowedAction {

  constructor(promiseAdapter) {
    this.promiseAdapter = promiseAdapter;
  }

  execute() {
    return this.promiseAdapter.resolve();
  }
}