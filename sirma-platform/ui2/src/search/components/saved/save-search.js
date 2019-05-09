import {Component, View, Inject} from 'app/app';
import {Configurable} from 'components/configurable';
import {InstanceRestService} from 'services/rest/instance-service';
import {DialogService} from 'components/dialog/dialog-service';
import {PermissionsRestService} from 'services/rest/permissions-service';
import {TranslateService} from 'services/i18n/translate-service';
import {NotificationService} from 'services/notification/notification-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {EVENT_CLEAR} from 'search/search-mediator';
import {OPEN_SAVED_SEARCH_EVENT} from 'search/components/saved/saved-search-select/saved-search-select';
import {SavedSearchLoadedEvent, SavedSearchCreatedEvent, SavedSearchUpdatedEvent} from './events';
import 'search/components/saved/saved-search-select/saved-search-select';

import './save-search.css!css';
import template from './save-search.html!text';

export const SAVED_SEARCH_DEFINITION_ID = 'savedSearch';
export const BUTTON_CREATE_NEW = 'CREATE_NEW';
export const BUTTON_UPDATE = 'UPDATE';

/**
 * Configurable component for saving search criteria from basic or advanced searches.
 *
 * The criteria is fetched from the provided search mediator and thus it is required by this component in order to
 * work properly.
 *
 * Additionally the component stores the orderBy and orderDirection properties from the search mediator's argument
 * map so they could be restored when loading the saved search,
 *
 * Example configuration:
 * {
 *  searchMediator: {...}
 * }
 *
 * @author Tsvetomir Dimitrov
 * @author Mihail Radkov
 */
@Component({
  selector: 'seip-save-search',
  properties: {
    'config': 'config'
  }
})
@View({template})
@Inject(InstanceRestService, DialogService, PermissionsRestService, TranslateService, NotificationService, Eventbus)
export class SaveSearch extends Configurable {

  constructor(instanceRestService, dialogService, permissionsRestService, translateService, notificationService, eventbus) {
    super({});
    this.instanceRestService = instanceRestService;
    this.dialogService = dialogService;
    this.permissionsRestService = permissionsRestService;
    this.notificationService = notificationService;
    this.translateService = translateService;
    this.eventbus = eventbus;

    this.assignSavedSearch();
    this.registerMediatorListeners();
  }

  assignSavedSearch() {
    if (this.config.searchId && this.config.searchTitle) {
      this.searchId = this.config.searchId;
      this.searchTitle = this.config.searchTitle;
      this.applyPermissions();
    }
  }

  registerMediatorListeners() {
    this.config.searchMediator.registerListener(OPEN_SAVED_SEARCH_EVENT, (savedSearch) => {
      this.searchId = savedSearch.id;
      this.searchTitle = savedSearch.text;

      this.applyPermissions();
      this.tempModel = this.getModel();
      this.eventbus.publish(new SavedSearchLoadedEvent(savedSearch));
    });

    this.config.searchMediator.registerListener(EVENT_CLEAR, () => {
      this.searchId = '';
      this.searchTitle = '';
    });
  }

  saveSearch() {
    this.showConfirmationDialog();
  }

  getModel() {
    return {
      definitionId: SAVED_SEARCH_DEFINITION_ID,
      properties: {
        title: this.searchTitle,
        searchType: this.config.searchMediator.searchMode,
        searchCriteria: this.getSearchCriteria(),
        mutable: true
      }
    };
  }

  getSearchCriteria() {
    var criteria = {
      criteria: this.config.searchMediator.queryBuilder.tree,
      orderBy: this.config.searchMediator.arguments.orderBy,
      orderDirection: this.config.searchMediator.arguments.orderDirection
    };
    return JSON.stringify(criteria);
  }

  applyPermissions() {
    this.hasPermissions = false;
    if (this.searchId) {
      this.permissionsRestService.load(this.searchId, true).then((request)=> {
        this.hasPermissions = request.data.editAllowed;
      });
    }
  }

  isUpdateAllowed() {
    return this.hasPermissions && this.searchId;
  }

  showConfirmationDialog() {
    var message = this.translateService.translateInstant('search.save.confirm.message.create');
    if (this.hasPermissions) {
      message = this.translateService.translateInstant('search.save.confirm.message.update');
    }

    this.dialogService.confirmation(message, null, {
      buttons: this.getConfirmButtons(),
      onButtonClick: (buttonId, componentScope, config) => this.onConfirmation(buttonId, config)
    });
  };

  onConfirmation(buttonId, config) {
    if (buttonId === BUTTON_CREATE_NEW) {
      this.createNew();
    } else if (buttonId === BUTTON_UPDATE) {
      this.update();
    }
    config.dismiss();
  }

  getConfirmButtons() {
    var buttons = [
      this.dialogService.createButton(BUTTON_CREATE_NEW, 'search.save.confirm.create.new', true),
      this.dialogService.createButton(DialogService.CANCEL, 'search.save.confirm.cancel')
    ];

    if (this.isUpdateAllowed()) {
      buttons.splice(1, 0, this.dialogService.createButton(BUTTON_UPDATE, 'search.save.confirm.update'));
    }

    return buttons;
  }

  createNew() {
    this.tempModel = this.getModel();
    this.instanceRestService.create(this.tempModel, {skipInterceptor: true}).then((response)=> {
      this.searchId = response.data.id;
      this.hasPermissions = true;
      var message = this.translateService.translateInstantWithInterpolation('search.save.notification.message.create', {
        searchName: response.data.properties.title
      });
      this.notificationService.success(message);
      this.eventbus.publish(new SavedSearchCreatedEvent(response.data));
    }).catch((error) => {
      this.notificationService.warning(error.data.message);
    });
  }

  update() {
    this.tempModel = this.getModel();
    this.instanceRestService.update(this.searchId, this.tempModel).then((response)=> {
      var message = this.translateService.translateInstantWithInterpolation('search.save.notification.message.updated', {
        searchName: response.data.properties.title
      });
      this.notificationService.success(message);
      this.eventbus.publish(new SavedSearchUpdatedEvent(response.data));
    });
  };
}
