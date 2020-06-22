import {ModelBase} from 'administration/model-management/model/model-base';
import {ModelControl} from 'administration/model-management/model/model-control';
import {ModelList} from 'administration/model-management/model/model-list';

/**
 * Represents a model of a field extending the {@link ModelBase} class.
 * A model field is usually related to a specific {@link ModelProperty}
 *
 * @author Svetlozar Iliev
 */
export class ModelField extends ModelBase {

  constructor(id) {
    super(id);
    this.regionId = null;
    this.property = null;
    this.controls = new ModelList();
  }

  getRegionId() {
    return this.regionId;
  }

  setRegionId(regionId) {
    this.regionId = regionId;
    return this;
  }

  getProperty() {
    return this.property;
  }

  setProperty(property) {
    this.property = property;
    return this;
  }

  getControls() {
    return this.controls.getModels();
  }

  getDirtyControls() {
    return ModelBase.getDirtyModels(this.getOwnControls());
  }

  getOwnControls() {
    return this.getOwnModels(this.getControls());
  }

  getControl(id) {
    return this.controls.getModel(id);
  }

  addControl(control) {
    if (control instanceof ModelControl) {
      this.controls.insert(control);
    }
    return this;
  }

  removeControl(control) {
    let id = control.getId();
    if (this.getControl(id)) {
      this.controls.remove(id);
    }
    return this;
  }

  /**
   * Returns the description for the user's current language. If the description
   * is empty then return the description of the semantic property to which this
   * field is linked. This is required by the behaviour of the model field which
   * can depend on the semantic property it is connected to, to collect some type
   * of information directly off of it.
   */
  //@Override
  getDescription() {
    let property = this.getProperty();
    let description = super.getDescription();

    // when properties are present and description is empty or not present or same as the base
    if (property && (!description || description.isEmpty() || description === this.description)) {
      return property.getDescription();
    }

    // use the defaults
    return description;
  }

  copyFrom(src) {
    super.copyFrom(src);
    this.regionId = src.regionId;
    this.property = src.property;
    this.controls.copyFrom(src.controls);
    return this;
  }

  isValid() {
    return super.isValid() && ModelBase.areModelsValid(this.getDirtyControls());
  }

  isDirty() {
    return super.isDirty() || ModelBase.areModelsDirty(this.getOwnControls());
  }

  isOwningModels() {
    return super.isOwningModels() || !!this.getOwnControls().length;
  }
}