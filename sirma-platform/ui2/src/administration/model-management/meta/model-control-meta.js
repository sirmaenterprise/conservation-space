import {ModelAttributeMetaData} from 'administration/model-management/meta/model-attribute-meta';

/**
 * Represents a specific field control meta data extending from {@link ModelAttributeMetaData}
 *
 * @author svelikov
 */
export class ModelControlMetaData extends ModelAttributeMetaData {

  constructor(id) {
    super(id);
    this.controlOptions = null;
  }

  getControlOptions() {
    return this.controlOptions;
  }

  setControlOptions(controlOptions) {
    this.controlOptions = controlOptions;
    return this;
  }

}

ModelControlMetaData.ID = 'id';
ModelControlMetaData.PARAMS = 'params';
