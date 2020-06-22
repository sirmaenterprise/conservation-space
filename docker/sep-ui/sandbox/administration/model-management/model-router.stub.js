import {Injectable, Inject} from 'app/app';
import {StateParamsAdapter} from 'adapters/router/state-params-adapter';
import {UrlUtils} from 'common/url-utils';

import _ from 'lodash';

@Injectable()
@Inject(StateParamsAdapter)
export class Router {

  constructor(stateParamsAdapter) {
    this.options = {};
    this.params = stateParamsAdapter.$stateParams;
    let model = UrlUtils.getParameter(window.location.hash, 'model');
    this.params.model = model && model.length ? atob(model) : '';
  }

  navigate(state, params, options) {
    _.merge(this.params, params);
    this.options = options;
    window.location.hash = '#/' + (this.params.model ? `?model=${btoa(this.params.model)}` : '');
  }
}
