import {ModelView} from 'administration/model-management/model/model-view';
import {ModelBase} from 'administration/model-management/model/model-base';

/**
 * Represents a model of a field extending the {@link ModelBase} class.
 * A model field is usually related to a specific {@link ModelProperty}
 *
 * @author Svetlozar Iliev
 */
export class ModelField extends ModelBase {

  constructor(id) {
    super(id);
    this.property = null;
    this.view = new ModelView();
  }

  getView() {
    return this.view;
  }

  getProperty() {
    return this.property;
  }

  setProperty(property) {
    this.property = property;
    return this;
  }
}