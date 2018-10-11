import {View, Component, Inject} from 'app/app';
import {RoleManagementService} from 'services/rest/role-management-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {TranslateService} from 'services/i18n/translate-service';
import 'components/filter/input-filter';
import 'components/select/select';
import _ from 'lodash';

import './role-actions-table.css!css';
import template from './role-actions-table.html!text';

/**
 * Administration component which provides table with actions mapped for roles in the system. Each action per role can
 * be activated/deactivated and filters can be chosen to apply filtering by some relationship between an instance and
 * a role of the current user.
 *
 * The table has two modes: preview and edit.
 * In order to optimize building of the table in preview mode is used plain stylized text for displaying the filters.
 * In edit mode the select component is used. Its initialized only when the user clicks on the filters cell for given
 * role action.
 */
@Component({
  selector: 'seip-role-actions-table'
})
@View({
  template: template
})
@Inject(RoleManagementService, PromiseAdapter, TranslateService)
export class RoleActionsTable {

  constructor(roleManagementService, promiseAdapter, translateService) {
    this.roleManagementService = roleManagementService;
    this.promiseAdapter = promiseAdapter;
    this.translateService = translateService;
    this.initialize();
    this.initFilterConfig();
  }

  initialize() {
    let requests = [this.roleManagementService.getFilters(), this.roleManagementService.getRoleActions()];
    this.promiseAdapter.all(requests).then((responses) => {
      this.filters = responses[0].data;
      this.filtersSelectConfig = this.createFiltersSelectConfig();
      this.initializeRoleActions(responses[1].data);
    });
  }

  initFilterConfig() {
    this.filterConfig = {
      inputPlaceholder: 'administration.role.actions.table.filter.placeholder'
    };
  }

  createFiltersSelectConfig() {
    return {
      multiple: true,
      placeholder: this.translateService.translateInstant('administration.role.actions.table.action.filters.placeholder'),
      data: this.filters
    };
  }

  initializeRoleActions(roleActions) {
    this.roleActionsModel = {};
    this.activatedFilters = {};
    this.actions = roleActions.actions;
    this.roles = roleActions.roles;
    this.roleActions = this.convertToMap(roleActions.roleActions);
  }

  convertToMap(roleActions) {
    let map = {};

    roleActions.forEach((roleAction) => {
      if (!map[roleAction.role]) {
        map[roleAction.role] = {};
        this.roleActionsModel[roleAction.role] = {};
      }
      map[roleAction.role][roleAction.action] = roleAction;
      this.roleActionsModel[roleAction.role][roleAction.action] = {
        enabled: roleAction.enabled,
        filters: roleAction.filters
      };
    });

    return map;
  }

  edit() {
    this.editMode = true;
  }

  cancelEdit() {
    this.editMode = false;
  }

  cancelEditAndInitialize() {
    this.cancelEdit();
    this.initialize();
  }

  save() {
    let changes = this.extractChanges();
    if (changes.length > 0) {
      this.isSaving = true;
      this.roleManagementService.saveRoleActions(changes).then((response) => {
        this.cancelEdit();
        this.initializeRoleActions(response.data);
      }).finally(() => {
        this.isSaving = false;
      });
    } else {
      this.cancelEdit();
    }
  }

  extractChanges() {
    let changed = [];
    Object.keys(this.roleActionsModel).forEach((roleId) => {
      Object.keys(this.roleActionsModel[roleId]).forEach((actionId) => {
        if (this.hasChanges(roleId, actionId)) {
          changed.push({
            role: roleId,
            action: actionId,
            enabled: this.roleActionsModel[roleId][actionId].enabled,
            filters: this.roleActionsModel[roleId][actionId].filters
          });
        }
      });
    });
    return changed;
  }

  hasChanges(roleId, actionId) {
    let enabled = this.roleActionsModel[roleId][actionId].enabled;
    let changedFilters = this.roleActionsModel[roleId][actionId].filters;
    let differentStatus = enabled !== this.isRoleActiveForAction(roleId, actionId);
    let filtersChanged = !_.isEqual(this.getFilters(roleId, actionId), changedFilters);

    if (differentStatus || filtersChanged) {
      return true;
    }
    return false;
  }

  activateFilter(roleId, actionId, open) {
    if (this.editMode && !this.filterActivated(roleId, actionId)) {
      this.activatedFilters[roleId + actionId] = true;
      if (open) {
        this.openFiltersSelection(roleId, actionId);
      }
    }
  }

  filterActivated(roleId, actionId) {
    return !!this.activatedFilters[roleId + actionId];
  }

  openFiltersSelection(roleId, actionId) {
    // clicks on the select component in order to open the filters dropdown
    // since the select component is initialized after clicking on the filters cell for a roleAction
    // and the user has to click twice to open the dropdown
    setTimeout(() => {
      // timeout needed because the select component might not be finished initializing, otherwise it will not be
      // able to click it
      $('.' + roleId + actionId + '-select .select2-selection').click();
    }, 0);
  }

  isRoleActiveForAction(roleId, actionId) {
    if (this.roleActions[roleId] && this.roleActions[roleId][actionId]) {
      return this.roleActions[roleId][actionId].enabled;
    }
    return false;
  }

  getFilters(roleId, actionId) {
    if (this.roleActions[roleId] && this.roleActions[roleId][actionId]) {
      return this.roleActions[roleId][actionId].filters;
    }
    return [];
  }

}