import {NgScope, Inject, View, Component} from 'app/app';

import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {DialogService} from 'components/dialog/dialog-service';
import {ConfirmationDialogService} from 'components/dialog/confirmation-dialog-service';
import {ValidationService} from 'form-builder/validation/validation-service';

import {ModelBase} from 'administration/model-management/model/model-base';
import {ModelField} from 'administration/model-management/model/model-field';
import {ModelRegion} from 'administration/model-management/model/model-region';
import {ModelSection} from 'administration/model-management/sections/model-section';

import {ModelList} from 'administration/model-management/model/model-list';
import {ModelEvents} from 'administration/model-management/model/model-events';
import {ModelAttribute} from 'administration/model-management/model/attributes/model-attribute';

import {ModelValidateAttributesAction} from 'administration/model-management/actions/state/model-validate-attributes-action';

import {ModelCreateFieldAction} from 'administration/model-management/actions/create/model-create-field-action';
import {ModelCreatePropertyAction} from 'administration/model-management/actions/create/model-create-property-action';

import {ModelRestoreInheritedFieldAction} from 'administration/model-management/actions/restore/model-restore-inherited-field-action';
import {ModelRestoreInheritedRegionAction} from 'administration/model-management/actions/restore/model-restore-inherited-region-action';
import {ModelRestoreInheritedAttributeAction} from 'administration/model-management/actions/restore/model-restore-inherited-attribute-action';
import {ModelRestoreInheritedControlAction} from 'administration/model-management/actions/restore/model-restore-inherited-control-action';

import {ModelControlMetaData} from 'administration/model-management/meta/model-control-meta';
import {ModelCreateControlAction} from 'administration/model-management/actions/create/model-create-control-action';
import {ModelCreateControlParamAction} from 'administration/model-management/actions/create/model-create-control-param-action';

import {ModelRemoveControlAction} from 'administration/model-management/actions/remove/model-remove-control-action';

import {ModelManagementUtility} from 'administration/model-management/utility/model-management-utility';
import _ from 'lodash';

import {ModelCreateField} from 'administration/model-management/sections/field/model-create-field';
import {ModelCreateProperty} from 'administration/model-management/sections/field/model-create-property';

import 'administration/model-management/sections/field/model-details';
import 'administration/model-management/components/field/model-field-view';
import 'administration/model-management/components/property/model-property-view';
import 'administration/model-management/components/container/model-container-view';

import 'administration/model-management/components/controls/model-controls';
import 'administration/model-management/components/controls/save/model-save';
import 'administration/model-management/components/controls/cancel/model-cancel';

import './model-fields.css!css';
import template from './model-fields.html!text';

const DISPLAY_ATTRIBUTE = ModelAttribute.DISPLAY_ATTRIBUTE;
const DISPLAY_TYPE_HIDDEN = ValidationService.DISPLAY_TYPE_HIDDEN;
const DISPLAY_TYPE_SYSTEM = ValidationService.DISPLAY_TYPE_SYSTEM;

/**
 * A component in charge of displaying the field structure of a given model.
 * The provided model is supplied through a component property. It should
 * be of type {@link ModelDefinition}.
 *
 * @author Svetlozar Iliev
 */
@Component({
  selector: 'seip-model-fields',
  properties: {
    'meta': 'meta',
    'model': 'model',
    'emitter': 'emitter'
  },
  events: ['onModelSave', 'onSectionStateChange', 'onModelStateChange', 'onModelLoad',
    'onModelActionCreateRequest', 'onModelActionExecuteRequest', 'onModelActionRevertRequest']
})
@View({
  template
})
@Inject(NgScope, DialogService, PromiseAdapter, ConfirmationDialogService)
export class ModelFields extends ModelSection {

  constructor($scope, dialogService, promiseAdapter, confirmationDialogService) {
    super({
      filterTerm: '',
      showSystem: false,
      showHidden: false,
      showInherited: true
    });
    this.$scope = $scope;
    this.dialogService = dialogService;
    this.promiseAdapter = promiseAdapter;
    this.confirmationDialogService = confirmationDialogService;
  }

  ngOnInit() {
    this.initialize(this.model);
    this.subscribeToModelChanged();
    this.registerValidationPolicies();
  }

  initialize(model) {
    this.visible = {};
    this.model = model;
    this.config.showInherited = this.isModelDefinition();
    this.filterRules = _.clone(this.config);

    this.getModels(true, true);
    this.afterModelChange();
    this.deselectModel();
    this.triggerFilter();
  }

  subscribeToModelChanged() {
    this.emitter && this.emitter.subscribe(ModelEvents.MODEL_CHANGED_EVENT, (model) => this.initialize(model));
  }

  registerValidationPolicies() {
    // attach the specific validation policies (based on the custom actions) bound to the given actions for the fields section
    Object.values(ModelFields.ACTION_TYPE_RESOLVER).forEach(a => this.insertValidation(a.getType(), ModelValidateAttributesAction));
  }

  onModelNavigated(model) {
    // request that the given model is loaded and immediately navigate to that model after loading
    this.notifyForModelLoaded(this.getActualModel(model)).then(() => this.validateAndSelectModel(model));
  }

  onModelRestoreInherited(model, toRestore) {
    let type = ModelFields.ACTION_TYPE_RESOLVER[ModelManagementUtility.getModelType(toRestore)];
    let action = this.notifyForModelActionCreateAndExecute(type, model, this.model, toRestore);
    this.insertAction(this.model, action);
  }

  onModelControlRestoreInherited(event, selectedModel) {
    event.stopPropagation();

    this.createRestoreInheritedDialog('administration.models.management.restore.inherited.model.confirm').then(() => {
      selectedModel.getControls().slice().forEach(control => {
        this.onModelRestoreInherited(selectedModel, control);
      });
    });
  }

  //@Override
  getSectionModels() {
    return this.model;
  }

  //@Override
  isModelValid(model) {
    return ModelBase.areModelsValid(this.getModels().filter(m => !ModelManagementUtility.isInherited(m, this.model)));
  }

  //@Override
  notifyForModelActionExecute(actions) {
    let results = super.notifyForModelActionExecute(actions);
    this.updateSectionSelectionOnActionResults();
    return results;
  }

  //@Override
  notifyForModelActionRevert(actions) {
    let results = super.notifyForModelActionRevert(actions);
    this.updateSectionSelectionOnActionResults();
    return results;
  }

  updateSectionSelectionOnActionResults() {
    this.getModels(true, true);

    // get the selection from the list
    let id = this.getSelectedModelId();
    let model = this.models.getModel(id);

    // update the current model selection
    if (this.isSelectionChangeable(model)) {
      this.selectModel(model);
    } else if (!model) {
      this.deselectModel();
      this.updateModelView(id, false);
    }
  }

  updateModelView(model, visible = true) {
    if (model) {
      this.visible[ModelManagementUtility.getModelId(model)] = visible;
    }
  }

  createField() {
    let action = this.notifyForModelActionCreate(ModelCreateFieldAction, null, this.model, this.meta.getFields());

    this.formConfig = this.getFieldCreateDialogConfig(action);
    this.dialogConfig = this.getDialogConfiguration('administration.models.management.field.form.header');
    this.dialogService.create(ModelCreateField, this.formConfig, this.dialogConfig);
  }

  createProperty() {
    let context = this.isModelDefinition() ? this.model.getType() : this.model;
    let definitions = !this.isModelDefinition() ? this.model.getSuperTypes() : [this.model];

    let propertyAction = this.notifyForModelActionCreate(ModelCreatePropertyAction, null, context, this.meta.getProperties());
    let fieldActions = definitions.map(d => this.notifyForModelActionCreate(ModelCreateFieldAction, null, d, this.meta.getFields()));

    this.formConfig = this.getPropertyCreateDialogConfig(propertyAction, fieldActions);
    this.dialogConfig = this.getDialogConfiguration('administration.models.management.property.form.header');
    this.dialogService.create(ModelCreateProperty, this.formConfig, this.dialogConfig);
  }

  createControl(controlId) {
    let actions = [];
    let createControlAction = this.notifyForModelActionCreate(ModelCreateControlAction,
      null, this.selectedModel, this.meta, controlId, this.model);
    actions.push(createControlAction);
    this.insertAction(this.model, createControlAction);

    _.result(_.find(this.meta.getControls().getModel(ModelControlMetaData.ID).getControlOptions(), (option) => {
      return option.id === controlId;
    }), ModelControlMetaData.PARAMS, []).forEach(attributes => {
      let createControlParamAction = this.notifyForModelActionCreate(ModelCreateControlParamAction,
        null, createControlAction.model, this.meta, attributes);
      actions.push(createControlParamAction);
      this.insertAction(this.model, createControlParamAction);
    });
    this.notifyForModelActionExecute(actions);
  }

  removeControl(control) {
    let removedControlId = control.getId();
    let removedFrom = this.selectedModel.getId();
    let actions = this.extractActions(this.model);
    let controlAction = _.find(this.filterActions(actions, ModelCreateControlAction), (action) => {
      return this.isSelectedControl(action, removedControlId, removedFrom);
    });
    let actionsToRemove = this.filterActions(actions, ModelCreateControlParamAction).filter(action => {
      return controlAction && this.hasControlParams(action, controlAction.getId(), removedFrom);
    });
    // Revert newly added control
    if (this.isTouched(this.selectedModel) && actions.length && controlAction) {
      actionsToRemove.push(controlAction);
      this.notifyForModelActionRevert(actionsToRemove);
      actionsToRemove.forEach(actionToRemove => {
        this.removeAction(this.model, actionToRemove, actionToRemove.getModel());
      });
    } else { // Remove existing control
      let deletedControl = this.notifyForModelActionCreate(ModelRemoveControlAction, control, this.selectedModel);
      this.notifyForModelActionExecute([deletedControl]);
      this.insertAction(this.model, deletedControl);
    }
  }

  isSelectedControl(action, id, from) {
    return action.getId() === id && action.getContext().getId() === from;
  }

  hasControlParams(action, id, from) {
    return action.getContext().getId() === id && action.getContext().getParent().getId() === from;
  }

  triggerFilter() {
    this.filter(this.getModels(), (m) => this.filterCallback(m));
  }

  filterCallback(field) {
    let visibility = true;
    if (this.isModelDefinition()) {
      let system = this.isFieldSystem(field) ? this.filterRules.showSystem : true;
      let hidden = this.isFieldHidden(field) ? this.filterRules.showHidden : true;
      visibility = system && hidden;
    }
    let matching = this.isFieldNameMatchingKeyword(field);
    let inherited = this.isFieldInherited(field) ? this.filterRules.showInherited : true;

    // resolve the field's actual visibility
    return matching && inherited && visibility;
  }

  getModels(forceReload, forceSort) {
    if (!this.models || forceReload || forceSort) {
      // list representing the view of the models
      forceReload && this.createModelsListView();

      if (this.isModelDefinition()) {
        if (forceReload) {
          // collect all fields and regions in a single unique array of models
          let models = this.model.getFields().concat(this.model.getRegions());
          // append final models to the view list
          models.forEach(m => this.models.insert(m));
        }
        // perform sorting of the fields / regions by order
        forceSort && this.sortDefinitionModels(this.models);
      } else {
        if (forceReload) {
          // collect only the properties of class
          let models = this.model.getProperties();
          // append final models to the view list
          models.forEach(m => this.models.insert(m));
        }
        // perform sorting of the properties by label
        forceSort && this.sortClassModels(this.models);
      }
    }
    return this.models.getModels();
  }

  createModelsListView() {
    this.models = new ModelList();
  }

  getPropertyCreateDialogConfig(propertyAction, fieldActions) {
    let actions = [propertyAction, ...fieldActions];
    this.isModelDefinition() && actions.reverse();

    return {
      context: this.model,
      model: propertyAction.getModel(),
      fields: fieldActions.map(a => a.getModel()),
      onModelLoad: this.onModelLoad.bind(this),
      onModelSave: s => this.onSaveForm(s, actions, this.dialogConfig),
      onModelCancel: s => this.onCancelForm(s, actions, this.dialogConfig),
      onModelActionCreateRequest: this.onModelActionCreateRequest.bind(this),
      onModelActionRevertRequest: this.onModelActionRevertRequest.bind(this),
      onModelActionExecuteRequest: this.onModelActionExecuteRequest.bind(this)
    };
  }

  getFieldCreateDialogConfig(action) {
    return {
      context: this.model,
      model: action.getModel(),
      onModelLoad: this.onModelLoad.bind(this),
      onModelSave: s => this.onSaveForm(s, action, this.dialogConfig),
      onModelCancel: s => this.onCancelForm(s, action, this.dialogConfig),
      onModelActionCreateRequest: this.onModelActionCreateRequest.bind(this),
      onModelActionRevertRequest: this.onModelActionRevertRequest.bind(this),
      onModelActionExecuteRequest: this.onModelActionExecuteRequest.bind(this)
    };
  }

  onSaveForm(stream, actions, dialogConfig) {
    actions = Array.isArray(actions) ? actions : [actions];
    return this.promiseAdapter.resolve().then(() => {
      this.notifyForModelActionExecute(actions);
      this.registerActions(actions.concat(stream.actions));
      this.selectModel(actions[0].getModel());
      dialogConfig.dismiss();
    });
  }

  onCancelForm(stream, actions, dialogConfig) {
    return this.promiseAdapter.resolve().then(() => {
      dialogConfig.dismiss();
    });
  }

  getDialogConfiguration(header) {
    return {largeModal: true, header};
  }

  // Generic methods for working with the section unrelated to the model type

  filter(models, callback) {
    return models.filter(item => {
      let visible = callback(item);
      if (this.isModelRegion(item)) {
        let fields = this.getFields(item);
        // get all visible fields from the region
        visible = this.filter(fields, callback);
        // hide region if all fields are hidden
        visible = !visible || visible.length > 0;
      }
      this.updateModelView(item, visible);
      return this.isModelVisible(item);
    });
  }

  registerActions(actions) {
    actions && actions.forEach(a => this.insertAction(this.getActualModel(a.getContext()), a));
  }

  isFieldsListVisible() {
    return Object.values(this.visible).filter(v => v).length > 0;
  }

  isFieldInherited(field) {
    // compare owner of the field with current model
    return this.getActualModel(field) !== this.model;
  }

  isFieldSystem(field) {
    return this.getDisplayAttribute(field) === DISPLAY_TYPE_SYSTEM;
  }

  isFieldHidden(field) {
    return this.getDisplayAttribute(field) === DISPLAY_TYPE_HIDDEN;
  }

  isFieldNameMatchingKeyword(field) {
    let term = this.filterRules.filterTerm;
    let description = field.getDescription();
    let name = description && description.getValue().toUpperCase();
    return (!name || !term || !term.length) ? true : name.indexOf(term.toUpperCase()) >= 0;
  }

  isModelVisible(model) {
    return model && this.visible[model.getId()];
  }

  isModelField(model) {
    return model instanceof ModelField;
  }

  isModelRegion(model) {
    return model instanceof ModelRegion;
  }

  isModelProperty(model) {
    return ModelManagementUtility.isModelProperty(model);
  }

  isModelClass(model) {
    // use this model as default model to check when no model is specified
    return ModelManagementUtility.isModelClass(model || this.model);
  }

  isModelDefinition(model) {
    // use this model as default model to check when no model is specified
    return ModelManagementUtility.isModelDefinition(model || this.model);
  }

  isSelectionChangeable(selection) {
    // changeable when both exist and selection differs from current selection
    return selection && this.selectedModel && this.selectedModel !== selection;
  }

  isCreateFieldVisible() {
    return this.isModelDefinition();
  }

  isCreateRegionVisible() {
    return false;
  }

  isCreatePropertyVisible() {
    return this.isModelDefinition() || this.isModelClass();
  }

  isRestoreInheritedForControlsEnabled(modelField) {
    let overriddenField = !this.isFieldInherited(modelField);
    let canBeRestored = !!this.model.getParent() && modelField.getReference();

    let overriddenControls = modelField.getControls().some(control => {
      return !ModelManagementUtility.isInherited(control, modelField);
    });

    return overriddenField && canBeRestored && overriddenControls;
  }

  createRestoreInheritedDialog(message) {
    return this.confirmationDialogService.confirm({message});
  }

  isHighlighted(model) {
    return this.selectedModel === model;
  }

  isTouched(model) {
    return model.isDirty() || this.isModelDirty(this.model, model);
  }

  getDisplayAttribute(field) {
    let attribute = field.getAttribute(DISPLAY_ATTRIBUTE);
    return attribute && attribute.getValue().getValue();
  }

  getSelectedModelId() {
    return this.selectedModel && this.selectedModel.getId();
  }

  getSelection(model) {
    // resolve the owner of the attribute such that it is supported by the section for displaying and managing - field, region & property
    return ModelManagementUtility.getOwningModel(model, (m) => this.isModelField(m) || this.isModelRegion(m) || this.isModelProperty(m));
  }

  getFields(region) {
    // filter out only those fields which belong to a given region based on the id of that model region
    return this.models.getModels().filter(f => this.isModelField(f) && f.getRegionId() === region.getId());
  }

  validateAndSelectModel(model) {
    this.selectModel(model);
    this.notifyForModelStateCalculation(model, this.model);
  }

  selectModel(model) {
    this.deselectModel();
    this.selectedModel = model;
    this.updateModelView(model);
  }

  deselectModel() {
    if (this.selectedModel) {
      delete this.selectedModel;
    }
  }

  sortClassModels(models) {
    models.sort((left, right) => this.labelSorter(left, right));
  }

  sortDefinitionModels(models) {
    let reference = models.getModels().map(model => model);
    models.sort((left, right) => this.inheritanceSorter(reference, left, right));

    reference = models.getModels().map(model => model);
    models.sort((left, right) => this.orderSorter(reference, left, right));
  }

  inheritanceSorter(reference, left, right) {
    let lhsInherited = ModelManagementUtility.isInherited(left, this.model) | 0;
    let rhsInherited = ModelManagementUtility.isInherited(right, this.model) | 0;

    return lhsInherited - rhsInherited || reference.indexOf(left) - reference.indexOf(right);
  }

  orderSorter(reference, left, right) {
    let lhsOrder = left.getAttribute(ModelAttribute.ORDER_ATTRIBUTE);
    let leftOrder = !ModelManagementUtility.isAttributeEmpty(lhsOrder) ? lhsOrder.getValue().getValue() : Number.MAX_VALUE;

    let rhsOrder = right.getAttribute(ModelAttribute.ORDER_ATTRIBUTE);
    let rightOrder = !ModelManagementUtility.isAttributeEmpty(rhsOrder) ? rhsOrder.getValue().getValue() : Number.MAX_VALUE;

    return leftOrder - rightOrder || reference.indexOf(left) - reference.indexOf(right);
  }

  labelSorter(left, right) {
    let lhsLabel = left.getDescription();
    let rhsLabel = right.getDescription();
    return lhsLabel.getValue().localeCompare(rhsLabel.getValue());
  }
}

ModelFields.ACTION_TYPE_RESOLVER = {};
ModelFields.ACTION_TYPE_RESOLVER[ModelManagementUtility.TYPES.FIELD] = ModelRestoreInheritedFieldAction;
ModelFields.ACTION_TYPE_RESOLVER[ModelManagementUtility.TYPES.REGION] = ModelRestoreInheritedRegionAction;
ModelFields.ACTION_TYPE_RESOLVER[ModelManagementUtility.TYPES.ATTRIBUTE] = ModelRestoreInheritedAttributeAction;
ModelFields.ACTION_TYPE_RESOLVER[ModelManagementUtility.TYPES.CONTROL] = ModelRestoreInheritedControlAction;