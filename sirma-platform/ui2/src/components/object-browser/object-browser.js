import _ from 'lodash';
import {View, Component, Inject, NgElement, NgScope} from 'app/app';
import {Configurable} from 'components/configurable';
import {UrlDecorator} from 'layout/url-decorator/url-decorator';
import {InstanceCreatedEvent} from 'idoc/events/instance-created-event';
import {Eventbus} from 'services/eventbus/eventbus';
import {ActionExecutedEvent} from 'services/actions/events';
import 'vakata/jstree';
import 'font-awesome/css/font-awesome.css!';
import template from './object-browser.html!text';
import './object-browser.css!';

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
    'context': 'context'
  },
  events: ['onNodeSelected']
})
@View({
  template: template
})
@Inject(UrlDecorator, Eventbus, NgElement, NgScope)
export class ObjectBrowser extends Configurable {

  constructor(urlDecorator, eventbus, element, $scope) {
    super({
      selectable: false,
      enableSearch: false,
      openInNewWindow: false,
      clickableLinks: true,
      preventLinkRedirect: false
    });

    this.urlDecorator = urlDecorator;
    this.eventbus = eventbus;
    this.element = element;
    this.$scope = $scope;
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
    //copy & then reverse the current nodePath
    this.path = this.config.nodePath.slice().reverse();
    this.treeElement = this.element.find('.tree-browser');
    this.treeElement.jstree('destroy');
    this.treeElement.jstree(this.createJsTreeConfig());

    this.onNodeSelection(this.treeElement);
    this.preventDefaultJstreeAnchors(this.treeElement);
    this.enableSingleSelection(this.treeElement);
    this.enableAutoExpansion(this.treeElement);
    this.enableHighlightOfEntityNode(this.treeElement, this.config.id);
  }

  search() {
    this.treeElement.jstree('close_all');
    this.treeElement.jstree(true).search(this.searchQuery);
  }

  createJsTreeConfig() {
    return {
      core: {
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
    var initialLoad = node.id === JSTREE_ROOT_NODE_ID;
    var nodePath = initialLoad ? this.config.rootId : node.data;
    this.loader.getNodes(nodePath, this.config).then((data) => {
      // If the element is destroyed before it's full initialization, we have to stop the loading of nodes
      // manually. Otherwise the element will have a reference to the jstree and this may cause a memory leak.
      // So we check if the element is detached from the DOM.
      if (!$.contains(document, this.element[0])) {
        return;
      }

      var treeData;

      if (initialLoad && this.config.rootId && this.config.rootText) {
        var foundElement = this.findElement(data, this.config.id);

        if (foundElement != null) {
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
      var result = {
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

      var style = 'instance-link';

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
        var result = this.findElement(element.children, id);
        if (result != null) {
          return result;
        }
      }
    }
    return null;
  }

  onNodeSelection(element) {
    if (this.onNodeSelected) {
      element.on('select_node.jstree', (event, data) => {
        this.onNodeSelected({node: data.node});
      });
    }
  }

  enableSingleSelection(element) {
    if (this.config.selectable) {
      element.on('check_node.jstree', (event, data) => {
        var selectedNode = data.node;

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
      if (data.node.id === this.config.id && !this.expanded) {
        this.expanded = true;

        // open the node and expand the tree upwards
        data.instance._open_to(data.node, false);
        data.instance.open_node(data.node, false);
        document.getElementById(this.config.id).scrollIntoView();
      }

      if (!this.expanded) {
        this.path.some(function (element) {
          var node = data.instance.get_node(element.id || element);
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

  enableHighlightOfEntityNode(element, id) {
    // redraw.jstree is also used because jstree sometimes redraws the nodes causing them to loose existing highlight
    element.on('after_open.jstree redraw.jstree', function () {
      $(document.getElementById(id)).find('.instance-link:first').addClass('highlighted');
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
    this.treeElement.jstree('destroy');
    this.element.remove();
    if (this.actionExecutedHandler) {
      this.actionExecutedHandler.unsubscribe();
    }
    if (this.instanceCreatedHandler) {
      this.instanceCreatedHandler.unsubscribe();
    }
  }
}