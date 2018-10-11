var DatetimeField = require('../../form-builder/form-control.js').DatetimeField;
var SandboxPage = require('../../page-object').SandboxPage;

var page = new SandboxPage();

describe('Test datetime picker', function () {

  var dateTimeField = new DatetimeField();

  beforeEach(() => {
    page.open('/sandbox/components/datetimepicker');
  });

  it('should autofill date on enter key press', function () {
    let todaysDate = element(by.id('todayFormatted'));
    let parent = element(by.id('datetime-picker-2'));
    let inputField = parent.element(by.tagName('input'));

    inputField.sendKeys(new Date().getDate());
    inputField.sendKeys(protractor.Key.ENTER);

    todaysDate.getText().then((text) => {
      expect(inputField.getAttribute('value')).to.eventually.equal(text);
    });
  });

  it('should not autofill date on enter key press if empty', function () {
    let parent = element(by.id('datetime-picker-2'));
    let inputField = parent.element(by.tagName('input'));

    inputField.sendKeys('');
    inputField.sendKeys(protractor.Key.ENTER);

    expect(inputField.getAttribute('value')).to.eventually.equal('');
  });

  it('should display a placeholder', function () {
    var placeholder = dateTimeField.getAttributeValue('#datetime-picker-4 input', 'placeholder');
    expect(placeholder).to.eventually.equal('Choose date');
  });
});