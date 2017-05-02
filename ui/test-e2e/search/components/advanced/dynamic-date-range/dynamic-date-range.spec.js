var DynamicDateRangeSandboxPage = require('./dynamic-date-range.js').DynamicDateRangeSandboxPage;
var DynamicDateRange = require('./dynamic-date-range.js').DynamicDateRange;

describe('DynamicDateRange', () => {

  var defaultForm;
  var page = new DynamicDateRangeSandboxPage();

  beforeEach(() => {
    page.open();
    defaultForm = page.getDefaultForm();
  });

  it('should allow to select different date steps', () => {
    return defaultForm.dateStepSelectMenu.getMenuValues().then((availableDateSteps) => {
      expect(availableDateSteps).to.deep.equal(['today', 'next', 'last', 'after', 'before']);
    });
  });

  describe('When the form is rendered with default offsets and configuration', () => {
    it('should render the form as enabled', () => {
      expect(defaultForm.isDisabled()).to.eventually.be.false;
    });

    it('should configure the form for "Today" by default', () => {
      expect(defaultForm.getSelectedDateStep()).to.eventually.equal('today');
    });

    it('should not render offset input and offset type select', () => {
      expect(defaultForm.dateOffsetInputElement.isDisplayed()).to.eventually.be.false;
      expect(defaultForm.dateOffsetTypeSelectElement.isDisplayed()).to.eventually.be.false;
    });
  });

  describe('When the date step is different than today', () => {
    it('should allow to enter a date offset', () => {
      return defaultForm.selectDateStep('next').then(() => {
        return expect(defaultForm.dateOffsetInputElement.isDisplayed()).to.eventually.be.true;
      });
    });

    it('should allow to choose different date offset types', () => {
      return defaultForm.selectDateStep('next').then(() => {
        return expect(defaultForm.dateOffsetTypeSelectElement.isDisplayed()).to.eventually.be.true;
      }).then(() => {
        return defaultForm.dateOffsetTypeSelectMenu.getMenuValues();
      }).then((availableDateOffsetTypes) => {
        expect(availableDateOffsetTypes).to.deep.equal(['hours', 'days', 'weeks', 'months', 'years']);
      });
    });
  });

  describe('When the form is disabled', () => {
    it('should render the form as disabled', () => {
      return page.toggleDefaultFormState().then(() => {
        return expect(defaultForm.isDisabled()).to.eventually.be.true;
      });
    });
  });

  describe('When initialized with predefined offsets and configuration', () => {
    it('should render the form with the predefined date offset config', () => {
      var predefinedForm = page.getPredefinedForm();
      return Promise.all([
        predefinedForm.getSelectedDateStep(),
        predefinedForm.getDateOffsetValue(),
        predefinedForm.getSelectedDateOffsetType()
      ]).then((offsetConfig) => {
        return expect(offsetConfig).to.deep.equal(['next','5','weeks']);
      });
    });

    it('should render the form as disabled', () => {
      var predefinedForm = page.getPredefinedForm();
      return expect(predefinedForm.isDisabled()).to.eventually.be.true;
    });
  });
});