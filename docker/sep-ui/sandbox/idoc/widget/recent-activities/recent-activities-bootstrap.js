import {Component, View, Inject} from 'app/app';
import {InstanceObject} from 'models/instance-object';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import 'idoc/widget/recent-activities/widget';
import template from 'recent-activities-bootstrap-template!text';

@Component({
  selector: 'recent-activities-bootstrap'
})
@View({
  template: template
})
@Inject(PromiseAdapter)
class RecentActivitiesBootstrap {

  constructor(promiseAdapter) {
    this.visible = false;
    this.isModelling = false;

    this.context = {
      getCurrentObject: () => promiseAdapter.resolve(new InstanceObject('current-object-id')),
      isPrintMode: () => false,
      isEditMode: () => false,
      isPreviewMode: () => true,
      isModeling: () => this.isModelling,
      getMode: () => undefined
    };

    this.control = {
      getId: () => 'my-widget'
    };

    this.config = {
      selectObjectMode: 'current',
      pageSize: 10
    };
  }

  showWidet() {
    this.visible = !this.visible;
  }

  toggleModelling() {
    this.isModelling = !this.isModelling;
  }
}