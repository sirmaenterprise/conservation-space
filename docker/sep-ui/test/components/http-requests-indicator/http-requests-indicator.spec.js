import {HttpRequestsIndicator} from 'components/http-requests-indicator/http-requests-indicator';
import {IdocMocks} from '../../idoc/idoc-mocks';
import {Eventbus} from 'services/eventbus/eventbus';
import {Logger} from 'services/logging/logger';
import {ActiveRequestsStatusChangedEvent} from 'services/interceptors/active-requests-status-changed-event';
import {RouterStateChangeSuccessEvent} from 'common/router/router-state-change-success-event';
import {stub, MockEventbus} from 'test/test-utils';

describe('HttpRequestsIndicator', () => {

  let httpRequestsIndicator;

  beforeEach(() => {
    let $timeout = IdocMocks.mockTimeout();
    sinon.stub($timeout, 'cancel');
    let $http = stubHttp(true);
    let $scope = {};
    httpRequestsIndicator = new HttpRequestsIndicator($timeout, stub(Eventbus), stub(Logger), $http, $scope);
  });

  afterEach(() => {
    httpRequestsIndicator.trackerThread && clearInterval(httpRequestsIndicator.trackerThread);
  });

  describe('on init', () => {
    it('should subscribe for ActiveRequestsStatusChangedEvent and RouterStateChangeSuccessEvent', () => {
      httpRequestsIndicator.ngOnInit();

      expect(httpRequestsIndicator.events.length).to.equal(2);
      expect(httpRequestsIndicator.eventbus.subscribe.getCall(0).args[0]).to.eql(ActiveRequestsStatusChangedEvent);
      expect(httpRequestsIndicator.eventbus.subscribe.getCall(1).args[0]).to.eql(RouterStateChangeSuccessEvent);
    });

    it('should start request tracker', () => {
      httpRequestsIndicator.ngOnInit();

      expect(httpRequestsIndicator.trackerThread !== undefined).to.be.true;
    });
  });

  describe('on route change', () => {
    it('should log prolonged pending requests', () => {
      httpRequestsIndicator.eventbus = new MockEventbus();
      sinon.stub(httpRequestsIndicator, 'performanceNow');
      // under threshold -> should not be reported
      httpRequestsIndicator.performanceNow.onCall(0).returns(4000.00000000000);
      // above threshold -> should be reported
      httpRequestsIndicator.performanceNow.onCall(1).returns(9000.00000000000);
      httpRequestsIndicator.ngOnInit();

      httpRequestsIndicator.eventbus.publish(new RouterStateChangeSuccessEvent());

      expect(httpRequestsIndicator.logger.warn.getCall(0).args).to.eql(['There were 1 pending requests after a view change: [{"correlationId":"c960af6aa6914452984f7b9ab008589e","url":"/remote/api/instances/batch","params":{},"ellapsedTime":"6.00"}]', true]);
    });

    it('should not log if there aren`t prolonged pending requests', () => {
      httpRequestsIndicator.eventbus = new MockEventbus();

      sinon.stub(httpRequestsIndicator, 'performanceNow');
      // under threshold -> should not be reported
      httpRequestsIndicator.performanceNow.onCall(0).returns(4000.00000000000);
      // under threshold -> should not be reported
      httpRequestsIndicator.performanceNow.onCall(1).returns(5000.00000000000);
      httpRequestsIndicator.ngOnInit();

      httpRequestsIndicator.eventbus.publish(new RouterStateChangeSuccessEvent());

      expect(httpRequestsIndicator.logger.warn.notCalled).to.be.true;
    });

    it('should not log if there aren`t pending requests', () => {
      httpRequestsIndicator.eventbus = new MockEventbus();
      httpRequestsIndicator.$http = stubHttp(false);
      httpRequestsIndicator.ngOnInit();

      httpRequestsIndicator.eventbus.publish(new RouterStateChangeSuccessEvent());

      expect(httpRequestsIndicator.logger.warn.notCalled).to.be.true;
    });
  });

  describe('changeLoadingComponentState', () => {
    it('should hide loading component if the active requests are equal to zero', () => {
      httpRequestsIndicator.changeLoadingComponentState(0);

      expect(httpRequestsIndicator.loadingComponentVisible).to.equal(false);
    });

    it('should show loading component if the active requests are more than zero', () => {
      httpRequestsIndicator.changeLoadingComponentState(1);

      expect(httpRequestsIndicator.loadingComponentVisible).to.equal(true);
    });
  });

  describe('on destroy', () => {
    it('should clean up', () => {
      httpRequestsIndicator.eventbus = stubEventBus();
      sinon.stub(httpRequestsIndicator, 'stopTrackerThread');
      httpRequestsIndicator.hideComponentTimeout = {};
      httpRequestsIndicator.ngOnInit();

      httpRequestsIndicator.ngOnDestroy();

      for (let event of httpRequestsIndicator.events) {
        expect(event.unsubscribe.calledOnce).to.be.true;
      }

      expect(httpRequestsIndicator.stopTrackerThread.calledOnce, 'Tracker thread should be stopped on destroy!').to.be.true;
      expect(httpRequestsIndicator.$timeout.cancel.calledOnce, 'Loader should be canceld on destroy!').to.be.true;
    });
  });

});

function stubHttp(withPending) {
  return withPending ? {
    pendingRequests: [
      {
        url: '/remote/api/codelist/503',
        headers: {
          'x-correlation-id': '201c242b9b9844abe18db10d0c465b04'
        },
        params: {},
        startTime: 2000.00000000000
      },
      {
        url: '/remote/api/instances/batch',
        headers: {
          'x-correlation-id': 'c960af6aa6914452984f7b9ab008589e'
        },
        params: {},
        startTime: 3000.00000000000
      }
    ]
  } : {
    pendingRequests: []
  };
}

function stubEventBus() {
  let eventBusStub = stub(Eventbus);
  eventBusStub.subscribe = sinon.spy(() => {
    return {
      unsubscribe: sinon.spy()
    };
  });
  return eventBusStub;
}
