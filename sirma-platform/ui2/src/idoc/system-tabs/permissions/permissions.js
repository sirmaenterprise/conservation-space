import {Component,View,Inject} from 'app/app';
import {PermissionsRestService} from 'services/rest/permissions-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {TranslateService} from 'services/i18n/translate-service';
import 'components/select/resource/resource-select';
import 'components/select/select';
import 'components/help/contextual-help';
import {NotificationService} from 'services/notification/notification-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {IconsService} from 'services/icons/icons-service';
import {PermissionsChangedEvent} from './permissions-changed-event';
import {StateParamsAdapter} from 'adapters/router/state-params-adapter';
import {DialogService} from 'components/dialog/dialog-service';
import 'font-awesome/css/font-awesome.css!';
import './permissions.css!css';
import permissionsTemplate from './permissions.html!text';

const CLASS_INSTANCE_TYPE = 'classinstance';
const MANAGER_VALUE = 'MANAGER';
const AT_LEAST_ONE_MANAGER_KEY = 'permissions.no.manager';
const AUTHORITY_ICON_SIZE = 24;
const AUTHORITY_USER_NAME = 'user';
const AUTHORITY_GROUP_NAME = 'group';
export const EDIT_PERMISSIONS_PARAM = 'edit-permissions';
export const NO_PERMISSION = '-';
export const NO_PERMISSIONS_LABEL = 'No permissions';

@Component({
  selector: 'seip-permissions',
  properties: {
    'context': 'context'
  }
})
@View({
  template: permissionsTemplate
})
@Inject(PermissionsRestService, PromiseAdapter, NotificationService, TranslateService, Eventbus, IconsService, StateParamsAdapter, DialogService)
export class Permissions {

  constructor(permissionsRestService, promiseAdapter, notificationsService, translateService, eventbus, iconsService, stateParamsAdapter, dialogService) {
    this.instanceId = this.context.getCurrentObjectId();
    this.promiseAdapter = promiseAdapter;
    this.permissionsService = permissionsRestService;
    this.notificationService = notificationsService;
    this.translateService = translateService;
    this.stateParamsAdapter = stateParamsAdapter;
    this.dialogService = dialogService;
    this.editMode = this.stateParamsAdapter.getStateParam(EDIT_PERMISSIONS_PARAM);
    this.inheritParentPermissions = true;
    this.inheritLibraryPermissions = true;
    this.inheritedPermissionsEnabled = undefined;
    this.inheritedLibraryPermissions = undefined;
    this.newAuthorities = [];
    this.authoritiesMap = new Map();
    this.eventbus = eventbus;
    this.iconsService = iconsService;
    this.restoreChildrenPermissionsEnabled = true;
    this.authoritySelectConfig = {
      multiple: false,
      includeUsers: true,
      includeGroups: true,
      dataConverter: (response) => {
        return this.hideAuthorities(response);
      }
    };
    this.isClassInstance();
  }

  isClassInstance() {
    this.context.getCurrentObject().then((object)=> {
      this.classInstance = object.instanceType === CLASS_INSTANCE_TYPE;
    });
  }

  ngAfterViewInit() {
    this.loadPermissions();
  }

  getAuthorityIcon(authorityType) {
    if (authorityType === AUTHORITY_USER_NAME) {
      return this.iconsService.getIconForInstance(AUTHORITY_USER_NAME, AUTHORITY_ICON_SIZE);
    }
    return this.iconsService.getIconForInstance(AUTHORITY_GROUP_NAME, AUTHORITY_ICON_SIZE);
  }

  getRoleLabelByValue(value) {
    for (let role of this.roles) {
      if (role.value === value) {
        return role.label;
      }
    }
    return NO_PERMISSION;
  }

  getRoleValueByLabel(label) {
    for (let role of this.roles) {
      if (role.label === label) {
        return role.value;
      }
    }
  }

  /**
   * Loads all the roles in the system and pushesh them in the config for the select.
   *
   * @returns {*}
   */
  loadRoles() {
    return this.permissionsService.getRoles().then((respnse)=> {
      this.roles = respnse.data;
      this.roleLabels = [NO_PERMISSION];
      this.roles.forEach((role)=> {
        this.roleLabels.push(role.label);
      });
      this.roleLabelsConfig = {
        data: this.roleLabels
      };
    });
  }

  /**
   * Transforms the authority obtained from the rest so it can be used by the select.
   *
   * @param authority the authority that will be transformed
   * @returns {{id: *, text: *, type: *, value: *}}
   */
  transformAuthorityFromRest(authority) {
    return {
      id: authority.id,
      text: authority.label,
      type: authority.type,
      value: authority.value
    };
  }

  /**
   * Removes the authorities which are present in the table with the permissions
   * from the select for adding new authority.
   *
   * @param response the loaded authorities
   * @returns {Array} with the authorities that can be added
   */
  hideAuthorities(response) {
    let currentHiddenAuthorities = [];
    response.data.items.forEach((authority)=> {
      if (!this.authoritiesMap.get(authority.id)) {
        currentHiddenAuthorities.push(this.transformAuthorityFromRest(authority));
      }
    });
    return currentHiddenAuthorities;
  }

  loadAuthorities() {
    this.permissionsService.load(this.instanceId, this.inheritedPermissionsEnabled, this.inheritedLibraryPermissions).then(
      (response)=> {
        this.permissions = response.data;
        this.editAllowed = this.permissions.editAllowed;
        this.inheritedPermissionsEnabled = this.permissions.inheritedPermissionsEnabled;
        this.inheritedLibraryPermissions = this.permissions.inheritedLibraryPermissions;
        this.inheritParentPermissions = this.permissions.inheritParentPermissions;
        this.inheritLibraryPermissions = this.permissions.inheritLibraryPermissions;
        this.authoritiesMap.clear();
        this.extractPermissions(this.permissions.permissions);
        this.showAuthorities();
      });
  }

  loadPermissions() {
    this.loadRoles().then(()=> {
      this.loadAuthorities();
    });
  }

  /**
   * Calculates the current active permission of a given authority.
   * It is calculated here because in edit mode the special permissions can be changed.
   * The management permissions are with the highest priority, followed by special, inherited parent and inherited library.
   *
   * @param authority the authority which active permissions must be calculated.
   * @returns {*}
   */
  calculateActive(authority) {
    authority.active = NO_PERMISSION;
    if (authority.library && this.inheritedLibraryPermissions) {
      authority.active = authority.library;
    }
    if (this.isValidProperty(authority.inherited) && this.inheritedPermissionsEnabled) {
      authority.active = authority.inherited;
    }
    if (this.isValidProperty(authority.newSpecialPermission)) {
      authority.active = authority.newSpecialPermission;
    }
    if (authority.isManager) {
      authority.active = authority.management;
    }
    return authority.active;
  }

  extractPermissions(permissions) {
    this.authorities = [];
    this.authorities = permissions.map((permission) => {
      let authority = {};
      authority.userIcon = this.getAuthorityIcon(permission.type);
      authority.id = permission.id;
      authority.name = permission.label;
      authority.isManager = permission.isManager;
      authority.special = this.getRoleLabelByValue(permission.special);
      authority.newSpecialPermission = this.getRoleLabelByValue(permission.special);
      authority.library = this.getRoleLabelByValue(permission.library);
      authority.inherited = this.getRoleLabelByValue(permission.inherited);
      if (authority.isManager) {
        authority.management = this.getRoleLabelByValue(MANAGER_VALUE);
      } else {
        authority.management = NO_PERMISSION;
      }
      return authority;
    });
  }

  isValidProperty(property) {
    if (property && property !== NO_PERMISSION) {
      return true;
    }
    return false;
  }

  isShownAuthority(authority) {
    authority.show = false;
    if (this.isValidProperty(authority.library) && this.inheritedLibraryPermissions) {
      authority.show = true;
    }
    if (this.isValidProperty(authority.inherited) && this.inheritedPermissionsEnabled) {
      authority.show = true;
    }
    if (this.isValidProperty(authority.special) || authority.isManager) {
      authority.show = true;
    }
    return authority.show;
  }

  showAuthorities() {
    for (let i = 0; i < this.authorities.length; i++) {
      if (this.isShownAuthority(this.authorities[i])) {
        this.removeAuthorityFromHidden(this.authorities[i]);
      } else {
        this.addAuthorityToHidden(this.authorities[i]);
      }
    }
    this.sort();
  }

  clickCheckbox() {
    this.authorities[this.authorities.length - 1].newSpecialPermission = NO_PERMISSION;
    this.loadPermissions();
  }

  /**
   * Adds the authority with the given label to the list with the authorities with no active permissions.
   * @param authorityLabel the authority label
   */
  addAuthorityToHidden(authority) {
    this.authoritiesMap.set(authority.id, false);
  }

  /**
   * Removes the authority with the given label from the list with the authorities with no active permissions.
   *
   * @param authorityLabel the label of the authority
   */
  removeAuthorityFromHidden(authority) {
    this.authoritiesMap.set(authority.id, true);
  }

  /**
   * Adds new authority row.
   */
  addNewAuthority() {
    let newAuthority = {id: null, newSpecialPermission: NO_PERMISSION};
    this.newAuthorities.push(newAuthority);
  }

  /**
   * Saves the current states of the enabling variables in order to revert them when cancel is clicked.
   */
  editPermissions() {
    this.editMode = true;
    this.oldInheritedEnabled = this.inheritedPermissionsEnabled;
    this.oldLibraryEnabled = this.inheritedLibraryPermissions;
    this.loadAuthorities();
  }

  /**
   * Cancels the edit of the permissions and reverts the state of the permissions.
   */
  cancelEdit() {
    this.editMode = false;
    this.newAuthorities = [];
    this.inheritedPermissionsEnabled = this.oldInheritedEnabled;
    this.inheritedLibraryPermissions = this.oldLibraryEnabled;

    this.loadAuthorities();
  }

  processEditedAuthorities(authorities, editedAuthorities) {
    for (let authority of authorities) {
      if (this.isValidProperty(authority.id) && this.isValidProperty(authority.newSpecialPermission)) {
        editedAuthorities.push({
          id: authority.id,
          special: this.getRoleValueByLabel(authority.newSpecialPermission),
          isManager: authority.isManager,
          inherited: this.getRoleValueByLabel(authority.inherited)
        });
      }
    }
  }

  /**
   * Saves the edited special permissions and the newly added ones.
   */
  savePermissions(editMode) {
    this.editMode = editMode;
    let newPermissions = [];
    //the newly added authorities with their new special permission
    if (this.newAuthorities) {
      this.processEditedAuthorities(this.newAuthorities, newPermissions);
      this.newAuthorities = [];
    }
    this.processEditedAuthorities(this.authorities, newPermissions);
    //the object with the edited or added permissions
    let permissions = {
      permissions: newPermissions,
      inheritedPermissionsEnabled: this.inheritedPermissionsEnabled,
      inheritedLibraryPermissions: this.inheritedLibraryPermissions
    };
    return this.permissionsService.save(this.instanceId, permissions).then((response)=> {
      this.permissions = response.data;
      this.editAllowed = this.permissions.editAllowed;
      this.eventbus.publish(new PermissionsChangedEvent(this.permissions.editAllowed));
      this.extractPermissions(this.permissions.permissions);
      this.showAuthorities();
    }).catch(()=> {
      this.editMode = true;
      this.notificationService.remove();
      this.translateService.translate(AT_LEAST_ONE_MANAGER_KEY).then((result)=> {
        this.notificationService.error(result);
      });
    });
  }

  toggleReverse() {
    this.reverse = !this.reverse;
    this.sort();
  }

  /**
   * Sorts the authorities.
   */
  sort() {
    this.authorities.sort((first, second)=> {
      if (this.reverse) {
        return -first.name.localeCompare(second.name);
      } else {
        return first.name.localeCompare(second.name);
      }
    });
  }

  restoreChildrenPermissions() {
    this.dialogService.confirmation(this.translateService.translateInstant('idoc.permissions.restore.children.confirmation'), this.translateService.translateInstant('idoc.permissions.restore.children'), {
      buttons: [
        {id: 'yes', label: this.translateService.translateInstant('dialog.button.yes'), cls: 'btn-primary'},
        {id: 'cancel', label: this.translateService.translateInstant('dialog.button.no')}
      ],
      onButtonClick: (buttonID, componentScope, dialogConfig) => {
        if (buttonID === 'yes') {
          this.restoreChildrenPermissionsEnabled = false;
          this.permissionsService.restoreChildrenPermissions(this.instanceId).then(() => {
            this.restoreChildrenPermissionsEnabled = true;
            this.notificationService.success({
              message: this.translateService.translateInstant('idoc.permissions.restore.children.done')
            });
          });
        }
        dialogConfig.dismiss();
      }
    });
  }
}