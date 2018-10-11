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

@Injectable()
@Inject(Eventbus, InstanceRestService, Logger, ValidationService, NotificationService, TranslateService,
  StateParamsAdapter, Router, SaveDialogService, ActionsService, SearchResolverService, PromiseAdapter,
  IdocDraftService, UserService, Configuration, InstanceContextService)
export class SaveIdocAndContinueAction extends SaveIdocAction {

  constructor(eventbus, instanceRestService, logger, validationService, notificationService, translateService,
              stateParamsAdapter, router, saveDialogService, actionsService, searchResolverService, promiseAdapter,
              idocDraftService, userService, configuration, instanceContextService) {
    super(eventbus, instanceRestService, logger, validationService, notificationService, translateService,
      stateParamsAdapter, router, saveDialogService, actionsService, searchResolverService, promiseAdapter,
      idocDraftService, userService, configuration, instanceContextService);
    this.forceRefresh = true;
  }

  reloadIdoc(response, id, context) {
    let instanceData = response.data[0];

    context.currentObject.mergePropertiesIntoModel(instanceData.properties);
    context.currentObject.headers = instanceData.headers;
    context.currentObject.mergeHeadersIntoModel(instanceData.headers);
    context.currentObject.setContent(response.config.data[0].content);
    context.currentObject.getModels()[SaveIdocAction.VERSION_MODE] = instanceData[SaveIdocAction.VERSION_MODE];
    let notification = this.translateService.translateInstantWithInterpolation('idoc.save.notification.success', {
      instanceType: instanceData.properties.type.text
    });

    this.notificationService.success(notification);
    context.idocActionsController.disableSaveButton(false);
  }

  afterUpdate(response, id, context, updatingCurrentUser) {
    super.executeUpdate(response, id, context, updatingCurrentUser);
  }
}