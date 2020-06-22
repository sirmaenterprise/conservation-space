import {View, Component, Inject, NgTimeout, NgHttp, NgScope} from 'app/app';
import {Eventbus} from 'services/eventbus/eventbus';
import {ActiveRequestsStatusChangedEvent} from 'services/interceptors/active-requests-status-changed-event';
import {RouterStateChangeSuccessEvent} from 'common/router/router-state-change-success-event';
import {X_CORRELATION_ID} from 'services/rest/http-headers';
import {Logger} from 'services/logging/logger';
import './http-requests-indicator.css!';
import template from './http-requests-indicator.html!text';

const HIDE_INDICATOR_DELAY = 500;
// Time interval in milliseconds which the request tracker will use to rescan pending requests.
const SCAN_INTERVAL = 5000;
// Time in seconds after which a pending request would be considered long running and most likely problematic.
const LONG_RUNNING_REQUEST_THRESHOLD = 60;
// Time in seconds used to filter requests which will be reported after view change.
const INCOMPLETED_REQUESTS_THRESHOLD = 5;

@Component({
  selector: 'seip-http-requests-indicator'
})
@View({
  template
})
@Inject(NgTimeout, Eventbus, Logger, NgHttp, NgScope)
export class HttpRequestsIndicator {
  constructor($timeout, eventbus, logger, $http, $scope) {
    this.logger = logger;
    this.$timeout = $timeout;
    this.$http = $http;
    this.$scope = $scope;
    this.eventbus = eventbus;
    this.loadingComponentVisible = false;
  }

  ngOnInit() {
    this.events = [
      this.eventbus.subscribe(ActiveRequestsStatusChangedEvent, (event) => this.changeLoadingComponentState(event[0].count)),
      this.eventbus.subscribe(RouterStateChangeSuccessEvent, () => this.routeChangeHandler())
    ];
    this.initRequestTracker();
  }

  changeLoadingComponentState(value) {
    if (value !== 0) {
      this.loadingComponentVisible = true;
      this.$timeout.cancel(this.hideComponentTimeout);
    } else {
      this.hideComponentTimeout = this.$timeout(() => {
        this.loadingComponentVisible = false;
      }, HIDE_INDICATOR_DELAY);
    }
  }

  routeChangeHandler() {
    let pendingRequests = this.getPendingRequests();
    if (pendingRequests.length > 0) {
      let data = [];
      pendingRequests.forEach((request) => {
        let requestData = this.getPendingRequestData(request);
        if (requestData.ellapsedTime > INCOMPLETED_REQUESTS_THRESHOLD) {
          data.push(requestData);
        }
      });
      if (data.length) {
        this.logger.warn(`There were ${data.length} pending requests after a view change: ${JSON.stringify(data)}`, true);
      }
    }
  }

  initRequestTracker() {
    this.trackerThread = setInterval(() => {
      this.getPendingRequests().forEach((request) => {
        if (!request.reported && request.startTime) {
          let ellapsedTime = this.getEllapsedTime(request);
          if (ellapsedTime > LONG_RUNNING_REQUEST_THRESHOLD) {
            // prevent reporting same request again
            request.reported = true;
            this.logger.warn(`Found long running request: ${ellapsedTime}sec : ${JSON.stringify(this.getPendingRequestData(request))}`, true);
          }
        }
      });
    }, SCAN_INTERVAL);
  }

  getPendingRequests() {
    return this.$http.pendingRequests;
  }

  performanceNow() {
    return performance.now();
  }

  getEllapsedTime(request) {
    if (request.startTime) {
      return ((this.performanceNow() - request.startTime) / 1000).toFixed(2);
    }
  }

  getPendingRequestData(request) {
    return {
      correlationId: request.headers[X_CORRELATION_ID],
      url: request.url,
      params: request.params,
      ellapsedTime: this.getEllapsedTime(request)
    };
  }

  stopTrackerThread() {
    clearInterval(this.trackerThread); // NOSONAR
  }

  ngOnDestroy() {
    for (let event of this.events) {
      event.unsubscribe();
    }
    if (this.trackerThread) {
      this.stopTrackerThread();
    }
    if (this.hideComponentTimeout) {
      this.$timeout.cancel(this.hideComponentTimeout);
    }
  }

}