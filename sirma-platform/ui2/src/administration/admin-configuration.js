import {View, Component, Inject} from 'app/app';
import {Eventbus} from 'services/eventbus/eventbus';
import {UserService} from 'services/identity/user-service';
import {TranslateService} from 'services/i18n/translate-service';
import {StateParamsAdapter} from 'adapters/router/state-params-adapter';
import {Router} from 'adapters/router/router';
import {AdminToolRegistry} from 'administration/admin-tool-registry';
import {ConfirmationDialogService} from 'components/dialog/confirmation-dialog-service';
import {RouterStateChangeSuccessEvent} from 'common/router/router-state-change-success-event';
import {USER, GROUP} from 'administration/resources-management/resource-management';
import 'components/extensions-panel/extensions-panel';

import './admin-configuration.css!css';
import template from './admin-configuration.html!text';

export const ADMINISTRATION_STATE = 'admin-configuration';

export const ADMIN_PANEL_QUERY_PARAMETER = 'tool';
export const ADMIN_PANEL_EXTENSION_POINT = 'admin-configuration';

export const MODELS_MANAGEMENT_EXTENSION = 'model-import';
export const USER_MANAGEMENT_EXTENSION = 'user-management';
export const GROUP_MANAGEMENT_EXTENSION = 'group-management';

/**
 * Component wrapping different administration tools.
 *
 * @author Svetlozar Iliev
 */
@Component({
  selector: 'seip-admin-configuration'
})
@View({
  template
})
@Inject(UserService, Eventbus, Router, StateParamsAdapter, TranslateService, AdminToolRegistry, ConfirmationDialogService)
export class AdminConfiguration {

  constructor(userService, eventbus, router, stateParamsAdapter, translateService, adminToolRegistry, confirmationDialogService) {
    this.userService = userService;
    this.eventbus = eventbus;
    this.router = router;
    this.stateParamsAdapter = stateParamsAdapter;
    this.translateService = translateService;
    this.adminToolRegistry = adminToolRegistry;
    this.confirmationDialogService = confirmationDialogService;
  }

  ngOnInit() {
    this.setupAdminPanel();
    this.subscribeToRouterChangeEvent();
    this.adminToolRegistry.clearStates();
  }

  subscribeToRouterChangeEvent() {
    this.routerStateEvent = this.eventbus.subscribe(RouterStateChangeSuccessEvent, () => {
      if (this.panelConfig) {
        // extract & set current tab from the url parameter
        this.panelConfig.defaultTab = this.getRequestedTab();
      }
    });
  }

  onTabChanged(newTab, oldTab, tabConfig) {
    if (this.adminToolRegistry.getState(oldTab.id)) {
      this.confirmTabChange(newTab, oldTab, tabConfig);
    } else {
      this.changeTabParameter(newTab);
    }
  }

  confirmTabChange(newTab, oldTab, tabConfig) {
    // Prevent rendering the new tab until confirmed
    tabConfig.activeTab = oldTab.id;
    this.confirmationDialogService.confirm({message: 'administration.tab.change.confirm'}).then(() => {
      // Restore new tab
      tabConfig.activeTab = newTab.id;
      this.changeTabParameter(newTab);
    });
  }

  changeTabParameter(tab) {
    this.stateParamsAdapter.setStateParam(ADMIN_PANEL_QUERY_PARAMETER, tab.id);
    this.router.navigate(ADMIN_PANEL_EXTENSION_POINT, this.stateParamsAdapter.getStateParams(), {reload: false});
  }

  /**
   * Requests the currently logged user and if he is an administrator
   * it will render the available configuration tools.
   */
  setupAdminPanel() {
    this.userService.getCurrentUser().then((response) => {
      if (response.isAdmin) {
        this.initExtensionPanelConfig(response);
      }
    });
  }

  initExtensionPanelConfig(user) {
    this.panelConfig = {
      // configure user and group management
      extensions: {
        [USER_MANAGEMENT_EXTENSION]: {
          resourceType: USER
        },
        [GROUP_MANAGEMENT_EXTENSION]: {
          resourceType: GROUP
        },
        [MODELS_MANAGEMENT_EXTENSION]: {
          tenantId: user.tenantId
        }
      },
      extensionPoint: ADMIN_PANEL_EXTENSION_POINT,
      sortComparator: this.adminPanelTabsComparator.bind(this),
      defaultTab: this.getRequestedTab()
    };
  }

  getRequestedTab() {
    return this.stateParamsAdapter.getStateParam(ADMIN_PANEL_QUERY_PARAMETER);
  }

  ngOnDestroy() {
    if (this.routerStateEvent) {
      this.routerStateEvent.unsubscribe();
    }
  }

  adminPanelTabsComparator(lhs, rhs) {
    let left = this.translateService.translateInstant(lhs.label);
    let right = this.translateService.translateInstant(rhs.label);
    return left.localeCompare(right);
  }
}
