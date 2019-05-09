import _ from 'lodash';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import {Component, Inject} from 'app/app';
import {Configuration} from 'common/application-config';
import {AuthenticationService} from 'security/authentication-service';
import {LocalStorageService} from 'services/storage/local-storage-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {UserActivityEvent} from 'layout/session/user-activity-event';

// using [set|clear]Timeout are directly used because of http://stackoverflow.com/a/16132314
// and we don't really need the digest cycle here

@Component({
  selector: 'session-keep-alive-handler'
})
@Inject(Configuration, AuthenticationService, WindowAdapter, LocalStorageService, Eventbus)
export class SessionKeepAliveHandler {
  constructor(configuration, authenticationService, windowAdapter, localStorageService, eventbus) {
    this.configuration = configuration;
    this.authenticationService = authenticationService;
    this.windowAdapter = windowAdapter;
    this.localStorageService = localStorageService;
    this.eventbus = eventbus;

    this.clearLocalStorageVars();
  }

  ngOnInit() {
    let ttl = this.configuration.get(Configuration.SESSION_TIMEOUT_PERIOD);
    if (ttl <= 0) {
      return;
    }
    this.addActivityMonitors(ttl);
    this.startSessionTimer(ttl);

    // this is for multi tab scenarios
    this.windowAdapter.window.addEventListener('storage', (event) => this.handleTimeoutEvent(event.key, event.newValue));
  }

  clearLocalStorageVars() {
    this.localStorageService.remove(LocalStorageService.LAST_USER_ACTIVITY);
    this.localStorageService.remove(LocalStorageService.SESSION_TIMEOUT);
  }

  handleTimeoutEvent(key, newValue) {
    if (key === LocalStorageService.SESSION_TIMEOUT && newValue === 'true') {
      this.windowAdapter.window.setTimeout(() => {
        this.authenticationService.removeToken();
        this.authenticationService.authenticate();
      }, 1000);
    }
  }

  addActivityMonitors(ttl) {
    let keepAliveHandler = _.debounce(() => this.registerUserActivity(ttl), 1000, { leading: true, maxWait: 1000 });

    $(document.body)
      .click(keepAliveHandler)
      .mousemove(keepAliveHandler)
      .keypress(keepAliveHandler);

    this.userActivityHandler = this.eventbus.subscribe(UserActivityEvent, keepAliveHandler);
  }

  registerUserActivity(ttl) {
    if (this.timerId) {
      this.windowAdapter.window.clearTimeout(this.timerId);
    }

    this.localStorageService.set(LocalStorageService.LAST_USER_ACTIVITY, new Date().getTime());
    this.startSessionTimer(ttl);
  }

  startSessionTimer(ttl) {
    let timeout = ttl * 60 * 1000;
    this.timerId = this.windowAdapter.window.setTimeout(() => {
      let now = new Date().getTime();
      let lastActivity = this.localStorageService.getNumber(LocalStorageService.LAST_USER_ACTIVITY) || 0;

      if ((now - lastActivity) >= timeout) {
        this.localStorageService.set(LocalStorageService.SESSION_TIMEOUT, true);
        this.authenticationService.logout(this.windowAdapter.location.href);
      }
    }, timeout);
  }

  ngOnDestroy() {
    this.userActivityHandler.unsubscribe();
  }
}