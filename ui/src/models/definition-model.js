import {EventEmitter} from 'common/event-emitter';
import _ from 'lodash';
const PREDEFINED_ATTRIBUTES = ['_isMandatory', '_displayType', '_preview', 'rendered', 'preview', 'disabled', 'previewEmpty', 'identifier', 'isMandatory', 'maxLength', 'codelist', 'filters', 'dataType', 'label', 'multivalue', 'tooltip', 'uri', 'isDataProperty', 'filtered', 'validators', 'control'];
/**
 * Wrapper class for the viewModel. Contains the original viewModel tree, the original tree wrapped in definitionModelProperties, the flatened tree and a map.
 */
export class DefinitionModel {

  constructor(definitionModel) {
    this.flatDefinitions = [];
    this.flatModelMap = {};

    this.definitionTree = _.mapValues(_.cloneDeep(definitionModel), (definition, attribute)=> {
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

  addFieldsToModel(newFields) {
    newFields.forEach((newField) => {
      let existingFields = this.serialize().fields;
      let fieldExists = existingFields.some((existingField) => {
        if(existingField.fields && newField.fields && existingField.identifier === newField.identifier) {
          this.addFieldsToModel(newField.fields);
        }
        return existingField.identifier === newField.identifier;
      });
      if(!fieldExists) {
        this.definitionTree.fields.push(this.createDefinitionProperty(newField));
      }
    });
  }

  /**
   * Creates a definition model property from the given definition.If a definition is a region,
   * it is flattened, and all its inner regions are converted to definition model properties
   * @param definition
   * @param attribute
   * @returns {*}
   */
  createDefinitionProperty(definition, attribute) {
    if (attribute === 'fields' || definition instanceof Array) {
      this.flattenDefinitionProperty(definition);
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
    definitionProperty.forEach((property, index) => {
      if (property.fields) {
        definitionProperty[index] = new DefinitionModelProperty(property);
        this.flattenDefinitionProperty(property.fields, property.identifier);
        this.flatDefinitions.push(definitionProperty[index]);
        this.flatModelMap[property.identifier] = definitionProperty[index];
      } else {
        let definitionModelProperty = new DefinitionModelProperty(property);
        if (parentId) {
          definitionModelProperty.parentId = parentId;
        }
        definitionProperty[index] = definitionModelProperty;
        this.flatDefinitions.push(definitionModelProperty);
        this.flatModelMap[property.identifier] = definitionModelProperty;
      }
    });
  }

  /**
   * Serializes the definition tree. Returns the original form of the tree, unwrapped from the DefinitonModelProperty class
   * @returns {{}}
   */
  serialize() {
    let originalTree = {};
    Object.keys(this.definitionTree).forEach((field)=> {
      if (field === 'fields') {
        originalTree[field] = this.definitionTree[field].map((field)=> {
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
        get: ()=> {
          return this.modelProperty;
        },
        set: (newValue)=> {
          if (!(_.isEqual(newValue, this.modelProperty))) {
            this.modelProperty = newValue;
            this.publish('propertyChanged', {[attributeName]: newValue});
          }
        }
      });
    } else {
      this.modelProperty = definitionProperty;
      Object.keys(this.modelProperty).forEach((attribute)=> {
        this.defineGetterAndSetter(attribute);
      });

      PREDEFINED_ATTRIBUTES.forEach((attribute)=> {
        if (!(attribute in this.modelProperty)) {
          this.defineGetterAndSetter(attribute);
        }
      });
    }
  }

  defineGetterAndSetter(attribute) {
    Object.defineProperty(this, attribute, {
      get: ()=> {
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
    fields.forEach((field)=> {
      //when inner fields are cloned only modelProperty attribute remains.
      if (field.modelProperty.fields) {
        field.modelProperty.fields = this.serializeFields(field.modelProperty.fields);
      }
      serializedField.push(field.modelProperty);
    });

    return serializedField;
  }
}