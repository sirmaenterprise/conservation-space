import {View, Component, Inject} from 'app/app';
import {ActionsService} from 'services/rest/actions-service';
import {ActionExecutor} from 'services/actions/action-executor';
import {ActionsHelper} from 'idoc/actions/actions-helper';
import 'components/dropdownmenu/dropdownmenu';
import template from './actions-menu.html!text';
import './actions-menu.css!';

export const ACTIONS_MENU_PLACEHOLDER = 'actions';

/**
 * A reusable menu component that allows instance actions to be loaded rendered in the menu and executed by the user.
 *
 * context: {
        currentObject: currentObject for which the actions should be loaded and executed,
        placeholder: 'a.placeholder.as.string',
        renderMenu: () => { is a callback that must return true|false as result according requirements for specific component where the menu is inserted }
        filterAction: (actions) => { a callback to invoke when actions needed to be filtered, the actions response will be passed as argument }
        displayMode: display mode of the actions, supported modes: dropdown (default) and links
      }
 */
@Component({
  selector: 'seip-actions-menu',
  properties: {
    context: 'context'
  }
})
@View({
  template
})
@Inject(ActionsService, ActionExecutor)
export class ActionsMenu {

  constructor(actionsService, actionExecutor) {
    this.actionsService = actionsService;
    this.actionExecutor = actionExecutor;
  }

  ngOnInit() {
    this.createActionsMenuConfig();
  }

  /**
   * Creates an actions menu configuration object and exposes it to the scope.
   */
  createActionsMenuConfig() {
    this.context.displayMode = this.context.displayMode || ActionsMenu.DROPDOWN_DISPLAY_MODE;

    if (this.context.displayMode === ActionsMenu.LINKS_DISPLAY_MODE) {
      this.loadItems().then((actionsData) => {
        this.actions = actionsData;
      });
    } else {
      this.actionsMenuConfig = {
        placeholder: this.context.placeholder,
        loadItems: () => this.loadItems(),
        buttonAsTrigger: false,
        triggerLabel: '',
        triggerClass: 'btn-xs button-ellipsis',
        wrapperClass: 'pull-right',
        triggerIcon: '<i class="fa fa-circle-column"></i>',
        switchlabel_onchange: false,
        reloadMenu: this.context.reloadMenu,
        context: this.context
      };
    }
  }

  /**
   * Callback function used for actions loading.
   *
   * @returns An array of actions definition objects as returned by the service.
   */
  loadItems() {
    let id = this.context.currentObject.getId();
    let config = ActionsHelper.getActionsLoaderConfig(this.context.currentObject, this.context.placeholder);
    return this.actionsService.getActions(id, config).then((response) => {
      if (!response.data || !response.data.length) {
        return [];
      }

      let actions = this.filterActions(response.data);
      let filterCriteria = ActionsHelper.getFilterCriteria(false, false, undefined, this.context.placeholder);

      return ActionsHelper.extractActions(actions, filterCriteria);
    });
  }

  filterActions(actions) {
    if (this.context.filterActions && typeof this.context.filterActions === 'function') {
      return this.context.filterActions(actions);
    }
    return actions;
  }

  renderMenu() {
    return this.context.renderMenu();
  }

  executeAction(action) {
    this.actionExecutor.execute(action, this.context);
  }

}

ActionsMenu.DROPDOWN_DISPLAY_MODE = 'dropdown';
ActionsMenu.LINKS_DISPLAY_MODE = 'links';
