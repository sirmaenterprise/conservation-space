import {LessStringLengthThan} from 'form-builder/validation/less-string-length-than/less-string-length-than';
import util from '../validator-test-util';

describe('LessStringLengthThan', function() {

  let validator = new LessStringLengthThan();

  it('should return true if view value length is less than expected', function() {
    let validatorDef = util.buildValidatorDefinition(10);
    let validationModel = util.buildValidationModel('testfield', 'test');
    let isValid = validator.validate('testfield', validatorDef, validationModel);
    expect(isValid).to.be.true;
  });

  it('should return false if view is greater than expected', function() {
    let validatorDef = util.buildValidatorDefinition(4);
    let validationModel = util.buildValidationModel('testfield', 'john doe');
    let isValid = validator.validate('testfield', validatorDef, validationModel);
    expect(isValid).to.be.false;
  });

  it('should return false if view value length is the same as expected', function() {
    let validatorDef = util.buildValidatorDefinition(5);
    let validationModel = util.buildValidationModel('testfield', 'test1');
    let isValid = validator.validate('testfield', validatorDef, validationModel);
    expect(isValid).to.be.false;
  });

  it('should return false if view is not a string', function() {
    let validatorDef = util.buildValidatorDefinition(5);
    let validationModel = util.buildValidationModel('testfield', 123);
    let isValid = validator.validate('testfield', validatorDef, validationModel);
    expect(isValid).to.be.false;
  });

  it('should return false if view is undefined', function() {
    let validatorDef = util.buildValidatorDefinition(5);
    let validationModel = util.buildValidationModel('testfield');
    let isValid = validator.validate('testfield', validatorDef, validationModel);
    expect(isValid).to.be.false;
  });

  it('should return false if view is zero length', function() {
    let validatorDef = util.buildValidatorDefinition(5);
    let validationModel = util.buildValidationModel('testfield', '');
    let isValid = validator.validate('testfield', validatorDef, validationModel);
    expect(isValid).to.be.false;
  });

});