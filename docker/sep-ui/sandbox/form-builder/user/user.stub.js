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
        'userPreview': {
          'messages': [],
          'value': {
            'total': 1,
            'offset': 0,
            'limit': 5,
            'results': ['1']
          }
        },
        'userHidden': {
          'messages': [],
          'value': {
            'total': 1,
            'offset': 0,
            'limit': 5,
            'results': ['1']
          }
        },
        'userPreviewEmpty': {
          'messages': [],
          'value': {
            'total': 0,
            'offset': 0,
            'limit': 5,
            'results': []
          }
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
              'identifier': 'userPreview',
              'disabled': false,
              'displayType': 'READ_ONLY',
              'dataType': 'text',
              'label': 'Preview user',
              'isMandatory': false,
              'tooltip': 'Test tooltip',
              'controlId': 'USER',
              'control': [{
                'identifier': 'USER'
              }]
            },
            {
              'previewEmpty': false,
              'identifier': 'userPreviewEmpty',
              'disabled': false,
              'displayType': 'READ_ONLY',
              'dataType': 'text',
              'label': 'previewEmpty=false',
              'isMandatory': false,
              'controlId': 'USER',
              'control': [{
                'identifier': 'USER'
              }]
            }, {
              'previewEmpty': true,
              'identifier': 'userHidden',
              'disabled': false,
              'displayType': 'HIDDEN',
              'dataType': 'text',
              'label': 'Hidden User',
              'isMandatory': false,
              'controlId': 'USER',
              'control': [{
                'identifier': 'USER'
              }]
            },
          ]
        }]
      })
    }
  }
}