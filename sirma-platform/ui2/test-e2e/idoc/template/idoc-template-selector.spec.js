var IdocTemplateSelectorSandboxPage = require('./idoc-template-selector').IdocTemplateSelectorSandboxPage;

describe('IdocTemplateSelectorSandboxPage', function () {
  var page = new IdocTemplateSelectorSandboxPage();

  beforeEach(() => {
    // Given I have opened the template dialog
    page.open();
  });

  it('should call event handler when template is selected', function () {
    // When I select template
    page.getTemplateSelector().selectOption("Test Document Template");
    // I should see that template gets applied
    browser.wait(EC.textToBePresentInElement(page.getSelectedTemplate(), 'testDocument'), DEFAULT_TIMEOUT);
  });

  it('should reload the templates when the filter criteria gets changes', function () {
    var templateSelector = page.getTemplateSelector();

    // I should see the Test Document Template option but should not see Common Document Template
    templateSelector.getMenuValues().then(function (values) {
      expect(values).contain('testDocument');
      expect(values).to.not.contain('commonDocument');
    });

    // When I select template
    templateSelector.selectOption('Test Document Template');

    // And uncheck the 'active' checkbox
    page.getActiveCheckbox().click();
    browser.wait(EC.textToBePresentInElement(page.getSelectedTemplate(), 'blank-instance-template-id'), DEFAULT_TIMEOUT);
    // Then I should see the Common Document Template option but should not see Test Document Template
    templateSelector.getMenuValues().then(function (values) {
      expect(values).contain('commonDocument');
      expect(values).to.not.contain('testDocument');
    });

    // And I should be able to select another template that was not present when the checkbox was checked
    templateSelector.selectOption('Common Document Template');

    browser.wait(EC.textToBePresentInElement(page.getSelectedTemplate(), 'commonDocument'), DEFAULT_TIMEOUT);
  });

  it('should reselect selected template when templates are reloaded', function () {
    var templateSelector = page.getTemplateSelector();

    // I should see the Test Document Template option but should not see Common Document Template
    expect(templateSelector.getMenuValues()).to.eventually.contain('testDocument');
    expect(templateSelector.getMenuValues()).to.eventually.contain('commonDocument2');
    expect(templateSelector.getMenuValues()).to.eventually.not.contain('commonDocument');

    // When I select template
    templateSelector.selectOption('Common Document Template 2');

    // And uncheck the 'active' checkbox
    page.getActiveCheckbox().click();

    // Then I should see the Common Document Template 2 is selected
    browser.wait(EC.textToBePresentInElement(page.getSelectedTemplate(), 'commonDocument2'), DEFAULT_TIMEOUT);

    browser.wait(EC.presenceOf($('.seip-select option[value="commonDocument"]')), DEFAULT_TIMEOUT);

    // Then I should see the Common Document Template option but should not see Test Document Template
    expect(templateSelector.getMenuValues()).to.eventually.contain('commonDocument2');
    expect(templateSelector.getMenuValues()).to.eventually.contain('commonDocument');
    expect(templateSelector.getMenuValues()).to.eventually.not.contain('testDocument');

  });

  it('should set default template when templates are reloaded and previously selected template is not present', function () {
    var templateSelector = page.getTemplateSelector();

    // I should see the Test Document Template option but should not see Common Document Template
    templateSelector.getMenuValues().then(function (values) {
      expect(values).contain('testDocument');
      expect(values).contain('commonDocument2');
      expect(values).to.not.contain('commonDocument');
    });

    // When I select template
    templateSelector.selectOption('Test Document Template');

    // And uncheck the 'active' checkbox
    page.getActiveCheckbox().click();

    // Then I should see the Blank Template is selected
    browser.wait(EC.textToBePresentInElement(page.getSelectedTemplate(), 'blank-instance-template-id'), DEFAULT_TIMEOUT);

    // Then I should see the Common Document Template option but should not see Test Document Template
    templateSelector.getMenuValues().then(function (values) {
      expect(values).contain('commonDocument');
      expect(values).contain('commonDocument2');
      expect(values).to.not.contain('testDocument');
    });
  });
});
