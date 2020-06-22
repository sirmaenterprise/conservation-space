import {SessionKeepAliveHandler} from 'layout/session/session-keep-alive-handler';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import {Component, Inject, View} from 'app/app';
import {Configuration} from 'common/application-config';
import {AuthenticationService} from 'security/authentication-service';
import {LocalStorageService} from 'services/storage/local-storage-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {UserActivityEvent} from 'layout/session/user-activity-event';

@Component({
  selector: 'session-keep-alive-handler-stub'
})
@View({
  template: '<button type="button" id="trigger" ng-click="sessionKeepAliveHandlerStub.triggerUserActivityEvent()">Trigger user activity event</button>'
})
@Inject(Configuration, AuthenticationService, WindowAdapter, LocalStorageService, Eventbus)
export class SessionKeepAliveHandlerStub extends SessionKeepAliveHandler {

  constructor(configuration, authenticationService, windowAdapter, localStorageService, eventbus) {
    super(configuration, authenticationService, windowAdapter, localStorageService, eventbus);
  }

  registerUserActivity(ttl) {
    $('.status').val('ping')
    super.registerUserActivity(ttl);
  }

  triggerUserActivityEvent() {
    this.eventbus.publish(new UserActivityEvent());
  }
}