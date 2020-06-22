/**
 * Represents a recursive model hierarchy structure which is used to build a hierarchy
 * of classes and definitions. The hierarchy contains a reference to the root and it's children
 * as well as reference to the parent of the hierarchy.
 *
 * The root of the hierarchy contains the actual data for a given node and
 * can be any class extending from the {@link ModelBase} class, the children
 * and parent of the hierarchy must be of type {@link ModelHierarchy}.
 *
 * @author Svetlozar Iliev
 */
class ModelHierarchy {

  constructor(root = null, parent = null, children = []) {
    this.root = root;
    this.parent = parent;
    this.children = children;
  }

  getRoot() {
    return this.root;
  }

  setRoot(root) {
    this.root = root;
  }

  getParent() {
    return this.parent;
  }

  setParent(parent) {
    this.parent = parent;
  }

  getChildren() {
    return this.children;
  }

  insertChild(child) {
    return child && this.children.push(child) && child.setParent(this);
  }

  insertChildren(children) {
    children.forEach(child => this.insertChild(child));
  }

  isLeaf() {
    return !this.children.length;
  }
}

export class ModelClassHierarchy extends ModelHierarchy {
  // Model Class Hierarchy left empty for now
}

export class ModelDefinitionHierarchy extends ModelHierarchy {
  // Model Definition Hierarchy left empty for now
}