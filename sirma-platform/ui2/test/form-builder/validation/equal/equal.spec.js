import {Equal} from 'form-builder/validation/equal/equal';
import util from '../validator-test-util';

describe('Equal validator', function () {

  var validator = new Equal();

  it('should return true if view value of type string is equal to expected string value', function () {
    var validatorDef = util.buildValidatorDefinition('test');
    var validationModel = util.buildValidationModel('testfield', 'test');
    var isValid = validator.validate('testfield', validatorDef, validationModel);
    expect(isValid).to.be.true;
  });

  it('should return true if view value of type number is equal to expected number value', function () {
    var validatorDef = util.buildValidatorDefinition(123);
    var validationModel = util.buildValidationModel('testfield', 123);
    var isValid = validator.validate('testfield', validatorDef, validationModel);
    expect(isValid).to.be.true;
  });

  it('should return false if view value is of type number and expected value is other type', function () {
    var validatorDef = util.buildValidatorDefinition('123');
    var validationModel = util.buildValidationModel('testfield', 123);
    var isValid = validator.validate('testfield', validatorDef, validationModel);
    expect(isValid).to.be.false;
  });

  it('should return false if view value of type number is not equal to expected number value', function () {
    var validatorDef = util.buildValidatorDefinition(123);
    var validationModel = util.buildValidationModel('testfield', 321);
    var isValid = validator.validate('testfield', validatorDef, validationModel);
    expect(isValid).to.be.false;
  });

  it('should return false if view value of type string is not equal to expected string value', function () {
    var validatorDef = util.buildValidatorDefinition('test');
    var validationModel = util.buildValidationModel('testfield', 'test123');
    var isValid = validator.validate('testfield', validatorDef, validationModel);
    expect(isValid).to.be.false;
  });

  it('should return false if view value is not defined', function () {
    var validatorDef = util.buildValidatorDefinition('test');
    var validationModel = util.buildValidationModel('testfield', null);
    var isValid = validator.validate('testfield', validatorDef, validationModel);
    expect(isValid).to.be.false;
  });
});