import {Injectable} from 'app/app';

/**
 * Service providing functionality allowing transitioning between {@link ModelHierarchy}
 * and a tree structure representation used by JSTree. This service is mainly used when
 * a complete and built {@link ModelHierarchy} is required to be visualised, it provides
 * a correct tree structure to be used for visualisation.
 *
 * This service also provides auxiliary utility methods for searching and traversing an
 * already constructed tree structure.
 *
 * @author Svetlozar Iliev
 */
@Injectable()
export class ModelTreeService {

  getTree(hierarchy) {
    let tree = this.buildTreeData(hierarchy);
    return this.sortTreeData(tree);
  }

  sortTreeData(data = []) {
    if (!data || !data.length) {
      return;
    }
    data.sort((left, right) => this.compareTreeItems(left, right));
    data.forEach(node => this.sortTreeData(node.children));
    return data;
  }

  buildTreeData(hierarchy, data = []) {
    if (Array.isArray(hierarchy)) {
      hierarchy.forEach(node => this.buildTreeData(node, data));
    } else {
      let baseItem = this.buildTreeItem(hierarchy, data);
      hierarchy.getChildren().forEach(child => this.buildTreeData(child, baseItem.children));
    }
    return data;
  }

  buildTreeItem(item, base) {
    let root = item.getRoot();
    let descr = root.getDescription();

    let treeItem = {
      dbId: root.getId(),
      leaf: item.isLeaf(),
      icon: root.getIcon(),
      text: descr.getValue(),
      children: []
    };
    base.push(treeItem);
    return treeItem;
  }

  findRootNodePath(tree, root = 0) {
    return Array.isArray(tree) ? [tree[root].dbId] : [tree.dbId];
  }

  findNodePathById(tree, id, path = []) {
    if (Array.isArray(tree)) {
      return this.findFirstResult(tree, item => this.findNodePathById(item, id, path));
    } else if (id && id.length) {
      path.push(tree.dbId);
      if (tree.dbId === id || this.findFirstResult(tree.children, item => this.findNodePathById(item, id, path))) {
        return path;
      }
      path.splice(-1, 1);
    }
  }

  findFirstResult(collection, condition) {
    let result = null;
    collection.some(item => (result = condition(item)) && !!result);
    return result;
  }

  compareTreeItems(left, right) {
    return (left.icon === right.icon) ? this.compareStringItems(left.text, right.text) : this.compareStringItems(left.icon, right.icon);
  }

  compareStringItems(left, right) {
    return left.localeCompare(right);
  }
}