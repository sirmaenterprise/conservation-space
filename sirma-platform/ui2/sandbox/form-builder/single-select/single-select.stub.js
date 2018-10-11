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
        'singleSelectEdit': {
          'dataType': 'text',
          'value': 'CH210001',
          'valueLabel': 'Препоръки за внедряване',
          'defaultValue': 'CH210001',
          'messages': []
        },
        'singleSelectPreview': {
          'dataType': 'text',
          'value': 'CH210001',
          'valueLabel': 'Препоръки за внедряване',
          'defaultValue': 'CH210001',
          'messages': []
        },
        'singleSelectDisabled': {
          'dataType': 'text',
          'value': 'CH210001',
          'valueLabel': 'Препоръки за внедряване',
          'defaultValue': 'CH210001',
          'messages': []
        },
        'singleSelectHidden': {
          'dataType': 'text',
          'value': 'CH210001',
          'valueLabel': 'Препоръки за внедряване',
          'defaultValue': 'CH210001',
          'messages': []
        },
        'singleSelectSystem': {
          'dataType': 'text',
          'value': 'CH210001',
          'valueLabel': 'Препоръки за внедряване',
          'defaultValue': 'CH210001',
          'messages': []
        },
        'editableField1': {
          'dataType': 'text',
          'value': 'CH210001',
          'valueLabel': 'Препоръки за внедряване',
          'defaultValue': 'CH210001',
          'messages': []
        }
      }),
      'viewModel': new DefinitionModel({
        'fields': [
          {
            'identifier': 'selectSingleFields',
            'label': 'Select single fields',
            'displayType': 'EDITABLE',
            'fields': [{
              'previewEmpty': true,
              'identifier': 'singleSelectEdit',
              'disabled': false,
              'displayType': 'EDITABLE',
              'codelist': 210,
              'tooltip': 'Test tooltip',
              'validators': [
                {
                  id: 'regex',
                  context: {
                    pattern: '[\\s\\S]{1,50}'
                  },
                  message: 'Invalid value',
                  level: 'error'
                },
                {
                  id: 'mandatory',
                  message: 'The field is mandatory',
                  level: 'error'
                }
              ],
              'dataType': 'text',
              'label': 'Editable single select',
              'isMandatory': true,
              'maxLength': 50
            },
              {
                'previewEmpty': true,
                'identifier': 'singleSelectPreview',
                'disabled': false,
                'displayType': 'READ_ONLY',
                'codelist': 210,
                'tooltip': 'Test tooltip',
                'validators': [
                  {
                    id: 'regex',
                    context: {
                      pattern: '[\\s\\S]{1,50}'
                    },
                    message: 'Invalid value',
                    level: 'error'
                  }
                ],
                'dataType': 'text',
                'label': 'Preview single select',
                'isMandatory': false,
                'maxLength': 50
              },
              {
                'previewEmpty': true,
                'identifier': 'singleSelectDisabled',
                'disabled': true,
                'displayType': 'EDITABLE',
                'codelist': 210,
                'tooltip': 'Test tooltip',
                'validators': [
                  {
                    id: 'regex',
                    context: {
                      pattern: '[\\s\\S]{1,50}'
                    },
                    message: 'Invalid value',
                    level: 'error'
                  }
                ],
                'dataType': 'text',
                'label': 'Disabled single select',
                'isMandatory': false,
                'maxLength': 50
              },
              {
                'previewEmpty': true,
                'identifier': 'singleSelectHidden',
                'disabled': false,
                'displayType': 'HIDDEN',
                'codelist': 210,
                'validators': [
                  {
                    id: 'regex',
                    context: {
                      pattern: '[\\s\\S]{1,50}'
                    },
                    message: 'Invalid value',
                    level: 'error'
                  }
                ],
                'dataType': 'text',
                'label': 'Hidden single select',
                'isMandatory': false,
                'maxLength': 50
              },
              {
                'previewEmpty': true,
                'identifier': 'singleSelectSystem',
                'disabled': false,
                'displayType': 'SYSTEM',
                'codelist': 210,
                'validators': [
                  {
                    id: 'regex',
                    context: {
                      pattern: '[\\s\\S]{1,50}'
                    },
                    message: 'Invalid value',
                    level: 'error'
                  }
                ],
                'dataType': 'text',
                'label': 'System single select',
                'isMandatory': false,
                'maxLength': 50
              }
            ]
          },
          {
            'identifier': 'linkedFields1',
            'label': 'Linked fields 1',
            'displayType': 'EDITABLE',
            'fields': [
              {
                'previewEmpty': true,
                'identifier': 'editableField1',
                'disabled': false,
                'displayType': 'EDITABLE',
                'codelist': 210,
                'validators': [
                  {
                    id: 'regex',
                    context: {
                      pattern: '[\\s\\S]{1,50}'
                    },
                    message: 'Invalid value',
                    level: 'error'
                  },
                  {
                    id: 'mandatory',
                    message: 'The field is mandatory',
                    level: 'error'
                  }
                ],
                'dataType': 'text',
                'label': 'Editable field 1',
                'isMandatory': true,
                'maxLength': 50
              }
            ]
          },
          {
            'identifier': 'linkedFields2',
            'label': 'Linked fields 2',
            'displayType': 'EDITABLE',
            'fields': [
              {
                'previewEmpty': true,
                'identifier': 'editableField1',
                'disabled': false,
                'displayType': 'EDITABLE',
                'codelist': 210,
                'validators': [
                  {
                    id: 'regex',
                    context: {
                      pattern: '[\\s\\S]{1,50}'
                    },
                    message: 'Invalid value',
                    level: 'error'
                  },
                  {
                    id: 'mandatory',
                    message: 'The field is mandatory',
                    level: 'error'
                  }
                ],
                'dataType': 'text',
                'label': 'Editable field 1',
                'isMandatory': true,
                'maxLength': 50
              }
            ]
          }
        ]
      })
    };
  }
}
