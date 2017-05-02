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
    var date = new Date('2015/12/22').toISOString();
    this.config.enableHint = true;
    this.formConfig.models = {
      definitionId: 'ET123121',
      'validationModel': new InstanceModel({
        'datefieldEditable': {
          'dataType': 'date',
          'value': date,
          'messages': []
        },
        'datefieldPreview': {
          'dataType': 'date',
          'value': date,
          'messages': []
        },
        'datefieldDisabled': {
          'dataType': 'date',
          'value': date,
          'messages': []
        },
        'datefieldHidden': {
          'dataType': 'date',
          'value': date,
          'messages': []
        },
        'datefieldSystem': {
          'dataType': 'date',
          'value': date,
          'messages': []
        },
        'datetimefieldEditable': {
          'dataType': 'datetime',
          'value': date,
          'messages': []
        },
        'datetimefieldPreview': {
          'dataType': 'datetime',
          'value': date,
          'messages': []
        },
        'datetimefieldDisabled': {
          'dataType': 'datetime',
          'value': date,
          'messages': []
        },
        'datetimefieldHidden': {
          'dataType': 'datetime',
          'value': date,
          'messages': []
        },
        'datetimefieldSystem': {
          'dataType': 'datetime',
          'value': date,
          'messages': []
        }
      }),
      'viewModel': new DefinitionModel({
        'fields': [
          {
            'identifier': 'datetimeFields',
            'label': 'Datetime fields',
            'displayType': 'EDITABLE',
            'fields': [
              {
                'previewEmpty': true,
                'identifier': 'datefieldEditable',
                'disabled': false,
                'displayType': 'EDITABLE',
                'tooltip':'Test tooltip',
                'validators': [
                  {
                    id: 'mandatory',
                    message: 'The field is mandatory',
                    level: 'error'
                  }
                ],
                'dataType': 'date',
                'label': 'Editable date',
                'isMandatory': true
              },
              {
                'previewEmpty': true,
                'identifier': 'datefieldPreview',
                'disabled': false,
                'displayType': 'READ_ONLY',
                'validators': [],
                'dataType': 'date',
                'label': 'Preview date',
                'isMandatory': false,
                'tooltip':'Test tooltip',
              },
              {
                'previewEmpty': true,
                'identifier': 'datefieldDisabled',
                'disabled': true,
                'displayType': 'EDITABLE',
                'validators': [],
                'dataType': 'date',
                'label': 'Disabled date',
                'isMandatory': false,
                'tooltip':'Test tooltip',
              },
              {
                'previewEmpty': true,
                'identifier': 'datefieldHidden',
                'disabled': true,
                'displayType': 'HIDDEN',
                'validators': [],
                'dataType': 'date',
                'label': 'Hidden date',
                'isMandatory': false,
                'tooltip':'Test tooltip',
              },
              {
                'previewEmpty': true,
                'identifier': 'datefieldSystem',
                'disabled': false,
                'displayType': 'SYSTEM',
                'validators': [],
                'dataType': 'date',
                'label': 'System date',
                'isMandatory': false,
                'tooltip':'Test tooltip',
              },
              {
                'previewEmpty': true,
                'identifier': 'datetimefieldEditable',
                'disabled': false,
                'displayType': 'EDITABLE',
                'validators': [
                  {
                    id: 'mandatory',
                    message: 'The field is mandatory',
                    level: 'error'
                  }
                ],
                'dataType': 'datetime',
                'label': 'Editable datetime',
                'isMandatory': true
              },
              {
                'previewEmpty': true,
                'identifier': 'datetimefieldPreview',
                'disabled': false,
                'displayType': 'READ_ONLY',
                'validators': [],
                'dataType': 'datetime',
                'label': 'Preview datetime',
                'isMandatory': false,
                'tooltip':'Test tooltip',
              },
              {
                'previewEmpty': true,
                'identifier': 'datetimefieldDisabled',
                'disabled': true,
                'displayType': 'EDITABLE',
                'validators': [],
                'dataType': 'datetime',
                'label': 'Disabled datetime',
                'isMandatory': false,
                'tooltip':'Test tooltip',
              },
              {
                'previewEmpty': true,
                'identifier': 'datetimefieldHidden',
                'disabled': true,
                'displayType': 'HIDDEN',
                'validators': [],
                'dataType': 'datetime',
                'label': 'Hidden datetime',
                'isMandatory': false,
                'tooltip':'Test tooltip',
              },
              {
                'previewEmpty': true,
                'identifier': 'datetimefieldSystem',
                'disabled': false,
                'displayType': 'SYSTEM',
                'validators': [],
                'dataType': 'datetime',
                'label': 'System datetime',
                'isMandatory': false,
                'tooltip':'',
              }
            ]
          }
        ]
      })
    };
  }
}
