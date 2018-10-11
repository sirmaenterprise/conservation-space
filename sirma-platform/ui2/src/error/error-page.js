import {View, Component, Inject} from 'app/app';
import {StateParamsAdapter} from 'adapters/router/state-params-adapter';
import {TranslateService} from 'services/i18n/translate-service';
import errorTemplate from './error-page.html!text';
import './error-page.css!';

export const ERROR_KEY = 'key';
export const NOT_FOUND = 'not_found';
export const UNKNOWN   = 'unknown';

@Component({
  selector: 'seip-error-page'
})
@View({
  template: errorTemplate
})
@Inject(StateParamsAdapter, TranslateService)
export class ErrorPage {
  constructor(stateParamsAdapter, translateService) {
    this.translateService = translateService;

    let messages = {};
    messages[NOT_FOUND] = this.translateService.translateInstant('error.page.not.found');
    messages[UNKNOWN] = this.translateService.translateInstant('error.page.unknown');

    this.stateParamsAdapter = stateParamsAdapter;
    this.errorKey = this.stateParamsAdapter.getStateParam(ERROR_KEY);
    this.errorMessage = messages[this.errorKey] || this.translateService.translateInstant(this.errorKey);
  }
}
