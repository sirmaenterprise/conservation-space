import {Inject, Injectable} from 'app/app';
import {Router} from 'adapters/router/router';
import {ActionHandler} from 'services/actions/action-handler';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';

@Injectable()
@Inject(Router, PromiseAdapter)
export class OpenUIAction extends ActionHandler {

  constructor(router, promiseAdapter) {
    super();
    this.router = router;
    this.promiseAdapter = promiseAdapter;
  }

  execute(actionDefinition) {
    this.router.navigate(actionDefinition.state, actionDefinition.params, {reload: true});
    return this.promiseAdapter.resolve();
  }
}