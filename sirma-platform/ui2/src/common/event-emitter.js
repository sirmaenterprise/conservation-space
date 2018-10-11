import _ from 'lodash';
import uuid from 'common/uuid';

class SubscriptionDefinition {
  constructor(event, handler, subscriptionList) {
    this.uuid = uuid();
    this.event = event;
    this.handler = handler;
    this.subscriptionList = subscriptionList;
  }

  unsubscribe() {
    this.subscriptionList.forEach((subscription, index)=> {
      if (subscription.uuid === this.uuid) {
        this.subscriptionList.splice(index, 1);
      }
    });
  }

}

export class EventEmitter {
  constructor() {
    this.subscriptions = {};
  }

  /**
   * Subscribes to a given event and executes the handler.
   * @param event event to subscribe to.
   * @param handler handler that will be executed on emitted event
   * @returns {*} subscripitionDefinition object, that can be used for individual unsibscription
   */
  subscribe(event, handler) {
    if (event === undefined) {
      throw new Error("Can't subscribe to undefined topic");
    } else if (!(typeof handler === 'function')) {
      throw new Error('The subscription handler must be a function');
    }
    this.subscriptions[event] = this.subscriptions[event] || [];
    let newSubscription = new SubscriptionDefinition(event, handler, this.subscriptions[event]);
    this.subscriptions[event].push(newSubscription);
    return newSubscription;
  }

  publish(event, payload) {
    if (this.subscriptions && this.subscriptions[event]) {
      this.subscriptions[event].forEach((subscription)=> {
        subscription.handler(payload);
      });
    }
  }

  /**
   * Use with caution. This is best suited when a component initializes
   * the event emitter and it is shared between its child components.
   * Call this method when parent component is destroyed.
   */
  unsubscribeAll() {
    Object.keys(this.subscriptions).forEach((event)=> {
      this.subscriptions[event].forEach((subscription) => subscription.unsubscribe());
    });
  }
}

//wrapper for objects, which can emit events.
export class EmittableObject extends EventEmitter {
  constructor(properties) {
    super();
    if (properties) {
      Object.keys(properties).forEach((property)=> {
        this[property] = properties[property];
        Object.defineProperty(this, property, {
          get: ()=> {
            return properties[property];
          },
          set: (newValue) => {
            if (!(_.isEqual(newValue, this[property]))) {
              properties[property] = newValue;
              this.publish('propertyChanged', {[property]: newValue});
            }
          }
        });
      });
    }

    Object.defineProperties(this, {
      '_cache': {enumerable: false, writable: true},
      '_subscriptions': {enumerable: false, writable: true}
    });
  }
}