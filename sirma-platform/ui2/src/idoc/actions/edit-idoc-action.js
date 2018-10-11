import {Injectable, Inject} from 'app/app';
import {Router} from 'adapters/router/router';
import {Logger} from 'services/logging/logger';
import {Eventbus} from 'services/eventbus/eventbus';
import {InstanceAction} from 'idoc/actions/instance-action';
import {InstanceRestService} from 'services/rest/instance-service';
import {StateParamsAdapter} from 'adapters/router/state-params-adapter';
import {AfterEditActionExecutedEvent} from 'idoc/actions/events/after-edit-action-executed-event';
import {ActionsService} from 'services/rest/actions-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {IdocDraftService} from 'services/idoc/idoc-draft-service';
import {SaveIdocAction} from 'idoc/actions/save-idoc-action';
import {MODE_EDIT, MODE_PREVIEW} from 'idoc/idoc-constants';
import {LOCK} from 'idoc/actions/action-constants';

@Injectable()
@Inject(Router, StateParamsAdapter, InstanceRestService, Eventbus, Logger, ActionsService, PromiseAdapter, IdocDraftService)
export class EditIdocAction extends InstanceAction {

  constructor(router, stateParamsAdapter, instanceRestService, eventbus, logger, actionsService, promiseAdapter, idocDraftService) {
    super(logger);
    this.router = router;
    this.eventbus = eventbus;
    this.stateParamsAdapter = stateParamsAdapter;
    this.instanceRestService = instanceRestService;
    this.actionsService = actionsService;
    this.promiseAdapter = promiseAdapter;
    this.idocDraftService = idocDraftService;
  }

  execute(action, context) {
    var currentObject = context.currentObject;
    var idocPageController = context.idocPageController;
    idocPageController.disableEditButton = true;
    idocPageController.idocIsReady = false;

    return this.actionsService.lock(currentObject.getId(), this.buildActionPayload(action, currentObject, LOCK)).then(() => {
      this.refreshInstance(currentObject, context);

      // always create new version with first save action, when entering edit mode from preview
      // this property will be overridden by the back-end, after the first save
      currentObject.getModels()[SaveIdocAction.VERSION_MODE] = SaveIdocAction.MINOR;
    }, () => {
      idocPageController.disableEditButton = false;
      this.router.navigate('idoc', this.stateParamsAdapter.getStateParams(), {reload: true});
    });
  }

  afterInstanceRefreshHandler(context) {
    var currentObject = context.currentObject;
    var idocPageController = context.idocPageController;

    return this.promiseAdapter.all([this.idocDraftService.loadDraft(context.idocContext), this.instanceRestService.loadView(currentObject.getId())]).then((results) => {
      let content;
      if (results[0].loaded) {
        content = results[0].content;
      } else {
        content = results[1].data;
      }
      currentObject.setContent(content);
      idocPageController.appendContent(content);

      idocPageController.setViewMode(MODE_EDIT);
      this.router.navigate('idoc', this.stateParamsAdapter.getStateParams(), {notify: false});
      this.eventbus.publish(new AfterEditActionExecutedEvent(context));
      idocPageController.startDraftInterval();
      idocPageController.disableEditButton = false;
    }).catch((error) => {
      idocPageController.disableEditButton = false;
      throw error;
    });
  }
}