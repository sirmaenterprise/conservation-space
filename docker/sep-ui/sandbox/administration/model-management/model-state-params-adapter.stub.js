import {Injectable} from 'app/app';
import {UrlUtils} from 'common/url-utils';

@Injectable()
export class StateParamsAdapter {

  constructor() {
    this.$stateParams = {};
    this.$stateParams.model = atob(UrlUtils.getParameter(window.location.hash, 'model'));
  }

  getStateParams() {
    return this.$stateParams;
  }

  getStateParam(name) {
    return this.$stateParams[name];
  }

  setStateParam(name, value) {
    this.$stateParams[name] = value;
  }
}
