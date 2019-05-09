import {View, Component, Inject, NgElement, NgScope} from 'app/app';
import {Configurable} from 'components/configurable';
import {NO_SELECTION, SINGLE_SELECTION, MULTIPLE_SELECTION} from 'search/search-selection-modes';
import 'idoc/actions-menu/actions-menu';
import {ExternalObjectService} from 'services/rest/external-object-service';
import 'instance-header/static-instance-header/static-instance-header';
import {NotificationService} from 'services/notification/notification-service';
import {TranslateService} from 'services/i18n/translate-service';
import {InstanceRestService} from 'services/rest/instance-service';
import {ReloadSearchEvent} from 'external-search/actions/reload-search-event';
import {Eventbus} from 'services/eventbus/eventbus';
import {StatusCodes} from 'services/rest/status-codes';

import './results-with-actions.css!css';
import template from 'external-search/components/results-with-actions.html!text';

@Component({
  selector: 'seip-result-template',
  properties: {
    'config': 'config',
    'results': 'results',
    'selectedItems': 'selected-items'
  }
})
@View({template})
@Inject(NgScope, NgElement, ExternalObjectService, InstanceRestService, TranslateService, Eventbus, NotificationService)
export class ResultsWithActions extends Configurable {

  constructor($scope, $element, externalObjectService, instanceRestService, translateService, eventbus, notificationService) {
    const defaultConfiguration = {
      selection: SINGLE_SELECTION
    };
    super(defaultConfiguration);
    this.$scope = $scope;
    this.$element = $element;
    this.service = externalObjectService;
    this.selectedItems = [];
    this.translateService = translateService;
    this.tabsConfig = this.getTabsConfig();
    this.searchMediator = this.config.searchMediator;
    this.instanceService = instanceRestService;
    this.showProcessAll = false;
    this.notification = notificationService;
    this.resultConfig = {
      placeholder: 'search',
      renderMenu: true
    };
    this.eventbus = eventbus;
  }

  getTabsConfig() {
    return {
      // Default if tabs should not be visible
      activeTab: 'results'
    };
  }

  isSelectable() {
    return this.config.selection !== NO_SELECTION;
  }

  selectionType() {
    if (this.config.selection === SINGLE_SELECTION) {
      return 'radio';
    } else if (this.config.selection === MULTIPLE_SELECTION) {
      return 'checkbox';
    }
  }

  /**
   * Deselect all objects in search results.
   */
  deselectAll() {
    this.checkItems(false);
    this.showProcessAll = false;
    this.selectedItems = [];
  }

  checkItems(check) {
    var checkboxes = document.getElementsByName('selectedItems');
    for (var i = 0, n = checkboxes.length; i < n; i++) {
      if (this.results.data[i] !== undefined && this.results.data[i].data.selectable === true) {
        checkboxes[i].checked = check;
      }
    }
  }

  /**
   * Select all objects in search results.
   */
  selectAll() {
    this.checkItems(true);
    this.showProcessAll = true;
    this.addAll();
  }

  /**
   * Add all search results to selected items.
   */
  addAll() {
    this.selectedItems = [];
    for (var index = 0; index < this.results.data.length; index++) {
      if (this.results.data[index].data.selectable === true) {
        this.selectedItems.push(this.results.data[index].data);
      }
    }
  }

  handleSelection(checkedItem) {
    if (this.contains(this.selectedItems, checkedItem.data)) {
      var index = this.selectedItems.indexOf(checkedItem.data);
      this.selectedItems.splice(index, 1);
    } else {
      this.selectedItems.push(checkedItem.data);
    }
    if (this.selectedItems.length > 1) {
      this.showProcessAll = true;
    } else {
      this.showProcessAll = false;
    }
  }

  /**
   * Check if array A contains object B
   * http://stackoverflow.com/a/9849276
   */
  contains(a, b) {
    return a.indexOf(b) !== -1;
  }

  /**
   * Processes all selected object.
   */
  processAll() {
    this.executeAction(this.selectedItems);
  }

  executeAction(result) {
    let items = [];
    for (let i = 0; i < result.length; i++) {
      items.push(result[i]);
    }
    this.selectedItems.splice(0);
    this.importObject(items).then((response) => {
      this.executeRefresh(response);
    }, (response) => {
      this.executeRefresh(response);
    });
  }

  importObject(data) {
    return this.service.importObjects(data);
  }

  executeRefresh(response) {
    let ms = {};
    ms.opts = {
      closeButton: true
    };

    if (response.status === StatusCodes.FORBIDDEN) {
      // This is a special case because we may have a situation where the loaded
      // instance might try to access a 3-rd party system.
      ms.message = this.translateService.translateInstant('external.operation.warning');
      this.notification.warning(ms);
    } else if (response.data.cause) {
      ms.message = response.data.cause.message.replace('\r\n', '<br>');
      this.notification.error(ms);
    } else {
      ms.message = this.translateService.translateInstant('external.operation.success');
      this.notification.success(ms);
    }

    this.eventbus.publish(new ReloadSearchEvent({}));
  }

  isImportable(instance) {
    return instance.data.selectable;
  }

}
