import {RegexPlain} from 'form-builder/validation/regex-plain/regex-plain';

describe('RegexPlain validator', function() {

  let validator = new RegexPlain();
  let validatorDef = {
    id: 'RegexPlain',
    context: {
      pattern: null
    }
  };
  let validationModelNoValue = {
    'testfield': {
      value: null
    }
  };
  let validationModelWithValue = {
    'testfield': {
      value: 'test'
    }
  };
  let flatViewModel = {
    'testfield': {}
  };

  it('should throw error if no validation pattern is provided', () => {
    expect(() => { validator.validate('testfield', validatorDef, validationModelWithValue) }).to.throw(Error);
  });

  it('should throw error if invalid regex pattern is found for a field', () => {
    validatorDef.context.pattern = '((.{1,4}';
    expect(() => { validator.validate('testfield', validatorDef, validationModelWithValue) }).to.throw(Error);
  });

  it('should put every new pattern inside the regex cache', () => {
    validatorDef.context.pattern = '[\\s\\S]{1,4}';
    validator.validate('testfield', validatorDef, validationModelWithValue);
    validatorDef.context.pattern = '.{1,4}';
    validator.validate('testfield', validatorDef, validationModelWithValue);
    expect(validator.getRegexCache()).to.have.all.keys('[\\s\\S]{1,4}', '.{1,4}');
  });

  it('should not duplicate patterns in the regex cache', () => {
    validatorDef.context.pattern = '[\\s\\S]{1,4}';
    validator.validate('testfield', validatorDef, validationModelWithValue);
    validatorDef.context.pattern = '.{1,4}';
    validator.validate('testfield', validatorDef, validationModelWithValue);
    validatorDef.context.pattern = '[\\s\\S]{1,4}';
    validator.validate('testfield', validatorDef, validationModelWithValue);
    expect(validator.getRegexCache()).to.have.all.keys('[\\s\\S]{1,4}', '.{1,4}');
  });

  it('should return true if value is matching a pattern', () => {
    validatorDef.context.pattern = '[\\s\\S]{1,4}';
    validationModelNoValue.testfield.value = 'a bc';
    let isValid = validator.validate('testfield', validatorDef, validationModelNoValue);
    expect(isValid).to.true;
  });

  it('should return false if value is not matching a pattern', () => {
    validatorDef.context.pattern = '[\\s\\S]{1,4}';
    validationModelNoValue.testfield.value = 'a bcdeqw';
    let isValid = validator.validate('testfield', validatorDef, validationModelNoValue);
    expect(isValid).to.false;
  });

  it('should mark the field with wasInvalid flag the first time it becomes invalid', () => {
    validatorDef.context.pattern = '[\\s\\S]{1,4}';

    validator.validate('testfield', validatorDef, validationModelWithValue, flatViewModel);
    expect(validationModelWithValue.testfield._wasInvalid).to.be.undefined;

    validationModelWithValue.testfield.value = 'invalid value';
    validator.validate('testfield', validatorDef, validationModelWithValue, flatViewModel);
    expect(validationModelWithValue.testfield._wasInvalid).to.be.true;
  });

});