import {Component, View} from 'app/app';
import {FormWidgetStub} from 'form-builder/form-widget-stub';
import {InstanceModel} from 'models/instance-model';
import {DefinitionModel} from 'models/definition-model';
import template from 'form-widget-template!text';

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
    this.formConfig.models = {
      'validationModel': new InstanceModel({
        'inputPasswordEdit': {
          'value': '',
          'messages': []
        }
      }),
      'viewModel':new DefinitionModel({
        'fields': [
          {
            'identifier': 'inputPasswordFields',
            'label': 'Input password fields',
            'displayType': 'EDITABLE',
            'fields': [
              {
                'previewEmpty': true,
                'identifier': 'inputPasswordEdit',
                'disabled': false,
                'displayType': 'EDITABLE',
                'tooltip': 'Test tooltip',
                'validators': [
                  {
                    id: 'regex',
                    context: {
                      pattern: '[\\s\\S]{1,20}'
                    },
                    message: 'This field should be max 20 characters length',
                    level: 'error'
                  },
                  {
                    id: 'mandatory',
                    message: 'The field is mandatory',
                    level: 'error'
                  }
                ],
                'dataType': 'password',
                'label': 'Editable password',
                'maxLength': 20,
                'isMandatory': true
              }
            ]
          }
        ]
      })
    };
  }
}
