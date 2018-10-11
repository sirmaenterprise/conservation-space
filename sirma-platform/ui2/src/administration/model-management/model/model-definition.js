import {ModelBase} from 'administration/model-management/model/model-base';
import {ModelField} from 'administration/model-management/model/model-field';
import {ModelRegion} from 'administration/model-management/model/model-region';
import {ModelList} from 'administration/model-management/model/model-list';

/**
 * Represents a model of a definition extending the {@link ModelBase}
 * with a property which is used to determine if the definition is
 * abstract or not.
 *
 * Each definition holds a collection of models which can be of different
 * type. Either of type {@link ModelField} or a {@link ModelRegion} type.
 *
 * @author Svetlozar Iliev
 */
export class ModelDefinition extends ModelBase {

  constructor(id, parent = null, abstract = false, type = null) {
    super(id, parent);
    this.type = type;
    this.abstract = abstract;
    this.models = new ModelList();
  }

  isAbstract() {
    return this.abstract;
  }

  getType() {
    return this.type;
  }

  setType(type) {
    this.type = type;
    return this;
  }

  getModels() {
    return this.models.getModels();
  }

  getRegion(id) {
    let region = this.models.getModel(id);
    return (region instanceof ModelRegion) && region;
  }

  getField(id) {
    let field = this.models.getModel(id);
    return (field instanceof ModelField) && field;
  }

  addField(field) {
    if (field instanceof ModelField) {
      this.models.insert(field);
    }
    return this;
  }

  addRegion(region) {
    if (region instanceof ModelRegion) {
      this.models.insert(region);
    }
    return this;
  }
}