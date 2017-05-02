import {View, Component, Inject} from 'app/app';
import {ActionsService} from 'services/rest/actions-service';
import {DropdownMenu} from 'components/dropdownmenu/dropdownmenu';
import template from './actions-menu.html!text';
import './actions-menu.css!';

/**
 * A reusable menu component that allows instance actions to be loaded rendered in the menu and executed by the user.
 *
 * context: {
        currentObject: currentObject for which the actions should be loaded and executed,
        placeholder: 'a.placeholder.as.string',
        renderMenu: () => { is a callback that must return true|false as result according requirements for specific component where the menu is inserted }
      }
 */
@Component({
  selector: 'seip-actions-menu',
  properties: {
    context: 'context'
  }
})
@View({
  template: template
})
@Inject(ActionsService)
export class ActionsMenu {

  constructor(actionsService) {
    this.actionsService = actionsService;
    this.actionHandlers = {};
    this.createActionsMenuConfig();
  }

  /**
   * Creates an actions menu configuration object and exposes it to the scope.
   */
  createActionsMenuConfig() {
    this.actionsMenuConfig = {
      placeholder: this.context.placeholder,
      loadItems: () => this.loadItems(),
      buttonAsTrigger: true,
      triggerLabel: this.context.triggerLabel || 'idoc.menu.actions',
      triggerClass: this.context.triggerClass || 'btn btn-sm btn-default',
      triggerIcon: this.context.triggerIcon || null,
      wrapperClass: 'context-actions-menu pull-right',
      switchlabel_onchange: false,
      reloadMenu: this.context.reloadMenu,
      context: this.context
    };
  }

  /**
   * Callback function used for actions loading.
   *
   * @returns An array of actions definition objects as returned by the service.
   */
  loadItems() {
    let handlers = ActionsMenu.collectImplementedHandlers(this.context.placeholder);
    let id = this.context.currentObject.getId();
    let config = ActionsMenu.getActionsLoaderConfig(this.context.currentObject, this.context.placeholder);
    return this.actionsService.getActions(id, config).then((response) => {
      if (!response.data || !response.data.length) {
        return [];
      }
      return this.extractActions(response.data, handlers);
    });
  }

  extractActions(actions, handlers) {
    return actions.filter((action) => {
        // skip actions that are not applicable
        if (handlers[action.serverOperation + 'Action'] || action.group) {
          return true;
        }
      })
      .filter((action) => {
        // Remove from dropdown the disabled actions.
        return !action.disabled;
      }).map((action) => {
        let handlerName;
        if (action.data) {
          action.data.items = this.extractActions(action.data, handlers);
        } else {
          handlerName = action.serverOperation + 'Action';
          if (!handlers[handlerName]) {
            handlerName = 'dummyAction';
          }
        }

        return {
          // The user operation is the actual operation that user requests. For example if user requests operation
          // 'approve', then the server operation would be 'transition'. This way a group of identical operations
          // might be handled by a single action handler.
          action: action.userOperation,
          // Defined action handler: resolved on the server.
          // If the server operation is 'transition', then action handler would be TransitionAction.
          // Action handlers are registered as plugins for the 'actions' extension point and are resolved by name.
          name: handlerName,
          //Data is object containing items array with the subactions.
          data: action.data,
          label: action.label,
          tooltip: action.tooltip,
          disabled: action.disabled,
          confirmationMessage: action.confirmationMessage,
          extensionPoint: 'actions',
          configuration: action.configuration
        };
      });
  }

  static collectImplementedHandlers(placeholder) {
    return PluginRegistry.get('actions').reduce((total, current) => {
      if (!current.notApplicable || current.notApplicable.indexOf(placeholder) === -1) {
        total[current.name] = current;
      }
      return total;
    }, {});
  }

  static getActionsLoaderConfig(currentObject, placeholder) {
    return {
      // FIXME: this should come from a breadcrumb service for example
      'context-id': null,
      placeholder: placeholder,
      path: currentObject.getContextPathIds()
    };
  }

  renderMenu() {
    return this.context.renderMenu();
  }
}