import {Component, View} from 'app/app';
import {InstanceObject} from 'models/instance-object';
import 'idoc/widget/aggregated-table/widget';
import template from 'sandbox/idoc/widget/aggregated-table/bootstrap.html!text';

@Component({
  selector: 'bootstrap'
})
@View({
  template: template
})
class AggregatedTableBootstrap {
  constructor() {
    this.visible = false;
    this.isModelling = false;

    this.context = {
      getMode: () => {
        return 'edit';
      },
      getCurrentObject: () => {
        return promiseAdapter.resolve({
          getId: () => {
            return 'resolved';
          }
        });
      },
      getSharedObject: () => {
        return Promise.resolve({});
      },
      getSharedObjects: () => {
        return Promise.resolve({data: [new InstanceObject('1', {definitionId: "definitionId"})]});
      }, isPrintMode: () => {
        return false;
      }, isEditMode: () => {
        return false;
      }, isPreviewMode: () => {
        return false;
      }, isModeling: () => this.isModelling
    };
  }

  toggleWidget() {
    this.visible = !this.visible;
  }

  setModellingMode() {
    this.isModelling = !this.isModelling;
  }
}
