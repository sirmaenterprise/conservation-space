import {Component, Inject, NgElement, NgScope, NgTimeout, View} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {ModelActionsTreeService} from './model-actions-tree-service';
import {ObjectBrowser} from 'components/object-browser/object-browser';
import {ModelEvents} from 'administration/model-management/model/model-events';
import {ModelAttribute} from 'administration/model-management/model/attributes/model-attribute';
import {ModelActionGroup} from 'administration/model-management/model/model-action-group';
import {ModelAction} from 'administration/model-management/model/model-action';
import {ModelAttributeTypes} from 'administration/model-management/model/attributes/model-attribute-types';

import {UrlDecorator} from 'layout/url-decorator/url-decorator';
import {Eventbus} from 'services/eventbus/eventbus';

import template from './model-actions-tree.html!text';
import 'components/object-browser/object-browser.css!css';
import './model-actions-tree.css!css';

export const REFRESH_TREE_COMMAND = 'tree-refresh-command';
export const TREE_NODE_SELECTED = 'treeNodeSelected';

/**
 * Represents the action tree visualization.
 * Takes care of the order and the hierarchy of the elements.
 * an extended class of the object browser. It uses a modified logic to generate
 * the tree to match the requirements for action and group preview.
 * An emitter is granted to support refresh commands.
 * Wraps the tree into an empty top element for more user-friendly search.
 *
 * @author: T. Dossev
 */
@Component({
  selector: 'seip-model-actions-tree',
  properties: {
    'model': 'model',
    'loader': 'loader',
    'config': 'config',
    'emitter': 'emitter'
  }
})
@View({
  template
})
@Inject(NgElement, NgScope, NgTimeout, UrlDecorator, Eventbus, ModelActionsTreeService, PromiseAdapter)
export class ModelActionsTree extends ObjectBrowser {

  constructor(element, $scope, $timeout, urlDecorator, eventbus, modelActionsTreeService, promiseAdapter) {
    super(urlDecorator, eventbus, element, $scope);
    this.$timeout = $timeout;
    this.modelActionsTreeService = modelActionsTreeService;
    this.promiseAdapter = promiseAdapter;
  }

  ngOnInit() {
    this.initialize(this.model);
  }

  initialize(model) {
    this.searchQuery = '';
    this.setupLoader(model);
    this.unSubscribeAllHandlers();
    this.subscribeToModelChanged();
    super.ngOnInit();
  }

  getObjectBrowserLoader(tree) {
    return {
      getNodes: () => this.promiseAdapter.resolve(tree)
    };
  }

  setupLoader(model) {
    if (model && model.id) {
      let tree = this.modelActionsTreeService.buildHierarchy(model, this.config.id, this.config.rootText);
      this.loader = this.getObjectBrowserLoader(tree);
    } else {
      this.loader = this.getObjectBrowserLoader([]);
    }
  }

  subscribeToModelChanged() {
    if (!this.handlers) {
      this.handlers = [];
    }
    this.handlers.push(this.emitter.subscribe(ModelEvents.MODEL_CHANGED_EVENT, (data) => this.initialize(data)));
    this.handlers.push(this.emitter.subscribe(ModelEvents.MODEL_ATTRIBUTE_CHANGED_EVENT, (data) => this.resolveNodeChange(data)));
    this.handlers.push(this.emitter.subscribe(ModelEvents.MODEL_STATE_CHANGED_EVENT, (data) => this.unselectAction(data)));
  }

  resolveNodeChange([attribute, context]) {
    if (!attribute) {
      return;
    }

    let model = attribute.getParent();
    let nodeId = this.getActionNodeId(model);
    let node = this.getNode(nodeId);

    this.resolveNodeMove(attribute, node, nodeId);
    this.resolveNodeColor(model, context);
    this.renameNode(model, node, context, attribute);
  }

  resolveNodeMove(attribute, node, nodeId) {
    let value = attribute.getValue().getValue();

    let attributeId = attribute.getId();
    if (ModelAttribute.ORDER_ATTRIBUTE === attributeId) {
      node.data.order = value || 0;
      this.treeElement.jstree(true).move_node(this.getNode(nodeId), node.data.parentId, node.data.order);
    } else if (ModelAttribute.GROUP_ATTRIBUTE === attributeId || ModelAttribute.PARENT_ATTRIBUTE === attributeId) {
      let treeActionGroupId = ModelActionsTreeService.getActionGroupTreeId(value);
      if (this.treeElement.jstree(true).get_node(treeActionGroupId)) {
        this.treeElement.jstree(true).move_node(this.getNode(nodeId), treeActionGroupId);
      } else {
        this.treeElement.jstree(true).move_node(this.getNode(nodeId), this.config.id);
      }
      node.data.parentId = value;
    }
  }

  renameNode(model, node, context, attribute) {
    // synchronize Angular with the DOM manipulations
    this.$timeout(() => {
      let isDirtyNode = this.isDirty(node);
      if (ModelAttributeTypes.isLabel(attribute.getType())) {
        if (isDirtyNode && this.isModelDirty(model)) {
          this.treeElement.jstree(true).rename_node(node, ModelActionsTreeService.getText(model, context, isDirtyNode));
        } else if (!isDirtyNode) {
          this.treeElement.jstree(true).rename_node(node, node.original.text);
        } else {
          this.treeElement.jstree(true).rename_node(node, ModelActionsTreeService.getText(model, context, isDirtyNode));
        }
      } else {
        this.treeElement.jstree(true).rename_node(node, ModelActionsTreeService.getText(model, context, isDirtyNode));
      }
    }, 0);
  }

  resolveNodeColor(model, context) {
    // synchronize Angular with the jsTree manipulations
    this.$timeout(() => {
      let node = this.getNode(this.getActionNodeId(model));
      let nodeType = (this.isModelDirty(context) || this.isModelDirty(model)) ? 'dirty' : 'default';
      this.treeElement.jstree(true).set_type(node, nodeType);
    }, 0);
  }

  unselectAction(model) {
    let nodes = model.getActionGroups().concat(model.getActions());
    nodes.forEach(node => {
      let treeNode = this.getNode(this.getActionNodeId(node));
      this.treeElement.jstree(true).set_type(treeNode, 'default');
    });
  }

  getActionNodeId(model) {
    if (model instanceof ModelActionGroup) {
      return ModelActionsTreeService.getActionGroupTreeId(model.getId());
    } else if (model instanceof ModelAction) {
      return ModelActionsTreeService.getActionTreeId(model.getId());
    }
    return model.getId();
  }

  isModelDirty(model) {
    return model.isDirty();
  }

  unSubscribeAllHandlers() {
    this.handlers && this.handlers.forEach(handler => handler && handler.unsubscribe());
    this.handlers = [];
  }

  createJsTreeConfig() {
    return {
      core: {
        cache: true,
        check_callback: true,
        dblclick_toggle: false,
        themes: {
          icons: false
        },
        data: (node, callback) => this.loadNodes(node, callback),
        animation: 0
      },
      search: {
        show_only_matches: true,
        show_only_matches_children: true
      },
      plugins: ['search', 'sort', 'types'],
      sort: ModelActionsTree.getSortFunction(),
      types: {
        default: {
          a_attr: ''
        },
        dirty: {
          a_attr: {
            class: 'modified-node'
          }
        }
      }
    };
  }

  /**
   * The sorting is done according to the orders set in the definition. If the orders are equal or not set,
   * the nodes are sorted by type. Advantage is given to the action when comparing it to a group.
   * Otherwise zero s returned.
   *
   * @returns {Function}
   */
  static getSortFunction() {
    return function (a, b) {
      let nodeA = this.get_node(a);
      let nodeB = this.get_node(b);
      let orderA = nodeA.data.order || Number.MAX_VALUE;
      let orderB = nodeB.data.order || Number.MAX_VALUE;

      if (orderA === orderB) {
        let isActionA = nodeA.data.isAction;
        let isActionB = nodeB.data.isAction;

        // When nodes are different types (eq action or group) we have to order the action first.
        // Otherwise we threat them as equals.
        if (isActionA ^ isActionB) {
          return isActionA ? -1 : 1;
        }
        return 0;
      }
      return orderA > orderB ? 1 : -1;
    };
  }

  adaptModel(entries) {
    return entries;
  }

  search() {
    let query = this.searchQuery;
    let jstree = this.treeElement.jstree(true);

    if (!query.length) {
      jstree.clear_search();
      jstree.open_all();
    } else {
      jstree.close_all();
      jstree.search(query);
    }
  }

  onNodeSelected({node: {data}}) {
    this.emitter.publish(TREE_NODE_SELECTED, {
      id: data.id,
      action: data.isAction,
      parent: data.parentId
    });
  }

  ngOnDestroy() {
    this.unSubscribeAllHandlers();
    super.ngOnDestroy();
  }
}