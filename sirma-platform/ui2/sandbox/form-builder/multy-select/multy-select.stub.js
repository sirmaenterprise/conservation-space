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
  template
})
export class FormWidget extends FormWidgetStub {

  constructor() {
    super();
    this.config.enableHint = true;
    this.formConfig.models = {
      definitionId: 'ET123121',
      'validationModel': new InstanceModel({
        'multiSelectEdit': {
          'value': ['CH210001'],
          'valueLabel': 'Препоръки за внедряване',
          'messages': []
        },
        'multiSelectPreview': {
          'value': ['CH210001', 'DT210099'],
          'valueLabel': 'Препоръки за внедряване, Other',
          'messages': []
        },
        'multiSelectDisabled': {
          'value': ['CH210001'],
          'valueLabel': 'Препоръки за внедряване',
          'messages': []
        },
        'multiSelectHidden': {
          'value': ['CH210001', 'DT210099'],
          'valueLabel': 'Препоръки за внедряване, Other',
          'messages': []
        },
        'multiSelectSystem': {
          'value': ['CH210001', 'DT210099'],
          'valueLabel': 'Препоръки за внедряване, Other',
          'messages': []
        },
        'editableMultiSelect1': {
          'value': ['CH210001'],
          'valueLabel': 'Препоръки за внедряване',
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
                'dataType': 'ANY',
                'label': 'Editable multy select',
                'isMandatory': true,
                'multivalue': true,
                'validators': [
                  {
                    id: 'mandatory',
                    message: 'The field is mandatory',
                    level: 'error'
                  }
                ]
              },
              {
                'previewEmpty': true,
                'identifier': 'multiSelectPreview',
                'disabled': false,
                'displayType': 'READ_ONLY',
                'codelist': 210,
                'tooltip': 'Test tooltip',
                'validators': [],
                'dataType': 'ANY',
                'label': 'Preview multy select',
                'isMandatory': false,
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
                'dataType': 'ANY',
                'label': 'Disabled multy select',
                'isMandatory': false,
                'multivalue': true
              },
              {
                'previewEmpty': true,
                'identifier': 'multiSelectHidden',
                'disabled': false,
                'displayType': 'HIDDEN',
                'codelist': 210,
                'validators': [],
                'dataType': 'ANY',
                'label': 'Hidden multy select',
                'isMandatory': false,
                'multivalue': true
              },
              {
                'previewEmpty': true,
                'identifier': 'multiSelectSystem',
                'disabled': false,
                'displayType': 'SYSTEM',
                'codelist': 210,
                'validators': [],
                'dataType': 'ANY',
                'label': 'System multy select',
                'isMandatory': false,
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
                'dataType': 'ANY',
                'label': 'Editable multy select',
                'isMandatory': true,
                'multivalue': true,
                'validators': [
                  {
                    id: 'mandatory',
                    message: 'The field is mandatory',
                    level: 'error'
                  }
                ]
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
                'dataType': 'ANY',
                'label': 'Editable multy select',
                'isMandatory': true,
                'multivalue': true,
                'validators': [
                  {
                    id: 'mandatory',
                    message: 'The field is mandatory',
                    level: 'error'
                  }
                ]
              }
            ]
          }
        ]
      })
    };
  }
}
