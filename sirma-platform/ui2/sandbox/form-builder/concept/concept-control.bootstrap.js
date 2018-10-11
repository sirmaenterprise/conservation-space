import {Component, View} from 'app/app';
import {FormWidgetStub} from 'sandbox/form-builder/form-widget.stub';
import {InstanceModel} from 'models/instance-model';
import {DefinitionModel} from  'models/definition-model';
import template from 'sandbox/form-builder/form-widget.stub.html!text';

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
      'definitionId': 'ET123121',
      'validationModel': new InstanceModel({
        'concept': {
          'messages': []
        },
        'conceptMultiple': {
          'value': {
            'results': ['metal'],
            'total': 1
          },
          'defaultValue': {
            'results': ['metal'],
            'total': 1
          },
          'messages': []
        },
        'conceptMultiple2': {
          'value': {
            'results': ['metal', 'reinforced', 'aluminium', 'cold_formed', 'hot_rolled', 'concrete', 'fiber', 'rebar'],
            'total': 8
          },
          'defaultValue': {
            'results': ['metal', 'reinforced', 'aluminium', 'cold_formed', 'hot_rolled', 'concrete', 'fiber', 'rebar'],
            'total': 8
          },
          'messages': []
        }
      }),
      'viewModel': new DefinitionModel({
        'fields': [
          {
            'previewEmpty': true,
            'identifier': 'concept',
            'disabled': false,
            'displayType': 'EDITABLE',
            'validators': [],
            'dataType': 'uri',
            'label': 'Concept (single value)',
            'isMandatory': true,
            'tooltip': 'Test tooltip',
            "controlId": "CONCEPT_PICKER",
            'control': [
              {
                'identifier': 'CONCEPT_PICKER',
                'controlParams': {
                  'scheme': 'materials',
                  'broader': 'metal'
                }
              }
            ]
          },
          {
            'previewEmpty': true,
            'identifier': 'conceptMultiple',
            'disabled': false,
            'displayType': 'EDITABLE',
            'validators': [],
            'dataType': 'uri',
            'label': 'Concept (multi value)',
            'isMandatory': false,
            'multivalue': true,
            'tooltip': 'Test tooltip',
            "controlId": "CONCEPT_PICKER",
            'control': [
              {
                'identifier': 'CONCEPT_PICKER',
                'controlParams': {
                  'scheme': 'materials',
                  'broader': 'metal'
                }
              }
            ]
          },
          {
            'previewEmpty': true,
            'identifier': 'conceptMultiple2',
            'disabled': false,
            'displayType': 'EDITABLE',
            'validators': [],
            'dataType': 'uri',
            'label': 'Concept (multi value)',
            'isMandatory': false,
            'multivalue': true,
            'tooltip': 'Test tooltip',
            "controlId": "CONCEPT_PICKER",
            'control': [
              {
                'identifier': 'CONCEPT_PICKER',
                'controlParams': {
                  'scheme': 'materials',
                  'broader': 'metal'
                }
              }
            ]
          }
        ]
      })
    };
  }
}
