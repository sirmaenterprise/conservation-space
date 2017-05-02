import {Inject, Injectable} from 'app/app';
import {ActionHandler} from 'services/actions/action-handler';
import {WindowAdapter} from 'adapters/angular/window-adapter';

@Injectable()
@Inject(WindowAdapter)
export class OpenUIAction extends ActionHandler {

  constructor(windowAdapter) {
    super();
    this.windowAdapter = windowAdapter;
  }

  execute(actionDefinition) {
    this.windowAdapter.location.href = actionDefinition.href;
  }
}