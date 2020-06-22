import {Component, View} from 'app/app';
import {FormWidgetStub} from 'form-builder/form-widget-stub';
import {InstanceModel} from 'models/instance-model';
import {DefinitionModel} from 'models/definition-model';
import _ from 'lodash';

import template from 'form-widget-template!text';
import models from 'sandbox/form-builder/validation/condition/models.json!';


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
    var date = new Date('2015/12/22').toISOString();

    var _models = _.cloneDeep(models);

    _models.validationModel.datetimefield1.value = date;
    _models.validationModel.datetimefield1.defaultValue = date;
    _models.validationModel = new InstanceModel(_models.validationModel);
    _models.viewModel = new DefinitionModel(_models.viewModel);
    this.formConfig.models = _models;
  }
}
