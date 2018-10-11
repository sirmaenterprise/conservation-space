import postal from 'postaljs/postal.js';
import {Injectable} from 'app/app';
import _ from 'lodash';

/**
 * Service wrapper for the postal event emitter library.
 *
 * @see https://github.com/postaljs/postal.js
 * @see https://github.com/postaljs/postal.js/wiki/API
 *
 * @author svelikov
 */
@Injectable()
export class Eventbus {

  /**
   * Subscribe for given topic and optional channel. If channel is not provided then
   * the default channel "/" is used.
   *
   * @param
   * Subscribe with an event object
   * <code>
   *   Eventbus.subscribe(OrderAddItemEvent, callback);
   * </code>
   * <code>
   * {
   *    channel: "Customer",
   *    topic: "order.add.item",
   *    callback: function(data, envelope) {}
   * }
   * </code>
   * The envelope format is predefined and is built by the library
   * <code>
   * {
   *    channel: "Customer",
   *    topic: "order:add.item",
   *    timeStamp: "the date/time the message was published",
   *    data: {}
   * }
   * </code>
   */
  subscribe() {
    var argsCount = arguments.length;
    if (argsCount < 1 || argsCount > 2) {
      throw new Error('Eventbus.subscribe expects 1 or 2 arguments but ' + argsCount + ' were passed');
    }

    var topic, callback, envelope;

    if (arguments.length === 1 && typeof arguments[0] === 'object') {
      envelope = arguments[0];
    } else if(argsCount === 2) {
      callback = arguments[1];
      if(typeof arguments[0] === 'string') {
        topic = arguments[0];
      } else {
        topic = arguments[0].EVENT_NAME;
      }
      envelope = {
          topic: topic,
          callback: callback
      };
    }
    return postal.subscribe(envelope);
  }

  /**
   * A shorthand method for subscribing for topic context.
   * <b>Here the options.channel should contain the topic context.</b>
   */
  subscribeForContext(options) {
    var _options = options;
    _options.topic = _options.topic + '.#';
    return this.subscribe(_options);
  }

  /**
   * Un-subscribe from a subscription.
   */
  unsubscribe(subscription) {
    postal.unsubscribe(subscription);
  }

  unsubscribeFor() {
    return postal.unsubscribeFor(arguments);
  }

  /**
   * Publish a message to a channel and topic.
   *
   * @param envelope
   * <code>
   *   Eventbus.publish(new OrderAddItemEvent(data))
   * </code>
   * <code>
   * {
   *    channel: "Customer",
   *    topic: "order:add.item",
   *    data: {}
   * }
   * </code>
   */
  publish(envelope) {
    var channel, topic, data, _envelope = {};
    if (typeof envelope === 'object') {
      topic = envelope.topic || envelope.constructor.EVENT_NAME;
      channel = envelope.channel || envelope.constructor.CHANNEL;
      data = envelope.data || (typeof envelope.getData === 'function' && envelope.getData());
      if (topic) {
        _envelope['topic'] = topic;
      }
      if (channel) {
        _envelope['channel'] = channel;
      }
      if (data) {
        _envelope['data'] = data;
      }
    } else {
      throw new TypeError('Eventbus.publish requires an object argument!');
    }
    return this.executePublish(_envelope);
  }

  /**
   * Proxy the library invocation because the postal publishes some own events internally and unit testing
   * should avoid this.
   *
   * @param arg
   * @returns {*}
   */
  executePublish(arg) {
    return postal.publish(arg);
  }

  /**
   * Get a channel by name or the default one if no name is provided.
   */
  channel(name) {
    return postal.channel(name);
  }

  /**
   * Links source and destination channels in order to allow inter-channel notifications.
   */
  linkChanel(source, destination) {
    return postal.linkChannels(source, destination);
  }

  /**
   * Removes all subscriptions from the postal instance.
   */
  reset() {
    postal.reset();
  }
}
