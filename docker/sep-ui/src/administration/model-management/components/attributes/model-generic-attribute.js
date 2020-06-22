import {ModelAttributeTypes} from 'administration/model-management/model/attributes/model-attribute-types';
import {Configurable} from 'components/configurable';
import {EventEmitter} from 'common/event-emitter';

const ATTRIBUTE_CHANGE = 'ATTRIBUTE_CHANGE';

/**
 * Generic attribute class responsible for providing common logic which extends
 * to all of the different model attribute types. This generic attribute provides
 * support for controlling manual attribute changes. This includes changing the
 * value of an attribute or clearing the attribute value to it's default value.
 * When a manual clear or change is issued internally the implementation has the
 * ability to publish that change to an event emitter which can be optionally provided
 * as a configuration to the attribute. The emitter should be of type {@link EventEmitter}
 * and must be explicitly provided as by default it is not. But In order to subscribe to
 * attribute changes emitter must be present and is not optional. To subscribe to change
 * simply call the {@ModelGenericAttribute#subscribe} method with the attribute to which
 * the subscription is targeted and a callback method to be executed when change occurs.
 *
 * @author Svetlozar Iliev
 */
export class ModelGenericAttribute extends Configurable {

  constructor(config) {
    super(config || {});
    this.listeners = [];
  }

  onModelChange(oldValue) {
    let attribute = this.onChange && this.onChange();

    if (this.attribute !== attribute) {
      this.setValue(oldValue);
      this.attribute = attribute;
    }
    this.publish(oldValue);
    return attribute;
  }

  clearAttributeValue() {
    let type = this.attribute.getType();
    let currentValue = this.attribute.getValue().getValue();
    let defaultValue = ModelAttributeTypes.getDefaultValue(type);

    if (currentValue !== defaultValue) {
      this.attribute.getValue().setValue(defaultValue);
      this.onModelChange(currentValue);
    }
  }

  publish(oldValue) {
    this.hasEmitter(false) && this.config.emitter.publish(this.getEvent(this.getId()), [this.getValue(), oldValue]);
  }

  subscribe(target, handler) {
    this.hasEmitter(true) && this.listeners.push(this.config.emitter.subscribe(this.getEvent(target.getId()), handler));
  }

  getId() {
    return this.attribute.getId();
  }

  getValue() {
    return this.attribute.getValue().getValueEscaped();
  }

  setValue(value) {
    this.attribute.getValue().setValue(value);
  }

  getEvent(id) {
    return `${ATTRIBUTE_CHANGE}_${id.toUpperCase()}`;
  }

  hasEmitter(fail) {
    let emitter = this.config.emitter;
    if((!emitter || !(emitter instanceof EventEmitter)) && fail) {
      throw new TypeError('Using this functionality requires ' +
        'emitter to be provided to the attribute\'s configuration');
    }
    return !!emitter;
  }

  ngOnDestroy() {
    this.listeners.forEach(listener => listener.unsubscribe());
  }
}