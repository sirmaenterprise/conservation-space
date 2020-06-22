import {GreaterNumberThan} from 'form-builder/validation/greater-number-than/greater-number-than';
import util from '../validator-test-util';

describe('GreaterNumberThan validator', function() {

  var validator = new GreaterNumberThan();
  var validatorDef = util.buildValidatorDefinition(123);

  it('should return true if view value of type number is greater than expected number value', function() {
    var validationModel = util.buildValidationModel('testfield', 246);
    var isValid = validator.validate('testfield', validatorDef, validationModel);
    expect(isValid).to.be.true;
  });

  it('should return true if view value is of type string and converted to number is greater than expected number value', function() {
    var validationModel = util.buildValidationModel('testfield', '246');
    var isValid = validator.validate('testfield', validatorDef, validationModel);
    expect(isValid).to.be.true;
  });

  it('should return false if view value is not of type number and cannot be successfully converted to number', function() {
    var validationModel = util.buildValidationModel('testfield', 'test246');
    var isValid = validator.validate('testfield', validatorDef, validationModel);
    expect(isValid).to.be.false;
  });

  it('should return false if view value is of type number and is equal to expected value', function() {
    var validationModel = util.buildValidationModel('testfield', 123);
    var isValid = validator.validate('testfield', validatorDef, validationModel);
    expect(isValid).to.be.false;
  });

  it('should return false if view value is of type number and is lower than expected value', function() {
    var validationModel = util.buildValidationModel('testfield', 100);
    var isValid = validator.validate('testfield', validatorDef, validationModel);
    expect(isValid).to.be.false;
  });

  it('should return false if view value is not defined', function() {
    var validationModel = util.buildValidationModel('testfield', null);
    var isValid = validator.validate('testfield', validatorDef, validationModel);
    expect(isValid).to.be.false;
  });
});