/**
 * Represents a restriction in the state of each attibute. Different flags shows if Attribute is editable or not
 * and mandatory or not
 *
 * @author Stela Djulgerova
 */
export class ModelRestrictions {

  setUpdateable(updateable) {
    this.updateable = updateable;
    return this;
  }

  isUpdateable() {
    return this.updateable;
  }

  setMandatory(mandatory) {
    this.mandatory = mandatory;
    return this;
  }

  isMandatory() {
    return this.mandatory;
  }

  setVisible(visible) {
    this.visible = visible;
    return this;
  }

  isVisible() {
    return this.visible;
  }

  copyFrom(src) {
    this.updateable = src.updateable;
    this.mandatory = src.mandatory;
    this.visible = src.visible;
    return this;
  }

  seal() {
    Object.freeze(this);
  }
}