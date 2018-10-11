import {Inject, HttpInterceptor} from 'app/app';
import {Eventbus} from 'services/eventbus/eventbus';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {ActiveRequestsStatusChangedEvent} from 'services/interceptors/active-requests-status-changed-event';

// A reference to 'this' context is stored here because interceptor functions are
// stored with no context when angular builds the interceptors chain.
// see: http://stackoverflow.com/questions/28638600/angularjs-http-interceptor-class-es6-loses-binding-to-this
let _self;

/**
 * An http interceptor that counts the active requests currently made to the server.
 *
 * @author aandreev
 */
@HttpInterceptor
@Inject(Eventbus, PromiseAdapter)
export class ActiveRequestsCounterInterceptor {

  constructor(eventbus, promiseAdapter) {
    this.eventbus = eventbus;
    this.promiseAdapter = promiseAdapter;
    this.activeRequests = 0;
    _self = this;
  }


  request(config) {
    _self.addActiveRequest();
    return config;
  }

  requestError(rejection) {
    _self.removeActiveRequest();
    return _self.promiseAdapter.reject(rejection);
  }

  response(response) {
    _self.removeActiveRequest();
    return response;
  }

  responseError(rejection) {
    _self.removeActiveRequest();
    return _self.promiseAdapter.reject(rejection);
  }

  addActiveRequest() {
    if (_self.activeRequests++ === 0) {
      _self.fireEvent();
    }
  }

  removeActiveRequest() {
    if (--_self.activeRequests === 0) {
      _self.fireEvent();
    }
  }

  fireEvent() {
    _self.eventbus.publish(new ActiveRequestsStatusChangedEvent({ count: _self.activeRequests }));
  }

}