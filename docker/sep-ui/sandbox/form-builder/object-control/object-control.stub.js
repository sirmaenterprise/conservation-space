import {Component, View} from 'app/app';
import {FormWidgetStub} from 'form-builder/form-widget-stub';
import {InstanceModel} from 'models/instance-model';
import {DefinitionModel} from  'models/definition-model';
import _ from 'lodash';
import template from 'sandbox/form-builder/form-widget.stub.html!text';
import data from 'sandbox/services/rest/instance-service.data.json!';

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
    this.config.enableHint = true;
    this.formConfig.models = _.cloneDeep(data.models.objectProperties);
    this.formConfig.models.validationModel = new InstanceModel(_.cloneDeep(data.models.objectProperties.validationModel));
    this.formConfig.models.viewModel = new DefinitionModel(_.cloneDeep(data.models.objectProperties.viewModel));
  }
}
