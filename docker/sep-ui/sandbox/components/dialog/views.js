import {View, Component, Inject} from 'app/app';
import 'layout/ui-view/ui-view';
import routerConfig from 'adapters/router/router';
import {Router} from 'adapters/router/router';
import application from 'app/app';

var routes = PluginRegistry.get('route');
application.config(routerConfig(application, routes));

@Component({
  selector: 'view-config'
})
@View({
  template: '<div><seip-ui-view></seip-ui-view></div>'
})
@Inject(Router)
export class ViewConfig {
  // injection of Router is required because the routing events get initialized there
  constructor(router) {
  }
}

@Component({
  selector: 'view1'
})
@View({
  template: '<div>This is the first view</div>'
})
export class View1 {
}