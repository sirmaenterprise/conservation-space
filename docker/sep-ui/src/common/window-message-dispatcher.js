import {Inject, Injectable} from 'app/app';
import {EventEmitter} from './event-emitter';
import {JsonUtil} from './json-util';

/**
 * Extends the window message event for external origin communication.
 * The format for communication using this api is an object, which has an attribute topic,
 * which is used for publishing it to its respected listeners.
 * The topic is extracted and published with the remaining data.
 */
@Injectable()
@Inject('$rootScope')
export class WindowMessageDispatcher extends EventEmitter {
  constructor($rootScope) {
    super();
    this.$rootScope = $rootScope;
    window.addEventListener('message', this.onMessageListener.bind(this));
  }

  /**
   * Extracts topic and data from the recieved message and publishes it to subscribers.
   * @param message
   */
  onMessageListener(message) {
    let [origin, data] = [message.origin, message.data];
    if (window.location.href.indexOf(origin) === -1) {
      if (JsonUtil.isJson(data)) {
        data = JSON.parse(data);
      }
      this.$rootScope.$evalAsync(() => {
        super.publish(data.topic, data);
      });
    }
  }
}

WindowMessageDispatcher.DELIMITER = ',';