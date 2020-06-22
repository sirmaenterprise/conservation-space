import {Injectable} from 'app/app';
import {UrlUtils} from 'common/url-utils';

@Injectable()
export class StateParamsAdapter {

  constructor() {
    this.init();
  }

  init() {
    this.$stateParams = {};
    this.$stateParams.mode = UrlUtils.getParameter(window.location.hash, 'mode');
    if (window.location.hash && window.location.hash.length > 0) {
      if (window.location.hash.indexOf('?') !== -1) {
        this.$stateParams.id = window.location.hash.substr(2, window.location.hash.indexOf('?') - 2);
      } else {
        this.$stateParams.id = window.location.hash.substr(2);
      }
    }
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
