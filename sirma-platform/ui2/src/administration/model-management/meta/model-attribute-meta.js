import {ModelMetaData} from 'administration/model-management/meta/model-meta';

/**
 * Represents a specific attribute meta data extending from {@link ModelMetaData}
 *
 * @author Svetlozar Iliev
 */
export class ModelAttributeMetaData extends ModelMetaData {

  constructor(id) {
    super(id);
    this.type = null;
    this.defaultValue = null;
  }

  getType() {
    return this.type;
  }

  setType(type) {
    this.type = type;
    return this;
  }

  getDefaultValue() {
    return this.defaultValue;
  }

  setDefaultValue(defaultValue) {
    this.defaultValue = defaultValue;
    return this;
  }
}