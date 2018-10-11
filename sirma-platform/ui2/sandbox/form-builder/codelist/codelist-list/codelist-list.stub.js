import {Component, View} from 'app/app';
import {FormWidgetStub} from 'form-builder/form-widget-stub';
import {InstanceModel} from 'models/instance-model';
import {DefinitionModel} from  'models/definition-model';
import {EventEmitter} from 'common/event-emitter';
import template from 'form-widget-template!text';
import models from 'sandbox/form-builder/codelist/codelist-list/models.json!';

@Component({
  selector: 'seip-form-widget',
  properties: {
    'config': 'config'
  }
})
@View({
  template: template
})
export class FormWidget extends FormWidgetStub {

  constructor() {
    super();
    models.validationModel = new InstanceModel(models.validationModel);
    models.viewModel = new DefinitionModel(models.viewModel);
    this.formConfig.models = models;
    this.formConfig.eventEmitter = new EventEmitter();
  }
}
