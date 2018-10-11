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
      definitionId: 'ET123121',
      'validationModel': new InstanceModel({
        'singleCheckboxEdit': {
          'dataType': 'boolean',
          'value': true,
          'defaultValue': true,
          'messages': []
        },
        'singleCheckboxPreview': {
          'dataType': 'boolean',
          'value': true,
          'defaultValue': true,
          'messages': []
        },
        'singleCheckboxDisabled': {
          'dataType': 'boolean',
          'value': true,
          'defaultValue': true,
          'messages': []
        },
        'singleCheckboxHidden': {
          'dataType': 'boolean',
          'value': true,
          'defaultValue': true,
          'messages': []
        },
        'singleCheckboxSystem': {
          'dataType': 'boolean',
          'value': true,
          'defaultValue': true,
          'messages': []
        }
      }),
      'viewModel': new DefinitionModel({
        'fields': [
          {
            'identifier': 'checkboxFields',
            'label': 'Checkbox fields',
            'displayType': 'EDITABLE',
            'fields': [{
              'previewEmpty': true,
              'identifier': 'singleCheckboxEdit',
              'disabled': false,
              'displayType': 'EDITABLE',
              'validators': [],
              'dataType': 'boolean',
              'label': 'Editable single checkbox',
              'isMandatory': true,
              'tooltip': 'Test tooltip',
              'mandatoryValidatorError': 'The field is mandatory'
            },
              {
                'previewEmpty': true,
                'identifier': 'singleCheckboxPreview',
                'disabled': false,
                'displayType': 'READ_ONLY',
                'validators': [],
                'dataType': 'boolean',
                'label': 'Preview single checkbox',
                'isMandatory': false
              },
              {
                'previewEmpty': true,
                'identifier': 'singleCheckboxDisabled',
                'disabled': true,
                'displayType': 'EDITABLE',
                'validators': [],
                'dataType': 'boolean',
                'label': 'Disabled single checkbox',
                'isMandatory': false,
                'tooltip': 'Test tooltip',
              },
              {
                'previewEmpty': true,
                'identifier': 'singleCheckboxHidden',
                'disabled': false,
                'displayType': 'HIDDEN',
                'validators': [],
                'dataType': 'boolean',
                'label': 'Hidden single checkbox',
                'isMandatory': false,
                'tooltip': 'Test tooltip',
              },
              {
                'previewEmpty': true,
                'identifier': 'singleCheckboxSystem',
                'disabled': false,
                'displayType': 'SYSTEM',
                'validators': [],
                'dataType': 'boolean',
                'label': 'System single checkbox',
                'isMandatory': false
              }
            ]
          }
        ]
      })
    };
  }
}
