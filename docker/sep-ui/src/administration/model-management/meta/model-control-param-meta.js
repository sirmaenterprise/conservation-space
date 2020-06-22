import {ModelAttributeMetaData} from 'administration/model-management/meta/model-attribute-meta';

/**
 * Represents a specific field control param meta data extending from {@link ModelAttributeMetaData}
 *
 * @author svelikov
 */
export class ModelControlParamMetaData extends ModelAttributeMetaData {

  constructor(id) {
    super(id);
    this.type = null;
    this.name = null;
    this.value = null;
  }

  getType() {
    return this.type;
  }

  setType(type) {
    this.type = type;
    return this;
  }

  getName() {
    return this.name;
  }

  setName(name) {
    this.name = name;
    return this;
  }

  getValue() {
    return this.value;
  }

  setValue(value) {
    this.value = value;
    return this;
  }

}
