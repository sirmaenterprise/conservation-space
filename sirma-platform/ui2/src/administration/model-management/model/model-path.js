/**
 * Class meant to describe a path which contains a name-value pair. The path is represented as a linked
 * list and it holds the previous and next path nodes. For a path to be complete at least a single path
 * node is required to exist and have a valid name and value pair. Model path is used to walk over a given
 * model using the walk method using default walkers or a custom provided.
 *
 * @author Svetlozar Iliev
 */
export class ModelPath {

  constructor(name, value) {
    // core fields
    this.name = name;
    this.value = value;

    // transitive fields
    this.previous = null;
    this.next = null;
  }

  getName() {
    return this.name;
  }

  getValue() {
    return this.value;
  }

  getNext() {
    return this.next;
  }

  setNext(next) {
    this.next = next;
    return this;
  }

  hasNext() {
    return !!this.next;
  }

  getPrevious() {
    return this.previous;
  }

  setPrevious(previous) {
    this.previous = previous;
    return this;
  }

  head() {
    if (this.previous) {
      return this.previous.head();
    }
    return this;
  }

  tail() {
    if (this.next) {
      return this.next.tail();
    }
    return this;
  }

  cut(node) {
    let current = this;
    while (current !== node) {
      current = current.next;
    }
    if (current.previous) {
      current.previous.next = null;
      current.previous = null;
    }
    return this;
  }

  walk(walkable, walkers) {
    walkable = this.step(walkable, walkers);
    if (walkable && this.hasNext()) {
      return this.getNext().walk(walkable, walkers);
    }
    return walkable;
  }

  step(walkable, walkers) {
    return walkable && walkers[this.getName()](walkable, this.getValue());
  }
}