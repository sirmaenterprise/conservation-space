import {View, Component, Inject} from 'app/app';
import {Router} from 'adapters/router/router';
import {ModelEvents} from './model/model-events';
import {EventEmitter} from 'common/event-emitter';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {StateParamsAdapter} from 'adapters/router/state-params-adapter';
import {ModelManagementService} from './services/model-management-service';

import 'components/tabs/tabs';
import 'administration/model-management/components/model-tree';
import 'administration/model-management/sections/field/model-fields';
import 'administration/model-management/sections/general/model-general';

import './model-management.css!css';
import template from './model-management.html!text';

export const MODEL_FIELDS_TAB = 'fields';
export const MODEL_GENERAL_TAB = 'general';

export const MODEL_MANAGEMENT_QUERY_PARAMETER = 'model';
export const MODEL_MANAGEMENT_EXTENSION_POINT = 'model-management';

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
@Inject(ModelManagementService, StateParamsAdapter, Router, PromiseAdapter)
export class ModelManagement {

  constructor(modelManagementService, stateParamsAdapter, router, promiseAdapter) {
    this.router = router;
    this.promiseAdapter = promiseAdapter;
    this.stateParamsAdapter = stateParamsAdapter;
    this.modelManagementService = modelManagementService;
    // handles component communication
    this.emitter = new EventEmitter();
  }

  ngOnInit() {
    this.initSectionsConfig();
    this.initModelTreeConfig();
  }

  initModelTreeConfig() {
    this.promiseAdapter.all([this.modelManagementService.getHierarchy(), this.modelManagementService.getProperties()]).then(response => {
      let model = this.getCurrentModel();
      let hierarchy = response[0];
      this.properties = response[1];
      this.flatTreeModel = hierarchy.flat;
      this.treeModel = hierarchy.tree;
      this.treeConfig = {node: model};
      model && this.loadModel(model);
    });
  }

  initSectionsConfig() {
    this.modelSectionsConfig = {
      tabs: [this.getSectionTab(MODEL_GENERAL_TAB), this.getSectionTab(MODEL_FIELDS_TAB)]
    };
  }

  loadModel(id) {
    let provider = (id) => this.getModel(id, this.flatTreeModel);
    this.modelManagementService.getModel(id, provider).then(model => this.afterModelLoad(model)).catch(() => this.onMissingModel());
  }

  afterModelLoad(model) {
    this.model = model;
    this.missingModel = false;
    this.modelManagementService.linkModel(model, this.properties);
    this.emitter.publish(ModelEvents.MODEL_CHANGED_EVENT, model);
  }

  onMissingModel() {
    this.missingModel = true;
  }

  onSelectedNode(node) {
    this.loadModel(node.id);
    this.setCurrentModel(node.id);
    this.router.navigate(MODEL_MANAGEMENT_EXTENSION_POINT, this.stateParamsAdapter.getStateParams(), {reload: false});
  }

  setCurrentModel(model) {
    this.stateParamsAdapter.setStateParam(MODEL_MANAGEMENT_QUERY_PARAMETER, model);
  }

  getCurrentModel() {
    return this.stateParamsAdapter.getStateParam(MODEL_MANAGEMENT_QUERY_PARAMETER);
  }

  getSectionTab(id) {
    return {
      id,
      label: `administration.models.management.tabs.${id}`
    };
  }

  getModel(id, flatHierarchy) {
    let node = flatHierarchy[id];
    if (node) {
      return node.getRoot();
    }
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
    return this.missingModel || !this.isModelSelected();
  }

  ngOnDestroy() {
    this.emitter && this.emitter.unsubscribeAll();
  }
}