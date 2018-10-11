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
        'emailControlEdit': {
          'defaultValue': 'project8-tenant-id@sirma.bg',
          'value': 'project8-tenant-id@sirma.bg',
          'valid': true,
          'messages': []
        },
        'emailControlPreview': {
          'defaultValue': 'project8-tenant-id@sirma.bg',
          'value': 'project8-tenant-id@sirma.bg',
          'valid': true,
          'messages': []
        }
      }),
      'viewModel': new DefinitionModel({
        'fields': [
          {
            'identifier': 'emailAddress',
            'label': 'E-mail address',
            'displayType': 'EDITABLE',
            'fields': [
              {
                'previewEmpty': true,
                'identifier': 'emailControlEdit',
                'disabled': false,
                'displayType': 'EDITABLE',
                'control': [{
                  'identifier': 'EMAIL',
                  'controlParams': {
                    'staticPart': '-tenant-id@sirma.bg'
                  }
                }],
                'tooltip': 'Address of the object`s email box',
                'validators': [
                  {
                    "id": "regex",
                    "level": "error",
                    "message": "Invalid format. Use letters, digits, underline or dash up to 40 signs.",
                    "context": {
                      "pattern": "^[A-Za-z0-9._-]{1,64}@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
                    }
                  },
                  {
                    id: 'mandatory',
                    message: 'The field is mandatory',
                    level: 'error'
                  }
                ],
                'dataType': 'text',
                'label': 'Editable email control',
                'maxLength': 40,
                'isMandatory': true
              },
              {
                'previewEmpty': true,
                'identifier': 'emailControlPreview',
                'disabled': false,
                'displayType': 'READ_ONLY',
                'control': [{
                  'identifier': 'EMAIL',
                  'controlParams': {
                    'staticPart': '-tenant-id@sirma.bg'
                  }
                }],
                'tooltip': 'Address of the object`s email box.',
                'validators': [
                  {
                    "id": "regex",
                    "level": "error",
                    "message": "Invalid format. Use letters, digits, underline or dash up to 40 signs.",
                    "context": {
                      "pattern": "^[A-Za-z0-9._-]{1,64}@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
                    }
                  }
                ],
                'dataType': 'text',
                'label': 'Preview email control',
                'maxLength': 40,
                'isMandatory': false
              }
            ]
          }
        ]
      })
    };
  }
}
