/**
 * Represents a model of a change set which holds data about modifications performed
 * upon a given model. Furthermore the change set holds the actual operation which is
 * related with the data and the exact path to the model affected by the change.
 *
 * @author Svetlozar Iliev
 */
export class ModelChangeSet {

  getModel() {
    return this.model;
  }

  setModel(model) {
    this.model = model;
    return this;
  }

  getSelector() {
    return this.selector;
  }

  setSelector(selector) {
    this.selector = selector;
    return this;
  }

  getOperation() {
    return this.operation;
  }

  setOperation(operation) {
    this.operation = operation;
    return this;
  }

  getNewValue() {
    return this.newValue;
  }

  setNewValue(newValue) {
    this.newValue = newValue;
    return this;
  }

  getOldValue() {
    return this.oldValue;
  }

  setOldValue(oldValue) {
    this.oldValue = oldValue;
    return this;
  }
}