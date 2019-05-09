/**
 * Represents a response which contains a model and the version related to this model. Version
 * is a positive integer. It indicates the version of the model contained by this response. The
 * provided model can be any kind model represented by the API.
 *
 * @author Svetlozar Iliev
 */
export class ModelResponse {

  constructor(model = null, version = 0) {
    this.model = model;
    this.version = version;
  }

  getVersion() {
    return this.version;
  }

  setVersion(version) {
    this.version = version;
    return this;
  }

  getModel() {
    return this.model;
  }

  setModel(model) {
    this.model = model;
    return this;
  }
}