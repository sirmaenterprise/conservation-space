var DatetimeField = require('../../form-builder/form-control.js').DatetimeField;
var SandboxPage = require('../../page-object').SandboxPage;
var hasClass = require('../../test-utils').hasClass;

var page = new SandboxPage();

describe('Test datetime picker', function () {

  var dateTimeField = new DatetimeField();

  beforeEach(() => {
    page.open('/sandbox/components/datetimepicker');
  });

  it.skip('should have different pickers (datetime, date, time)', function () {
    // Datetime picker
    var triggerButton = dateTimeField.getTriggerButton('#datetime-picker-1');
    var icon = triggerButton.$('span')

    expect(hasClass(icon, 'fa-calendar')).to.eventually.be.true;
    expect(hasClass(icon, 'fa-clock-o')).to.eventually.be.false;

    triggerButton.click();
    browser.wait(EC.presenceOf($('.bootstrap-datetimepicker-widget a[data-action="togglePicker"]')), DEFAULT_TIMEOUT);
    browser.actions().sendKeys(protractor.Key.ESCAPE).perform();

    // Date picker only
    triggerButton = triggerButton = dateTimeField.getTriggerButton('#datetime-picker-2');
    icon = triggerButton.$('span')

    expect(hasClass(icon, 'fa-calendar')).to.eventually.be.true;
    expect(hasClass(icon, 'fa-clock-o')).to.eventually.be.false;

    triggerButton.click();
    browser.wait(EC.presenceOf($('.bootstrap-datetimepicker-widget')), DEFAULT_TIMEOUT);
    browser.wait(EC.not(EC.presenceOf($('.bootstrap-datetimepicker-widget a[data-action="togglePicker"]'))), DEFAULT_TIMEOUT);
    browser.actions().sendKeys(protractor.Key.ESCAPE).perform();

    // Time picker only
    triggerButton = triggerButton = dateTimeField.getTriggerButton('#datetime-picker-3');
    icon = triggerButton.$('span')

    expect(hasClass(icon, 'fa-calendar')).to.eventually.be.false;
    expect(hasClass(icon, 'fa-clock-o')).to.eventually.be.true;

    triggerButton.click();
    browser.wait(EC.presenceOf($('.bootstrap-datetimepicker-widget')), DEFAULT_TIMEOUT);
    browser.wait(EC.not(EC.presenceOf($('.bootstrap-datetimepicker-widget a[data-action="togglePicker"]'))), DEFAULT_TIMEOUT);
  });

  it('should display a placeholder', function () {
    var placeholder = dateTimeField.getAttributeValue('#datetime-picker-4 input', 'placeholder');
    expect(placeholder).to.eventually.equal('Choose date');
  });
});