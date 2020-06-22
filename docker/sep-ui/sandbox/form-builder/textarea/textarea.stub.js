import {Component, View} from 'app/app';
import {FormWidgetStub} from 'form-builder/form-widget-stub';
import {InstanceModel} from 'models/instance-model';
import {DefinitionModel} from  'models/definition-model';
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
        'textareaEdit': {
          'dataType': 'text',
          'messages': [],
          'value': 'textareaEdit'
        },
        'textareaPreview': {
          'dataType': 'text',
          'messages': [],
          'value': 'textareaPreview'
        },
        'textareaDisabled': {
          'dataType': 'text',
          'messages': [],
          'value': 'textareaDisabled'
        },
        'textareaHidden': {
          'dataType': 'text',
          'messages': [],
          'value': 'textareaHidden'
        },
        'textareaSystem': {
          'dataType': 'text',
          'messages': [],
          'value': 'textareaSystem'
        }
      }),
      'viewModel': new DefinitionModel({
        'fields': [
          {
            'identifier': 'textareaFields',
            'label': 'Textarea fields',
            'displayType': 'EDITABLE',
            'fields': [{
              'previewEmpty': true,
              'identifier': 'textareaEdit',
              'disabled': false,
              'displayType': 'EDITABLE',
              'tooltip': 'Test tooltip',
              'validators': [
                {
                  id: 'regex',
                  context: {
                    pattern: '[\\s\\S]{1,60}'
                  },
                  message: 'This field should be max 60 characters length',
                  level: 'error'
                },
                {
                  id: 'mandatory',
                  message: 'The field is mandatory',
                  level: 'error'
                }
              ],
              'dataType': 'text',
              'label': 'Editable textarea',
              'isMandatory': true,
              'maxLength': 60
            },
              {
                'previewEmpty': true,
                'identifier': 'textareaPreview',
                'disabled': false,
                'displayType': 'READ_ONLY',
                'tooltip': 'Test tooltip',
                'validators': [
                  {
                    id: 'regex',
                    context: {
                      pattern: '[\\s\\S]{1,60}'
                    },
                    message: 'This field should be max 60 characters length',
                    level: 'error'
                  }
                ],
                'dataType': 'text',
                'label': 'Preview textarea',
                'isMandatory': false,
                'maxLength': 60
              },
              {
                'previewEmpty': true,
                'identifier': 'textareaDisabled',
                'disabled': true,
                'displayType': 'EDITABLE',
                'tooltip': 'Test tooltip',
                'validators': [
                  {
                    id: 'regex',
                    context: {
                      pattern: '[\\s\\S]{1,60}'
                    },
                    message: 'This field should be max 60 characters length',
                    level: 'error'
                  }
                ],
                'dataType': 'text',
                'label': 'Disabled textarea',
                'isMandatory': false,
                'maxLength': 60
              },
              {
                'previewEmpty': true,
                'identifier': 'textareaHidden',
                'disabled': true,
                'displayType': 'HIDDEN',
                'tooltip': 'Test tooltip',
                'validators': [
                  {
                    id: 'regex',
                    context: {
                      pattern: '[\\s\\S]{1,60}'
                    },
                    message: 'This field should be max 60 characters length',
                    level: 'error'
                  }
                ],
                'dataType': 'text',
                'label': 'Hidden textarea',
                'isMandatory': false,
                'maxLength': 60
              },
              {
                'previewEmpty': true,
                'identifier': 'textareaSystem',
                'disabled': false,
                'displayType': 'SYSTEM',
                'validators': [
                  {
                    id: 'regex',
                    context: {
                      pattern: '[\\s\\S]{1,60}'
                    },
                    message: 'This field should be max 60 characters length',
                    level: 'error'
                  }
                ],
                'dataType': 'text',
                'label': 'System textarea',
                'isMandatory': false,
                'maxLength': 60
              }
            ]
          }
        ]
      })
    };
  }
}
