import {View, Component, Inject, NgTimeout} from 'app/app';
import {ModelEvents} from 'administration/model-management/model/model-events';
import {ModelSection} from '../model-section';
import {EventEmitter} from 'common/event-emitter';
import {TREE_NODE_SELECTED} from 'administration/model-management/components/action-group-tree/model-actions-tree';
import {ModelManagementUtility} from 'administration/model-management/utility/model-management-utility';
import {ModelActionGroup} from 'administration/model-management/model/model-action-group';
import {ModelAction} from 'administration/model-management/model/model-action';
import {ModelList} from 'administration/model-management/model/model-list';
import {ModelDefinition} from 'administration/model-management/model/model-definition';

import 'administration/model-management/sections/action/action-groups/model-action-group-view';
import 'administration/model-management/sections/action/actions/model-action-view';
import {TranslateService} from 'services/i18n/translate-service';

import './model-actions.css!css';
import template from './model-actions.html!text';

export const ROOT_ACTION_GROUP_NODE = 'ROOT_ACTION_GROUP_NODE';

/**
 * Represents wrapper class.
 * Used to control the view and configuration of the actions and groups tree {@see ModelActionsTree}.
 *
 * @author T. Dossev
 */
@Component({
  selector: 'seip-model-actions',
  properties: {
    'model': 'model',
    'emitter': 'emitter'
  },
  events: [
    'onModelSave',
    'onSectionStateChange',
    'onModelStateChange',
    'onModelActionCreateRequest',
    'onModelActionExecuteRequest',
    'onModelActionRevertRequest'
  ]
})
@View({
  template
})
@Inject(NgTimeout, TranslateService)
export class ModelActions extends ModelSection {

  constructor($timeout, translateService) {
    super({
      nodePath: [ROOT_ACTION_GROUP_NODE],
      enableSearch: true,
      id: ROOT_ACTION_GROUP_NODE,
      rootText: translateService.translateInstant('administration.models.management.actions.root.node')
    });
    this.$timeout = $timeout;
  }

  ngOnInit() {
    this.actionEmitter = new EventEmitter();
    this.eventHandlers = [];
    this.subscribeToModelChanged();
  }

  subscribeToModelChanged() {
    if (this.emitter) {
      this.eventHandlers.push(this.emitter.subscribe(ModelEvents.MODEL_ATTRIBUTE_CHANGED_EVENT, (model) => this.refreshActionGroupTree(model)));
      this.eventHandlers.push(this.emitter.subscribe(ModelEvents.MODEL_CHANGED_EVENT, (model) => this.initAfterModelChange(model)));
      this.eventHandlers.push(this.emitter.subscribe(ModelEvents.MODEL_STATE_CHANGED_EVENT, (data) => this.unselectAction(data.model, data.state)));
      this.eventHandlers.push(this.actionEmitter.subscribe(TREE_NODE_SELECTED, (placeholder) => this.selectionChanged(placeholder)));
    }
  }

  initAfterModelChange(model) {
    this.navigateActionTree(model);
    this.deselectPreviousModel();
  }

  navigateActionTree(model) {
    this.model = this.getActualModel(model);
    this.actionEmitter.publish(ModelEvents.MODEL_CHANGED_EVENT, this.model);
  }

  refreshActionGroupTree(model) {
    if (this.isModelAction(model.parent) || this.isModelActionGroup(model.parent)) {
      this.actionEmitter.publish(ModelEvents.MODEL_ATTRIBUTE_CHANGED_EVENT, [model, this.model]);
    }
  }

  unselectAction(model, isDirty) {
    if (!isDirty && (model instanceof ModelDefinition)) {
      this.actionEmitter.publish(ModelEvents.MODEL_STATE_CHANGED_EVENT, model);
    }
  }

  selectionChanged(placeholder) {
    // synchronize Angular with the DOM manipulations, as isAction is used in the tree build process.
    this.$timeout(() => {
      this.isAction = placeholder.action;
      this.selectionId = placeholder.id;
      this.selectedModel = this.isAction ? this.model.getAction(this.selectionId) : this.model.getActionGroup(this.selectionId);
      this.selectModel(this.selectedModel);
    }, 0);
  }

  isActionModelSelected() {
    return this.isAction !== undefined && this.isAction;
  }

  isActionGroupModelSelected() {
    return this.isAction !== undefined && !this.isAction;
  }

  selectModel(model) {
    // prepare the selected model
    this.deselectPreviousModel();
    this.selectedModel = model;

    // re-compute the state of the current model selection
    this.notifyForModelStateCalculation(model, this.model);
  }

  deselectPreviousModel() {
    if (this.selectedModel) {
      delete this.selectedModel;
    }
  }

  //@Override
  notifyForModelActionExecute(actions) {
    // process attribute changes and retrieve the resulting attribute
    let results = super.notifyForModelActionExecute(actions);
    this.updateSectionSelectionOnActionResults();
    return results;
  }

  //@Override
  notifyForModelActionRevert(actions) {
    // process attribute restores and retrieve the resulting attribute
    let results = super.notifyForModelActionRevert(actions);
    this.updateSectionSelectionOnActionResults();
    return results;
  }

  updateSectionSelectionOnActionResults() {
    this.getModels();

    // get the selection from the list
    let id = this.getSelectedModelId();
    let model = this.models.getModel(id);

    // update the current model selection
    if (this.isSelectionChangeable(model)) {
      this.selectModel(model);
    } else if (!model) {
      this.deselectPreviousModel();
    }
  }

  getModels() {
    this.createModelsListView();
    if (this.isModelDefinition()) {
      let models = this.model.getActions().concat(this.model.getActionGroups());
      models.forEach(m => this.models.insert(m));
    }

    return this.models.getModels();
  }

  createModelsListView() {
    this.models = new ModelList();
  }

  isSelectionChangeable(toSelect) {
    return this.selectedModel !== toSelect && this.selectedModel.getId() === toSelect.getId();
  }

  isModelAction(model) {
    return model instanceof ModelAction;
  }

  isModelActionGroup(model) {
    return model instanceof ModelActionGroup;
  }

  isModelDefinition() {
    return ModelManagementUtility.isModelDefinition(this.model);
  }

  //@Override
  getSectionModels() {
    return this.model;
  }

  getSelectedModelId() {
    return this.selectedModel && this.selectedModel.getId();
  }

  unSubscribeAllHandlers() {
    this.eventHandlers.forEach(handler => handler && handler.unsubscribe());
    this.eventHandlers = [];
  }

  ngOnDestroy() {
    this.unSubscribeAllHandlers();
  }
}