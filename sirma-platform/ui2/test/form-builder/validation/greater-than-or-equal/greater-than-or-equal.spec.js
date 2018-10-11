import {GreaterThanOrEqual} from 'form-builder/validation/greater-than-or-equal/greater-than-or-equal';
import util from '../validator-test-util';

describe('GreaterThanOrEqual validator', function() {

  var validator = new GreaterThanOrEqual();
  var validatorDef = util.buildValidatorDefinition(123);

  it('should return true if view value of type number is greater than expected number value', function() {
    var validationModel = util.buildValidationModel('testfield', 246);
    var isValid = validator.validate('testfield', validatorDef, validationModel);
    expect(isValid).to.be.true;
  });

  it('should return true if view value of type number is equal to expected number value', function() {
    var validationModel = util.buildValidationModel('testfield', 123);
    var isValid = validator.validate('testfield', validatorDef, validationModel);
    expect(isValid).to.be.true;
  });

  it('should return true if view value of type string is equal to expected number value', function() {
    var validationModel = util.buildValidationModel('testfield', '123');
    var isValid = validator.validate('testfield', validatorDef, validationModel);
    expect(isValid).to.be.true;
  });

  it('should return true if view value of type string is greater than expected number value', function() {
    var validationModel = util.buildValidationModel('testfield', '246');
    var isValid = validator.validate('testfield', validatorDef, validationModel);
    expect(isValid).to.be.true;
  });

  it('should return false if view value of type number is lower than expected number value', function() {
    var validationModel = util.buildValidationModel('testfield', 100);
    var isValid = validator.validate('testfield', validatorDef, validationModel);
    expect(isValid).to.be.false;
  });

  it('should return false if view value of type string can not be converted to number', function() {
    var validationModel = util.buildValidationModel('testfield', 'test');
    var isValid = validator.validate('testfield', validatorDef, validationModel);
    expect(isValid).to.be.false;
  });

  it('should return false if view value is not defined', function() {
    var validationModel = util.buildValidationModel('testfield', null);
    var isValid = validator.validate('testfield', validatorDef, validationModel);
    expect(isValid).to.be.false;
  });
});