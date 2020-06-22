import {ModelValidation} from 'administration/model-management/model/validation/model-validation';
import {ModelValue} from 'administration/model-management/model/model-value';
import {ModelRestrictions} from 'administration/model-management/model/model-restrictions';

/**
 * Represents a model of an attribute extending the {@link ModelBase} with
 * additional properties such as type, value and default value.
 *
 * The difference between the member method <code>isDirty()</code> and
 * <code>isDirtyComparedToReference()</code> is that the first one  compares
 * the current value and the old value. The second method compares the
 * current value of the attribute and the value of the reference attribute.
 *
 * @author Svetlozar Iliev
 */
export class ModelAttribute {

  constructor(id, type, value) {
    this._id = id;
    this._type = type;
    this._value = value;

    // "Transient" fields
    this._metaData = null;
    this.validation = new ModelValidation();
    this.restrictions = new ModelRestrictions();
  }

  getParent() {
    return this.parent;
  }

  setParent(parent) {
    this.parent = parent;
    return this;
  }

  getReference() {
    return this.reference;
  }

  setReference(reference) {
    this.reference = reference;
    return this;
  }

  getSource() {
    return this.source;
  }

  setSource(source) {
    this.source = source;
    return this;
  }

  getId() {
    return this._id;
  }

  setId(id) {
    this._id = id;
    return this;
  }

  getType() {
    return this._type;
  }

  get type() {
    return this.getType();
  }

  setType(type) {
    this._type = type;
    return this;
  }

  set type(type) {
    this.setType(type);
  }

  getValue() {
    return this._value;
  }

  get value() {
    return this.getValue();
  }

  setValue(value) {
    this._value = value;
    return this;
  }

  set value(value) {
    this.setValue(value);
  }

  getMetaData() {
    return this._metaData;
  }

  get metaData() {
    return this.getMetaData();
  }

  setMetaData(metaData) {
    this._metaData = metaData;
    return this;
  }

  set metaData(metaData) {
    this.setMetaData(metaData);
  }

  getValidation() {
    return this.validation;
  }

  isEmpty() {
    let value = this.getValue();
    return !value || value.isEmpty();
  }

  isDirty() {
    let value = this.getValue();
    return !value || value.isDirty();
  }

  isDirtyComparedToReference() {
    let referenceValue = this.getReference().getValue();
    return !this.getValue().equals(referenceValue);
  }

  isValid() {
    return this.validation.isValid();
  }

  setDirty(dirty) {
    this.getValue().setDirty(dirty);
    return this;
  }

  restoreValue() {
    this.getValue().restoreValue();
    return this;
  }

  getRestrictions() {
    return this.restrictions;
  }

  copyFrom(src) {
    this._id = src._id;
    this._type = src._type;
    this._value = new ModelValue();
    this._value.copyFrom(src._value);

    this.parent = src.parent;
    this.reference = src.reference;

    this._metaData = src._metaData;
    this.validation.copyFrom(src.validation);
    this.restrictions.copyFrom(src.restrictions);
    return this;
  }
}

// attribute source data
ModelAttribute.SOURCE = {
  META_DATA: 'meta-data',
  MODEL_DATA: 'model-data'
};

// specify non semantic attributes
ModelAttribute.URI_ATTRIBUTE = 'uri';
ModelAttribute.NAME_ATTRIBUTE = 'name';
ModelAttribute.TYPE_ATTRIBUTE = 'type';
ModelAttribute.VALUE_ATTRIBUTE = 'value';
ModelAttribute.ORDER_ATTRIBUTE = 'order';
ModelAttribute.LABEL_ATTRIBUTE = 'label';
ModelAttribute.TOOLTIP_ATTRIBUTE = 'tooltip';
ModelAttribute.CODELIST_ATTRIBUTE = 'codeList';
ModelAttribute.DISPLAY_ATTRIBUTE = 'displayType';
ModelAttribute.MANDATORY_ATTRIBUTE = 'mandatory';
ModelAttribute.MULTIVALUE_ATTRIBUTE = 'multiValued';
ModelAttribute.TYPE_OPTION_ATTRIBUTE = 'typeOption';
ModelAttribute.HEADER_TYPE_ATTRIBUTE = 'headerType';
ModelAttribute.GROUP_ATTRIBUTE = 'group';
ModelAttribute.PARENT_ATTRIBUTE = 'parent';
ModelAttribute.PURPOSE_ATTRIBUTE = 'purpose';

// specify semantic attributes
//TODO: Move all semantic attributes to the related meta model
ModelAttribute.RANGE_ATTRIBUTE = 'http://www.w3.org/2000/01/rdf-schema#range';
ModelAttribute.INVERSE_ATTRIBUTE = 'http://www.w3.org/2002/07/owl#inverseOf';
ModelAttribute.DESCRIPTION_ATTRIBUTE = 'http://purl.org/dc/terms/description';
ModelAttribute.DOMAIN_ATTRIBUTE = 'http://www.w3.org/2000/01/rdf-schema#domain';
ModelAttribute.SUBPROPERTY_ATTRIBUTE = 'http://www.w3.org/2000/01/rdf-schema#subPropertyOf';
ModelAttribute.AUDIT_ATTRIBUTE = 'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#auditEvent';

ModelAttribute.PROPERTY_TITLE = 'http://purl.org/dc/terms/title';
ModelAttribute.PROPERTY_TYPE = 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type';
ModelAttribute.PROPERTY_URI = 'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#uri';
