import {Component, View, Inject} from 'app/app';
import {ConfirmationDialogService} from 'components/dialog/confirmation-dialog-service';
import {CodelistManagementService} from 'administration/code-lists/services/codelist-management-service';
import {PREVIEW, EDIT, CREATE} from 'administration/code-lists/manage/code-manage-modes';
import 'administration/code-lists/manage/code-descriptions-button';
import 'administration/code-lists/manage/code-values';
import 'administration/code-lists/manage/code-validation-messages';
import _ from 'lodash';

import './code-list.css!css';
import template from './code-list.html!text';

/**
 * Shows details about specific code list provided with the <code>model</code> component property.
 *
 * The supported model structure is:
 * {
 *   id: '1'
 *   descriptions: { // A map with language abbreviation for key
 *     'EN': {
 *        name: 'Project types'
 *     }
 *   },
 *   description: { // The description for the current users/system language which will be rendered
 *      name: 'Project types'
 *   },
 *   extras: { // A map with extras
 *      '1': 'Extra 1'
 *   },
 *   values: [] // Array with the code values
 * }
 *
 * @author Mihail Radkov
 */
@Component({
  selector: 'code-list',
  properties: {
    'model': 'model',
    'canEdit': 'can-edit'
  },
  events: ['onEdit', 'onSave', 'onCancel', 'onCreateCancel', 'onChange']
})
@View({
  template
})
@Inject(CodelistManagementService, ConfirmationDialogService)
export class CodeList {

  constructor(codelistManagementService, confirmationDialogService) {
    this.codelistManagementService = codelistManagementService;
    this.confirmationDialogService = confirmationDialogService;
    this.renderDetails = false;
  }

  ngOnInit() {
    this.descriptionsButtonConfig = {renderLabel: true};
    this.enablePreview();

    if (this.model.isNew) {
      this.enableCreate();
      this.toggleDetails();
    }
  }

  toggleDetails() {
    this.renderDetails = !this.renderDetails;
  }

  edit() {
    this.enableEdit();
    this.originalModel = this.cloneModel();

    this.setModelDirtyState(true);
    this.onEdit && this.onEdit();
  }

  save() {
    this.confirm('code.lists.manage.edit.confirm').then(() => this.performSave());
  }

  performSave() {
    this.savingChanges = true;
    this.codelistManagementService.saveCodeList(this.originalModel, this.model).then(() => {
      this.enablePreview();
      this.notifyForSave();
      this.setModelDirtyState(false);
    }).finally(() => {
      this.savingChanges = false;
    });
  }

  notifyForSave() {
    this.onSave && this.onSave();
  }

  cancel() {
    if (this.isCodeListUpdated()) {
      this.confirm('code.lists.manage.cancel.confirm').then(() => {
        this.afterCancel();
      });
    } else {
      this.afterCancel();
    }
  }

  isCodeListUpdated() {
    return !this.codelistManagementService.areCodeListsEqual(this.originalModel, this.model);
  }

  afterCancel() {
    this.enablePreview();
    this.model = this.originalModel;

    this.setModelDirtyState(false);
    this.onCancel && this.onCancel({codeList: this.model});
  }

  cancelCreate() {
    this.confirm('code.lists.manage.create.cancel.confirm').then(() => {
      if (this.onCreateCancel) {
        this.setModelDirtyState(false);
        this.onCreateCancel({codeList: this.model});
      }
    });
  }

  onModelChange() {
    this.setModelDirtyState(true);
    this.onChange({codeList: this.model});
  }

  confirm(message) {
    return this.confirmationDialogService.confirm({message});
  }

  setModelDirtyState(dirty) {
    if (dirty) {
      this.model.isModified = true;
    } else {
      delete this.model.isNew;
      delete this.model.isModified;
    }
  }

  enableCreate() {
    this.mode = CREATE;
  }

  enableEdit() {
    this.mode = EDIT;
  }

  enablePreview() {
    this.mode = PREVIEW;
  }

  cloneModel() {
    return _.cloneDeep(this.model);
  }

  isPreviewMode() {
    return this.mode === PREVIEW;
  }

  isEditMode() {
    return this.mode === EDIT;
  }

  isCreateMode() {
    return this.mode === CREATE;
  }

  isSaveDisabled() {
    // If there is no validation model then there are no changes that have been validated
    return this.savingChanges || (!!this.model.validationModel && !this.model.validationModel.valid);
  }

  isIdInvalid() {
    return this.isFieldInvalid('id');
  }

  isNameInvalid() {
    return this.isFieldInvalid('name', 'description');
  }

  isFieldInvalid(field, property) {
    let validationModel = (this.model[property] || this.model).validationModel;
    return !!validationModel && validationModel[field] && !validationModel[field].valid;
  }
}
