import {Component, Inject, View} from 'app/app';
import {CodelistManagementService} from 'administration/code-lists/services/codelist-management-service';
import {CodelistValidationService} from 'administration/code-lists/services/codelist-validation-service';
import {AdminToolRegistry} from 'administration/admin-tool-registry';

import 'administration/code-lists/export/code-lists-export';
import 'administration/code-lists/upload/code-lists-upload';
import 'administration/code-lists/search/code-lists-search';
import 'administration/code-lists/manage/code-list';

import './code-lists.css!css';
import template from './code-lists.html!text';

const CODE_LISTS_TOOL = 'code-lists';

/**
 * Administration component for managing controlled vocabularies in the system.
 *
 * @author Mihail Radkov
 */
@Component({
  selector: 'seip-code-lists'
})
@View({
  template
})
@Inject(CodelistManagementService, CodelistValidationService, AdminToolRegistry)
export class CodeLists {

  constructor(codelistManagementService, codelistValidationService, adminToolRegistry) {
    this.codelistManagementService = codelistManagementService;
    this.codelistValidationService = codelistValidationService;
    this.adminToolRegistry = adminToolRegistry;
  }

  ngOnInit() {
    this.adding = false;
    this.codeLists = [];
    this.fetchCodeLists();
  }

  onUpload() {
    this.fetchCodeLists();
  }

  onExport() {
    return this.codelistManagementService.exportCodeLists().then((file) => file.data);
  }

  fetchCodeLists() {
    this.codelistManagementService.getCodeLists().then((codeLists) => {
      this.codeLists = codeLists;
      this.adding = false;
      this.assignVisibility();
    });
  }

  onFilter(filteredIds) {
    this.codeLists.forEach(codeList => {
      if (!codeList.isNew) {
        codeList.visible = filteredIds.indexOf(codeList.id) > -1;
      }
    });
  }

  addCodeList() {
    let newCodeListModel = this.codelistManagementService.createCodeList();
    newCodeListModel.isNew = true;
    newCodeListModel.visible = true;

    this.codeLists.unshift(newCodeListModel);
    this.adding = true;
    this.setRegistryState(true);

    this.validate(newCodeListModel);
  }

  onEdit() {
    this.setRegistryState(true);
  }

  onSave() {
    this.fetchCodeLists();
    this.setRegistryState(false);
  }

  onChange(codeList) {
    this.validate(codeList);
    this.setRegistryState(true);
  }

  onCancel(codeList, index) {
    this.setRegistryState(false);

    if (codeList && index >= 0) {
      // restore the original code list
      this.codeLists[index] = codeList;
    }
  }

  onCreateCancel(codeList) {
    let index = this.codeLists.indexOf(codeList);
    if (index > -1) {
      this.codeLists.splice(index, 1);
      this.adding = false;
      this.setRegistryState(false);
    }
  }

  canAdd() {
    return !this.getRegistryState() && !this.adding;
  }

  canEdit(codeList) {
    return !this.getRegistryState() || codeList.isModified || codeList.isNew || false;
  }

  validate(codeList) {
    this.codelistValidationService.validate(this.codeLists, codeList);
  }

  assignVisibility() {
    this.codeLists.forEach(codeList => {
      codeList.visible = true;
    });
  }

  /**
   * Sets the state of this tool in the tool registry.
   * @param state <code>true</code> then this tool has unsaved changes for code lists or
   *              <code>false</code> if there are no unsaved changes
   */
  setRegistryState(state) {
    this.adminToolRegistry.setState(CODE_LISTS_TOOL, state);
  }

  /**
   * Gets the state of this tool from the tool registry
   *
   * @returns state <code>true</code> then this tool has unsaved changes for code lists or
   *              <code>false</code> if there are no unsaved changes
   */
  getRegistryState() {
    return this.adminToolRegistry.getState(CODE_LISTS_TOOL);
  }

}
