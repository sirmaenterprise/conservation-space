import {Component, View, Inject} from 'app/app';
import {UrlUtils} from 'common/url-utils';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import {AuthenticationService} from 'services/security/authentication-service';

import './disabled-user.css!css';
import template from './disabled-user.html!text';

/**
 * User will be redirected by the backend to this page when his account is disabled and not allowed to login
 * in the system.
 */
@Component({
  selector: 'seip-disabled-user'
})
@View({
  template
})
@Inject(WindowAdapter, AuthenticationService)
export class DisabledUser {

  constructor(windowAdapter, authenticationService) {
    this.windowAdapter = windowAdapter;
    this.authenticationService = authenticationService;
  }

  ngOnInit() {
    this.userId = UrlUtils.getParameter(this.windowAdapter.location.href, 'id');
  }

  redirectToLogin() {
    this.authenticationService.authenticate();
  }

}