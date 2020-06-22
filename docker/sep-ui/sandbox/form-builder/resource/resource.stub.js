import {Component, View} from 'app/app';
import {InstanceModel} from 'models/instance-model';
import {DefinitionModel} from 'models/definition-model';
import {FormWidgetStub} from 'form-builder/form-widget-stub';
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
        'editableResource': {
          'dataType': 'text',
          'value':{
            'lastName': 'Administrator',
            'displayName': 'Admin velikov1.bg',
            'label': 'velikov1.bg Administrator',
            'title': 'velikov1.bg Administrator',
            'userId': 'admin@velikov1.bg',
            'firstName': 'velikov1.bg'
          },
          'messages': []
        },
        'previewResource': {
          'dataType': 'text',
          'value':{
            'lastName': 'Administrator',
            'displayName': 'Admin velikov1.bg',
            'label': 'velikov1.bg Administrator',
            'title': 'velikov1.bg Administrator',
            'userId': 'admin@velikov1.bg',
            'firstName': 'velikov1.bg'
          },
          'messages': []
        }
      }),
      'viewModel': new DefinitionModel({
        'fields': [
          {
            'previewEmpty': true,
            'identifier': 'editableResource',
            'disabled': false,
            'displayType': 'EDITABLE',
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
            'label': 'Editable resource picker',
            'isMandatory': true,
            'tooltip':'Test tooltip',
            "controlId": "PICKLIST",
            'control': [{
              'identifier': 'PICKLIST'
            }]
          },
          {
            'previewEmpty': true,
            'identifier': 'previewResource',
            'disabled': false,
            'displayType': 'READ_ONLY',
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
            'label': 'Preview resource picker',
            'isMandatory': false,
            'tooltip':'Test tooltip',
            "controlId": "PICKLIST",
            'control': [{
              'identifier': 'PICKLIST'
            }]
          }
        ]
      })
    };
  }
}
