import {Injectable, Inject} from 'app/app';
import {BreadcrumbEntryManager} from 'layout/breadcrumb/breadcrumb-entry-manager';
import {Logger} from 'services/logging/logger';
import {Eventbus} from 'services/eventbus/eventbus';
import {Router} from 'adapters/router/router';
import {InstanceAction} from 'idoc/actions/instance-action';
import {ActionsService} from 'services/rest/actions-service';
import {TranslateService} from 'services/i18n/translate-service';
import {AfterIdocDeleteEvent} from 'idoc/actions/events/after-idoc-delete-event';
import {NotificationService} from 'services/notification/notification-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import {IDOC_PAGE_ACTIONS_PLACEHOLDER, USER_DASHBOARD} from 'idoc/idoc-constants';
import {StatusCodes} from 'services/rest/status-codes';

@Injectable()
@Inject(ActionsService, Logger, NotificationService, TranslateService, BreadcrumbEntryManager, Eventbus, Router, PromiseAdapter, WindowAdapter)
export class DeleteAction extends InstanceAction {

  constructor(actionsService, logger, notificationService, translateService, breadcrumbEntryManager, eventbus, router, promiseAdapter, windowAdapter) {
    super(logger);
    this.eventbus = eventbus;
    this.router = router;
    this.translateService = translateService;
    this.actionsService = actionsService;
    this.notificationService = notificationService;
    this.breadcrumbEntryManager = breadcrumbEntryManager;
    this.promiseAdapter = promiseAdapter;
    this.windowAdapter = windowAdapter;
  }

  execute(actionDefinition, context) {
    return this.promiseAdapter.promise((resolve, reject) => {
      let currentObjectId = context.currentObject.getId();

      this.actionsService.delete(currentObjectId, actionDefinition.action).then(response => {

        let successMessage = this.translateService.translateInstant('action.notification.success');
        this.notificationService.success(successMessage + actionDefinition.label);

        this.eventbus.publish(new AfterIdocDeleteEvent({id: currentObjectId}));

        // If action is executed from idoc page action menu, then we should navigate back to previous state after delete
        if (context.placeholder === IDOC_PAGE_ACTIONS_PLACEHOLDER) {
          let navigationState;
          if (response.data && response.data.length > 1) {
            response.data.forEach(id => this.breadcrumbEntryManager.removeEntry(id));
            navigationState = this.breadcrumbEntryManager.getLastEntry();
          } else {
            navigationState = this.breadcrumbEntryManager.getPreviousEntry();
          }

          this.navigateBack(navigationState);

          // forceRefresh is set to false to indicate to ActionExecutedEvent listeners not to reload objects and elements
          // as the instance is deleted.
          actionDefinition.forceRefresh = false;
        }

        // empty response so that there is no object to add to recent object list
        resolve({data: []});

      }).catch(error => {
        if (error && error.status === StatusCodes.CONFLICT) {
          this.notificationService.remove();
          this.notificationService.warning(this.translateService.translateInstant('action.delete.warn.processed.instances'));
        } else {
          this.loadUserDashboard();
          if (error) {
            this.logger.error(error);
          }
        }
        reject();
      });
    });
  }

  navigateBack(entryState) {
    this.breadcrumbEntryManager.back();

    if (entryState && entryState.getStateUrl()) {
      this.windowAdapter.navigate(entryState.getStateUrl());
    } else {
      this.loadUserDashboard();
    }
  }

  loadUserDashboard() {
    this.router.navigate(USER_DASHBOARD, undefined, {location: 'replace'});
  }
}