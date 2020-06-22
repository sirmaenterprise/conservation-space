import {Inject, Component} from 'app/app';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import {Router} from 'adapters/router/router';
import {TranslateService} from 'services/i18n/translate-service';
import {STATE_PARAM_ID, STATE_PARAM_MODE, MODE_EDIT} from 'idoc/idoc-constants';
import {StateParamsAdapter} from 'adapters/router/state-params-adapter';
import {ActionsService} from 'services/rest/actions-service';

/**
 * @author Svetlosar Kovatchev
 */
@Component({
  selector: 'navigation-retainment'
})
@Inject(WindowAdapter, Router, TranslateService, StateParamsAdapter, ActionsService)
export class NavigationRetainment {
  constructor(windowAdapter, router, translateService, stateParamsAdapter, actionsService) {
    this.windowAdapter = windowAdapter;
    this.router = router;
    this.translateService = translateService;
    this.stateParamsAdapter = stateParamsAdapter;
    this.actionsService = actionsService;
    this.windowAdapter.window.onbeforeunload = () => this.retain();
    this.windowAdapter.window.onunload = () => this.unlock();
  }

  unlock() {
    if(this.isLocked()) {
      this.actionsService.unlock(this.stateParamsAdapter.getStateParam(STATE_PARAM_ID));
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