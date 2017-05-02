import {Injectable, Inject} from 'app/app';
import {BreadcrumbEntryManager} from 'layout/breadcrumb/breadcrumb-entry-manager';
import {Logger} from 'services/logging/logger';
import {Eventbus} from 'services/eventbus/eventbus';
import {Router} from 'adapters/router/router';
import {StateParamsAdapter} from "adapters/router/state-params-adapter";
import {InstanceAction} from 'idoc/actions/instance-action';
import {TranslateService} from 'services/i18n/translate-service';
import {InstanceRestService} from 'services/rest/instance-service';
import {AfterIdocDeleteEvent} from 'idoc/actions/events/after-idoc-delete-event';
import {NotificationService} from 'services/notification/notification-service';
import {IDOC_PAGE_ACTIONS_PLACEHOLDER} from 'idoc/idoc-constants';
import {STATE_PARAM_ID} from "idoc/idoc-page";

@Injectable()
@Inject(InstanceRestService, Logger, NotificationService, TranslateService, BreadcrumbEntryManager, Eventbus, Router)
export class DeleteAction extends InstanceAction {

  constructor(instanceRestService, logger, notificationService, translateService, breadcrumbEntryManager, eventbus, router) {
    super(logger);
    this.eventbus = eventbus;
    this.router = router;
    this.translateService = translateService;
    this.instanceRestService = instanceRestService;
    this.notificationService = notificationService;
    this.breadcrumbEntryManager = breadcrumbEntryManager;
  }

  execute(actionDefinition, context) {
    let currentObjectId = context.currentObject.getId();
    return this.instanceRestService.deleteInstance(currentObjectId).then(() => {

      var successMessage = this.translateService.translateInstant('action.notification.success');
      this.notificationService.success(successMessage + actionDefinition.label);

      this.eventbus.publish(new AfterIdocDeleteEvent({id: currentObjectId}));

      // If action is executed from idoc page action menu, then we should navigate back to previous state after delete
      if (context.placeholder === IDOC_PAGE_ACTIONS_PLACEHOLDER) {
        // get the previous state because current is already deleted and by requirement the user should be navigated back
        // to the previous state
        let lastStateEntry = this.breadcrumbEntryManager.getPreviousEntry();
        this.breadcrumbEntryManager.back();

        let lastStateInstanceId = lastStateEntry && lastStateEntry.getId();
        if (lastStateInstanceId) {
          let stateParams = {};
          stateParams[STATE_PARAM_ID] = lastStateInstanceId;
          this.router.navigate('idoc', stateParams, {reload: true, inherit: false});
        } else {
          this.router.navigate('userDashboard');
        }
      }

      // empty response so that there is no object to add to recent object list
      return {data: []};
    });
  }
}