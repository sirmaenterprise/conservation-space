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
        'inputTextEdit': {
          'value': 'inputTextEdit',
          'messages': []
        },
        'inputTextPreview': {
          'value': 'inputTextPreview',
          'messages': []
        },
        'inputTextDisabled': {
          'value': 'inputTextDisabled',
          'messages': []
        },
        'inputTextHidden': {
          'value': 'inputTextHidden',
          'messages': []
        },
        'inputTextSystem': {
          'value': 'inputTextSystem',
          'messages': []
        }
      }),
      'viewModel':new DefinitionModel({
        'fields': [
          {
            'identifier': 'inputTextFields',
            'label': 'Input text fields',
            'displayType': 'EDITABLE',
            'fields': [
              {
                'previewEmpty': true,
                'identifier': 'inputTextEdit',
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
                'dataType': 'text',
                'label': 'Editable input text',
                'maxLength': 20,
                'isMandatory': true
              },
              {
                'previewEmpty': true,
                'identifier': 'inputTextPreview',
                'disabled': false,
                'displayType': 'READ_ONLY',
                'tooltip': 'Test tooltip',
                'validators': [
                  {
                    id: 'regex',
                    context: {
                      pattern: '[\\s\\S]{1,20}'
                    },
                    message: 'This field should be max 20 characters length',
                    level: 'error'
                  }
                ],
                'dataType': 'text',
                'label': 'Preview input text',
                'maxLength': 20,
                'isMandatory': false
              },
              {
                'previewEmpty': true,
                'identifier': 'inputTextDisabled',
                'disabled': true,
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
                  }
                ],
                'dataType': 'text',
                'label': 'Disabled input text',
                'maxLength': 20,
                'isMandatory': false
              },
              {
                'previewEmpty': true,
                'identifier': 'inputTextHidden',
                'disabled': false,
                'displayType': 'HIDDEN',
                'validators': [
                  {
                    id: 'regex',
                    context: {
                      pattern: '[\\s\\S]{1,20}'
                    },
                    message: 'This field should be max 20 characters length',
                    level: 'error'
                  }
                ],
                'dataType': 'text',
                'label': 'Hidden input text',
                'maxLength': 20,
                'isMandatory': false
              },
              {
                'previewEmpty': true,
                'identifier': 'inputTextSystem',
                'disabled': false,
                'displayType': 'SYSTEM',
                'validators': [
                  {
                    id: 'regex',
                    context: {
                      pattern: '[\\s\\S]{1,20}'
                    },
                    message: 'This field should be max 20 characters length',
                    level: 'error'
                  }
                ],
                'dataType': 'text',
                'label': 'System input text',
                'maxLength': 20,
                'isMandatory': false
              }
            ]
          }
        ]
      })
    };
  }
}
