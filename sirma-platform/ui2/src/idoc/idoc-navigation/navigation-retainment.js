import {Inject, Component} from 'app/app';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import {Router} from 'adapters/router/router';
import {TranslateService} from 'services/i18n/translate-service';
import {BASE_PATH, HEADER_V2_JSON} from 'services/rest-client';
import {STATE_PARAM_ID, STATE_PARAM_MODE, MODE_EDIT} from 'idoc/idoc-constants';
import {StateParamsAdapter} from 'adapters/router/state-params-adapter';
import {AuthenticationService} from 'services/security/authentication-service';

/**
 * @author Svetlosar Kovatchev
 */
@Component({
  selector: 'navigation-retainment'
})
@Inject(WindowAdapter, Router, TranslateService, StateParamsAdapter, AuthenticationService)
export class NavigationRetainment {
  constructor(windowAdapter, router, translateService, stateParamsAdapter, authenticationService) {
    this.windowAdapter = windowAdapter;
    this.router = router;
    this.translateService = translateService;
    this.stateParamsAdapter = stateParamsAdapter;
    this.authenticationService = authenticationService;
    this.windowAdapter.window.onbeforeunload = () => this.retain();
    this.windowAdapter.window.onunload = () => this.unlock();
  }

  unlock() {
    if(this.isLocked()) {
      $.ajax({
        type: 'POST',
        contentType: HEADER_V2_JSON,
        accept: HEADER_V2_JSON,
        url: BASE_PATH + '/instances/' + this.stateParamsAdapter.getStateParam(STATE_PARAM_ID) + '/actions/unlock',
        async: false,
        headers: {
          'Authorization': 'Bearer ' + this.authenticationService.getToken()
        }
      });
    }
    this.windowAdapter.window.removeEventListener('onunload', onunload);
  }

  retain() {
    if (this.router.shouldInterrupt()) {
      this.windowAdapter.window.removeEventListener('onbeforeunload', onbeforeunload);
      return this.translateService.translateInstant('router.interrupt.dialog.message');
    }
  }

  isLocked() {
    return this.stateParamsAdapter.getStateParam(STATE_PARAM_ID) && this.stateParamsAdapter.getStateParam(STATE_PARAM_MODE) === MODE_EDIT;
  }
}