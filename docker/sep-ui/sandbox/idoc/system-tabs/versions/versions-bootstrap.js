import {Component, View,Inject} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {InstanceObject} from 'models/instance-object';
import template from './versions-bootstrap-template.html!text';
import 'style/bootstrap.css!';

@Component({
  selector: 'versions-bootstrap'
})
@View({
  template: template
})
@Inject(PromiseAdapter)
class Versions {
  constructor(promiseAdapter) {
    let models = {
      instanceType: 'documentinstance',
      validationModel: {
        'emf:contentId': {defaultValue: 'preview'}
      }
    };
    let currentObject = new InstanceObject('emf:111222', models);

    this.context = {
      currentObjectId: 'emf:111222',
      getCurrentObjectId: function () {
        return 'emf:111222';
      },
      getCurrentObject: function () {
        return promiseAdapter.resolve(currentObject);
      }
    };
  }
}