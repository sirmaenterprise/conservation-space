import {Component, View} from 'app/app';
import {FormWidgetStub} from 'form-builder/form-widget-stub';
import {DefinitionModel} from 'models/definition-model';
import {InstanceModel} from 'models/instance-model';
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
    var date = new Date('2015/12/22').toISOString();
    this.config.enableHint = true;
    this.formConfig.models = {
      definitionId: 'ET123121',
      'validationModel': new InstanceModel({
        'textareaEdit': {
          'dataType': 'text',
          'messages': [],
          'value': 'textareaEdit'
        },
        'textareaPreview': {
          'dataType': 'text',
          'messages': [],
          'value': ''
        },
        'textareaDisabled': {
          'dataType': 'text',
          'messages': [],
          'value': 'textareaDisabled'
        }, 'inputTextEdit': {
          'value': 'inputTextEdit',
          'messages': []
        },
        'inputTextPreview': {
          'value': 'inputTextPreview',
          'messages': []
        },
        'inputTextDisabled': {
          'value': 'inputTextDisabled',
          'messages': []
        }, 'singleSelectEdit': {
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
        }, 'multiSelectEdit': {
          'dataType': 'text',
          'value': ['CH210001'],
          'valueLabel': ['Препоръки за внедряване'],
          'defaultValue': ['CH210001'],
          'messages': []
        },
        'multiSelectPreview': {
          'dataType': 'text',
          'value': ['CH210001', 'DT210099'],
          'valueLabel': ['Препоръки за внедряване', 'Other'],
          'defaultValue': ['CH210001', 'DT210099'],
          'messages': []
        },
        'multiSelectDisabled': {
          'dataType': 'text',
          'value': ['CH210001'],
          'valueLabel': ['Препоръки за внедряване'],
          'defaultValue': ['CH210001'],
          'messages': []
        },'datefieldEditable': {
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
        }, 'datetimefieldEditable': {
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
        },'objectProperty': {
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
        },'editableResource': {
          'dataType': 'text',
          'value':{
            'lastName': 'Administrator',
            'displayName': 'Admin velikov1.bg',
            'label': 'velikov1.bg Administrator',
            'title': 'velikov1.bg Administrator',
            'userId': 'admin@velikov1.bg',
            'firstName': 'velikov1.bg'
          },
          'messages': []
        },
        'previewResource': {
          'dataType': 'text',
          'value':{
            'lastName': 'Administrator',
            'displayName': 'Admin velikov1.bg',
            'label': 'velikov1.bg Administrator',
            'title': 'velikov1.bg Administrator',
            'userId': 'admin@velikov1.bg',
            'firstName': 'velikov1.bg'
          },
          'messages': []
        },'singleCheckboxEdit': {
          'dataType': 'boolean',
          'value': true,
          'defaultValue': true,
          'messages': []
        },
        'singleCheckboxPreview': {
          'dataType': 'boolean',
          'value': true,
          'defaultValue': true,
          'messages': []
        },
        'singleCheckboxDisabled': {
          'dataType': 'boolean',
          'value': true,
          'defaultValue': true,
          'messages': []
        },'radioButtonGroupEditable2': {
          'dataType': 'text',
          'value': 'COL1',
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
        },'userPreview': {
          'dataType': 'text',
          'value': [{
            'headers': {
              'compact_header': 'Compact header'
            }
          }],
          'messages': []
        },
        'innerRegion': {
          'valid':true
        },
        'emailControlEdit': {
          'defaultValue': 'project8',
          'value': 'project8',
          'valid': true,
          'messages': []
        },
        'emailControlPreview': {
          'defaultValue': 'project8',
          'value': 'project8',
          'valid': true,
          'messages': []
        }
      }),
      'viewModel': new DefinitionModel({
        'fields': [
          {
            'identifier': 'allControls',
            'label': 'All controls in a form',
            'displayType': 'EDITABLE',
            'fields': [
              {
                'previewEmpty': true,
                'identifier': 'textareaEdit',
                'disabled': false,
                'displayType': 'EDITABLE',
                'tooltip': 'Test tooltip',
                'validators': [
                  {
                    id: 'regex',
                    context: {
                      pattern: '[\\s\\S]{1,60}'
                    },
                    message: 'This field should be max 60 characters length',
                    level: 'error'
                  },
                  {
                    id: 'mandatory',
                    message: 'The field is mandatory',
                    level: 'error'
                  }
                ],
                'dataType': 'text',
                'label': 'Editable textarea',
                'isMandatory': true,
                'maxLength': 60
              },
              {
                'previewEmpty': true,
                'identifier': 'textareaPreview',
                'disabled': false,
                'displayType': 'READ_ONLY',
                'tooltip': 'Test tooltip',
                'validators': [
                  {
                    id: 'regex',
                    context: {
                      pattern: '[\\s\\S]{1,60}'
                    },
                    message: 'This field should be max 60 characters length',
                    level: 'error'
                  }
                ],
                'dataType': 'text',
                'label': 'Preview textarea',
                'isMandatory': false,
                'maxLength': 60
              },
              {
                'previewEmpty': true,
                'identifier': 'textareaDisabled',
                'disabled': true,
                'displayType': 'EDITABLE',
                'validators': [
                  {
                    id: 'regex',
                    context: {
                      pattern: '[\\s\\S]{1,60}'
                    },
                    message: 'This field should be max 60 characters length',
                    level: 'error'
                  }
                ],
                'dataType': 'text',
                'label': 'Disabled textarea',
                'isMandatory': false,
                'maxLength': 60
              }, {
                'previewEmpty': true,
                'identifier': 'inputTextEdit',
                'disabled': false,
                'displayType': 'EDITABLE',
                'tooltip': 'Test tooltip',
                'validators': [
                  {
                    id: 'regex',
                    context: {
                      pattern: '[\\s\\S]{1,20}'
                    },
                    message: 'This field should be max 20 characters length',
                    level: 'error'
                  },
                  {
                    id: 'mandatory',
                    message: 'The field is mandatory',
                    level: 'error'
                  }
                ],
                'dataType': 'text',
                'label': 'Editable input text',
                'maxLength': 20,
                'isMandatory': true
              },
              {
                'previewEmpty': true,
                'identifier': 'inputTextPreview',
                'disabled': false,
                'displayType': 'READ_ONLY',
                'tooltip': 'Test tooltip',
                'validators': [
                  {
                    id: 'regex',
                    context: {
                      pattern: '[\\s\\S]{1,20}'
                    },
                    message: 'This field should be max 20 characters length',
                    level: 'error'
                  }
                ],
                'dataType': 'text',
                'label': 'Preview input text',
                'maxLength': 20,
                'isMandatory': false
              },
              {
                'previewEmpty': true,
                'identifier': 'inputTextDisabled',
                'disabled': true,
                'displayType': 'EDITABLE',
                'tooltip': 'Test tooltip',
                'validators': [
                  {
                    id: 'regex',
                    context: {
                      pattern: '[\\s\\S]{1,20}'
                    },
                    message: 'This field should be max 20 characters length',
                    level: 'error'
                  }
                ],
                'dataType': 'text',
                'label': 'Disabled input text',
                'maxLength': 20,
                'isMandatory': false
              }, {
                'previewEmpty': true,
                'identifier': 'singleSelectEdit',
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
              }, {
                'previewEmpty': true,
                'identifier': 'multiSelectEdit',
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
                'validators': [],
                'dataType': 'text',
                'label': 'Disabled multy select',
                'isMandatory': false,
                'maxLength': 50,
                'multivalue': true
              },{
                'previewEmpty': true,
                'identifier': 'datefieldEditable',
                'disabled': false,
                'tooltip':'Test tooltip',
                'displayType': 'EDITABLE',
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
                'tooltip':'Test tooltip'
              },
              {
                'previewEmpty': true,
                'identifier': 'datefieldDisabled',
                'disabled': true,
                'displayType': 'EDITABLE',
                'validators': [],
                'dataType': 'date',
                'label': 'Disabled date',
                'isMandatory': false
              },{
                'previewEmpty': true,
                'identifier': 'objectProperty',
                'disabled': false,
                'displayType': 'EDITABLE',
                'validators': [],
                'dataType': 'text',
                'label': 'Object property (single value)',
                'isMandatory': true,
                'tooltip': 'Test tooltip',
                "controlId": "PICKER",
                'control': [{
                  'identifier': 'PICKER'
                }]
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
                "controlId": "PICKER",
                'control': [{
                  'identifier': 'PICKER'
                }]
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
                'tooltip':'Test tooltip'
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
                'tooltip':'Test tooltip'
              },
              {
                'previewEmpty': true,
                'identifier': 'editableResource',
                'disabled': false,
                'displayType': 'EDITABLE',
                'validators': [
                  {
                    id: 'regex',
                    context: {
                      pattern: '[\\s\\S]{1,20}'
                    },
                    message: 'This field should be max 20 characters length',
                    level: 'error'
                  },
                  {
                    id: 'mandatory',
                    message: 'The field is mandatory',
                    level: 'error'
                  }
                ],
                'dataType': 'text',
                'label': 'Editable resource picker',
                'isMandatory': true,
                'tooltip':'Test tooltip',
                "controlId": "PICKLIST",
                'control': [{
                  'identifier': 'PICKLIST'
                }]
              },
              {
                'previewEmpty': true,
                'identifier': 'previewResource',
                'disabled': false,
                'displayType': 'READ_ONLY',
                'validators': [
                  {
                    id: 'regex',
                    context: {
                      pattern: '[\\s\\S]{1,20}'
                    },
                    message: 'This field should be max 20 characters length',
                    level: 'error'
                  }
                ],
                'dataType': 'text',
                'label': 'Preview resource picker',
                'isMandatory': false,
                'tooltip':'Test tooltip',
                "controlId": "PICKLIST",
                'control': [{
                  'identifier': 'PICKLIST'
                }]
              },{
                'previewEmpty': true,
                'identifier': 'singleCheckboxEdit',
                'disabled': false,
                'displayType': 'EDITABLE',
                'validators': [],
                'dataType': 'boolean',
                'label': 'Editable single checkbox',
                'isMandatory': true,
                'tooltip': 'Test tooltip',
                'mandatoryValidatorError': 'The field is mandatory'
              },
              {
                'previewEmpty': true,
                'identifier': 'singleCheckboxPreview',
                'disabled': false,
                'displayType': 'READ_ONLY',
                'validators': [],
                'dataType': 'boolean',
                'label': 'Preview single checkbox',
                'tooltip': 'Test tooltip',
                'isMandatory': false
              },
              {
                'previewEmpty': true,
                'identifier': 'singleCheckboxDisabled',
                'disabled': true,
                'displayType': 'EDITABLE',
                'validators': [],
                'dataType': 'boolean',
                'label': 'Disabled single checkbox',
                'tooltip': 'Test tooltip',
                'isMandatory': false
              },{
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
                "controlId": "RADIO_BUTTON_GROUP",
                'control': [{
                  'identifier': 'RADIO_BUTTON_GROUP',
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
              },{
                'previewEmpty': true,
                'identifier': "userPreview",
                'disabled':false,
                'displayType':'READ_ONLY',
                'dataType':'text',
                'label':'User Information',
                'isMandatory': 'false',
                'tooltip': 'Test tooltip',
                "controlId": "USER",
                'control': [{
                  'identifier': 'USER'
                }]
              },
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
                    "message": "Invalid format. Use letters, digits, underline or dash up to 10 signs.",
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
                    "message": "Invalid format. Use letters, digits, underline or dash up to 10 signs.",
                    "context": {
                      "pattern": "^[A-Za-z0-9._-]{1,64}@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
                    }
                  }
                ],
                'dataType': 'text',
                'label': 'Preview email control',
                'maxLength': 40,
                'isMandatory': false
              },
              {
                'identifier' : 'innerRegion',
                'label': 'Inner Region Inside Form',
                'displayType':'EDITABLE',
                'fields':[
                  {
                    'previewEmpty': true,
                    'identifier': 'textareaEdit',
                    'disabled': false,
                    'displayType': 'EDITABLE',
                    'tooltip': 'Test tooltip',
                    'validators': [
                      {
                        id: 'regex',
                        context: {
                          pattern: '[\\s\\S]{1,60}'
                        },
                        message: 'This field should be max 60 characters length',
                        level: 'error'
                      },
                      {
                        id: 'mandatory',
                        message: 'The field is mandatory',
                        level: 'error'
                      }
                    ],
                    'dataType': 'text',
                    'label': 'Editable textarea',
                    'isMandatory': true,
                    'maxLength': 60
                  },{
                    'previewEmpty': true,
                    'identifier': 'inputTextPreview',
                    'disabled': false,
                    'displayType': 'READ_ONLY',
                    'tooltip': 'Test tooltip',
                    'validators': [
                      {
                        id: 'regex',
                        context: {
                          pattern: '[\\s\\S]{1,20}'
                        },
                        message: 'This field should be max 20 characters length',
                        level: 'error'
                      }
                    ],
                    'dataType': 'text',
                    'label': 'Preview input text',
                    'maxLength': 20,
                    'isMandatory': false
                  },{
                    'previewEmpty': true,
                    'identifier': 'singleSelectEdit',
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
                    'label': 'Editable single select',
                    'isMandatory': true,
                    'maxLength': 50
                  },{
                    'previewEmpty': true,
                    'identifier': 'multiSelectEdit',
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
                  },{
                    'previewEmpty': true,
                    'identifier': 'datefieldEditable',
                    'disabled': false,
                    'displayType': 'EDITABLE',
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
                  },{
                    'previewEmpty': true,
                    'identifier': 'objectProperty',
                    'disabled': false,
                    'displayType': 'EDITABLE',
                    'validators': [],
                    'dataType': 'text',
                    'label': 'Object property (single value)',
                    'isMandatory': true,
                    'tooltip': 'Test tooltip',
                    "controlId": "PICKER",
                    'control': [{
                      'identifier': 'PICKER'
                    }]
                  },{
                    'previewEmpty': true,
                    'identifier': 'singleCheckboxEdit',
                    'disabled': false,
                    'displayType': 'EDITABLE',
                    'validators': [],
                    'dataType': 'boolean',
                    'label': 'Editable single checkbox',
                    'isMandatory': true,
                    'tooltip': 'Test tooltip',
                    'mandatoryValidatorError': 'The field is mandatory'
                  }
                  //end of inner region
                ]
              }
            ]
          }
        ]
      })
    }
  }

  isAllControlsSandbox(){
    return true;
  }
  toggleBorders(){
    $('form').toggleClass('with-border');
  }
}