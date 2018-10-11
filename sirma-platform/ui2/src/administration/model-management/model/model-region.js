import {ModelView} from 'administration/model-management/model/model-view';
import {ModelBase} from 'administration/model-management/model/model-base';
import {ModelField} from 'administration/model-management/model/model-field';
import {ModelList} from 'administration/model-management/model/model-list';

/**
 * Represents a model of a region extending the {@link ModelBase} class.
 * The region class extends the base model by defining a list of fields
 * A model region contains & stores a collection of fields {@link ModelField}
 *
 * @author Svetlozar Iliev
 */
export class ModelRegion extends ModelBase {

  constructor(id) {
    super(id);
    this.view = new ModelView();
    this.fields = new ModelList();
  }

  getView() {
    return this.view;
  }

  getFields() {
    return this.fields.getModels();
  }

  setFields(fields) {
    this.fields = fields;
    return this;
  }

  getField(id) {
    return this.fields.getModel(id);
  }

  addField(field) {
    this.fields.insert(field);
    return this;
  }
}