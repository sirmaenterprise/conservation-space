import {Described} from 'administration/model-management/model/described';
import {ModelAttribute} from 'administration/model-management/model/attributes/model-attribute';
import {ModelAttributeTypes} from 'administration/model-management/model/attributes/model-attribute-types';
import {ModelList} from 'administration/model-management/model/model-list';
import _ from 'lodash';

const LABEL = ModelAttributeTypes.MULTI_VALUE.MODEL_LABEL_TYPE;

/**
 * Represents a base model which is defined by an id, attributes and a parent.
 * Provides minimal accessor and mutator methods. Core properties of this class
 * include:
 *
 * - id - represents an identifier to the model, usually as a string primitive
 * - loaded - boolean flag to indicate if the model has been loaded completely
 * - parent - a reference to the direct owning model node of the current model.
 * - reference - a reference to the same model node with a different owner.
 * - attributes - list of attributes which belong to this model node
 *
 *  Note: Deprecated properties, should be removed from this class definition
 * - icon - a string representing the model icon used to represent this model node
 *
 * @author Svetlozar Iliev
 * @see ModelAttribute
 */
export class ModelBase extends Described {

  constructor(id, parent = null) {
    super();
    this.id = id;
    this.loaded = false;
    this.parent = parent;
    this.reference = null;
    this.attributes = new ModelList();
  }

  getId() {
    return this.id;
  }

  setId(id) {
    this.id = id;
    return this;
  }

  //@Override
  getDescription() {
    let label = this.getAttributeByType(LABEL);
    let value = label && label.getValue();
    // provide the value of the label as description when it's not empty or is made dirty
    return value && (!value.isEmpty() || value.isDirty()) ? value : super.getDescription();
  }

  getOriginalDescription() {
    return super.getDescription();
  }

  getReference() {
    return this.reference;
  }

  setReference(reference) {
    this.reference = reference;
    return this;
  }

  getParent() {
    return this.parent;
  }

  getParents() {
    let parent = this.getParent();
    return parent ? [parent, ...parent.getParents()] : [];
  }

  setParent(parent) {
    this.parent = parent;
    return this;
  }

  getIcon() {
    return this.icon;
  }

  setIcon(icon) {
    this.icon = icon;
    return this;
  }

  getAttributes() {
    return this.attributes.getModels();
  }

  getOwnAttributes() {
    return this.getOwnModels(this.getAttributes());
  }

  getDirtyAttributes() {
    return ModelBase.getDirtyModels(this.getOwnAttributes());
  }

  getNotDirtyAttributes() {
    return ModelBase.getNotDirtyModels(this.getOwnAttributes());
  }

  resetDirtyAttributes() {
    this.getOwnAttributes().forEach(attribute => attribute.setDirty(false));
  }

  setAttributes(attributes) {
    this.attributes = attributes;
    return this;
  }

  getAttribute(name) {
    return this.attributes.getModel(name);
  }

  getAttributeByType(type) {
    return _.find(this.getAttributes(), attr => attr.getType() === type);
  }

  addAttribute(attribute) {
    if (attribute instanceof ModelAttribute) {
      this.attributes.insert(attribute);
    }
    return this;
  }

  isLoaded() {
    return this.loaded;
  }

  isValid() {
    return ModelBase.areModelsValid(this.getOwnAttributes());
  }

  isDirty() {
    return ModelBase.areModelsDirty(this.getOwnAttributes());
  }

  isOwningModels() {
    return !!this.getOwnAttributes().length;
  }

  setLoaded(loaded) {
    this.loaded = loaded;
    return this;
  }

  copyFrom(src) {
    super.copyFrom(src);

    this.id = src.id;
    this.loaded = src.loaded;
    this.parent = src.parent;
    this.reference = src.reference;

    this.attributes.copyFrom(src.attributes);
    return this;
  }

  getOwnModels(models) {
    return ModelBase.getOwnModels(models, this);
  }

  static getId(model) {
    return model instanceof ModelBase ? model.getId() : model;
  }

  static isRootModel(model, models) {
    return !model.getParent() || models.every(item => model.getParent() !== item);
  }

  static getOwnModels(models, source) {
    return models.filter(model => model.getParent() === source);
  }

  static getDirtyModels(models) {
    return models.filter(model => model.isDirty());
  }

  static getNotDirtyModels(models) {
    return models.filter(model => !model.isDirty());
  }

  static areModelsValid(models) {
    return models.every(model => model.isValid());
  }

  static areModelsDirty(models) {
    return models.some(model => model.isDirty());
  }

  static areModelsNotDirty(models) {
    return models.every(model => !model.isDirty());
  }
}