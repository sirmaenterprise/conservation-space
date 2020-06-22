import {Component, View} from 'app/app';
import {FormWidgetStub} from 'form-builder/form-widget-stub';
import {InstanceModel} from 'models/instance-model';
import {EventEmitter} from 'common/event-emitter';
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
    this.formConfig.eventEmitter = new EventEmitter();
    this.formConfig.models = {
      definitionId: 'ET123121',
      'validationModel': new InstanceModel({
        'radioButtonGroupEditable2': {
          'dataType': 'text',
          'value': 'COL3',
          'defaultValue': 'COL1',
          'valueLabel': 'option 1',
          'messages': []
        },
        'radioButtonGroupPreview': {
          'dataType': 'text',
          'value': 'COL1',
          'defaultValue': 'COL1',
          'valueLabel': 'option 1',
          'messages': []
        },
        'radioButtonGroupDisabled': {
          'dataType': 'text',
          'value': 'COL1',
          'defaultValue': 'COL1',
          'valueLabel': 'option 1',
          'messages': []
        },
        'radioButtonGroupHidden': {
          'dataType': 'text',
          'value': 'COL1',
          'defaultValue': 'COL1',
          'valueLabel': 'option 1',
          'messages': []
        },
        'radioButtonGroupSystem': {
          'dataType': 'text',
          'value': 'COL1',
          'defaultValue': 'COL1',
          'valueLabel': 'option 1',
          'messages': []
        }
      }),
      'viewModel': new DefinitionModel({
        'fields': [
          {
            'identifier': 'radioButtonsRegion',
            'label': 'Radio button groups',
            'displayType': 'EDITABLE',
            'fields': [
              {
                'previewEmpty': true,
                'identifier': 'radioButtonGroupEditable2',
                'displayType': 'EDITABLE',
                'defaultValue': 'COL1',
                'dataType': 'text',
                'label': 'Editable radio button group',
                'maxLength': 4,
                'isMandatory': true,
                'tooltip': 'Test tooltip',
                "controlId": "RADIO_BUTTON_GROUP",
                'control': [{
                  'identifier': 'RADIO_BUTTON_GROUP',
                  // FIXME: rest service to be fixed to build control fields correctly
                  'controlFields': [
                    {
                      'identifier': 'COL1',
                      'type': 'an4',
                      'label': 'option 1'
                    },
                    {
                      'identifier': 'COL2',
                      'type': 'an4',
                      'label': 'option 2'
                    },
                    {
                      'identifier': 'COL3',
                      'type': 'an4',
                      'label': 'option 3'
                    }
                  ],
                  'controlParams': {
                    'layout': 'pageDirection'
                  }
                }],
                'validators': [{
                  'level': 'error',
                  'context': {
                    'pattern': '.{4}'
                  },
                  'id': 'regex',
                  'message': 'Invalid format. Use letters and digists only up to 4 signs.'
                }]
              },
              {
                'previewEmpty': true,
                'identifier': 'radioButtonGroupPreview',
                'displayType': 'READ_ONLY',
                'defaultValue': 'COL1',
                'dataType': 'text',
                'label': 'Preview radio button group',
                'maxLength': 4,
                'isMandatory': false,
                'tooltip': 'Test tooltip',
                "controlId": "RADIO_BUTTON_GROUP",
                'control': [{
                  'identifier': 'RADIO_BUTTON_GROUP',
                  // FIXME: rest service to be fixed to build control fields correctly
                  'controlFields': [
                    {
                      'identifier': 'COL1',
                      'type': 'an4',
                      'label': 'option 1'
                    },
                    {
                      'identifier': 'COL2',
                      'type': 'an4',
                      'label': 'option 2'
                    },
                    {
                      'identifier': 'COL3',
                      'type': 'an4',
                      'label': 'option 3'
                    }
                  ],
                  'controlParams': {
                    'layout': 'pageDirection'
                  }
                }],
                'validators': [{
                  'level': 'error',
                  'context': {
                    'pattern': '.{4}'
                  },
                  'id': 'regex',
                  'message': 'Invalid format. Use letters and digists only up to 4 signs.'
                }]
              },
              {
                'previewEmpty': true,
                'identifier': 'radioButtonGroupDisabled',
                'displayType': 'EDITABLE',
                'disabled': true,
                'defaultValue': 'COL1',
                'dataType': 'text',
                'label': 'Disabled radio button group',
                'maxLength': 4,
                'isMandatory': false,
                'tooltip': 'Test tooltip',
                "controlId": "RADIO_BUTTON_GROUP",
                'control': [{
                  'identifier': 'RADIO_BUTTON_GROUP',
                  // FIXME: rest service to be fixed to build control fields correctly
                  'controlFields': [
                    {
                      'identifier': 'COL1',
                      'type': 'an4',
                      'label': 'option 1'
                    },
                    {
                      'identifier': 'COL2',
                      'type': 'an4',
                      'label': 'option 2'
                    },
                    {
                      'identifier': 'COL3',
                      'type': 'an4',
                      'label': 'option 3'
                    }
                  ],
                  'controlParams': {
                    'layout': 'pageDirection'
                  }
                }],
                'validators': [{
                  'level': 'error',
                  'context': {
                    'pattern': '.{4}'
                  },
                  'id': 'regex',
                  'message': 'Invalid format. Use letters and digists only up to 4 signs.'
                }]
              },
              {
                'previewEmpty': true,
                'identifier': 'radioButtonGroupHidden',
                'displayType': 'HIDDEN',
                'defaultValue': 'COL1',
                'dataType': 'text',
                'label': 'Hidden radio button group',
                'maxLength': 4,
                'isMandatory': false,
                "controlId": "RADIO_BUTTON_GROUP",
                'control': [{
                  'identifier': 'RADIO_BUTTON_GROUP',
                  // FIXME: rest service to be fixed to build control fields correctly
                  'controlFields': [
                    {
                      'identifier': 'COL1',
                      'type': 'an4',
                      'label': 'option 1'
                    },
                    {
                      'identifier': 'COL2',
                      'type': 'an4',
                      'label': 'option 2'
                    },
                    {
                      'identifier': 'COL3',
                      'type': 'an4',
                      'label': 'option 3'
                    }
                  ],
                  'controlParams': {
                    'layout': 'pageDirection'
                  }
                }],
                'validators': [{
                  'level': 'error',
                  'context': {
                    'pattern': '.{4}'
                  },
                  'id': 'regex',
                  'message': 'Invalid format. Use letters and digists only up to 4 signs.'
                }]
              },
              {
                'previewEmpty': true,
                'identifier': 'radioButtonGroupSystem',
                'displayType': 'SYSTEM',
                'defaultValue': 'COL1',
                'dataType': 'text',
                'label': 'System radio button group',
                'maxLength': 4,
                'isMandatory': false,
                "controlId": "RADIO_BUTTON_GROUP",
                'control': [{
                  'identifier': 'RADIO_BUTTON_GROUP',
                  // FIXME: rest service to be fixed to build control fields correctly
                  'controlFields': [
                    {
                      'identifier': 'COL1',
                      'type': 'an4',
                      'label': 'option 1'
                    },
                    {
                      'identifier': 'COL2',
                      'type': 'an4',
                      'label': 'option 2'
                    },
                    {
                      'identifier': 'COL3',
                      'type': 'an4',
                      'label': 'option 3'
                    }
                  ],
                  'controlParams': {
                    'layout': 'pageDirection'
                  }
                }],
                'validators': [{
                  'level': 'error',
                  'context': {
                    'pattern': '.{4}'
                  },
                  'id': 'regex',
                  'message': 'Invalid format. Use letters and digists only up to 4 signs.'
                }]
              }
            ]
          }
        ]
      })
    };
  }

  isRadionButtonGroupSandbox() {
    return true;
  }

  setPageDirection() {
    this.setLayout('pageDirection');
    this.config.models.definitionId = '123';
  }

  setLineDirection() {
    this.setLayout('lineDirection');
    this.config.models.definitionId = '321';
  }

  setLayout(layout) {
    this.config.models.viewModel.fields[0].fields.forEach((field) => {
      field.control.controlParams.layout = layout;
    });
  }
}
