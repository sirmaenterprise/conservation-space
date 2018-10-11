/**
 * Represents a display model for the model classes. Each model which
 * requires specific display settings or actions to be carried out
 * should hold an instance of this class.
 *
 * @author Svetlozar Iliev
 */
export class ModelView {

  constructor() {
    this.visible = true;
    this.parent = false;
  }

  showParent() {
    return this.parent;
  }

  setShowParent(parent) {
    this.parent = parent;
    return this;
  }

  isVisible() {
    return this.visible;
  }

  setVisible(visible) {
    this.visible = visible;
    return this;
  }
}