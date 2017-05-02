import {Component, View} from 'app/app';
import template from 'form-widget-template!text';
import {FormWidgetStub} from 'form-builder/form-widget-stub';
import {InstanceModel} from 'models/instance-model';
import {DefinitionModel} from 'models/definition-model';

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
        'userPreview': {
          'dataType': 'text',
          'value': [{
            'headers': {
              'compact_header': '<span>Compact header</span>'
            }
          }],
          'messages': []
        },
        'userHidden': {
          'dataType': 'text',
          'value': [{
            'headers': {
              'compact_header': '<span>Compact header hidden</span>'
            }
          }]
        },
        'userPreviewEmpty': {
          'dataType': 'text',
          'messages': []
        }
      }),
      'viewModel': new DefinitionModel({
        'fields': [{
          'identifier': 'userTextFields',
          'label': 'User Information',
          'displayType': 'EDITABLE',
          'fields': [
            {
              'previewEmpty': true,
              'identifier': "userPreview",
              'disabled': false,
              'displayType': 'READ_ONLY',
              'dataType': 'text',
              'label': 'User Information',
              'isMandatory': 'false',
              'tooltip': 'Test tooltip',
              'control': {
                'identifier': 'USER'
              }
            },
            {
              'previewEmpty': false,
              'identifier': "userPreviewEmpty",
              'disabled': false,
              'displayType': "READ_ONLY",
              'dataType': "text",
              'label': "PreviewEmpty",
              'isMandatory': false,
              'control': {
                'identifier': 'USER'
              }
            }, {
              'previewEmpty': true,
              'identifier': "userHidden",
              'disabled': false,
              'displayType': "HIDDEN",
              'dataType': "text",
              'label': 'Hidden User',
              'isMandatory': false,
              'control': {
                'identifier': 'USER'
              }
            },
          ]
        }]
      })
    }
  }
}