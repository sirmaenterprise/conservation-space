import {DisabledIfEqual} from 'form-builder/validation/disabled-if-equal/disabled-if-equal';
import util from '../validator-test-util';

describe('DisabledIfEqual validator', function() {

  var validator = new DisabledIfEqual();
  var validatorDef = util.buildValidatorDefinition({
    target: 'testfield2'
  });

  it('should return false if the fields are equal so the message to be shown', function() {
    var validationModel = {};
    util.buildValidationModel('testfield1', 'test', validationModel);
    util.buildValidationModel('testfield2', 'test', validationModel);
    var flatViewModel = util.buildFlatViewModel('testfield1', 'disabled', null);
    var isValid = validator.validate('testfield1', validatorDef, validationModel, flatViewModel);
    expect(isValid).to.be.false;
  });

  it('should return true if the fields are not equal so the message to be hidden', function() {
    var validationModel = {};
    util.buildValidationModel('testfield1', 'test', validationModel);
    util.buildValidationModel('testfield2', '123', validationModel);
    var flatViewModel = util.buildFlatViewModel('testfield1', 'disabled', null);
    var isValid = validator.validate('testfield1', validatorDef, validationModel, flatViewModel);
    expect(isValid).to.be.true;
  });

  it('should set the current field as disabled', function() {
    var validationModel = {};
    util.buildValidationModel('testfield1', 'test', validationModel);
    util.buildValidationModel('testfield2', 'test', validationModel);
    var flatViewModel = util.buildFlatViewModel('testfield1', 'disabled', null);
    validator.validate('testfield1', validatorDef, validationModel, flatViewModel);
    expect(flatViewModel.testfield1.disabled).to.be.true;
  });

  it('should set the current field as enabled', function() {
    var validationModel = {};
    util.buildValidationModel('testfield1', 'test', validationModel);
    util.buildValidationModel('testfield2', '123', validationModel);
    var flatViewModel = util.buildFlatViewModel('testfield1', 'disabled', null);
    validator.validate('testfield1', validatorDef, validationModel, flatViewModel);
    expect(flatViewModel.testfield1.disabled).to.be.false;
  });

});