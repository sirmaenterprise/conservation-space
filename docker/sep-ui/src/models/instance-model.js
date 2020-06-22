import {EventEmitter} from 'common/event-emitter';
import _ from 'lodash';

const ARRAY_MUTATOR_FUNCTIONS = ['copyWithin', 'fill', 'pop', 'push', 'reverse', 'shift', 'sort', 'splice', 'unshift'];

/**
 * Class wrapping the Validation Model. It extends Event Emitter and has
 * custom getters and setters. Whenever a property is changed, an event
 * will be published. All subscribed to  the event will be notified.
 */
export class InstanceModel extends EventEmitter {

  constructor(validationModel) {
    super();
    let propertyDefinitionConfiguration = {};
    this._instanceModel = _.mapValues(validationModel, (property, attribute) => {
      propertyDefinitionConfiguration[attribute] = {
        get: () => {
          return this._instanceModel[attribute];
        },
        set: (newValue) => {
          this._instanceModel[attribute] = newValue;
        }
      };
      return new InstanceModelProperty(property);
    });

    //define getter/setter for for isValid
    propertyDefinitionConfiguration['isValid'] = {
      get: () => {
        return this._isValid;
      },
      set: (newProperty) => {
        this._isValid = newProperty;
        this.publish('modelValidated', newProperty);
      }
    };
    //both models shouldnt be enumerable
    propertyDefinitionConfiguration['_instanceModel'] = {
      enumerable: false
    };
    Object.defineProperties(this, propertyDefinitionConfiguration);
    this.isValid = true;
  }

  addPropertiesToModel(properties) {
    Object.keys(properties.serialize()).forEach((propertyName) => {
      if (!this[propertyName]) {
        this._instanceModel[propertyName] = properties[propertyName];
        Object.defineProperty(this, propertyName, {
          get: () => {
            return this._instanceModel[propertyName];
          },
          set: (newValue) => {
            this._instanceModel[propertyName] = newValue;
          }
        });
      }
    });
  }

  /**
   * returns the original validation model in json format.
   * @returns {{}}
   */
  serialize() {
    let originalModel = {};
    Object.keys(this._instanceModel).forEach((field) => {
      originalModel[field] = this._instanceModel[field].serialize();
    });
    return originalModel;
  }

  clone() {
    return new InstanceModel(_.cloneDeep(this.serialize()));
  }

  getProperties() {
    return this._instanceModel;
  }
}

/**
 * Represents a single property of the validation model.
 * Custom getters and setters are defined upon creation.
 * Each time a property is changed, a  'propertyChanged' event will be published.
 */
export class InstanceModelProperty extends EventEmitter {

  constructor(properties) {
    super();
    this._instanceProperties = properties;
    Object.keys(properties).forEach((attribute) => {
      if (properties[attribute] instanceof Array) {
        this.defineArrayMutatorMethods(attribute, this._instanceProperties[attribute]);
      } else if (properties[attribute] && properties[attribute].results) {
        // An object property
        this.defineArrayMutatorMethods(attribute, this._instanceProperties[attribute].results);
      }
    });
    Object.defineProperty(this, '_instanceProperties', {
      enumerable: false
    });
  }

  /**
   * Redefines the mutator methods on an array attribute of the InstanceModelProperty.
   * It is done by proxying the function and throwing a 'propertyChanged' event after manipulation.
   * Timeout is needed because of cases where multiple singular operations on the array is done (e.g: 10 pushes in a row).
   * One propertyChanged event will be fired, only when the current "thread" has finished its operations on the array and the timed out method executes.
   * @param attribute The attribute name (like: value, valueLabel, messages, validators and so on)
   * @param attributeValue
   */
  defineArrayMutatorMethods(attribute, attributeValue) {
    let mutatorMethodsConfig = {};

    ARRAY_MUTATOR_FUNCTIONS.forEach((func) => {
      let timeout = undefined;
      let proxyFunction = (...args) => {
        let result = Array.prototype[func].apply(attributeValue, args);
        if (timeout === undefined) {
          timeout = setTimeout(() => {
            this.publish('propertyChanged', {[attribute]: attributeValue});
            timeout = undefined;
          }, 0);
        }
        return result;
      };

      mutatorMethodsConfig[func] = {
        writable: false,
        value: proxyFunction,
        enumerable: false,
        configurable: true
      };
    });

    Object.defineProperties(attributeValue, mutatorMethodsConfig);
  }

  /**
   * Returns the original property in json format.
   * @returns {{}}
   */
  serialize() {
    return this._instanceProperties;
  }

  clone() {
    return _.cloneDeep(this._instanceProperties);
  }

  // manually define setter function.
  set(attribute, newValue) {
    if (!(_.isEqual(newValue, this._instanceProperties[attribute]))) {
      this._instanceProperties[attribute] = newValue;
      if (newValue instanceof Array) {
        this.defineArrayMutatorMethods(attribute, this._instanceProperties[attribute]);
      }
      if (newValue instanceof Object && newValue.results) {
        this.defineArrayMutatorMethods(attribute, this._instanceProperties[attribute].results);
      }

      this.publish('propertyChanged', {[attribute]: newValue});
    }
  }

  //  Manually defining getters
  get dataType() {
    return this._instanceProperties.dataType;
  }

  get value() {
    return this._instanceProperties.value;
  }

  get defaultValue() {
    return this._instanceProperties.defaultValue;
  }

  get valueLabel() {
    return this._instanceProperties.valueLabel;
  }

  get defaultValueLabel() {
    return this._instanceProperties.defaultValueLabel;
  }

  get valid() {
    return this._instanceProperties.valid;
  }

  get _wasInvalid() {
    return this._instanceProperties._wasInvalid;
  }

  get displayedByMandatoryCondition() {
    return this._instanceProperties.displayedByMandatoryCondition;
  }

  get validators() {
    return this._instanceProperties.validators;
  }

  get messages() {
    return this._instanceProperties.messages;
  }

  get richtextValue() {
    return this._instanceProperties.richtextValue;
  }

  get defaultRichTextValue() {
    return this._instanceProperties.defaultRichTextValue;
  }

  //  manually defining setters.
  set dataType(newValue) {
    this.set('dataType', newValue);
  }

  set value(newValue) {
    this.set('value', newValue);
  }

  set defaultValue(newValue) {
    this.set('defaultValue', newValue);
  }

  set valueLabel(newValue) {
    this.set('valueLabel', newValue);
  }

  set defaultValueLabel(newValue) {
    this.set('defaultValueLabel', newValue);
  }

  set valid(newValue) {
    this.set('valid', newValue);
  }

  set _wasInvalid(newValue) {
    this.set('_wasInvalid', newValue);
  }

  set displayedByMandatoryCondition(newValue) {
    this.set('displayedByMandatoryCondition', newValue);
  }

  set validators(newValue) {
    this.set('validators', newValue);
  }

  set messages(newValue) {
    this.set('messages', newValue);
  }

  set richtextValue(newValue) {
    this.set('richtextValue', newValue);
  }

  set defaultRichTextValue(newValue) {
    this.set('defaultRichTextValue', newValue);
  }

}