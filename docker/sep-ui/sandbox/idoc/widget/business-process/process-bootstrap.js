import {Component, View, Inject} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import 'idoc/widget/business-process/business-process-diagram-widget';
import template from './process-bootstrap.html!text';

@Component({
  selector: 'bootstrap'
})
@View({
  template: template
})
@Inject(PromiseAdapter)
class ProcessBootstrap {
  constructor(promiseAdapter) {
    this.visible = true;
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
      }
    };

    this.control = {
      getId: () => 'process-widget'
    };
  }

  toggleWidget() {
    this.visible = !this.visible;
  }
}
