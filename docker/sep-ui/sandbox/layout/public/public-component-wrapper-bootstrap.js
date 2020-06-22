import {Component, View, Inject} from 'app/app';
import {StateParamsAdapter} from 'adapters/router/state-params-adapter';
import {UrlUtils} from 'common/url-utils';

@Component({
  selector: 'public-component-wrapper-bootstrap'
})
@View({
  template: '<div> <seip-public-component-wrapper></seip-public-component-wrapper> </div>'
})
@Inject(StateParamsAdapter)
class PublicComponentWrapperBootstrap {

  constructor(stateParamsAdapter) {
    stateParamsAdapter.init();
    this.setComponentStateParam(stateParamsAdapter);
  }

  setComponentStateParam(stateParamsAdapter) {
    let componentId = UrlUtils.getUrlFragment(window.location.hash);
    stateParamsAdapter.setStateParam('componentId', componentId);
  }

}


@Component({
  selector: 'info'
})
@View({
  template: '<div class="info-component"> Info Component </div>'
})
export class Info {
}


@Component({
  selector: 'home'
})
@View({
  template: '<div class="home-component"> Home Component </div>'
})
export class Home {
}


PluginRegistry.add('public-components', {
  'name': 'info',
  'component': 'info',
  'module': 'sandbox/layout/public/public-component-wrapper-bootstrap'
});

PluginRegistry.add('public-components', {
  'name': 'home',
  'component': 'home',
  'module': 'sandbox/layout/public/public-component-wrapper-bootstrap'
});