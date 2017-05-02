import {View, Component, Inject, NgScope} from 'app/app';
import {UserService} from 'services/identity/user-service';
import {UrlUtils} from 'common/url-utils';
import {LocationAdapter} from 'adapters/angular/location-adapter';
import {Tabs} from 'components/tabs/tabs';
import 'administration/tenant/tenant-configuration';
import 'administration/role-actions-table/role-actions-table';

import './admin-configuration.css!css';
import template from './admin-configuration.html!text';

/**
 * Component wrapping different administration tools.
 *
 * @author Svetlozar Iliev
 */
@Component({
  selector: 'seip-admin-configuration'
})
@View({
  template: template
})
@Inject(NgScope, UserService, LocationAdapter)
export class AdminConfiguration {

  constructor($scope, userService, locationAdapter) {
    this.$scope = $scope;
    this.userService = userService;
    this.locationAdapter = locationAdapter;
    this.setupAdminPanel();
  }

  /**
   * Requests the currently logged user and if he is an administrator it will render the available configuration tools.
   */
  setupAdminPanel() {
    this.userService.getCurrentUser().then((response)=> {
      this.renderMenu = response.isAdmin;
      if (this.renderMenu) {
        this.setupTabsConfig();
      }
    });
  }

  setupTabsConfig() {
    this.tabsConfig = {
      tabs: [{
        id: 'tenantConfig',
        label: 'administration.panel.tab.tenant.config'
      }, {
        id: 'roleActionsTable',
        label: 'administration.panel.tab.manage.actions.per.role'
      }],
      activeTab: 'tenantConfig',
      classes: 'horizontal'
    };

    this.activateRequestedTab();
  }

  activateRequestedTab() {
    let requestedTabId = UrlUtils.getUrlFragment(this.locationAdapter.url());
    if (requestedTabId) {
      this.tabsConfig.tabs.some((tab) => {
        if (tab.id === requestedTabId) {
          this.tabsConfig.activeTab = requestedTabId;
          return true;
        }
        return false;
      });
    }
  }

}
