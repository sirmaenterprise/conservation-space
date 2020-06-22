import {GreaterStringLengthThan} from 'form-builder/validation/greater-string-length-than/greater-string-length-than';
import util from '../validator-test-util';

describe('GreaterStringLengthThan', function () {

  let validator = new GreaterStringLengthThan();

  it('should return true if view length is greater than the expected length', function () {
    let validatorDef = util.buildValidatorDefinition(5);
    let validationModel = util.buildValidationModel('testfield', 'test123');
    let isValid = validator.validate('testfield', validatorDef, validationModel);
    expect(isValid).to.be.true;
  });

  it('should return false if view length is less than the expected length', function () {
    let validatorDef = util.buildValidatorDefinition(5);
    let validationModel = util.buildValidationModel('testfield', '321');
    let isValid = validator.validate('testfield', validatorDef, validationModel);
    expect(isValid).to.be.false;
  });

  it('should return false if view is undefined', function () {
    let validatorDef = util.buildValidatorDefinition(3);
    let validationModel = util.buildValidationModel('testfield');
    let isValid = validator.validate('testfield', validatorDef, validationModel);
    expect(isValid).to.be.false;
  });

  it('should return false if view is empty string', function () {
    let validatorDef = util.buildValidatorDefinition(6);
    let validationModel = util.buildValidationModel('testfield', '');
    let isValid = validator.validate('testfield', validatorDef, validationModel);
    expect(isValid).to.be.false;
  });

  it('should return true if view length is one and expected length is zero', function () {
    let validatorDef = util.buildValidatorDefinition(0);
    let validationModel = util.buildValidationModel('testfield', 'a');
    let isValid = validator.validate('testfield', validatorDef, validationModel);
    expect(isValid).to.be.true;
  });

  it('should return false if view length is one and expected length is one', function () {
    let validatorDef = util.buildValidatorDefinition(1);
    let validationModel = util.buildValidationModel('testfield', 'a');
    let isValid = validator.validate('testfield', validatorDef, validationModel);
    expect(isValid).to.be.false;
  });

  it('should return false if view is not a string', function () {
    let validatorDef = util.buildValidatorDefinition(1);
    let validationModel = util.buildValidationModel('testfield', 300);
    let isValid = validator.validate('testfield', validatorDef, validationModel);
    expect(isValid).to.be.false;
  });

});