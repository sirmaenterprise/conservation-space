import _ from 'lodash';
import {View, Component, Inject, NgElement, NgScope} from 'app/app';
import {Configurable} from 'components/configurable';
import {UrlDecorator} from 'layout/url-decorator/url-decorator';
import {ObjectBrowserRestService} from 'services/rest/object-browser-service';
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
    'config': 'config',
    'context': 'context'
  }
})
@View({
  template: template
})
@Inject(ObjectBrowserRestService, UrlDecorator, NgElement, NgScope)
export class ObjectBrowser extends Configurable {

  constructor(objectBrowserService, urlDecorator, element, $scope) {
    super({
      selectable: false,
      openInNewWindow: false,
      clickableLinks: true
    });

    this.objectBrowserService = objectBrowserService;
    this.urlDecorator = urlDecorator;
    this.element = element;
    this.$scope = $scope;
  }

  ngOnInit() {
    this.path = this.config.nodePath.reverse();
    this.element.jstree("destroy");

    this.element.jstree({
      core: {
        dblclick_toggle: false,
        themes: {
          icons: false
        },
        data: (node, callback) => this.loadNodes(node, callback),
        animation: 0
      },
      plugins: ['checkbox'],
      checkbox: {
        visible: this.config.selectable,
        tie_selection: false,
        whole_node: false,
        // disable cascade checks
        three_state: false
      }
    });

    this.preventDefaultJstreeAnchors(this.element);
    this.enableSingleSelection(this.element);
    this.enableAutoExpansion(this.element);
    this.enableHighlightOfEntityNode(this.element, this.config.id);
  }

  loadNodes(node, callback) {
    var initialLoad = node.id === JSTREE_ROOT_NODE_ID;
    var nodePath = initialLoad ? this.config.rootId : node.data;
    this.objectBrowserService.getChildNodes(nodePath, this.config).then((data) => {

      // If the element is destroyed before it's full initialization, we have to stop the loading of nodes
      // manually. Otherwise the element will have a reference to the jstree and this may cause a memory leak.
      // So we check if the element is detached from the DOM.
      if (!$.contains(document, this.element[0])) {
        return;
      }

      var treeData;

      if (initialLoad) {
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
        text: entry.text
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
      }

      if (!this.expanded) {
        this.path.forEach(function (element) {
          var node = data.instance.get_node(element.id);
          if (node) {
            data.instance.open_node(node, false);
            return false;
          }
        })
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
      this.urlDecorator.decorate(event);
      event.stopPropagation();
    });

    element.on('click', 'a.jstree-anchor', function (event) {
      event.preventDefault();
    });
  }

  ngOnDestroy() {
    this.element.jstree('destroy');
    this.element.remove();
  }
}