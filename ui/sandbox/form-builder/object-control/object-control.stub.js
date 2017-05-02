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
      'validationModel':new InstanceModel({
        'objectProperty': {
          'dataType': 'uri',
          'value':[{
            'id': 'emf:123456',
            properties: {
              compact_header: 'Compact header 123456'
            },
            instanceType: 'documentinstance'
          }],
          'messages': []
        },
        'objectPropertyMultiple': {
          'dataType': 'uri',
          'value':[{
            'id': 'emf:123456',
            properties: {
              compact_header: 'Compact header 123456'
            },
            instanceType: 'documentinstance'
          }, {
            'id': 'emf:999888',
            properties: {
              compact_header: 'Compact header 999888'
            },
            instanceType: 'documentinstance'
          }],
          'messages': []
        }
      }),
      'viewModel': new DefinitionModel({
        'fields': [
          {
            'previewEmpty': true,
            'identifier': 'objectProperty',
            'disabled': false,
            'displayType': 'EDITABLE',
            'validators': [],
            'dataType': 'text',
            'label': 'Object property (single value)',
            'isMandatory': true,
            'tooltip': 'Test tooltip',
            'control': {
              'identifier': 'PICKER'
            }
          },
          {
            'previewEmpty': true,
            'identifier': 'objectPropertyMultiple',
            'disabled': false,
            'displayType': 'EDITABLE',
            'validators': [],
            'dataType': 'text',
            'label': 'Object property (multi value)',
            'isMandatory': false,
            'multivalue': true,
            'tooltip': 'Test tooltip',
            'control': {
              'identifier': 'PICKER'
            }
          }
        ]
      })
    };
  }
}
