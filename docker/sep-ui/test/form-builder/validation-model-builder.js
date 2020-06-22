import {InstanceModel} from 'models/instance-model';

export class ValidationModelBuilder {

  constructor() {
    this.model = {};
  }

  addProperty(id, value, valid) {
    let propertyModel = {
      'messages': [],
      'value': value,
      'valid': valid !== undefined ? valid : true
    };
    this.model[id] = propertyModel;
    return this;
  }

  getModel() {
    return new InstanceModel(this.model);
  }
}
