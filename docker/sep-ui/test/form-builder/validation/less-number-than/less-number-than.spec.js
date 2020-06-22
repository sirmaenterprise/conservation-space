import {LessNumberThan} from 'form-builder/validation/less-number-than/less-number-than';
import util from '../validator-test-util';

describe('LessNumberThan validator', function() {

  var validator = new LessNumberThan();
  var validatorDef = util.buildValidatorDefinition(123);

  it('should return true if view value of type number is less than expected number value', function() {
    var validationModel = util.buildValidationModel('testfield', 100);
    var isValid = validator.validate('testfield', validatorDef, validationModel);
    expect(isValid).to.be.true;
  });

  it('should return true if view value of type string converted to number is less than expected number value', function() {
    var validationModel = util.buildValidationModel('testfield', '100');
    var isValid = validator.validate('testfield', validatorDef, validationModel);
    expect(isValid).to.be.true;
  });

  it('should return false if view value of type number is equal to expected number value', function() {
    var validationModel = util.buildValidationModel('testfield', 123);
    var isValid = validator.validate('testfield', validatorDef, validationModel);
    expect(isValid).to.be.false;
  });

  it('should return false if view value of type number is greater than expected number value', function() {
    var validationModel = util.buildValidationModel('testfield', 234);
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