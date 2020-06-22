import {View, Component, Inject, NgElement, NgScope, NgCompile} from 'app/app';
import {Configurable} from 'components/configurable';
import {UrlDecorator} from 'layout/url-decorator/url-decorator';
import {InstanceCreatedEvent} from 'idoc/events/instance-created-event';
import {Eventbus} from 'services/eventbus/eventbus';
import {ActionExecutedEvent} from 'services/actions/events';
import _ from 'lodash';

import 'vakata/jstree';
import 'font-awesome/css/font-awesome.css!';

import template from './object-browser.html!text';
import './object-browser.css!';

export const NODE_SELECT_EVENT = 'NODE_SELECT_EVENT';
export const NODE_SET_TEXT_EVENT = 'NODE_SET_TEXT_EVENT';
export const NODE_ADD_CLASS_EVENT = 'NODE_ADD_CLASS_EVENT';
export const NODE_REMOVE_CLASS_EVENT = 'NODE_REMOVE_CLASS_EVENT';

const SELECT_NODE_STYLE = 'jstree-selected';
const HIDE_CHECKBOX_STYLE = 'hide-checkbox';
const JSTREE_ROOT_NODE_ID = '#';

/**
 * Builds a tree structure using https://www.jstree.com/ from a backend model.
 *
 * Config:
 *  - id - id of the entity that is root of the tree.
 *  - rootId: id of the root entity of the current context.
 *  - rootText: text representing the root entity
 *  - openInNewWindow - if true, the links displayed in the links open a new window. Default: true
 *  - clickableLinks: if true, the tree nodes contain clickable links for opening the entity. Default: true,
 *  - selectable - if true, the browser allows nodes to be selected. Default: false.
 *  - onSelectionChanged: event handler called when the tree selection gets changed.
 *  - nodePath - array containing the path from the root to the currently displayed node.
 */
@Component({
  selector: 'seip-object-browser',
  properties: {
    'loader': 'loader',
    'config': 'config',
    'context': 'context',
    'emitter': 'emitter'
  },
  events: ['onNodeSelected']
})
@View({template})
@Inject(UrlDecorator, Eventbus, NgElement, NgScope, NgCompile)
export class ObjectBrowser extends Configurable {

  constructor(urlDecorator, eventbus, $element, $scope, $compile) {
    super({
      selectable: false,
      enableSearch: false,
      openInNewWindow: false,
      clickableLinks: true,
      preventLinkRedirect: false
    });

    this.urlDecorator = urlDecorator;
    this.eventbus = eventbus;
    this.$element = $element;
    this.$scope = $scope;
    this.$compile = $compile;
    this.eventHandlers = [];
  }

  ngOnInit() {
    this.init();
    this.actionExecutedHandler = this.eventbus.subscribe(ActionExecutedEvent, (action) => {
      // forceRefresh is an action's indicator, that this action does some entity changes.
      // Used to refresh jstree after action execution
      if (action.action.forceRefresh) {
        this.expanded = false;
        this.init();
      }
    });
    this.instanceCreatedHandler = this.eventbus.subscribe(InstanceCreatedEvent, () => {
      // Used to refresh jstree after instance creation
      this.expanded = false;
      this.init();
    });
  }

  init() {
    this.unSubscribeToNodeEvents();
    //copy & then reverse the current nodePath
    this.path = this.config.nodePath.slice().reverse();
    this.treeElement = this.$element.find('.tree-browser');
    this.treeElement.jstree('destroy');
    this.treeElement.jstree(this.createJsTreeConfig());

    this.onNodeSelection(this.treeElement);
    this.preventDefaultJstreeAnchors(this.treeElement);
    this.enableSingleSelection(this.treeElement);
    this.enableAutoExpansion(this.treeElement);
    this.compileNodesBeforeOpen(this.treeElement);
    this.enableActionsAfterLoaded(this.treeElement);
  }

  subscribeToNodeEvents() {
    this.eventHandlers.push(this.emitter.subscribe(NODE_SELECT_EVENT, data => this.selectNode(data.id, data.text)));
    this.eventHandlers.push(this.emitter.subscribe(NODE_SET_TEXT_EVENT, data => this.renameNode(data.id, data.text)));
    this.eventHandlers.push(this.emitter.subscribe(NODE_ADD_CLASS_EVENT, data => this.addClassToNode(data.id, data.clazz)));
    this.eventHandlers.push(this.emitter.subscribe(NODE_REMOVE_CLASS_EVENT, data => this.removeClassFromNode(data.id, data.clazz)));
  }

  unSubscribeToNodeEvents() {
    this.eventHandlers.forEach((handler) => {
      handler && handler.unsubscribe();
    });
    this.eventHandlers = [];
  }

  getNode(id) {
    // chances are same node will be accessed sequentially
    if (!this.nodeCache || this.nodeCache.id !== id) {
      let jsNode = this.treeElement.jstree(true).get_node(id);
      let domNode = $(document.getElementById(jsNode.a_attr.id));
      this.nodeCache = {id, jsNode, domNode};
    }
    // fetch the js tree node
    return this.nodeCache.jsNode;
  }

  isDirty(node) {
    return 'dirty' === node.type;
  }

  search() {
    let query = this.searchQuery;
    let jstree = this.treeElement.jstree(true);
    jstree.close_all();

    if (!query.length) {
      let root = this.config.nodePath[0];
      let node = this.getNode(root);
      jstree.show_all();
      jstree.open_node(node.id);
    } else {
      jstree.search(query);
    }
  }

  redraw() {
    this.treeElement.jstree(true).redraw(true);
  }

  addClassToNode(id, clazz, redraw = true) {
    let node = this.getNode(id);
    node.a_attr.class += ` ${clazz}`;
    redraw && this.redraw();
    return node;
  }

  removeClassFromNode(id, clazz, redraw = true) {
    let node = this.getNode(id);
    let regex = new RegExp(`\\b${clazz}\\b`);
    node.a_attr.class = node.a_attr.class.replace(regex, '');
    redraw && this.redraw();
    return node;
  }

  renameNode(id, text, redraw = true) {
    let node = this.getNode(id);
    node.text = text;
    redraw && this.redraw();
    return node;
  }

  selectNode(id, redraw = true) {
    // make sure not to select the same tree node again
    if (this.selectedNode && this.selectedNode.id === id) {
      return this.selectedNode;
    }

    // deselect previous node
    if (this.selectedNode) {
      let current = this.selectedNode.id;
      // remove the selection styling from the selected tree node
      this.removeClassFromNode(current, SELECT_NODE_STYLE, false);
    }

    // select the new tree node by adding a the selection styling to it
    this.selectedNode = this.addClassToNode(id, SELECT_NODE_STYLE, false);
    this.treeElement.jstree(true)._open_to(this.selectedNode, false);
    redraw && this.redraw();
    return this.selectedNode;
  }

  createJsTreeConfig() {
    return {
      core: {
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
      plugins: ['checkbox', 'search'],
      checkbox: {
        visible: this.config.selectable,
        tie_selection: false,
        whole_node: false,
        // disable cascade checks
        three_state: false
      }
    };
  }

  loadNodes(node, callback) {
    let initialLoad = node.id === JSTREE_ROOT_NODE_ID;
    let nodePath = initialLoad ? this.config.rootId : node.data;
    this.loader.getNodes(nodePath, this.config).then((data) => {
      // If the element is destroyed before it's full initialization, we have to stop the loading of nodes
      // manually. Otherwise the element will have a reference to the jstree and this may cause a memory leak.
      // So we check if the element is detached from the DOM.
      if (!$.contains(document, this.$element[0])) {
        return;
      }

      let treeData;

      if (initialLoad && this.config.rootId && this.config.rootText) {
        let foundElement = this.findElement(data, this.config.id);

        if (foundElement !== null) {
          this.nodeContextPath = foundElement.id;
        } else {
          //usually means that the opened entity is the root of the context
          this.nodeContextPath = this.config.id;
        }

        treeData = [{
          id: this.config.rootId,
          dbId: this.config.rootId,
          text: this.config.rootText,
          icon: this.config.rootIcon,
          children: data
        }];
      } else {
        treeData = data;
      }

      callback(this.adaptModel(treeData));
    });
  }

  /**
   * Transforms backend model to node entries requred by jstree.
   * @param entries list of model entries to transform.
   * @return transformed model
   */
  adaptModel(entries) {
    return entries.map((entry) => {
      let result = {
        id: entry.dbId,
        data: entry.id,
        text: entry.text,
        icon: entry.icon
      };

      if (_.isArray(entry.children)) {
        result.children = this.adaptModel(entry.children);
      } else {
        result.children = !entry.leaf;
      }

      let style = 'instance-link';

      if (_.isUndefined(entry.checked)) {
        style = style + ' ' + HIDE_CHECKBOX_STYLE;
      }

      result.a_attr = {
        'class': style
      };

      return result;
    });
  }

  findElement(entries, id) {
    for (let element of entries) {
      if (element.dbId === id) {
        return element;
      }

      if (element.children) {
        let result = this.findElement(element.children, id);
        if (result !== null) {
          return result;
        }
      }
    }
    return null;
  }

  onNodeSelection(element) {
    element.on('select_node.jstree', (event, data) => {
      this.selectNode(data.node.id);
      this.onNodeSelected && this.onNodeSelected({node: data.node});
    });
  }

  enableActionsAfterLoaded(element) {
    element.bind('ready.jstree', () => {
      this.emitter && this.subscribeToNodeEvents();
      this.config && this.config.id && this.selectNode(this.config.id);

      this.compileNodes();
    });
  }

  compileNodesBeforeOpen(element) {
    element.on('before_open.jstree', () => {
      this.compileNodes();
    });
  }

  compileNodes() {
    // Tree nodes may contain instance links which are expected to trigger tooltip header loading,
    // but in order to do that, they need to be compiled first. The tooltip directive is bound
    // to instance-link css class. We only compile links which are not compiled already.
    this.$element.find('.instance-link.has-tooltip:not(.ng-scope)').each((index, elem) => {
      let link = this.$compile(elem)(this.$scope.$new());
      $(elem).replaceWith(link);
    });
  }

  enableSingleSelection(element) {
    if (this.config.selectable) {
      element.on('check_node.jstree', (event, data) => {
        let selectedNode = data.node;

        data.selected.forEach(function (current) {
          if (current !== selectedNode.id) {
            data.instance.uncheck_node(current);
          }
        });

        if (_.isFunction(this.config.onSelectionChanged)) {
          // a digest cycle has to be initiated because the tree is outside of angular control
          this.$scope.$apply(() => {
            this.config.onSelectionChanged(data.node.id, data.node.data);
          });
        }
      });
    }
  }

  enableAutoExpansion(element) {
    element.on('load_node.jstree', (event, data) => {
      this.compileNodes();

      if (data.node.id === this.config.id && !this.expanded) {
        this.expanded = true;

        // open the node and expand the tree upwards
        data.instance._open_to(data.node, false);
        data.instance.open_node(data.node, false);
        document.getElementById(this.config.id).scrollIntoView();
      }

      if (!this.expanded) {
        this.path.some(element => {
          let node = data.instance.get_node(element.id || element);
          if (node) {
            data.instance._open_to(node, false);
            data.instance.open_node(node, false);
            return true;
          }
          return false;
        });
      }
    });
  }

  /**
   * Adds an event handler that stops the bubbling for nested anchors.
   * JsTree adds an anchor for each node and nests the node content in it
   * and that breaks an anchor added by the content of the node.
   *
   * @param element jstree element
   */
  preventDefaultJstreeAnchors(element) {
    element.on('click', 'a a', (event) => {
      if (this.config.preventLinkRedirect) {
        event.preventDefault();
      } else {
        this.urlDecorator.decorate(event);
        event.stopPropagation();
      }
    });

    element.on('click', 'a.jstree-anchor', function (event) {
      event.preventDefault();
    });
  }

  ngOnDestroy() {
    this.unSubscribeToNodeEvents();
    this.treeElement.jstree('destroy');
    this.$element.remove();
    if (this.actionExecutedHandler) {
      this.actionExecutedHandler.unsubscribe();
    }
    if (this.instanceCreatedHandler) {
      this.instanceCreatedHandler.unsubscribe();
    }
  }
}
