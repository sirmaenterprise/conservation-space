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
      let instanceProperty = new InstanceModelProperty(property);
      return instanceProperty;
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
      if(!this[propertyName]) {
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
}
/**
 * Represents a single property of the validaiton model.
 * Custom getters and setters are defined upon creation.
 * Each time a property is changed, a  'propertyChanged' event will be published.
 */
export class InstanceModelProperty extends EventEmitter {

  constructor(properties) {
    super();
    this._instanceProperties = properties;
    Object.keys(properties).forEach((attribute) => {
      if (properties[attribute] instanceof Array) {
        this.defineArrayMutatorMethods(attribute);
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
   * @param attribute
   */
  defineArrayMutatorMethods(attribute) {
    let mutatorMethodsConfig = {};

    ARRAY_MUTATOR_FUNCTIONS.forEach((func) => {
      let timeout = undefined;
      let proxyFunction = (...args) => {
        let result = Array.prototype[func].apply(this._instanceProperties[attribute], args);
        if (timeout === undefined) {
          timeout = setTimeout(() => {
            this.publish('propertyChanged', {[attribute]: this._instanceProperties[attribute]});
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

    Object.defineProperties(this._instanceProperties[attribute], mutatorMethodsConfig);
  }

  /**
   * Returns the original property in json forman.
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
        this.defineArrayMutatorMethods(attribute);
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

  get displayedByMandatoryCondition() {
    return this._instanceProperties.displayedByMandatoryCondition;
  }

  get validators() {
    return this._instanceProperties.validators;
  }

  get messages() {
    return this._instanceProperties.messages;
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

  set displayedByMandatoryCondition(newValue) {
    this.set('displayedByMandatoryCondition', newValue);
  }

  set validators(newValue) {
    this.set('validators', newValue);
  }

  set messages(newValue) {
    this.set('messages', newValue);
  }

}