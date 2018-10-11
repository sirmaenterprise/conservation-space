import {Inject, Injectable} from 'app/app';
import _ from 'lodash';
import {StateParamsAdapter} from 'adapters/router/state-params-adapter';
import {UrlUtils} from 'common/url-utils';

@Injectable()
@Inject(StateParamsAdapter)
export class Router {
  constructor(stateParamsAdapter) {
    this.params = stateParamsAdapter.$stateParams;
    this.params.mode = UrlUtils.getParameter(window.location.hash, 'mode');
    if (window.location.hash && window.location.hash.length > 0) {
      if (window.location.hash.indexOf('?') !== -1) {
        this.params.id = window.location.hash.substr(2, window.location.hash.indexOf('?') - 2);
      } else {
        this.params.id = window.location.hash.substr(2);
      }
    }
    this.options = {};
  }
  navigate(state, params, options) {
    _.merge(this.params, params);
    this.options = options;
    window.location.hash = `#/${this.params.id}` + (this.params.mode?`?mode=${this.params.mode}`:'') + (this.params['#']?`#${this.params['#']}`:'');
  }
}
