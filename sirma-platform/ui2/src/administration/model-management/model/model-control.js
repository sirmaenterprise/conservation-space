import {ModelBase} from 'administration/model-management/model/model-base';
import {ModelControlParam} from 'administration/model-management/model/model-control-param';
import {ModelList} from 'administration/model-management/model/model-list';

/**
 * Represents a model of a control extending the {@link ModelBase} class.
 * A model controls are bound to {@link ModelField}.
 *
 * @author svelikov
 */
export class ModelControl extends ModelBase {

  constructor(id) {
    super(id);
    this.controlParams = new ModelList();
  }

  getControlParams() {
    return this.controlParams.getModels();
  }

  getOwnControlParams() {
    return this.getOwnModels(this.getControlParams());
  }

  getDirtyControlParams() {
    return ModelBase.getDirtyModels(this.getOwnControlParams());
  }

  getControlParam(id) {
    return this.controlParams.getModel(id);
  }

  addControlParam(controlParam) {
    if (controlParam instanceof ModelControlParam) {
      this.controlParams.insert(controlParam);
    }
    return this;
  }

  removeControlParam(controlParam) {
    let id = controlParam.getId();
    if (this.getControlParam(id)) {
      this.controlParams.remove(id);
    }
    return this;
  }

  isValid() {
    return super.isValid() && ModelBase.areModelsValid(this.getDirtyControlParams());
  }

  isDirty() {
    return super.isDirty() || ModelBase.areModelsDirty(this.getOwnControlParams());
  }

  copyFrom(src) {
    super.copyFrom(src);
    this.controlParams.copyFrom(src.controlParams);
    return this;
  }

  isOwningModels() {
    return super.isOwningModels() || !!this.getOwnControlParams().length;
  }
}