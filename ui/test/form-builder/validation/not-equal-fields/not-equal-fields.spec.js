import {NotEqualFields} from 'form-builder/validation/not-equal-fields/not-equal-fields';
import util from '../validator-test-util';

describe('NotEqualFields', function () {

  let validator = new NotEqualFields();
  let validatorDef = util.buildValidatorDefinition('testfield2');

  it('should return true if string field value is different from expected', function () {
    let validationModel = util.buildValidationModel('testfield1', '1234');
    util.buildValidationModel('testfield2', 'test', validationModel);

    let isValid = validator.validate('testfield1', validatorDef, validationModel);
    expect(isValid).to.be.true;
  });

  it('should return true if string field value is different from expected number value', function () {
    let validationModel = util.buildValidationModel('testfield1', '1234');
    util.buildValidationModel('testfield2', 1234, validationModel);

    let isValid = validator.validate('testfield1', validatorDef, validationModel);
    expect(isValid).to.be.true;
  });

  it('should return false if string field value is same as expected string field value', function () {
    let validationModel = util.buildValidationModel('testfield1', 'test');
    util.buildValidationModel('testfield2', 'test', validationModel);

    let isValid = validator.validate('testfield1', validatorDef, validationModel);
    expect(isValid).to.be.false;
  });

  it('should return false if number field value is same as expected number field value', function () {
    let validationModel = util.buildValidationModel('testfield1', 654);
    util.buildValidationModel('testfield2', 654, validationModel);

    let isValid = validator.validate('testfield1', validatorDef, validationModel);
    expect(isValid).to.be.false;
  });

  it('should return true if other field value is undefined', function () {
    let validationModel = util.buildValidationModel('testfield1', 654);
    util.buildValidationModel('testfield2', undefined, validationModel);

    let isValid = validator.validate('testfield1', validatorDef, validationModel);
    expect(isValid).to.be.true;
  });

  it('should return false if the two fields are undefined', function () {
    let validationModel = util.buildValidationModel('testfield1');
    util.buildValidationModel('testfield2', undefined, validationModel);

    let isValid = validator.validate('testfield1', validatorDef, validationModel);
    expect(isValid).to.be.true;
  });

  it('should return false if the two fields are empty strings', function () {
    let validationModel = util.buildValidationModel('testfield1', '');
    util.buildValidationModel('testfield2', '', validationModel);

    let isValid = validator.validate('testfield1', validatorDef, validationModel);
    expect(isValid).to.be.true;
  });

  it('should return true if expected value is empty', function () {
    let validationModel = util.buildValidationModel('testfield1', '');
    util.buildValidationModel('testfield2', 'test', validationModel);
    validatorDef = util.buildValidatorDefinition('');

    let isValid = validator.validate('testfield1', validatorDef, validationModel);
    expect(isValid).to.be.true;
  });

});