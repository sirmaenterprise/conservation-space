import {ModelBase} from 'administration/model-management/model/model-base';
import {ModelList} from 'administration/model-management/model/model-list';

import {ModelProperty} from 'administration/model-management/model/model-property';
import {ModelDefinition} from 'administration/model-management/model/model-definition';

/**
 * Represents a model of a class extending the {@link ModelBase} with a list
 * of properties which are required to be of type {@link ModelProperty}.
 *
 * @author Svetlozar Iliev
 */
export class ModelClass extends ModelBase {

  constructor(id, parent) {
    super(id, parent);
    this.types = new ModelList();
    this.properties = new ModelList();
  }

  hasProperties() {
    return !!this.properties.size();
  }

  getProperties() {
    return this.properties.getModels();
  }

  getDirtyProperties() {
    return ModelBase.getDirtyModels(this.getOwnProperties());
  }

  getOwnProperties() {
    return this.getOwnModels(this.getProperties());
  }

  getProperty(id) {
    return this.properties.getModel(id);
  }

  addProperty(property) {
    if (property instanceof ModelProperty) {
      this.properties.insert(property);
    }
    return this;
  }

  removeProperty(property) {
    let id = property.getId();
    if (this.getProperty(id)) {
      this.properties.remove(id);
    }
    return this;
  }

  hasTypes() {
    return !!this.types.size();
  }

  getTypes() {
    return this.types.getModels();
  }

  getSuperTypes() {
    return this.getTypes().filter(type => ModelBase.isRootModel(type, this.getTypes()));
  }

  getDirtyTypes() {
    return ModelBase.getDirtyModels(this.getOwnTypes());
  }

  getOwnTypes() {
    return this.getOwnModels(this.getTypes());
  }

  getType(id) {
    return this.types.getModel(id);
  }

  addType(type) {
    if (type instanceof ModelDefinition) {
      this.types.insert(type);
    }
    return this;
  }

  isValid() {
    return super.isValid() && ModelBase.areModelsValid(this.getDirtyProperties());
  }

  isDirty() {
    return super.isDirty() || ModelBase.areModelsDirty(this.getOwnProperties());
  }

  isOwningModels() {
    return super.isOwningModels() || !!this.getOwnProperties().length;
  }
}
