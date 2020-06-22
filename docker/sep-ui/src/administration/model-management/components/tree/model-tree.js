import {View, Component, Inject, NgElement} from 'app/app';
import {Configurable} from 'components/configurable';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {ModelEvents} from 'administration/model-management/model/model-events';
import {ModelAttributeTypes} from 'administration/model-management/model/attributes/model-attribute-types';
import {ModelTreeService} from 'administration/model-management/services/model-tree-service';
import {ModelManagementUtility} from 'administration/model-management/utility/model-management-utility';
import {
  NODE_SELECT_EVENT,
  NODE_ADD_CLASS_EVENT,
  NODE_REMOVE_CLASS_EVENT,
  NODE_SET_TEXT_EVENT
} from 'components/object-browser/object-browser';

import 'common/lib/jquery-ui/jquery-ui.min';
import 'common/lib/jquery-ui/jquery-ui.min.css!';

import './model-tree.css!css';
import template from './model-tree.html!text';

export const MODIFIED_NODE_CLASS = 'modified-node';

/**
 * Component for browsing class or definition models represented as hierarchy. The hierarchy is provided
 * as a separate property to this component, it is required that the hierarchy is an instance of the
 * {@link ModelClassHierarchy} or {@link ModelDefinitionHierarchy}. This component will internally convert
 * the provided hierarchy to a proper intermediate tree structure which will be used as a source for the
 * {@link ObjectBrowser} which is used internally by this component
 *
 * This component also supports custom component events such as:
 *  - onSelectedNode - triggered when a node from the tree is clicked (selected). The node is provided as a payload.
 *
 * @author Svetlozar Iliev
 */
@Component({
  selector: 'seip-model-tree',
  properties: {
    'model': 'model',
    'config': 'config',
    'emitter': 'emitter'
  },
  events: ['onSelectedNode']
})
@View({
  template
})
@Inject(NgElement, ModelTreeService, PromiseAdapter)
export class ModelTree extends Configurable {

  constructor($element, modelTreeService, promiseAdapter) {
    super({
      nodePath: [],
      selectable: false,
      enableSearch: true,
      openInNewWindow: false,
      preventLinkRedirect: true
    });
    this.$element = $element;
    this.promiseAdapter = promiseAdapter;
    this.modelTreeService = modelTreeService;
  }

  ngOnInit() {
    this.subscribeToModelModificationsEvent();
    this.tree = this.modelTreeService.getTree(this.model);
    this.loader = this.getObjectBrowserLoader(this.tree);
    this.config.nodePath = this.getCurrentNodePath(this.tree);
    this.initResizible();
  }

  getObjectBrowserLoader(tree) {
    return {
      getNodes: () => this.promiseAdapter.resolve(tree)
    };
  }

  getCurrentNodePath(tree) {
    return this.modelTreeService.findNodePathById(tree, this.config.id) || this.modelTreeService.findRootNodePath(tree);
  }

  subscribeToModelModificationsEvent() {
    if (this.emitter) {
      this.emitter.subscribe(ModelEvents.MODEL_CHANGED_EVENT, (data) => this.resolveNodeSelection(data));
      this.emitter.subscribe(ModelEvents.MODEL_ATTRIBUTE_CHANGED_EVENT, (data) => this.resolveNodeText(data));
      this.emitter.subscribe(ModelEvents.MODEL_STATE_CHANGED_EVENT, (data) => this.resolveNodeColor(data.model, data.state));
    }
  }

  initResizible() {
    this.$element.resizable({
      minWidth: 200,
      handles: 'e',
      start: () => {
        $('body').addClass('dragging');
        this.$element.css('z-index', '100');
      },
      stop: () => {
        $('body').removeClass('dragging');
        this.$element.css('z-index', '');
      }
    });
  }

  resolveNodeSelection(model) {
    // since the tree only supports and displays some types
    if (!model || !this.isModelSupportedByTree(model)) {
      return;
    }
    this.emitter.publish(NODE_SELECT_EVENT, {id: model.getId()});
  }

  resolveNodeColor(model, state) {
    // since the tree only supports and displays some types
    if (!model || !this.isModelSupportedByTree(model)) {
      return;
    }

    let event = state ? NODE_ADD_CLASS_EVENT : NODE_REMOVE_CLASS_EVENT;
    this.emitter.publish(event, {id: model.getId(), clazz: MODIFIED_NODE_CLASS});
  }

  resolveNodeText(attribute) {
    // make sure proper attribute is passed and it is strictly a label which is owned by model type supported by the model tree
    if (!attribute || !ModelAttributeTypes.isLabel(attribute.getType()) || !this.isModelSupportedByTree(attribute.getParent())) {
      return;
    }

    let name = attribute.getValue();
    let model = attribute.getParent();
    this.emitter.publish(NODE_SET_TEXT_EVENT, {id: model.getId(), text: name.getValue()});
  }

  isModelSupportedByTree(model) {
    return ModelManagementUtility.isOwningType(model);
  }

  onNodeSelected(node) {
    this.onSelectedNode && this.onSelectedNode({node});
  }
}