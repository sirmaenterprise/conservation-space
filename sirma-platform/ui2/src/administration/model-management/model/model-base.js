import {Described} from 'administration/model-management/model/described';
import {ModelAttribute} from 'administration/model-management/model/model-attribute';
import {ModelList} from 'administration/model-management/model/model-list';
import _ from 'lodash';

/**
 * Represents a base model which is defined by an id, attributes and a parent.
 * Provides minimal accessor and mutator methods.
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
    this.attributes = new ModelList();
  }

  getId() {
    return this.id;
  }

  setId(id) {
    this.id = id;
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

  setLoaded(loaded) {
    this.loaded = loaded;
    return this;
  }
}