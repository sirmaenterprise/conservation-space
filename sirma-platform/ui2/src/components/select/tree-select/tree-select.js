import {View, Component, Inject, NgElement, NgScope, NgTimeout} from 'app/app';
import {Select} from 'components/select/select';
import 'clivezhg/select2-to-tree/src/select2totree';
import 'clivezhg/select2-to-tree/src/select2totree.css!';
import './tree-select.css!';
import template from './tree-select.html!text';

/**
 * Selection component that provides options as hierarchical trees.
 * Config parameters:
 * - data - array of objects that represent the options where each object has the following fields
 * -- id - value of the item to be applied to the model when the item is selected
 * -- text - text to be displayed to the user when browsing the options
 * -- children - array of children option objects
 */
@Component({
  selector: 'seip-tree-select',
  properties: {
    'config': 'config',
    'form': 'form'
  }
})
@View({
  template: template
})
@Inject(NgElement, NgScope, NgTimeout)
export class TreeSelect extends Select {
  
  constructor($element, $scope, $timeout) {
    super($element, $scope, $timeout);
  }

  initSelectPlugin(config) {
    config.treeData = {
      dataArr: config.data,
      incFld: 'children'
    };
    
    config.matcher = (query, option) => {
      if (query.term) {
        if (this.hasDescendantWithTitle(option.id, query.term)) {
          return option;
        } else {
          return null;
        }
      }

      // no filtering is applied
      return option;
    };
    
    config.data = null;

    this.$element.select2ToTree(config);
  }

  /**
   * Traverses the hierarchy downwards to find a child matching the text part.
   */
  hasDescendantWithTitle(id, textPart) {
    if (!this.nodeIndex) {
      let nodeIndex = [];
      
      let options = this.actualConfig.treeData.dataArr;

      options.forEach(function traverse(item) {
        nodeIndex[item.id] = item;
        if (item.children) {
          item.children.forEach(traverse);
        }
      });
      
      this.nodeIndex = nodeIndex;
    }
    
    let node = this.nodeIndex[id];
    
    return this.visitNode(node, textPart.toLowerCase());
  }
  
  visitNode(node, textPart) {
    if (node.text && node.text.toLowerCase().includes(textPart)) {
      return true;
    }

    let found = false;
    if (node.children) {
      node.children.forEach(child => {
        if (this.visitNode(child, textPart)) {
          found = true;
          return false;
        }
      });
    }
    
    return found;
  }
  
}