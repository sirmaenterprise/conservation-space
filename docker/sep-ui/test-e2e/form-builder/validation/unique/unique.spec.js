var FormControl = require('./../../form-control.js').FormControl;
var SandboxPage = require('../../../page-object').SandboxPage;
var InputField = require('./../../form-control.js').InputField;

const NOT_UNIQUE_VALUE = 'Title';
const UNIQUE_FIELD_SELECTOR = '#selected_1';

var page = new SandboxPage();

describe('Unique validator', () => {

  var uniqueField;
  var uniqueFieldWrapper;

  beforeEach(() => {
    uniqueField = new InputField($(UNIQUE_FIELD_SELECTOR));
    uniqueFieldWrapper = new FormControl($(`${UNIQUE_FIELD_SELECTOR}-wrapper`));
    page.open('sandbox/form-builder/validation/unique');
    browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
  });

  describe('when field is not unique', () => {
    it('should show error', () => {
      uniqueField.setValue(UNIQUE_FIELD_SELECTOR, NOT_UNIQUE_VALUE);
      expect(uniqueFieldWrapper.hasError()).to.eventually.be.true;
    });
  });

  describe('when field is unique', () => {
    it('should not show error', () => {
      uniqueField.setValue(UNIQUE_FIELD_SELECTOR, 'asdf');
      expect(uniqueFieldWrapper.hasError()).to.eventually.be.false;
    });
  });

});