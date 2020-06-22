import {EventEmitter} from 'common/event-emitter';
import {ModelUtils} from 'models/model-utils';
import _ from 'lodash';
const PREDEFINED_ATTRIBUTES = ['_hiddenByCondition', '_isMandatory', '_displayType', '_preview', 'rendered', 'preview', 'disabled', 'previewEmpty', 'identifier', 'isMandatory', 'maxLength', 'codelist', 'filters', 'dataType', 'label', 'multivalue', 'tooltip', 'uri', 'isDataProperty', 'filtered', 'validators', 'control'];
/**
 * Wrapper class for the viewModel. Contains the original viewModel tree, the original tree wrapped in definitionModelProperties, the flatened tree and a map.
 */
export class DefinitionModel {

  constructor(definitionModel) {
    this.flatDefinitions = [];
    this.flatModelMap = {};

    this.definitionTree = _.mapValues(_.cloneDeep(definitionModel), (definition, attribute) => {
      let definitionProperty = this.createDefinitionProperty(definition, attribute);
      Object.defineProperty(this, attribute, {
        enumerable: true,
        value: definitionProperty,
        writable: true
      });
      return definitionProperty;
    });
    Object.defineProperty(this, 'flatDefinitions', {
      enumerable: false
    });
    Object.defineProperty(this, 'flatModelMap', {
      enumerable: false
    });
    Object.defineProperty(this, 'definitionTree', {
      enumerable: false
    });
  }

  /**
   * Adds new fields to model. Fields are added at their respectful positions.
   * @param newFields array with new fields to be added
   */
  addFieldsToModel(newFields) {
    this.addFieldsToExistingFields(_.cloneDeep(newFields), this.definitionTree.fields);
  }

  addFieldsToExistingFields(newFields, existingFields) {
    let insertPosition = 0;
    newFields.forEach((newField) => {
      let exitingFieldIndex = _.findIndex(existingFields, (existingField) => {
        return existingField.identifier === newField.identifier;
      });
      if (exitingFieldIndex !== -1) {
        //if existing field and new field are regions insert region fields
        if (ModelUtils.isRegion(newField) && ModelUtils.isRegion(existingFields[exitingFieldIndex])) {
          this.addFieldsToExistingFields(newField.fields, existingFields[exitingFieldIndex].fields);
        }
        // add next new field after the existing field
        insertPosition = exitingFieldIndex + 1;
      } else {
        existingFields.splice(insertPosition, 0, this.flattenDefinitionProperty(newField));
        insertPosition++;
      }
    });
  }

  /**
   * Creates a definition model property from the given definition.
   * @param definition
   * @param attribute
   * @returns {*}
   */
  createDefinitionProperty(definition, attribute) {
    if (attribute === 'fields') {
      definition.forEach((value, index, arr) => {
        arr[index] = this.flattenDefinitionProperty(value);
      });
      return definition;
    }
    return new DefinitionModelProperty(definition, attribute);
  }

  /**
   * Flattens a definition property and adds it to the flat model and model map.
   * @param definitionProperty
   * @param parentId
   */
  flattenDefinitionProperty(definitionProperty, parentId) {
    let definitionModelProperty = new DefinitionModelProperty(definitionProperty);
    if (ModelUtils.isRegion(definitionProperty)) {
      definitionModelProperty.fields.forEach((field, index, arr) => {
        arr[index] = this.flattenDefinitionProperty(field, definitionProperty.identifier);
      });
    }
    if (parentId) {
      definitionModelProperty.parentId = parentId;
    }
    this.flatDefinitions.push(definitionModelProperty);
    this.flatModelMap[definitionProperty.identifier] = definitionModelProperty;
    return definitionModelProperty;
  }

  /**
   * Serializes the definition tree. Returns the original form of the tree, unwrapped from the DefinitonModelProperty class
   * @returns {{}}
   */
  serialize() {
    let originalTree = {};
    Object.keys(this.definitionTree).forEach((field) => {
      if (field === 'fields') {
        originalTree[field] = this.definitionTree[field].map((field) => {
          return field.serialize();
        });
      } else {
        originalTree[field] = this.definitionTree[field].modelProperty;
      }
    });
    return originalTree;
  }

  /**
   * Produces a clone of this definition model. First its serialized in order to get the original tree.
   */
  clone() {
    return new DefinitionModel(_.cloneDeep(this.serialize()));
  }
}
/**
 * Wraps a definition model field in a emittable class.
 */
export class DefinitionModelProperty extends EventEmitter {

  constructor(definitionProperty, attributeName) {
    super();
    //if a single attribute is wrapped, only it is the modelProperty to ensure easy access.
    if (attributeName) {
      this.modelProperty = definitionProperty;
      Object.defineProperty(this, attributeName, {
        get: () => {
          return this.modelProperty;
        },
        set: (newValue) => {
          if (!(_.isEqual(newValue, this.modelProperty))) {
            this.modelProperty = newValue;
            this.publish('propertyChanged', {[attributeName]: newValue});
          }
        }
      });
    } else {
      this.modelProperty = definitionProperty;
      Object.keys(this.modelProperty).forEach((attribute) => {
        this.defineGetterAndSetter(attribute);
      });

      PREDEFINED_ATTRIBUTES.forEach((attribute) => {
        if (!(attribute in this.modelProperty)) {
          this.defineGetterAndSetter(attribute);
        }
      });
    }
  }

  defineGetterAndSetter(attribute) {
    Object.defineProperty(this, attribute, {
      get: () => {
        return this.modelProperty[attribute];
      },
      set: (newValue) => {
        if (!(_.isEqual(newValue, this.modelProperty[attribute]))) {
          this.modelProperty[attribute] = newValue;
          this.publish('propertyChanged', {[attribute]: newValue});
        }
      }
    });
  }

  serialize() {
    //avoids alteration of original model property.
    let serializedProperty = _.cloneDeep(this.modelProperty);
    if (serializedProperty.fields) {
      serializedProperty.fields = this.serializeFields(serializedProperty.fields);
    }
    return serializedProperty;
  }

  serializeFields(fields) {
    let serializedField = [];
    fields.forEach((field) => {
      //when inner fields are cloned only modelProperty attribute remains.
      if (field.modelProperty.fields) {
        field.modelProperty.fields = this.serializeFields(field.modelProperty.fields);
      }
      serializedField.push(field.modelProperty);
    });

    return serializedField;
  }
}