import {Inject, Injectable} from 'app/app';
import {DialogService} from 'components/dialog/dialog-service';
import {PluginsService} from 'services/plugin/plugins-service';
import {ActionHandler} from 'services/actions/action-handler';
import {TranslateService} from 'services/i18n/translate-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {ActionExecutedEvent, ActionInterruptedEvent} from './events';

/**
 * Handles instance actions execution dispatch. On successful action execution publishes ActionExecutedEvent
 * to notify any subscribers.
 *
 * Supports actions that return a promise and those which do nothing(undefined).
 *
 * @author svelikov
 */
@Injectable()
@Inject(DialogService, PluginsService, TranslateService, Eventbus, PromiseAdapter)
export class ActionExecutor {

  constructor(dialogService, pluginsService, translateService, eventbus, promiseAdapter) {
    this.dialogService = dialogService;
    this.pluginsService = pluginsService;
    this.translateService = translateService;
    this.eventbus = eventbus;
    this.promiseAdapter = promiseAdapter;
    this.actionHandlers = {};
    this.actionPlugins = {};
  }

  /**
   * @param action Can be the plugin definition in which case the handler is loaded and stored immediately or an action
   * handler that can be invoked immediately.
   * @param context
   */
  execute(action, context) {
    if (action instanceof ActionHandler) {
      this.invoke(action, action, context);
    } else {
      let plugins = this.getActionPlugins(action.extensionPoint);
      this.pluginsService.loadPluginModule(action, this.actionHandlers, 'name', plugins)
        .then(actionHandler => {
          if ((action.confirm && action.confirm === true) || action.confirmationMessage) {
            this.confirm(actionHandler, action, context);
          } else {
            this.invoke(actionHandler, action, context);
          }
        });
    }
  }

  invoke(actionHandler, action, context) {
    var promise = ActionExecutor.invokeHandler(actionHandler, action, context);

    if (!this.isPromise(promise)) {
      throw new TypeError('Action execution must return a \'promise\'!');
    }
    promise.then((actionResponse) => {
      this.eventbus.publish(new ActionExecutedEvent(action, context, actionResponse));
    })
      .catch(() => {
        this.eventbus.publish(new ActionInterruptedEvent(action));
      });
  }

  isPromise(promise) {
    return !!promise && (typeof promise === 'object' || typeof promise === 'function') && typeof promise.then === 'function';
  }

  /**
   * Load plugins for given extension point and return it. Cache all plugins under extension point name as key.
   * @param extensionPoint
   * @returns {*}
   */
  getActionPlugins(extensionPoint) {
    if (!this.actionPlugins[extensionPoint]) {
      this.actionPlugins[extensionPoint] = this.pluginsService.getPluginDefinitions(extensionPoint, 'name');
    }
    return this.actionPlugins[extensionPoint];
  }

  static invokeHandler(actionHandler, actionDefinition, context) {
    return actionHandler.execute(actionDefinition, context);
  }

  confirm(actionHandler, actionDefinition, context) {
    let message = ActionExecutor.getConfirmationMessage(actionDefinition, this.translateService);

    return this.dialogService.confirmation(message, null, {
      buttons: [
        {id: ActionExecutor.CONFIRM, label: 'Yes', cls: 'btn-primary'},
        {id: ActionExecutor.CANCEL, label: 'Cancel'}
      ],
      onButtonClick: (buttonID, componentScope, dialogConfig) => this.confirmHandler(buttonID, dialogConfig, actionHandler, actionDefinition, context),
      onClose: (componentScope, dialogConfig) => {
        if (!dialogConfig.confirmed) {
          this.eventbus.publish(new ActionInterruptedEvent(actionDefinition));
        }
      }
    });
  }

  confirmHandler(buttonID, dialogConfig, actionHandler, actionDefinition, context) {
    if (buttonID === ActionExecutor.CONFIRM) {
      this.invoke(actionHandler, actionDefinition, context);
      dialogConfig.confirmed = true;
    }
    dialogConfig.dismiss();
  }

  /**
   * Confirmation messages can be configured in definitions and they should come in the proper language. If this is the
   * case, then the message is used as is. Otherwise a default confirmation message is used.
   * @param actionDefinition
   * @param translateService
   * @returns string message
   */
  static getConfirmationMessage(actionDefinition, translateService) {
    let message = actionDefinition.confirmationMessage || 'action.confirm.operation';
    return translateService.translateInstantWithInterpolation(message, {
      operationName: actionDefinition.label
    });
  }
}

ActionExecutor.CONFIRM = 'CONFIRM';
ActionExecutor.CANCEL = 'CANCEL';