import {Component, View, Inject} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {InstanceRestService} from "services/rest/instance-service";
import 'idoc/widget/object-link-widget/object-link-widget';
import 'instance-header/static-instance-header/static-instance-header';
import template from 'object-link-widget-template!text';

@Component({
  selector: 'object-link-widget-bootstrap'
})
@View({
  template: template
})
@Inject(PromiseAdapter, InstanceRestService)
class ObjectLinkWidgetBootstrap {
  constructor(promiseAdapter, instanceRestService) {

    this.context = {
      isPrintMode: ()=> {
        return true
      },
      getCurrentObject: ()=> {
        return promiseAdapter.resolve({
          getId: ()=> {
            return 'resolved';
          }
        });
      }
    };

    this.control = {
      getId: () => 'object-link'
    };
  }

  insertWidget() {
    this.visible = true;
  }
}