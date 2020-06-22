import {Component, Inject, View} from 'app/app';
import {Router} from 'adapters/router/router';
import {EventEmitter} from 'common/event-emitter';
import {TranslateService} from 'services/i18n/translate-service';
import {DialogService} from 'components/dialog/dialog-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {StateParamsAdapter} from 'adapters/router/state-params-adapter';

import {ModelList} from 'administration/model-management/model/model-list';
import {ModelEvents} from 'administration/model-management/model/model-events';

import {ModelCreatePropertyAction} from 'administration/model-management/actions/create/model-create-property-action';
import {ModelChangeAttributeAction} from 'administration/model-management/actions/change/model-change-attribute-action';

import {ModelActionFactory} from 'administration/model-management/actions/model-action-factory';
import {ModelActionProcessor} from 'administration/model-management/actions/model-action-processor';

import {ModelManagementModelsService} from 'administration/model-management/services/utility/model-management-models-service';
import {ModelManagementUtility} from 'administration/model-management/utility/model-management-utility';
import {ModelManagementStateRegistry} from 'administration/model-management/services/model-management-state-registry';

import _ from 'lodash';

import 'components/tabs/tabs';

import 'administration/model-management/sections/field/model-fields';
import 'administration/model-management/sections/action/model-actions';
import 'administration/model-management/sections/general/model-general';
import 'administration/model-management/sections/header/model-headers';

import 'administration/model-management/components/tree/model-tree';
import 'administration/model-management/components/controls/deploy/model-deploy';

import './model-management.css!css';
import template from './model-management.html!text';

export const MODEL_HEADERS_TAB = 'headers';
export const MODEL_FIELDS_TAB = 'fields';
export const MODEL_GENERAL_TAB = 'general';
export const MODEL_ACTIONS_TAB = 'actions';

export const MODEL_MANAGEMENT_QUERY_PARAMETER = 'model';
export const MODEL_MANAGEMENT_SECTION_QUERY_PARAMETER = 'section';
export const MODEL_MANAGEMENT_EXTENSION_POINT = 'model-management';

export const MODIFIED_SECTION_CLASS = 'modified-section';

/**
 * Administration page for the model management tool. Serves as an entry point to the
 * model management page. Wraps a {@link ModelTree} browser and a model management panel.
 *
 * @author Svetlozar Iliev
 */
@Component({
  selector: 'seip-model-management'
})
@View({
  template
})
@Inject(ModelManagementModelsService, ModelActionFactory, ModelActionProcessor, ModelManagementStateRegistry, StateParamsAdapter, TranslateService, PromiseAdapter, Router, DialogService)
export class ModelManagement {

  constructor(modelManagementModelsService, modelActionFactory, modelActionProcessor, modelManagementStateRegistry, stateParamsAdapter, translateService, promiseAdapter, router, dialogService) {
    // base services used for model management process
    this.modelManagementModelsService = modelManagementModelsService;
    this.modelManagementStateRegistry = modelManagementStateRegistry;
    this.modelActionProcessor = modelActionProcessor;
    this.modelActionFactory = modelActionFactory;

    // utility services aiding model management
    this.stateParamsAdapter = stateParamsAdapter;
    this.translateService = translateService;
    this.promiseAdapter = promiseAdapter;
    this.router = router;
    this.dialogService = dialogService;

    // handles component communication
    this.emitter = new EventEmitter();
  }

  ngOnInit() {
    this.initSectionsConfig();
    this.initActionHandlers();
    this.initModelTreeConfig();
  }

  initActionHandlers() {
    this.actionHandlers = {};
    this.actionHandlers[ModelChangeAttributeAction.getType()] = action => this.notifyForModelAttributeChange(action.getModel());
    this.actionHandlers[ModelCreatePropertyAction.getType()] = action => this.modelManagementModelsService.addModel(action.getModel());
  }

  initModelTreeConfig() {
    return this.modelManagementModelsService.getModels(true).then(() => this.promiseAdapter.all([
      this.modelManagementModelsService.getMetaData(),
      this.modelManagementModelsService.getHierarchy()
    ])).then(([metaData, hierarchy]) => {
      let model = this.getCurrentModel();

      this.metaData = metaData;
      this.treeModel = hierarchy;
      this.treeConfig = {id: model};
      model && this.onSelectedNode(this.treeConfig);
    });
  }

  initSectionsConfig() {
    this.modelManagementStateRegistry.clearSectionStates();
    let currentSection = this.getCurrentSection();
    this.modelSectionsConfig = {
      mapping: {},
      activeTab: currentSection,
      tabs: [
        this.getSectionTab(MODEL_GENERAL_TAB),
        this.getSectionTab(MODEL_FIELDS_TAB),
        this.getSectionTab(MODEL_ACTIONS_TAB),
        this.getSectionTab(MODEL_HEADERS_TAB)
      ]
    };
    this.modelSectionsConfig.tabs.forEach(tab => this.modelSectionsConfig.mapping[tab.id] = tab);
    this.modifiedMessage = this.translateService.translateInstant('administration.models.management.tabs.modified');
  }

  loadModel(id) {
    return this.modelManagementModelsService.getModel(id);
  }

  onModelActionCreateRequested(type, args) {
    return this.modelActionFactory.create(type.getType(), ...args);
  }

  onModelActionExecuteRequested(actions) {
    let result = this.modelActionProcessor.execute(actions);
    this.modelActionProcessor.on(actions, this.actionHandlers);
    return result;
  }

  onModelActionRevertRequested(actions) {
    let result = this.modelActionProcessor.restore(actions);
    this.modelActionProcessor.on(actions, this.actionHandlers);
    return result;
  }

  /**
   * @returns a Promise resolving to an instance of {@link ModelDeployRequest}
   */
  onModelDeployRequested() {
    return this.modelManagementModelsService.getDeploymentModels().then(deploymentRequest => {
      // compute the correct models to be selected for the deployment process
      let selectedModels = this.getDeploymentSelection(deploymentRequest.getModels());
      deploymentRequest.setSelectedModels(selectedModels);
      return deploymentRequest;
    });
  }

  /**
   * @param deploymentRequest an instance of {@link ModelDeployRequest}
   */
  onModelDeployConfirmed(deploymentRequest) {
    // after deployment reload the model management to make sure all models are up to date
    return this.modelManagementModelsService.deploy(deploymentRequest).then(() => this.updateRouterState(true));
  }

  onModelSave(actions) {
    // collect only the valid changes from actions required to be saved filter out any invalid change sets
    let changes = this.modelActionProcessor.changeset(actions).filter(r => !_.isUndefined(r) && !_.isNull(r));

    return this.modelManagementModelsService.save(changes);
  }

  onModelLoad(model, navigate) {
    return !navigate ? this.loadModel(model.getId()) : this.onSelectedNode({id: model.getId()});
  }

  onModelStateChange(model, state) {
    // when a model dirty state is changed
    this.notifyForModelStateChange(model, state);
  }

  onSectionStateChange(isDirty, source) {
    let tab = this.getSectionTabById(source);

    if (tab) {
      this.setTabModifiedState(tab, isDirty);
      this.modelManagementStateRegistry.setSectionState(tab.id, isDirty);
    }
  }

  onSelectedNode(node) {
    this.setCurrentModel(node.id);
    this.updateRouterState();
    return this.loadModel(node.id)
      .then(this.onModelLoadSuccess.bind(this))
      .catch(this.onModelLoadFail.bind(this));
  }

  onSectionChange(section) {
    this.setCurrentSection(section.id);
    this.updateRouterState();
  }

  updateRouterState(reload = false) {
    this.router.navigate(MODEL_MANAGEMENT_EXTENSION_POINT, this.stateParamsAdapter.getStateParams(), {reload});
  }

  onModelLoadSuccess(response) {
    this.missingModel = false;
    this.model = response.getModel();
    this.notifyForModelChange(this.model);
  }

  onModelLoadFail() {
    this.missingModel = true;
  }

  notifyForModelChange(model) {
    this.emitter.publish(ModelEvents.MODEL_CHANGED_EVENT, model);
  }

  notifyForModelStateChange(model, state) {
    this.emitter.publish(ModelEvents.MODEL_STATE_CHANGED_EVENT, {model, state});
  }

  notifyForModelAttributeChange(attribute) {
    this.emitter.publish(ModelEvents.MODEL_ATTRIBUTE_CHANGED_EVENT, attribute);
  }

  getCurrentModel() {
    return this.stateParamsAdapter.getStateParam(MODEL_MANAGEMENT_QUERY_PARAMETER);
  }

  getCurrentSection() {
    return this.stateParamsAdapter.getStateParam(MODEL_MANAGEMENT_SECTION_QUERY_PARAMETER);
  }

  getSectionTab(id) {
    return {
      id,
      label: `administration.models.management.tabs.${id}`
    };
  }

  getSectionTabById(id) {
    return this.modelSectionsConfig.mapping[id];
  }

  getDeploymentSelection(models) {
    let list = new ModelList();
    if (this.model && models.size()) {
      // insert the currently selected model to the selection list
      models.hasModel(this.model.getId()) && list.insert(this.model);

      if (ModelManagementUtility.isModelDefinition(this.model)) {
        // insert the definition type or it's actual class to the selection list as well
        models.hasModel(this.model.getType().getId()) && list.insert(this.model.getType());
      }
    }
    return list;
  }

  isSectionVisible(section) {
    return this.modelSectionsConfig.activeTab === section;
  }

  isModelLoading() {
    return this.isModelSelected() && (!this.model || !this.model.isLoaded());
  }

  isModelSelected() {
    return !!this.getCurrentModel();
  }

  isModelMissing() {
    return this.missingModel;
  }

  isModelPresent() {
    return this.missingModel || !this.isModelSelected() || !this.model;
  }

  setCurrentModel(model) {
    this.stateParamsAdapter.setStateParam(MODEL_MANAGEMENT_QUERY_PARAMETER, model);
  }

  setCurrentSection(section) {
    this.stateParamsAdapter.setStateParam(MODEL_MANAGEMENT_SECTION_QUERY_PARAMETER, section);
  }

  setTabModifiedState(tab, isDirty) {
    if (!isDirty) {
      delete tab.classes;
      tab.postfix = () => '';
    } else {
      tab.classes = MODIFIED_SECTION_CLASS;
      tab.postfix = () => `<span class="message">(${this.modifiedMessage})</span>`;
    }
  }

  ngOnDestroy() {
    this.emitter && this.emitter.unsubscribeAll();
  }
}