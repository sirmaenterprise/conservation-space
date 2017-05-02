import {FieldValidator} from 'form-builder/validation/field-validator';
import util from './validator-test-util';

describe('FieldValidator', function() {

  it('should throw error if a validator does not implemented a validator function', function() {
    expect(function() { new FieldValidator() }).to.throw(TypeError);
  });

  it('should return the field\'s viewValue when getViewValue is called', function() {
    var validator = new DummyValidator();
    var fieldName = 'testfield';
    var testValue = 'testvalue';
    var validationModel = util.buildValidationModel(fieldName, testValue);
    var viewValue = validator.getViewValue(fieldName, validationModel);
    expect(viewValue).to.equal(testValue);
  });

  it('should return the field\'s viewValue as number when getViewValueAsNumber is called', function() {
    var validator = new DummyValidator();
    var fieldName = 'testfield';
    var testValue = '123';
    var validationModel = util.buildValidationModel(fieldName, testValue);
    var viewValue = validator.getViewValueAsNumber(fieldName, validationModel);
    expect(viewValue).to.equal(123);
  });

  it('should return the field\'s viewValue as string when getViewValueAsString is called', function() {
    var validator = new DummyValidator();
    var fieldName = 'testfield';
    var testValue = 123;
    var validationModel = util.buildValidationModel(fieldName, testValue);
    var viewValue = validator.getViewValueAsString(fieldName, validationModel);
    expect(viewValue).to.equal('123');
  });

  describe('removeValidator', () => {
    it('should remove a validator detected by its id from the validators array in the fields model', () => {
      let validator = new DummyValidator();
      let fieldViewModel = {
        'validators': [
          { 'id': 'regex' },
          { 'id': 'mandatory' },
          { 'id': 'condition' }
        ]
      };
      validator.removeValidator('mandatory', fieldViewModel);
      expect(fieldViewModel.validators.length).to.equal(2);
      expect(fieldViewModel.validators[0].id).to.equal('regex');
      expect(fieldViewModel.validators[1].id).to.equal('condition');
    });

    it('should not modify the validators array if a validator is not found in the validators array', () => {
      let validator = new DummyValidator();
      let fieldViewModel = {
        'validators': [
          { 'id': 'regex' },
          { 'id': 'mandatory' },
          { 'id': 'condition' }
        ]
      };
      validator.removeValidator('greaterThan', fieldViewModel);
      expect(fieldViewModel.validators.length).to.equal(3);
    });
  });

});

class DummyValidator extends FieldValidator {

  validate() {

  }
}