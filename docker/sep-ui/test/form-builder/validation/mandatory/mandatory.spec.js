import {Mandatory} from 'form-builder/validation/mandatory/mandatory';
import {ConditionEvaluator} from 'form-builder/validation/condition-evaluator';

describe('Mandatory validator', function () {

  let conditionEvaluator = new ConditionEvaluator({}, {
    get: ()=> {
    }
  });
  let stubEvaluate = sinon.stub(conditionEvaluator, 'evaluate');
  let validator = new Mandatory(conditionEvaluator);

  it('should evaluate a mandatory condition and return result', () => {
    let validatorDef = {
      id: 'mandatory',
      rules: []
    };
    let validationModel = {
      testfield: {
        value: 123
      }
    };
    let flatModel = {
      testfield: {
        isMandatory: true
      }
    };
    let argArray = ['testfield', validatorDef, validationModel, flatModel];
    let result = validator.validate.apply(validator, argArray);
    expect(stubEvaluate.calledOnce).to.be.true;
    expect(stubEvaluate.getCall(0).args).to.eql(argArray);
  });

  describe('validate using mandatory condition', () => {
    let validatorDef = {
      id: 'mandatory',
      rules: []
    };
    let validationModel = {
      testfield: {
        value: 123
      }
    };
    let flatModel = {
      testfield: {
        'isMandatory': true
      }
    };
    let argArray = ['testfield', validatorDef, validationModel, flatModel];

    it('should return valid=true if is not mandatory by condition and has no value', () => {
      flatModel.testfield.isMandatory = false;
      validationModel.testfield.value = null;
      expect(validator.validate.apply(validator, argArray)).to.be.true;
    });

    it('should return valid=true if is not mandatory by condition and has value', () => {
      flatModel.testfield.isMandatory = false;
      validationModel.testfield.value = 'test';
      expect(validator.validate.apply(validator, argArray)).to.be.true;
    });

    it('should return valid=false if is mandatory by condition and has no value', () => {
      flatModel.testfield.isMandatory = true;
      validationModel.testfield.value = null;
      expect(validator.validate.apply(validator, argArray)).to.be.false;
    });

    it('should return valid=true if is mandatory by condition and has value', () => {
      flatModel.testfield.isMandatory = false;
      validationModel.testfield.value = 'test';
      expect(validator.validate.apply(validator, argArray)).to.be.true;
    });
  });

  it('should validate fields using the isMandatory attribute if there are no conditions', () => {
    let testdata = [
      {isMandatory: true, value: null, valid: false},
      {isMandatory: true, value: undefined, valid: false},
      {isMandatory: true, value: '', valid: false},
      {isMandatory: true, value: [], valid: false},
      {isMandatory: true, value: 123, valid: true},
      {isMandatory: true, value: 0, valid: true},
      {isMandatory: true, value: -1, valid: true},
      {isMandatory: true, value: 'string', valid: true},
      {isMandatory: true, value: {id: 123}, valid: true},
      {isMandatory: true, value: [1, 2], valid: true},
      {isMandatory: true, value: '    ', valid: false}
    ];
    let validatorDef = {
      id: 'mandatory'
    };
    let validationModel = {
      'testfield': {
        value: 123
      }
    };
    let flatModel = {
      testfield: {
        isMandatory: true
      }
    };
    testdata.forEach((data) => {
      validationModel.testfield.value = data.value;
      flatModel.testfield.isMandatory = data.isMandatory;
      let result = validator.validate('testfield', validatorDef, validationModel, flatModel, {}, false, []);
      expect(result, (data.isMandatory ? 'Mandatory' : 'Optional') +
        ' field with value "' + JSON.stringify(data.value) + '" should be ' + (data.valid ? 'valid' : 'invalid')).to.equal(data.valid);
    });
  });

  describe('when field is of type PICKER (object properties)', () => {
    it('should validate if field is mandatory or not', () => {
      let validatorDef = {
        id: 'mandatory'
      };
      let validationModel = {
        references: {
          value: {
            results: []
          }
        }
      };
      let flatModel = {
        references: {
          isMandatory: true,
          controlId: 'PICKER'
        }
      };

      // should be considered as invalid as there are no results in the model
      let valid = validator.validate('references', validatorDef, validationModel, flatModel, {}, false, []);
      expect(valid).to.be.false;

      // should be considered as valid as there are results in the model
      validationModel.references.value.results.push('id1');
      valid = validator.validate('references', validatorDef, validationModel, flatModel, {}, false, []);
      expect(valid).to.be.true;
    });

    it('should validate if field with old model is mandatory or not', () => {
      let validatorDef = {
        id: 'mandatory'
      };
      let validationModel = {
        references: {
          // the old object properties model
          value: {
            id: 'id1'
          }
        }
      };
      let flatModel = {
        references: {
          isMandatory: true,
          controlId: 'PICKER'
        }
      };

      // should be valid as it has value
      let valid = validator.validate('references', validatorDef, validationModel, flatModel, {}, false, []);
      expect(valid).to.be.true;

      // should be invalid as it doesn't have value
      validationModel.references.value = undefined;
      valid = validator.validate('references', validatorDef, validationModel, flatModel, {}, false, []);
      expect(valid).to.be.false;

      // should be invalid as it doesn't have value
      delete validationModel.references.value;
      valid = validator.validate('references', validatorDef, validationModel, flatModel, {}, false, []);
      expect(valid).to.be.false;
    });
  });

});