import {Component, View} from 'app/app';
import {FormWidgetStub} from 'form-builder/form-widget-stub';
import {UrlUtils} from 'common/url-utils';
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
    var params = '?' + window.location.hash.substring(2);

    let collapsibleRegions = UrlUtils.getParameter(params, 'collapsibleRegions');
    this.config.collapsibleRegions = collapsibleRegions !== undefined ? collapsibleRegions : true;

    this.formConfig.models = {
      definitionId: 'ET123121',
      'validationModel': new InstanceModel({
        'singleCheckboxEdit1': {
          'dataType': 'boolean',
          'value': true,
          'defaultValue': true,
          'messages': []
        },
        'singleCheckboxEdit2': {
          'dataType': 'boolean',
          'value': false,
          'defaultValue': false,
          'messages': []
        },
        'singleCheckboxEdit3': {
          'dataType': 'boolean',
          'value': false,
          'defaultValue': false,
          'messages': []
        }
      }),
      'viewModel': new DefinitionModel({
        'fields': [
          {
            'identifier': 'region1field',
            'label': 'Region 1 field',
            'displayType': 'EDITABLE',
            'fields': [{
              'previewEmpty': true,
              'identifier': 'singleCheckboxEdit1',
              'disabled': false,
              'displayType': 'EDITABLE',
              'validators': [],
              'dataType': 'boolean',
              'label': 'Editable single checkbox',
              'regexValidator': '',
              'regexValidatorError': 'Invalid value',
              'isMandatory': true,
              'mandatoryValidatorError': 'The field is mandatory'
            }]
          },
          {
            'identifier': 'region2field',
            'label': 'Region 2 field',
            'displayType': 'EDITABLE',
            'fields': [{
              'previewEmpty': true,
              'identifier': 'singleCheckboxEdit2',
              'disabled': false,
              'displayType': 'EDITABLE',
              'validators': [],
              'dataType': 'boolean',
              'label': 'Editable single checkbox',
              'regexValidator': '',
              'regexValidatorError': 'Invalid value',
              'isMandatory': true,
              'mandatoryValidatorError': 'The field is mandatory'
            }]
          }, {
            'identifier': 'region3field',
            'label': 'Region 3 field',
            'displayType': 'EDITABLE',
            'collapsed': true,
            'fields': [{
              'previewEmpty': true,
              'identifier': 'singleCheckboxEdit3',
              'disabled': false,
              'displayType': 'EDITABLE',
              'validators': [],
              'dataType': 'boolean',
              'label': 'Editable single checkbox',
              'regexValidator': '',
              'regexValidatorError': 'Invalid value',
              'isMandatory': true,
              'mandatoryValidatorError': 'The field is mandatory'
            }]
          }
        ]
      })
    };
  }
}
