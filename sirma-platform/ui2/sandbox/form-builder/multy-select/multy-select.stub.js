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
        'multiSelectEdit': {
          'dataType': 'text',
          'value': ['CH210001'],
          'valueLabel': 'Препоръки за внедряване',
          'defaultValue': ['CH210001'],
          'messages': []
        },
        'multiSelectPreview': {
          'dataType': 'text',
          'value': ['CH210001', 'DT210099'],
          'valueLabel': 'Препоръки за внедряване, Other',
          'defaultValue': ['CH210001', 'DT210099'],
          'messages': []
        },
        'multiSelectDisabled': {
          'dataType': 'text',
          'value': ['CH210001'],
          'valueLabel': 'Препоръки за внедряване',
          'defaultValue': ['CH210001'],
          'messages': []
        },
        'multiSelectHidden': {
          'dataType': 'text',
          'value': ['CH210001', 'DT210099'],
          'valueLabel': 'Препоръки за внедряване, Other',
          'defaultValue': ['CH210001', 'DT210099'],
          'messages': []
        },
        'multiSelectSystem': {
          'dataType': 'text',
          'value': ['CH210001', 'DT210099'],
          'valueLabel': 'Препоръки за внедряване, Other',
          'defaultValue': ['CH210001', 'DT210099'],
          'messages': []
        },
        'editableMultiSelect1': {
          'dataType': 'text',
          'value': ['CH210001'],
          'valueLabel': 'Препоръки за внедряване',
          'defaultValue': ['CH210001'],
          'messages': []
        }
      }),
      'viewModel': new DefinitionModel({
        'fields': [
          {
            'identifier': 'selectMultipleFields',
            'label': 'Select multiple fields',
            'displayType': 'EDITABLE',
            'fields': [
              {
                'previewEmpty': true,
                'identifier': 'multiSelectEdit',
                'disabled': false,
                'displayType': 'EDITABLE',
                'codelist': 210,
                'tooltip': 'Test tooltip',
                'validators': [
                  {
                    id: 'mandatory',
                    message: 'The field is mandatory',
                    level: 'error'
                  }
                ],
                'dataType': 'text',
                'label': 'Editable multy select',
                'isMandatory': true,
                'maxLength': 50,
                'multivalue': true
              },
              {
                'previewEmpty': true,
                'identifier': 'multiSelectPreview',
                'disabled': false,
                'displayType': 'READ_ONLY',
                'codelist': 210,
                'tooltip': 'Test tooltip',
                'validators': [],
                'dataType': 'text',
                'label': 'Preview multy select',
                'isMandatory': false,
                'maxLength': 50,
                'multivalue': true
              },
              {
                'previewEmpty': true,
                'identifier': 'multiSelectDisabled',
                'disabled': true,
                'displayType': 'EDITABLE',
                'codelist': 210,
                'tooltip': 'Test tooltip',
                'validators': [],
                'dataType': 'text',
                'label': 'Disabled multy select',
                'isMandatory': false,
                'maxLength': 50,
                'multivalue': true
              },
              {
                'previewEmpty': true,
                'identifier': 'multiSelectHidden',
                'disabled': false,
                'displayType': 'HIDDEN',
                'codelist': 210,
                'validators': [],
                'dataType': 'text',
                'label': 'Hidden multy select',
                'isMandatory': false,
                'maxLength': 50,
                'multivalue': true
              },
              {
                'previewEmpty': true,
                'identifier': 'multiSelectSystem',
                'disabled': false,
                'displayType': 'SYSTEM',
                'codelist': 210,
                'validators': [],
                'dataType': 'text',
                'label': 'System multy select',
                'isMandatory': false,
                'maxLength': 50,
                'multivalue': true
              }
            ]
          }, {
            'identifier': 'linkedSelectMultipleFields1',
            'label': 'Linked select multiple fields 1',
            'displayType': 'EDITABLE',
            'fields': [
              {
                'previewEmpty': true,
                'identifier': 'editableMultiSelect1',
                'disabled': false,
                'displayType': 'EDITABLE',
                'codelist': 210,
                'validators': [
                  {
                    id: 'mandatory',
                    message: 'The field is mandatory',
                    level: 'error'
                  }
                ],
                'dataType': 'text',
                'label': 'Editable multy select',
                'isMandatory': true,
                'maxLength': 50,
                'multivalue': true
              }
            ]
          },
          {
            'identifier': 'linkedSelectMultipleFields2',
            'label': 'Linked select multiple fields 2',
            'displayType': 'EDITABLE',
            'fields': [
              {
                'previewEmpty': true,
                'identifier': 'editableMultiSelect1',
                'disabled': false,
                'displayType': 'EDITABLE',
                'codelist': 210,
                'validators': [
                  {
                    id: 'mandatory',
                    message: 'The field is mandatory',
                    level: 'error'
                  }
                ],
                'dataType': 'text',
                'label': 'Editable multy select',
                'isMandatory': true,
                'maxLength': 50,
                'multivalue': true
              }
            ]
          }
        ]
      })
    };
  }
}
