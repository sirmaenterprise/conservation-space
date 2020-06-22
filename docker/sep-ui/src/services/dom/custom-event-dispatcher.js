import {Injectable} from 'app/app';
import _ from 'lodash';

@Injectable()
export class CustomEventDispatcher {

  dispatchEvent(eventTarget, eventName, params) {
    params = params || {};
    _.defaultsDeep(params, {bubbles: false, cancelable: false});
    let customEvent;
    if ( typeof window.CustomEvent === 'function' ) {
      customEvent = new CustomEvent(eventName, params);
    } else {
      customEvent = document.createEvent('CustomEvent');
      customEvent.initCustomEvent(eventName, params.bubbles, params.cancelable, params.details);
    }
    eventTarget.dispatchEvent(customEvent);
  }
}
