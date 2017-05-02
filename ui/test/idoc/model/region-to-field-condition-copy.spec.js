import {RegionToFieldConditionCopy} from 'idoc/model/region-to-field-condition-copy';
import {DefinitionModel} from 'models/definition-model';

describe('RegionToFieldConditionCopy', () => {
  it('should copy all condition validators applied on regions in region fields', () => {
    let convertor = new RegionToFieldConditionCopy();
    convertor.convert({
      viewModel: model
    });
    expect(model).to.eql(convertedModel);
    // should not duplicate validators on consecutive calls
    convertor.convert({
      viewModel: model
    });
    expect(model).to.eql(convertedModel);
  });

  var model = new DefinitionModel({
    'fields': [
      {
        'identifier': 'field1',
        'validators': [
          {
            'id': 'mandatory',
            'message': 'The field is mandatory',
            'level': 'error'
          }
        ]
      },
      {
        'identifier': 'region1',
        'label': 'Region 1',
        'displayType': 'EDITABLE',
        'validators': [
          {
            'id': 'condition',
            'rules': [
              {
                'id': 'readonly',
                'renderAs': 'READONLY',
                'expression': '+[triggertextfield1]'
              }
            ]
          }
        ],
        'fields': [
          {
            'identifier': 'regionfield1',
            'validators': [
              {
                'id': 'mandatory',
                'message': 'The field is mandatory',
                'level': 'error'
              }
            ]
          },
          {
            'identifier': 'regionfield2',
            'validators': []
          }
        ]
      },
      {
        'identifier': 'region2',
        'label': 'Region 2',
        'validators': [
          {
            'id': 'condition',
            'rules': [
              {
                'id': 'readonly',
                'renderAs': 'READONLY',
                'expression': '+[triggertextfield1]'
              }
            ]
          }
        ],
        'fields': [
          {
            'identifier': 'regionfield3',
            'validators': [
              {
                'id': 'mandatory',
                'message': 'The field is mandatory',
                'level': 'error'
              }
            ]
          },
          {
            'identifier': 'regionfield4',
            'validators': []
          }
        ]
      }
    ]
  });

  var convertedModel = new DefinitionModel({
    'fields': [
      {
        'identifier': 'field1',
        'validators': [
          {
            'id': 'mandatory',
            'message': 'The field is mandatory',
            'level': 'error'
          }
        ]
      },
      {
        'identifier': 'region1',
        'label': 'Region 1',
        'displayType': 'EDITABLE',
        'validators': [
          {
            'id': 'condition',
            'rules': [
              {
                'id': 'readonly',
                'renderAs': 'READONLY',
                'expression': '+[triggertextfield1]'
              }
            ]
          }
        ],
        'fields': [
          {
            'identifier': 'regionfield1',
            'validators': [
              {
                'id': 'mandatory',
                'message': 'The field is mandatory',
                'level': 'error'
              },
              {
                'id': 'condition',
                'rules': [
                  {
                    'id': 'readonly',
                    'renderAs': 'READONLY',
                    'expression': '+[triggertextfield1]'
                  }
                ]
              }
            ]
          },
          {
            'identifier': 'regionfield2',
            'validators': [
              {
                'id': 'condition',
                'rules': [
                  {
                    'id': 'readonly',
                    'renderAs': 'READONLY',
                    'expression': '+[triggertextfield1]'
                  }
                ]
              }
            ]
          }
        ]
      },
      {
        'identifier': 'region2',
        'label': 'Region 2',
        'validators': [
          {
            'id': 'condition',
            'rules': [
              {
                'id': 'readonly',
                'renderAs': 'READONLY',
                'expression': '+[triggertextfield1]'
              }
            ]
          }
        ],
        'fields': [
          {
            'identifier': 'regionfield3',
            'validators': [
              {
                'id': 'mandatory',
                'message': 'The field is mandatory',
                'level': 'error'
              },
              {
                'id': 'condition',
                'rules': [
                  {
                    'id': 'readonly',
                    'renderAs': 'READONLY',
                    'expression': '+[triggertextfield1]'
                  }
                ]
              }
            ]
          },
          {
            'identifier': 'regionfield4',
            'validators': [
              {
                'id': 'condition',
                'rules': [
                  {
                    'id': 'readonly',
                    'renderAs': 'READONLY',
                    'expression': '+[triggertextfield1]'
                  }
                ]
              }
            ]
          }
        ]
      }
    ]
  });
});