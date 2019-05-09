import {View, Component, Inject} from 'app/app';
import {ModelBase} from 'administration/model-management/model/model-base';
import {ModelEvents} from 'administration/model-management/model/model-events';
import {ModelSection} from 'administration/model-management/sections/model-section';
import {ModelManagementUtility} from 'administration/model-management/utility/model-management-utility';
import {ModelManagementLanguageService} from 'administration/model-management/services/utility/model-management-language-service';
import {ConfirmationDialogService} from 'components/dialog/confirmation-dialog-service';

import {ModelRestoreInheritedHeaderAction} from 'administration/model-management/actions/restore/model-restore-inherited-header-action';

import _ from 'lodash';

import 'administration/model-management/components/controls/model-controls';
import 'administration/model-management/components/controls/save/model-save';
import 'administration/model-management/components/controls/cancel/model-cancel';

import 'administration/model-management/components/header/model-header-view';
import 'administration/model-management/components/language/model-language-select';

import 'components/collapsible/collapsible-panel';
import 'filters/to-trusted-html';

import './model-headers.css!css';
import template from './model-headers.html!text';

/**
 * Base component representing headers management section. Displays all headers for given definition
 * and controls for managing them.
 *
 * @author svelikov
 */
@Component({
  selector: 'seip-model-headers',
  properties: {
    'model': 'model',
    'emitter': 'emitter'
  },
  events: ['onSectionStateChange', 'onModelSave', 'onModelStateChange', 'onModelActionCreateRequest',
    'onModelActionExecuteRequest', 'onModelActionRevertRequest']
})
@View({
  template
})
@Inject(ConfirmationDialogService, ModelManagementLanguageService)
export class ModelHeaders extends ModelSection {

  constructor(confirmationDialogService, modelManagementLanguageService) {
    super();
    this.confirmationDialogService = confirmationDialogService;
    this.modelManagementLanguageService = modelManagementLanguageService;
  }

  ngOnInit() {
    this.languages = [];
    this.selectedLanguage = this.modelManagementLanguageService.getSystemLanguage();

    this.initializeModels(this.model);
    this.subscribeToModelChanged();
  }

  initializeModels(model) {
    this.model = model;

    if (this.isModelDefinition()) {
      this.getHeaders().forEach(header => {
        let labelAttribute = header.getLabelAttribute();
        this.notifyForModelStateCalculation(header, this.model);
        this.languages = this.languages.concat(labelAttribute.getLanguages());
      });
      this.languages = _.uniq(this.languages);
      this.afterModelChange();
    }
  }

  subscribeToModelChanged() {
    this.emitter && this.emitter.subscribe(ModelEvents.MODEL_CHANGED_EVENT, model => this.initializeModels(model));
  }

  onLanguageChanged(language) {
    this.selectedLanguage = language;
  }

  onHeaderRestoreInherited(event, header) {
    // Prevent expand/collapse
    event.stopPropagation();

    this.createRestoreInheritedDialog().then(() => {
      let action = this.notifyForModelActionCreateAndExecute(ModelRestoreInheritedHeaderAction, header, this.model);
      this.notifyForModelStateCalculation(header, this.model);
      this.insertAction(this.model, action);
    });
  }

  //@Override
  afterModelChange() {
    super.afterModelChange();
    this.sortHeaders(this.getHeaders());
  }

  sortHeaders(headers) {
    return headers.sort((leftHeader, rightHeader) => this.getHeaderOrder(leftHeader) - this.getHeaderOrder(rightHeader));
  }

  getHeaders() {
    return this.model.getHeaders();
  }

  getHeaderOrder(header) {
    let options = header.getHeaderTypeOptions();
    let option = header.getHeaderTypeOption();
    return options.indexOf(option) + 1;
  }

  //@Override
  getSectionModels() {
    return this.model;
  }

  //@Override
  isModelValid(model) {
    return ModelBase.areModelsValid(model.getOwnHeaders());
  }

  isHeaderInherited(header) {
    return ModelManagementUtility.isInherited(header, this.model);
  }

  isModelDefinition() {
    return ModelManagementUtility.isModelDefinition(this.model);
  }

  isRestoreInheritedForHeaderEnabled(header) {
    return !this.isHeaderInherited(header) && !!header.getReference()
      && header.isOwningModels() && this.model.getParent() !== null;
  }

  createRestoreInheritedDialog() {
    return this.confirmationDialogService.confirm({message: 'administration.models.management.restore.inherited.header.confirm'});
  }
}