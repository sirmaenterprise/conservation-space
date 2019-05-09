/**
 * Model for the idoc table of contents.
 * It provides basic api for traversal, creation, removal of sections.
 */
export class ToCVirtualTree {

  constructor() {
    /**
     * https://en.wikipedia.org/wiki/Adjacency_list
     * This is adjacency list of the forest. It maps each node with array of the children nodes.
     * @type {Map}
     */
    this.tree = new Map();

    /**
     * This keeps the direct parent of the section.
     * i.e Parents.get(A) will return the parent node.
     * @type {Map}
     */
    this.parents = new Map();
  }

  /**
   * Adds the section id to the adjacency list.If a parent id is not provided it creates a new tree in the forest.
   * @param sectionID The section id.
   * @param parentID The id of the parent section.
   */
  createSection(sectionID, parentID) {
    if (this.tree.has(parentID)) {
      let childrenOfParentID = this.tree.get(parentID);
      childrenOfParentID.push(sectionID);
      this.tree.set(parentID, childrenOfParentID);
      this.parents.set(sectionID, parentID);
    } else {
      if (parentID) {
        let childrenOfParentID = [];
        childrenOfParentID.push(sectionID);
        this.parents.set(sectionID, parentID);
        if (!this.parents.has(parentID)) {
          this.parents.set(parentID, parentID);
        }
        this.tree.set(parentID, childrenOfParentID);
      } else if (!this.tree.has(sectionID)) {
        this.parents.set(sectionID, sectionID);
      }
    }
    this.tree.set(sectionID, []);
  }

  /**
   * Moves section
   * @param sectionID {String}
   * @param destinationID {String}
   * The id of the new parent.If the id is not specified
   * it makes the section and all its children new tree in the forest.
   * @param nextSiblingID {String}
   * If the id is provided it will insert the @param sectionID just before @param nextSiblingID .
   */
  moveSection(sectionID, destinationID, nextSiblingID) {
    if ((this.parents.has(sectionID)) && (this.parents.get(sectionID) !== sectionID)) {
      let siblings = this.tree.get(this.parents.get(sectionID));
      let index = siblings.indexOf(sectionID);
      if (index > -1) {
        siblings.splice(index, 1);
      }
    }
    if (this.tree.has(destinationID)) {
      let siblings = this.tree.get(destinationID);
      let index = siblings.indexOf(nextSiblingID);
      if (index > -1) {
        siblings.splice(index - 1, 0, sectionID);
      } else {
        siblings.push(sectionID);
      }
      this.parents.set(sectionID, destinationID);
    } else {
      if (destinationID === null) {
        this.parents.set(sectionID, sectionID);
      }
    }
  }

  /**
   * Deletes section from the tree.
   * @param sectionID {String} The id of the section.
   * @returns {boolean} Whether the deletion was successful.
   */
  deleteSection(sectionID) {
    let result = true;
    if (this.parents.get(sectionID) === sectionID) {
      result = this.tree.delete(sectionID);
    } else {
      result = this.deleteChild(this.tree.get(this.parents.get(sectionID)), sectionID);
      result = result && this.tree.delete(sectionID);
    }
    result = result && this.parents.delete(sectionID);
    return result;
  }

  /**
   * Deletes the given child from the array of children.
   * @param children {Array} The children array.
   * @param child {String} The child that needs removal.
   */
  deleteChild(children, child) {
    let index = -1;
    if (children) {
      index = children.indexOf(child);
      if (index > -1) {
        children.splice(index, 1);
      }
    }
    return index > -1;
  }

  /**
   * Traverses and deletes the whole tree including the root from which it was started.
   * @param rootID {String} The id of the root section.
   */
  deleteSubTree(rootID) {
    this.traverse(rootID, new DeleteDecorator(this));
    this.deleteSection(rootID);
  }

  /**
   * Traverses the tree with dfs and apply the provided decorator.
   * https://en.wikipedia.org/wiki/Depth-first_search
   * @param sectionID {String} The sectionID.
   * @param treeAbstractDecorator {Object} The decorator instance
   */
  traverse(sectionID, treeAbstractDecorator) {
    treeAbstractDecorator.decorateNode(sectionID);
    let children = this.tree.get(sectionID);
    if (children) {
      for (let child of children) {
        this.traverse(child, treeAbstractDecorator);
        treeAbstractDecorator.decorateNodeChild(sectionID, child);
      }
    }
  }

  /**
   * Starts traverse for each root of the forest.
   * @param treeDecorator The decorator instance which will be used during the traversal
   * @returns {Array} Array with each tree root sectionID.
   */
  fullTraversal(treeDecorator) {
    let roots = [];
    for (let sectionID of this.parents.keys()) {
      if (sectionID === this.parents.get(sectionID)) {
        this.traverse(sectionID, treeDecorator);
        roots.push(sectionID);
      }
    }
    roots = treeDecorator.decorateRoots(roots);
    return roots;
  }

}

class DeleteDecorator {
  constructor(tree) {
    this.tree = tree;
  }

  decorateNode() {
  }

  decorateNodeChild(sectionID, child) {
    this.tree.deleteSection(child);
  }

  decorateRoots() {
  }

}
