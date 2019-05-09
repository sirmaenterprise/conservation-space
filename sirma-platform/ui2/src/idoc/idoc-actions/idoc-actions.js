import {View, Component, Inject, NgTimeout} from 'app/app';
import {Eventbus} from 'services/eventbus/eventbus';
import {ActionExecutor} from 'services/actions/action-executor';
import {ActionsHelper} from 'idoc/actions/actions-helper';
import {SaveIdocAndPreviewAction} from 'idoc/actions/save-idoc-and-preview-action';
import {SaveIdocAndContinueAction} from 'idoc/actions/save-idoc-and-continue-action';
import {CancelSaveIdocAction} from 'idoc/actions/cancel-save-idoc-action';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {TranslateService} from 'services/i18n/translate-service';
import {ActionsService} from 'services/rest/actions-service';
import {ActionExecutedEvent, ActionInterruptedEvent} from 'services/actions/events';
import 'idoc/actions-menu/actions-menu';

import actionsTemplate from 'idoc/idoc-actions/idoc-actions.html!text';
import 'idoc/idoc-actions/idoc-actions.css!';

@Component({
  selector: 'seip-idoc-actions',
  properties: {
    'context': 'context',
    'actionContext': 'actionContext',
    'actionsConfig': 'config'
  }
})
@View({
  template: actionsTemplate
})
@Inject(Eventbus, ActionExecutor, SaveIdocAndPreviewAction, SaveIdocAndContinueAction, CancelSaveIdocAction, ActionsService, PromiseAdapter, TranslateService, NgTimeout)
export class IdocActions {

  constructor(eventbus, actionExecutor, saveIdocAndPreviewAction, saveIdocAndContinueAction, cancelSaveIdocAction, actionsService, promiseAdapter, translateService, $timeout) { //NOSONAR
    this.eventbus = eventbus;
    this.actionExecutor = actionExecutor;
    this.saveIdocAndPreviewAction = saveIdocAndPreviewAction;
    this.saveIdocAndContinueAction = saveIdocAndContinueAction;
    this.cancelSaveIdocAction = cancelSaveIdocAction;
    this.actionsService = actionsService;
    this.promiseAdapter = promiseAdapter;
    this.translateService = translateService;
    this.timeout = $timeout;

    this.headerActions = [];

    this.actionContext.idocActionsController = this;
  }

  ngOnInit() {
    this.context.getCurrentObject().then((currentObject) => {
      this.currentObject = currentObject;
      this.editAllowed = currentObject.getWriteAllowed();
      this.loadActions();
    });

    this.events = [
      this.eventbus.subscribe(ActionExecutedEvent, (data) => {
        if (data.action.forceRefresh || data.action instanceof CancelSaveIdocAction) {
          this.loadActions();
        }
        this.enableHeaderAction(data.action);
      }),
      this.eventbus.subscribe(ActionInterruptedEvent, (data) => {
        this.enableHeaderAction(data.action);
      })];
  }

  // Enabled the action button after the action completion or failure.
  enableHeaderAction(execAction) {
    for (let action of this.headerActions) {
      if (action.name === execAction.name) {
        this.timeout(() => {
          action.disableButton = false;
        }, 0);
      }
    }
  }

  // Disables the header action button until the action is executed.
  executeHeaderAction(action, disableSave) {
    if (disableSave) {
      this.disableSaveButton(disableSave);
    } else {
      action.disableButton = true;
    }
    this.actionContext.idocContext.setShowTemplateSelector(false);
    this.actionExecutor.execute(action, this.actionContext);
  }

  getHeaderActions(actions) {
    let filterCriteria = ActionsHelper.getFilterCriteria(false, true, undefined, this.context.placeholder);
    return ActionsHelper.extractActions(actions, filterCriteria);
  }

  //Should load actions only if the idoc is persisted and idoc in preview mode.
  loadActions() {
    if (this.currentObject.isPersisted() && this.actionContext.idocContext.isPreviewMode()) {
      let config = ActionsHelper.getActionsLoaderConfig(this.currentObject, this.actionContext.placeholder);
      return this.actionsService.getFlatActions(this.currentObject.id, config).then((response) => {
        this.headerActions = this.getHeaderActions(response.data);
      });
    }
  }

  disableSaveButton(state) {
    this.actionsConfig.disableSaveButton = state;
    this.animateSaveButton(state);
  }

  animateSaveButton(state) {
    $('.seip-btn-save span, .seip-btn-just-save span').each((index, element) => {
      let span = $(element);
      if (state) {
        span.css('width', span.width()).text('').attr('class', 'fa fa-spinner fa-spin');
      } else {
        span.text(this.translateService.translateInstant(element.dataset.label)).attr('class', '');
      }
    });
  }

  ngOnDestroy() {
    if (this.events) {
      for (let event of this.events) {
        event.unsubscribe();
      }
    }
  }
}
