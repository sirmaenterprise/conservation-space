import {View, Component, Inject} from 'app/app';
import {Configurable} from 'components/configurable';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {ModelTreeService} from 'administration/model-management/services/model-tree-service';
import 'components/object-browser/object-browser';

import template from './model-tree.html!text';

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
    'config': 'config'
  },
  events: ['onSelectedNode']
})
@View({
  template
})
@Inject(ModelTreeService, PromiseAdapter)
export class ModelTree extends Configurable {

  constructor(modelTreeService, promiseAdapter) {
    super({
      nodePath: [],
      selectable: false,
      enableSearch: true,
      openInNewWindow: false,
      preventLinkRedirect: true,
    });
    this.promiseAdapter = promiseAdapter;
    this.modelTreeService = modelTreeService;
  }

  ngOnInit() {
    this.tree = this.modelTreeService.getTree(this.model);
    this.loader = this.getObjectBrowserLoader(this.tree);
    this.config.nodePath = this.getCurrentNodePath(this.tree);
  }

  getObjectBrowserLoader(tree) {
    return {
      getNodes: () => this.promiseAdapter.resolve(tree)
    };
  }

  onNodeSelected(node) {
    this.onSelectedNode && this.onSelectedNode({node});
  }

  getCurrentNodePath(tree) {
    return this.modelTreeService.findNodePathById(tree, this.config.node) || this.modelTreeService.findRootNodePath(tree);
  }
}