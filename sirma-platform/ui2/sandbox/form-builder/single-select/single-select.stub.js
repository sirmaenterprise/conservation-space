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
        'singleSelectEdit': {
          'value': 'CH210001',
          'valueLabel': 'Препоръки за внедряване',
          'messages': []
        },
        'singleSelectPreview': {
          'value': 'CH210001',
          'valueLabel': 'Препоръки за внедряване',
          'messages': []
        },
        'singleSelectDisabled': {
          'value': 'CH210001',
          'valueLabel': 'Препоръки за внедряване',
          'messages': []
        },
        'singleSelectHidden': {
          'value': 'CH210001',
          'valueLabel': 'Препоръки за внедряване',
          'messages': []
        },
        'singleSelectSystem': {
          'value': 'CH210001',
          'valueLabel': 'Препоръки за внедряване',
          'messages': []
        },
        'editableField1': {
          'value': 'CH210001',
          'valueLabel': 'Препоръки за внедряване',
          'messages': []
        }
      }),
      'viewModel': new DefinitionModel({
        'fields': [
          {
            'identifier': 'selectSingleFields',
            'label': 'Select single fields',
            'displayType': 'EDITABLE',
            'fields': [
              {
                'previewEmpty': true,
                'identifier': 'singleSelectEdit',
                'disabled': false,
                'displayType': 'EDITABLE',
                'codelist': 210,
                'tooltip': 'Test tooltip',
                'dataType': 'ANY',
                'label': 'Editable single select',
                'isMandatory': true,
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
                'identifier': 'singleSelectPreview',
                'disabled': false,
                'displayType': 'READ_ONLY',
                'codelist': 210,
                'tooltip': 'Test tooltip',
                'dataType': 'ANY',
                'label': 'Preview single select',
                'isMandatory': false,
                'validators': []
              },
              {
                'previewEmpty': true,
                'identifier': 'singleSelectDisabled',
                'disabled': true,
                'displayType': 'EDITABLE',
                'codelist': 210,
                'tooltip': 'Test tooltip',
                'dataType': 'ANY',
                'label': 'Disabled single select',
                'isMandatory': false,
                'validators': [],
              },
              {
                'previewEmpty': true,
                'identifier': 'singleSelectHidden',
                'disabled': false,
                'displayType': 'HIDDEN',
                'codelist': 210,
                'dataType': 'ANY',
                'label': 'Hidden single select',
                'isMandatory': false,
                'validators': []
              },
              {
                'previewEmpty': true,
                'identifier': 'singleSelectSystem',
                'disabled': false,
                'displayType': 'SYSTEM',
                'codelist': 210,
                'dataType': 'ANY',
                'label': 'System single select',
                'isMandatory': false,
                'validators': []
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
                'dataType': 'ANY',
                'label': 'Editable field 1',
                'isMandatory': true,
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
                'dataType': 'ANY',
                'label': 'Editable field 1',
                'isMandatory': true,
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
