import {View, Component, Inject} from 'app/app';
import 'layout/ui-view/ui-view';
import routerConfig from 'adapters/router/router';
import {Router} from 'adapters/router/router';
import {MODE_EDIT, IDOC_STATE} from 'idoc/idoc-constants';
import application from 'app/app';

var routes = PluginRegistry.get('route');
application.config(routerConfig(application, routes));

@Component({
  selector: 'view-config'
})
@View({
  template: '<div><seip-ui-view></seip-ui-view></div>'
})
export class ViewConfig {
}

@Component({
  selector: 'view1'
})
@View({
  template: '<div id="first-view-content">' +
  'This is the first view' +
  '<div><a id="second-view-link" href="#/view2">Open second view</a></div>' +
  '<div><button id="second-view-button" ng-click="view1.openView2()">Open second view</button></div>' +
  '</div>'
})
@Inject(Router)
export class View1 {
  constructor(router) {
    router.$state.current.name = IDOC_STATE;
    router.$state.params.mode = MODE_EDIT;
    this.router = router;
  }

  openView2() {
    this.router.navigate('view2');
  }
}

@Component({
  selector: 'view2'
})
@View({
  template: '<div id="content">' +
  'This is the second view ' +
  '<div><a id="first-view-link" href="#/view1">Open first view</a></div></div>'
})
@Inject(Router)
export class View2 {
  constructor(router) {
    this.router = router;
  }
}