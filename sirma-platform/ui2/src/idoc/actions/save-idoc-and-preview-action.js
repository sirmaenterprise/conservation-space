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
import {UserService} from 'services/identity/user-service';
import {Configuration} from 'common/application-config';
import {InstanceContextService} from 'services/idoc/instance-context-service';
import {UNLOCK} from 'idoc/actions/action-constants';
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

  /**
   * Unlocks iDoc and navigates to preview page where all data is reloaded
   * @param response
   * @param id
   * @param context
   * @returns {*}
   */
  reloadIdoc(response, id, context) {
    let instanceData = response.data[0];
    this.stateParamsAdapter.setStateParam(STATE_PARAM_ID, id);
    this.stateParamsAdapter.setStateParam(STATE_PARAM_MODE, MODE_PREVIEW);
    return this.actionsService.unlock(id, this.buildActionPayload({}, context.currentObject, UNLOCK)).then(() => {
      let notification = this.translateService.translateInstantWithInterpolation('idoc.save.notification.success', {
        instanceType: instanceData.properties.type.text
      });

      this.notificationService.success(notification);
      // Reload idoc view
      this.router.navigate(IDOC_STATE, this.stateParamsAdapter.getStateParams(), {
        notify: true,
        skipRouteInterrupt: true
      });
    });
  }

  stopDraftInterval(context) {
    context.idocPageController.stopDraftInterval();
  }
}