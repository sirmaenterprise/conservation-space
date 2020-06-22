import {Injectable, Inject} from 'app/app';
import {Router} from 'adapters/router/router';
import {Logger} from 'services/logging/logger';
import {Eventbus} from 'services/eventbus/eventbus';
import {SaveDialogService} from 'idoc/save-idoc-dialog/save-dialog-service';
import {TranslateService} from 'services/i18n/translate-service';
import {InstanceRestService} from 'services/rest/instance-service';
import {StateParamsAdapter} from 'adapters/router/state-params-adapter';
import {ValidationService} from 'form-builder/validation/validation-service';
import {NotificationService} from 'services/notification/notification-service';
import {ActionsService} from 'services/rest/actions-service';
import {SearchResolverService} from 'services/resolver/search-resolver-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {IdocDraftService} from 'services/idoc/idoc-draft-service';
import {SaveIdocAction} from 'idoc/actions/save-idoc-action';
import {UserService} from 'security/user-service';
import {Configuration} from 'common/application-config';
import {IdocTabBody} from 'idoc/idoc-tab-body';
import {UNLOCK} from 'idoc/actions/action-constants';
import {InstanceContextService} from 'services/idoc/instance-context-service';
import {STATE_PARAM_MODE, STATE_PARAM_ID, IDOC_STATE, MODE_PREVIEW} from 'idoc/idoc-constants';

@Injectable()
@Inject(Eventbus, InstanceRestService, Logger, ValidationService, NotificationService, TranslateService,
  StateParamsAdapter, Router, SaveDialogService, ActionsService, SearchResolverService, PromiseAdapter,
  IdocDraftService, UserService, Configuration, InstanceContextService)
export class SaveIdocAndPreviewAction extends SaveIdocAction {

  constructor(eventbus, instanceRestService, logger, validationService, notificationService, translateService,
              stateParamsAdapter, router, saveDialogService, actionsService, searchResolverService, promiseAdapter,
              idocDraftService, userService, configuration, instanceContextService) {
    super(eventbus, instanceRestService, logger, validationService, notificationService, translateService,
      stateParamsAdapter, router, saveDialogService, actionsService, searchResolverService, promiseAdapter,
      idocDraftService, userService, configuration, instanceContextService);
  }

  reloadIdoc(response, id, context) {
    // This two lines are used to simulate routing because there is no real routing and page reload in the sandbox
    context.idocPageController.setViewMode(MODE_PREVIEW);

    context.idocPageController.tabsConfig.tabs.forEach((tab) => {
      tab.isEditMode = IdocTabBody.getIsEditMode(context.idocPageController.context, tab);
    });

    this.stateParamsAdapter.setStateParam(STATE_PARAM_ID, id);
    this.stateParamsAdapter.setStateParam(STATE_PARAM_MODE, MODE_PREVIEW);
    return this.actionsService.unlock(id, this.buildActionPayload({}, context.currentObject, UNLOCK)).then(() => {
      this.notificationService.success(this.translateService.translateInstant('idoc.save.notification.success'));
      // Reload idoc view
      this.router.navigate(IDOC_STATE, this.stateParamsAdapter.getStateParams(), {notify: true, skipRouteInterrupt: true});
    });
  }

  stopDraftInterval(context) {
    context.idocPageController.stopDraftInterval();
  }
}
