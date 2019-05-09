import {Injectable, Inject} from 'app/app';
import {EditIdocAction} from 'idoc/actions/edit-idoc-action';
import {ActionsService} from 'services/rest/actions-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {Logger} from 'services/logging/logger';
import {Eventbus} from 'services/eventbus/eventbus';
import {InstanceRestService} from 'services/rest/instance-service';
import {Router} from 'adapters/router/router';
import {StateParamsAdapter} from 'adapters/router/state-params-adapter';
import {IdocDraftService} from 'services/idoc/idoc-draft-service';
import {
  STATE_PARAM_MODE,
  STATE_PARAM_ID,
  MODE_EDIT,
  IDOC_STATE
} from 'idoc/idoc-constants';

@Injectable()
@Inject(Router, StateParamsAdapter, InstanceRestService, Eventbus, Logger, ActionsService, PromiseAdapter, IdocDraftService)
export class EditDetailsAction extends EditIdocAction {

  constructor(router, stateParamsAdapter, instanceRestService, eventbus, logger, actionsService, promiseAdapter, idocDraftService) {
    super(router, stateParamsAdapter, instanceRestService, eventbus, logger, actionsService, promiseAdapter, idocDraftService);
  }

  /**
   * Executes edit action. Invokes EditIdocAction's execute method, because there is the full logic for going in edit
   * mode on idoc landing page. Otherwise if the action is executed from page different than the idoc it uses the
   * router to navigate to the idoc in edit mode.
   *
   * @param action the action
   * @param context the context
   */
  execute(action, context) {
    if (context.idocPageController) {
      return super.execute(action, context);
    } else {
      let currentObjectId = context.currentObject.getId();
      this.stateParamsAdapter.setStateParam(STATE_PARAM_MODE, MODE_EDIT);
      this.stateParamsAdapter.setStateParam(STATE_PARAM_ID, currentObjectId);
      this.router.navigate(IDOC_STATE, this.stateParamsAdapter.getStateParams(), {notify: true});
      return this.promiseAdapter.resolve();
    }
  }
}
