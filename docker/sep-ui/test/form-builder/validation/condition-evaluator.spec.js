import {ConditionEvaluator} from 'form-builder/validation/condition-evaluator';
import {InstanceModel} from 'models/instance-model';
import {ModelUtils} from 'models/model-utils';

describe('ConditionEvaluator', () => {

  let logger = {
    debug: () => {
    }
  };
  let configuration = {
    get: () => {
      return false;
    }
  };

  function getViewModelValidationResult(data) {
    return `\nExpected: ${JSON.stringify(data.viewModelUpdated)} \nActual:   ${JSON.stringify(data.viewModel)} \nCondition: ${data.renderAs}=${data.evaluatedCondition}\n`;
  }

  function getValidationModelValidationResult(data) {
    return `\nExpected field value should be "${data.expectedValue}" with data ${JSON.stringify(data)}`;
  }

  describe('applyCondition', () => {
    it('should apply HIDDEN|VISIBLE conditions', () => {
      let validator = new ConditionEvaluator(logger, configuration);
      testDataForVisibleAndHidden.forEach((data) => {
        validator.applyCondition(data.viewModel, data.renderAs, data.evaluatedCondition, data.validationModel);
        expect(data.viewModelUpdated, getViewModelValidationResult(data)).to.eql(data.viewModel);
        expect(data.validationModel[data.viewModel.identifier].value, getValidationModelValidationResult(data)).to.equal(data.expectedValue);
      });
    });

    it('should apply READONLY|ENABLED conditions', () => {
      let validator = new ConditionEvaluator(logger, configuration);
      testDataForReadonlyAndEditable.forEach((data) => {
        validator.applyCondition(data.viewModel, data.renderAs, data.evaluatedCondition, data.validationModel);
        expect(data.viewModelUpdated, getViewModelValidationResult(data)).to.eql(data.viewModel);
        expect(data.validationModel[data.viewModel.identifier].value, getValidationModelValidationResult(data)).to.equal(data.expectedValue);
      });
    });

    it('should not apply READONLY|ENABLED conditions if form is in PREVIEW mode', () => {
      let validator = new ConditionEvaluator(logger, configuration);
      testDataForReadonlyAndEditableInPreview.forEach((data) => {
        validator.applyCondition(data.viewModel, data.renderAs, data.evaluatedCondition, data.validationModel);
        expect(data.viewModelUpdated, getViewModelValidationResult(data)).to.eql(data.viewModel);
        expect(data.validationModel[data.viewModel.identifier].value, getValidationModelValidationResult(data)).to.equal(data.expectedValue);
      });
    });

    it('should apply MANDATORY|OPTIONAL conditions', () => {
      let validator = new ConditionEvaluator(logger, configuration);
      testDataForMandatoryAndOptional.forEach((data) => {
        validator.applyCondition(data.viewModel, data.renderAs, data.evaluatedCondition, data.validationModel);
        expect(data.viewModelUpdated, `The field "${data.viewModel.identifier}" view model: \n${JSON.stringify(data.viewModel)} \nshould be updated to: \n${JSON.stringify(data.viewModelUpdated)}`).to.eql(data.viewModel);
        expect(data.validationModel[data.viewModel.identifier].value, getValidationModelValidationResult(data)).to.equal(data.expectedValue);
      });
    });

    it('should put region field and its children view models in array for processing', () => {
      let validator = new ConditionEvaluator(logger, configuration);
      let viewModel = {
        'identifier': 'region',
        'displayType': 'EDITABLE',
        'fields': [
          {
            'identifier': 'field1'
          }
        ]
      };
      let validationModel = {
        'field1': {
          'value': '1',
          'messages': {}
        }
      };
      validator.applyCondition(viewModel, 'HIDDEN', true, validationModel);
      expect(viewModel.displayType, 'The region should be displayType=HIDDEN').to.equal('HIDDEN');
      expect(viewModel.fields[0].displayType, 'Region fields should be displayType=HIDDEN').to.equal('HIDDEN');
    });
  });

  describe('resetFieldValue', () => {
    it('should set the field value to be default value or null', () => {
      testDataForResetValue.forEach((data) => {
        ConditionEvaluator.resetFieldValue(data.reset, data.validationModel);
        expect(data.validationModel.value, `Field value should be set to [${data.expectedValue}] with data ${JSON.stringify(data)}`).to.equal(data.expectedValue);
      });
    });
  });

  describe('clearFieldValue', () => {
    it('should clear the field value', () => {
      testDataForClearValue.forEach((data) => {
        ConditionEvaluator.clearFieldValue(data.clear, data.validationModel, data.viewModel);
        expect(data.validationModel, 'Field value and defaultValue should be cleared').to.eql(data.expectedValidationModel);
      });
    });

    it('should rise the _hiddenByCondition flag', () => {
      testDataForClearValue.forEach((data) => {
        ConditionEvaluator.clearFieldValue(data.clear, data.validationModel, data.viewModel);
        expect(data.viewModel).to.eql(data.expectedViewModel);
      });
    });
  });

  describe('convertToBooleanExpression', () => {
    it('should transform passed condition to a boolean expression using the validation model values', () => {
      testDataForEvaluation.forEach((data) => {
        let expression = ConditionEvaluator.convertToBooleanExpression(data.tokens, data.model);
        expect(expression, `Condition parsed to following tokens ${data.tokens} should be evaluated to "${data.evaluated}"`).to.equal(data.evaluated);
      });
    });
  });

  describe('parseExpression', () => {
    it('should split passed expression to meaningful tokens', () => {
      let validator = new ConditionEvaluator(logger, configuration);
      testDataForParsing.forEach((data) => {
        let tokens = validator.parseExpression(data.expression);
        expect(tokens, `Condition "${data.expression}" should be parsed to ${data.tokens} but found ${tokens}`).to.eql(data.tokens);
      });
    });

    it('should put every tokenized expression inside a tokenized expression cache if its not already there', () => {
      let validator = new ConditionEvaluator(logger, configuration);
      validator.parseExpression('+[field1]');
      validator.parseExpression('+[field1]');
      validator.parseExpression('-[field1]');
      validator.parseExpression('(+[field1] AND [field2] IN ("opt1","opt2"))');
      expect(validator.tokenizedExpressionCache).to.eql({
        '+[field1]': ['+[field1]'],
        '-[field1]': ['-[field1]'],
        '(+[field1] AND [field2] IN ("opt1","opt2"))': ['(', '+[field1]', 'AND', '[field2]', 'IN', '("opt1","opt2")', ')']
      });
    });

    it('should throw Error if tokenieze does not return a token for any reason', () => {
      let validator = new ConditionEvaluator(logger, configuration);
      expect(function () {
        validator.parseExpression('+[field1');
      }).to.throw(Error);
    });

    it('should throw Error if an expression contains invalid collection token', () => {
      let validator = new ConditionEvaluator(logger, configuration);
      expect(function () {
        validator.parseExpression('[field] IN ("opts1"');
      }).to.throw(Error);
    });
  });

  describe('evaluate', () => {
    it('should evaluate and parse condition expressions', () => {
      let validator = new ConditionEvaluator(logger, configuration);
      let validatorDef = {
        'id': 'condition',
        'rules': [
          {
            'id': 'hideField',
            'renderAs': 'HIDDEN',
            'expression': '+[field2]'
          }
        ]
      };
      let validationModel = {
        'field1': {
          'value': '1',
          'messages': {}
        },
        'field2': {
          'value': '2',
          'messages': {}
        }
      };
      let flatModel = {
        'field1': {
          'identifier': 'field1',
          'validators': [
            {
              'id': 'condition',
              'rules': [
                {
                  'id': 'hideField',
                  'renderAs': 'HIDDEN',
                  'expression': '+[field2]'
                }
              ]
            }
          ]
        }
      };
      validator.$timeout = (fun) => {
        fun && fun();
      };
      // should be undefined always
      let validatorResult = validator.evaluate('field1', validatorDef, validationModel, flatModel);
      expect(validatorResult).to.not.be.defined;
    });
  });

  describe('getCollectionConditionValues', () => {
    it('should extract values from given token and return them as array', () => {
      let values = ConditionEvaluator.getCollectionConditionValues(`('BGR', 'AUS', 'USA', 'AT123)`);
      expect(values).to.eql(['BGR', 'AUS', 'USA', 'AT123']);
    });
  });

  // Readonly fields with HIDDEN condition should not get their values cleared when the condition is applied!
  let testDataForVisibleAndHidden = [
    {
      evaluatedCondition: true,
      renderAs: 'HIDDEN',
      viewModel: {
        'identifier': 'field0',
        'displayType': 'READ_ONLY'
      },
      viewModelUpdated: {
        'identifier': 'field0',
        'displayType': 'HIDDEN',
        '_displayType': 'READ_ONLY'
      },
      validationModel: {
        'field0': {
          defaultValue: '1',
          value: '1'
        }
      },
      expectedValue: '1'
    },
    {
      evaluatedCondition: true,
      renderAs: 'HIDDEN',
      viewModel: {
        'identifier': 'field1',
        'displayType': 'EDITABLE'
      },
      viewModelUpdated: {
        'identifier': 'field1',
        'displayType': 'HIDDEN',
        '_displayType': 'EDITABLE',
        '_hiddenByCondition': true
      },
      validationModel: {
        'field1': {
          defaultValue: '1',
          value: '2'
        }
      },
      expectedValue: null
    },
    {
      evaluatedCondition: false,
      renderAs: 'HIDDEN',
      viewModel: {
        'identifier': 'field2',
        'displayType': 'HIDDEN'
      },
      viewModelUpdated: {
        'identifier': 'field2',
        'displayType': 'HIDDEN',
        '_displayType': 'HIDDEN'
      },
      validationModel: {
        'field2': {
          defaultValue: '1',
          value: '2'
        }
      },
      expectedValue: '2'
    },
    {
      evaluatedCondition: true,
      renderAs: 'HIDDEN',
      viewModel: {
        'identifier': 'field3',
        'displayType': 'HIDDEN'
      },
      viewModelUpdated: {
        'identifier': 'field3',
        'displayType': 'HIDDEN',
        '_displayType': 'HIDDEN',
        '_hiddenByCondition': true
      },
      validationModel: {
        'field3': {
          defaultValue: '1',
          value: '2'
        }
      },
      expectedValue: null
    },
    {
      evaluatedCondition: false,
      renderAs: 'HIDDEN',
      viewModel: {
        'identifier': 'field4',
        'displayType': 'EDITABLE'
      },
      viewModelUpdated: {
        'identifier': 'field4',
        'displayType': 'EDITABLE',
        '_displayType': 'EDITABLE'
      },
      validationModel: {
        'field4': {
          defaultValue: '1',
          value: '2'
        }
      },
      expectedValue: '2'
    },


    {
      evaluatedCondition: true,
      renderAs: 'VISIBLE',
      viewModel: {
        'identifier': 'field5',
        'displayType': 'EDITABLE'
      },
      viewModelUpdated: {
        'identifier': 'field5',
        'displayType': 'EDITABLE',
        '_displayType': 'EDITABLE'
      },
      validationModel: {
        'field5': {
          defaultValue: '1',
          value: '2'
        }
      },
      expectedValue: '2'
    },
    {
      evaluatedCondition: false,
      renderAs: 'VISIBLE',
      viewModel: {
        'identifier': 'field6',
        'displayType': 'EDITABLE'
      },
      viewModelUpdated: {
        'identifier': 'field6',
        'displayType': 'EDITABLE',
        '_displayType': 'EDITABLE'
      },
      validationModel: {
        'field6': {
          defaultValue: '1',
          value: '2'
        }
      },
      expectedValue: '2'
    },
    {
      evaluatedCondition: true,
      renderAs: 'VISIBLE',
      viewModel: {
        'identifier': 'field7',
        'displayType': 'HIDDEN'
      },
      viewModelUpdated: {
        'identifier': 'field7',
        'displayType': 'READ_ONLY',
        '_displayType': 'HIDDEN'
      },
      validationModel: {
        'field7': {
          defaultValue: '1',
          value: '2'
        }
      },
      expectedValue: '2'
    },
    {
      evaluatedCondition: false,
      renderAs: 'VISIBLE',
      viewModel: {
        'identifier': 'field8',
        'displayType': 'HIDDEN'
      },
      viewModelUpdated: {
        'identifier': 'field8',
        'displayType': 'HIDDEN',
        '_displayType': 'HIDDEN',
        '_hiddenByCondition': true
      },
      validationModel: {
        'field8': {
          defaultValue: '1',
          value: '2'
        }
      },
      expectedValue: null
    }
  ];

//validationModel is an instance of InstanceModel
  let testDataForReadonlyAndEditable = [
    {
      evaluatedCondition: true,
      renderAs: 'READONLY',
      viewModel: {
        'identifier': 'field1',
        'preview': true
      },
      viewModelUpdated: {
        'identifier': 'field1',
        'preview': true,
        '_preview': true
      },
      validationModel: {
        'field1': {
          defaultValue: '1',
          value: '2',
          '_cleared': false
        }
      },
      expectedValue: '1'
    },
    {
      evaluatedCondition: false,
      renderAs: 'READONLY',
      viewModel: {
        'identifier': 'field2',
        'preview': true
      },
      viewModelUpdated: {
        'identifier': 'field2',
        'preview': true,
        '_preview': true
      },
      validationModel: {
        'field2': {
          defaultValue: '1',
          value: '2'
        }
      },
      expectedValue: '2'
    },
    {
      evaluatedCondition: true,
      renderAs: 'READONLY',
      viewModel: {
        'identifier': 'field3',
        'preview': false
      },
      viewModelUpdated: {
        'identifier': 'field3',
        'preview': true,
        '_preview': false
      },
      validationModel: {
        'field3': {
          defaultValue: '1',
          value: '2',
          '_cleared': false
        }
      },
      expectedValue: '1'
    },
    {
      evaluatedCondition: false,
      renderAs: 'READONLY',
      viewModel: {
        'identifier': 'field4',
        'preview': false
      },
      viewModelUpdated: {
        'identifier': 'field4',
        'preview': false,
        '_preview': false
      },
      validationModel: {
        'field4': {
          defaultValue: '1',
          value: '2'
        }
      },
      expectedValue: '2'
    },


    {
      evaluatedCondition: true,
      renderAs: 'ENABLED',
      viewModel: {
        'identifier': 'field5',
        'preview': true
      },
      viewModelUpdated: {
        'identifier': 'field5',
        'preview': false,
        '_preview': true
      },
      validationModel: {
        'field5': {
          defaultValue: '1',
          value: '2'
        }
      },
      expectedValue: '2'
    },
    {
      evaluatedCondition: false,
      renderAs: 'ENABLED',
      viewModel: {
        'identifier': 'field6',
        'preview': true
      },
      viewModelUpdated: {
        'identifier': 'field6',
        'preview': true,
        '_preview': true
      },
      validationModel: {
        'field6': {
          defaultValue: '1',
          value: '2',
          '_cleared': false
        }
      },
      expectedValue: '1'
    },
    {
      evaluatedCondition: true,
      renderAs: 'ENABLED',
      viewModel: {
        'identifier': 'field7',
        'preview': false
      },
      viewModelUpdated: {
        'identifier': 'field7',
        'preview': false,
        '_preview': false
      },
      validationModel: {
        'field7': {
          defaultValue: '1',
          value: '2'
        }
      },
      expectedValue: '2'
    },
    {
      evaluatedCondition: false,
      renderAs: 'ENABLED',
      viewModel: {
        'identifier': 'field8',
        'preview': false
      },
      viewModelUpdated: {
        'identifier': 'field8',
        'preview': false,
        '_preview': false
      },
      validationModel: {
        'field8': {
          defaultValue: '1',
          value: '2',
          '_cleared': false
        }
      },
      expectedValue: '1'
    }
  ];

  let testDataForReadonlyAndEditableInPreview = [
    {
      evaluatedCondition: true,
      renderAs: 'READONLY',
      viewModel: {
        'identifier': 'field1',
        'preview': true
      },
      viewModelUpdated: {
        'identifier': 'field1',
        'preview': true,
        '_preview': true
      },
      validationModel: {
        'field1': {
          defaultValue: '1',
          value: '2',
          '_cleared': false
        }
      },
      expectedValue: '1'
    },
    {
      evaluatedCondition: true,
      renderAs: 'ENABLED',
      viewModel: {
        'identifier': 'field2',
        'preview': false
      },
      viewModelUpdated: {
        'identifier': 'field2',
        'preview': false,
        '_preview': false
      },
      validationModel: new InstanceModel({
        'field2': {
          defaultValue: '1',
          value: '2'
        }
      }),
      expectedValue: '2'
    }
  ];

  let testDataForMandatoryAndOptional = [
    {
      evaluatedCondition: true,
      renderAs: 'MANDATORY',
      viewModel: {
        'identifier': 'field1',
        'isMandatory': true,
        'validators': [
          {
            'level': 'error',
            'id': 'mandatory',
            'message': 'validation.field.mandatory'
          }
        ]
      },
      viewModelUpdated: {
        'identifier': 'field1',
        'isMandatory': true,
        '_isMandatory': true,
        'validators': [
          {
            'level': 'error',
            'id': 'mandatory',
            'message': 'validation.field.mandatory'
          }
        ]
      },
      validationModel: new InstanceModel({
        'field1': {
          'defaultValue': '1',
          'value': '2',
          'messages': {
            'mandatory': {}
          }
        }
      }),
      expectedValue: '2'
    },
    {
      evaluatedCondition: false,
      renderAs: 'MANDATORY',
      viewModel: {
        'identifier': 'field2',
        'isMandatory': true,
        'validators': [
          {
            'level': 'error',
            'id': 'mandatory',
            'message': 'validation.field.mandatory'
          }
        ]
      },
      viewModelUpdated: {
        'identifier': 'field2',
        'isMandatory': true,
        '_isMandatory': true,
        'validators': [
          {
            'level': 'error',
            'id': 'mandatory',
            'message': 'validation.field.mandatory'
          }
        ]
      },
      validationModel: new InstanceModel({
        'field2': {
          'defaultValue': '1',
          'value': '2',
          'messages': {}
        }
      }),
      expectedValue: '2'
    },
    {
      evaluatedCondition: true,
      renderAs: 'MANDATORY',
      viewModel: {
        'identifier': 'field3',
        'isMandatory': false,
        'rendered': false,
        'validators': [
          {
            'level': 'error',
            'id': 'mandatory',
            'message': 'validation.field.mandatory'
          }
        ]
      },
      viewModelUpdated: {
        'identifier': 'field3',
        'isMandatory': true,
        '_isMandatory': false,
        'rendered': false,
        'validators': [
          {
            'level': 'error',
            'id': 'mandatory',
            'message': 'validation.field.mandatory'
          }
        ]
      },
      validationModel: new InstanceModel({
        'field3': {
          'defaultValue': '1',
          'value': '2',
          'messages': {
            'mandatory': {}
          }
        }
      }),
      expectedValue: '2'
    },
    {
      evaluatedCondition: false,
      renderAs: 'MANDATORY',
      viewModel: {
        'identifier': 'field4',
        'isMandatory': false,
        'validators': []
      },
      viewModelUpdated: {
        'identifier': 'field4',
        'isMandatory': false,
        '_isMandatory': false,
        'validators': []
      },
      validationModel: new InstanceModel({
        'field4': {
          'defaultValue': '1',
          'value': '2',
          'messages': {}
        }
      }),
      expectedValue: '2'
    },

    {
      evaluatedCondition: true,
      renderAs: 'OPTIONAL',
      viewModel: {
        'identifier': 'field5',
        'isMandatory': true,
        'validators': [
          {
            'level': 'error',
            'id': 'mandatory',
            'message': 'validation.field.mandatory'
          }
        ]
      },
      viewModelUpdated: {
        'identifier': 'field5',
        'isMandatory': false,
        '_isMandatory': true,
        'validators': [
          {
            'level': 'error',
            'id': 'mandatory',
            'message': 'validation.field.mandatory'
          }
        ]
      },
      validationModel: new InstanceModel({
        'field5': {
          'defaultValue': '1',
          'value': '2',
          'messages': {}
        }
      }),
      expectedValue: '2'
    },
    {
      evaluatedCondition: false,
      renderAs: 'OPTIONAL',
      viewModel: {
        'identifier': 'field6',
        'isMandatory': true,
        'validators': [
          {
            'level': 'error',
            'id': 'mandatory',
            'message': 'validation.field.mandatory'
          }
        ]
      },
      viewModelUpdated: {
        'identifier': 'field6',
        'isMandatory': true,
        '_isMandatory': true,
        'validators': [
          {
            'level': 'error',
            'id': 'mandatory',
            'message': 'validation.field.mandatory'
          }
        ]
      },
      validationModel: new InstanceModel({
        'field6': {
          'defaultValue': '1',
          'value': '2',
          'messages': {
            'mandatory': {}
          }
        }
      }),
      expectedValue: '2'
    },
    {
      evaluatedCondition: true,
      renderAs: 'OPTIONAL',
      viewModel: {
        'identifier': 'field7',
        'isMandatory': false,
        'validators': []
      },
      viewModelUpdated: {
        'identifier': 'field7',
        'isMandatory': false,
        '_isMandatory': false,
        'validators': []
      },
      validationModel: new InstanceModel({
        'field7': {
          'defaultValue': '1',
          'value': '2',
          'messages': {}
        }
      }),
      expectedValue: '2'
    },
    {
      evaluatedCondition: false,
      renderAs: 'OPTIONAL',
      viewModel: {
        'identifier': 'field8',
        'isMandatory': false,
        'rendered': true,
        'validators': [
          {
            'level': 'error',
            'id': 'mandatory',
            'message': 'validation.field.mandatory'
          }
        ]
      },
      viewModelUpdated: {
        'identifier': 'field8',
        'isMandatory': false,
        '_isMandatory': false,
        'rendered': true,
        'validators': [
          {
            'level': 'error',
            'id': 'mandatory',
            'message': 'validation.field.mandatory'
          }
        ]
      },
      validationModel: new InstanceModel({
        'field8': {
          'defaultValue': '1',
          'value': '2',
          'messages': {
            'mandatory': {}
          }
        }
      }),
      expectedValue: '2'
    }
  ];

  let testDataForEvaluation = [
    {
      tokens: ['+[multivaluedFieldWithValue]'],
      evaluated: 'true',
      model: {'multivaluedFieldWithValue': {value: ['test']}}
    },
    {
      tokens: ['+[multivaluedFieldWithoutValue]'],
      evaluated: 'false',
      model: {'multivaluedFieldWithoutValue': {value: []}}
    },
    {
      tokens: ['-[multivaluedFieldWithValue]'],
      evaluated: 'true',
      model: {'multivaluedFieldWithValue': {value: []}}
    },
    {
      tokens: ['-[multivaluedFieldWithValue]'],
      evaluated: 'false',
      model: {'multivaluedFieldWithValue': {value: ['test']}}
    },
    {
      tokens: ['+[fieldWithValue]'],
      evaluated: 'true',
      model: {'fieldWithValue': {value: 'test'}}
    },
    {
      tokens: ['-[fieldWithoutValue]'],
      evaluated: 'true',
      model: {'fieldWithoutValue': {value: ''}}
    },
    {
      tokens: ['[fieldValueInclusive]', 'IN', '("opt1","opt2")'],
      evaluated: 'true',
      model: {'fieldValueInclusive': {value: 'opt1'}}
    },
    {
      tokens: ['[fieldValueExclusive]', 'NOTIN', '("opt1","opt2")'],
      evaluated: 'true',
      model: {'fieldValueExclusive': {value: 'opt3'}}
    },
    {
      tokens: ['+[fieldWithValue]', 'AND', '-[fieldWithoutValue]'],
      evaluated: 'true&&true',
      model: {'fieldWithValue': {value: 'test'}, 'fieldWithoutValue': {value: ''}}
    },
    {
      tokens: ['+[fieldWithValue]', 'OR', '-[fieldWithoutValue]'],
      evaluated: 'true||true',
      model: {'fieldWithValue': {value: 'test'}, 'fieldWithoutValue': {value: ''}}
    },
    {
      tokens: ['+[fieldWithValue1]', 'AND', '(', '+[fieldWithoutValue]', 'OR', '+[fieldWithValue2]', ')'],
      evaluated: 'true&&(false||true)',
      model: {'fieldWithValue1': {value: 'test'}, 'fieldWithoutValue': {value: ''}, 'fieldWithValue2': {value: 'test'}}
    },
    {
      tokens: ['+[FieldWithObjectValue]'],
      evaluated: 'false',
      model: {'FieldWithObjectValue': {value: {results: []}}}
    }
  ];

  let testDataForParsing = [
    {
      expression: '+[field1]',
      tokens: ['+[field1]']
    },
    {
      expression: '-[field1]',
      tokens: ['-[field1]']
    },
    {
      expression: '[field1] IN ("opt1","opt2")',
      tokens: ['[field1]', 'IN', '("opt1","opt2")']
    },
    {
      expression: '[field1] NOTIN ("opt1","opt2")',
      tokens: ['[field1]', 'NOTIN', '("opt1","opt2")']
    },
    {
      expression: '+[field1] AND -[field2]',
      tokens: ['+[field1]', 'AND', '-[field2]']
    },
    {
      expression: '+[field1] AND (+[field2] OR +[field3])',
      tokens: ['+[field1]', 'AND', '(', '+[field2]', 'OR', '+[field3]', ')']
    },
    {
      expression: '((+[field1] AND +[field2]) OR (+[field3] AND [field4] NOTIN ("opt1","opt2")))',
      tokens: ['(', '(', '+[field1]', 'AND', '+[field2]', ')', 'OR', '(', '+[field3]', 'AND', '[field4]', 'NOTIN', '("opt1","opt2")', ')', ')']
    }
  ];

  let testDataForResetValue = [
    {
      reset: true,
      expectedValue: '1',
      validationModel: {value: '2', defaultValue: '1', '_cleared': false}
    },
    {
      reset: true,
      expectedValue: null,
      validationModel: {value: '2', '_cleared': false}
    },
    {
      reset: false,
      expectedValue: '2',
      validationModel: {value: '2'}
    }
  ];

  let testDataForClearValue = [
    {
      clear: true,
      expectedValidationModel: {value: null, defaultValue: '1', _cleared: true},
      expectedViewModel: { identifier: 'field1', _hiddenByCondition: true },
      validationModel: {value: '2', defaultValue: '1'},
      viewModel: { identifier: 'field1' }
    },
    {
      clear: false,
      expectedValidationModel: {value: '2', defaultValue: '2', _cleared: false},
      expectedViewModel: { identifier: 'field1' },
      validationModel: {value: '2', defaultValue: '2'},
      viewModel: { identifier: 'field1' }
    },
    {
      clear: true,
      expectedValidationModel: {value: ModelUtils.getEmptyObjectPropertyValue(), _cleared: true},
      expectedViewModel: { identifier: 'field1', _hiddenByCondition: true },
      validationModel: {
        value: {
          results: ['test', 'test2'],
          total: 2,
          add: [],
          remove: [],
          headers: {}
        }
      },
      viewModel: { identifier: 'field1' }
    }
  ];

});