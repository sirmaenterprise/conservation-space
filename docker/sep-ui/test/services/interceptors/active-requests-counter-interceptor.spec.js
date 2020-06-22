import {ActiveRequestsCounterInterceptor} from 'services/interceptors/active-requests-counter-interceptor';
import {Eventbus} from 'services/eventbus/eventbus';
import {PromiseAdapterMock} from 'test/adapters/angular/promise-adapter-mock';

describe('ActiveRequestsCounterInterceptor', function () {
  it('increase the activeRequests on request', () => {
    let eventbus = new Eventbus();
    let interceptor = new ActiveRequestsCounterInterceptor(eventbus);
    interceptor.activeRequests = 0;
    interceptor.request();
    expect(interceptor.activeRequests).to.equals(1);
  });

  it('decrease the activeRequests on requestError', () => {
    let eventbus = new Eventbus();
    let promiseAdapter = PromiseAdapterMock.mockImmediateAdapter();
    let interceptor = new ActiveRequestsCounterInterceptor(eventbus, promiseAdapter);
    interceptor.activeRequests = 1;
    var rejection = {
      statusText: 'Please try again'
    };
    interceptor.requestError(rejection).catch(() => {
    });
    expect(interceptor.activeRequests).to.equals(0);
  });

  it('decrease the activeRequests on response', () => {
    let eventbus = new Eventbus();
    let interceptor = new ActiveRequestsCounterInterceptor(eventbus);
    interceptor.activeRequests = 1;
    interceptor.response();
    expect(interceptor.activeRequests).to.equals(0);
  });

  it('decrease the activeRequests on responseError', () => {
    let eventbus = new Eventbus();
    let promiseAdapter = PromiseAdapterMock.mockImmediateAdapter();
    let interceptor = new ActiveRequestsCounterInterceptor(eventbus, promiseAdapter);
    interceptor.activeRequests = 1;
    var rejection = {
      statusText: 'Please try again'
    };
    interceptor.responseError(rejection).catch(() => {
    });
    expect(interceptor.activeRequests).to.equals(0);
  });

});