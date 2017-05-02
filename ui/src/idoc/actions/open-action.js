import {Injectable, Inject} from 'app/app';
import {ActionHandler} from 'services/actions/action-handler';
import {Router} from 'adapters/router/router';
import {StateParamsAdapter} from 'adapters/router/state-params-adapter';
import {MODE_PREVIEW, IDOC_STATE} from 'idoc/idoc-constants';
import {STATE_PARAM_MODE, STATE_PARAM_ID} from 'idoc/idoc-page';

@Injectable()
@Inject(StateParamsAdapter, Router)
export class OpenAction extends ActionHandler {
  constructor(stateParamsAdapter, router) {
    super();
    this.router = router;
    this.stateParamsAdapter = stateParamsAdapter;
  }

  execute(action, context) {
    this.stateParamsAdapter.setStateParam(STATE_PARAM_MODE, MODE_PREVIEW);
    this.stateParamsAdapter.setStateParam(STATE_PARAM_ID, context.currentObject.getId());
    this.router.navigate(IDOC_STATE, this.stateParamsAdapter.getStateParams());
  }
}