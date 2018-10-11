import {Component, View, Inject} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {InstanceObject} from 'models/instance-object';
import 'idoc/widget/chart-view-widget/chart-view-widget';
import template from './chart-view-bootstrap.html!text';

@Component({
  selector: 'bootstrap'
})
@View({
  template: template
})
@Inject(PromiseAdapter)
class ChartViewBootstrap {
  constructor(promiseAdapter) {
    this.visible = false;
    this.isModeling = false;

    this.context = {
      getMode: () => {
        return 'edit';
      },
      isModeling: () => {
        return this.isModeling;
      },
      isPrintMode: () => false,
      isEditMode: () => false,
      getCurrentObject: () => {
        if (this.isModeling) {
          return promiseAdapter.resolve();
        }
        return promiseAdapter.resolve({
          isVersion: () => {
            return false;
          },
          getId: () => {
            return 'emf:123';
          }
        });
      },
      getSharedObjects: () => {
        return Promise.resolve({data: [new InstanceObject('emf:123', {definitionId: "definitionId"})]});
      }
    };

    this.control = {
      getId: () => 'chart-view-widget'
    };
  }

  toggleWidget() {
    this.visible = !this.visible;
  }
}
