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

@Injectable()
@Inject(Eventbus, InstanceRestService, Logger, ValidationService, NotificationService, TranslateService,
  StateParamsAdapter, Router, SaveDialogService, ActionsService, SearchResolverService, PromiseAdapter,
  IdocDraftService, UserService, Configuration)
export class SaveIdocAndPreviewAction extends SaveIdocAction {

  constructor(eventbus, instanceRestService, logger, validationService, notificationService, translateService,
              stateParamsAdapter, router, saveDialogService, actionsService, searchResolverService, promiseAdapter,
              idocDraftService, userService, configuration) {
    super(eventbus, instanceRestService, logger, validationService, notificationService, translateService,
      stateParamsAdapter, router, saveDialogService, actionsService, searchResolverService, promiseAdapter,
      idocDraftService, userService, configuration);
  }

  reloadIdoc(response, id, context) {
    this.reloadIDocSilently(response, id, context);
  }

  stopDraftInterval(context) {
    context.idocPageController.stopDraftInterval();
  }
}