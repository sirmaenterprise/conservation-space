import {EqualFields} from 'form-builder/validation/equal-fields/equal-fields';
import util from '../validator-test-util';

describe('EqualFields', function () {

  let validator = new EqualFields();
  let validatorDef = util.buildValidatorDefinition('testfield2');

  it('should return true if another field string value is equal to expected field string value', function () {
    let validationModel = util.buildValidationModel('testfield1', 'test');
    util.buildValidationModel('testfield2', 'test', validationModel);

    let isValid = validator.validate('testfield1', validatorDef, validationModel);
    expect(isValid).to.be.true;
  });

  it('should return false if another field string value is not equal to expected field string value', function () {
    let validationModel = util.buildValidationModel('testfield1', 'test');
    util.buildValidationModel('testfield2', 'pass', validationModel);

    let isValid = validator.validate('testfield1', validatorDef, validationModel);
    expect(isValid).to.be.false;
  });

  it('should return true if another field number value is equal to expected field number value', function () {
    let validationModel = util.buildValidationModel('testfield1', 123);
    util.buildValidationModel('testfield2', 123, validationModel);

    let isValid = validator.validate('testfield1', validatorDef, validationModel);
    expect(isValid).to.be.true;
  });

  it('should return false if another field number value is not equal to expected field number value', function () {
    let validationModel = util.buildValidationModel('testfield1', 123);
    util.buildValidationModel('testfield2', 123456, validationModel);

    let isValid = validator.validate('testfield1', validatorDef, validationModel);
    expect(isValid).to.be.false;
  });

  it('should return false if another field number value is not equal to other field string value', function () {
    let validationModel = util.buildValidationModel('testfield1', '123');
    util.buildValidationModel('testfield2', 123456, validationModel);

    let isValid = validator.validate('testfield1', validatorDef, validationModel);
    expect(isValid).to.be.false;
  });

  it('should return false if view value is not defined', function () {
    let validationModel = util.buildValidationModel('testfield1');
    util.buildValidationModel('testfield2', null, validationModel);

    let isValid = validator.validate('testfield1', validatorDef, validationModel);
    expect(isValid).to.be.false;
  });

  it('should return true if two field values are empty strings', function () {
    let validationModel = util.buildValidationModel('testfield1', '');
    util.buildValidationModel('testfield2', '', validationModel);

    let isValid = validator.validate('testfield1', validatorDef, validationModel);
    expect(isValid).to.be.true;
  });

  it('should return true if two field values are undefined', function () {
    let validationModel = util.buildValidationModel('testfield1');
    util.buildValidationModel('testfield2', undefined, validationModel);

    let isValid = validator.validate('testfield1', validatorDef, validationModel);
    expect(isValid).to.be.true;
  });

  it('should return false if the expected value is empty', function () {
    let validationModel = util.buildValidationModel('testfield1');
    validatorDef = util.buildValidatorDefinition('');
    util.buildValidationModel('testfield2', 'test', validationModel);

    let isValid = validator.validate('testfield1', validatorDef, validationModel);
    expect(isValid).to.be.false;
  });

});