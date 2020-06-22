var SaveAsTemplateSandboxPage = require('./save-as-template').SaveAsTemplateSandboxPage;

describe('SaveAsTemplateAction', () => {

  var page = new SaveAsTemplateSandboxPage();
  var dialog;

  beforeEach(() => {
    // Given I have opened the template dialog
    dialog = page.open().openDialog();
  });

  it('should allow template saving', () => {
    const TITLE = 'Primary Tag template';

    // Then the type of the "current object" has to be selected as default
    browser.wait(function () {
      return dialog.getObjectType().getSelectedLabel().then(function (value) {
        return value == 'Common document';
      });
    }, DEFAULT_TIMEOUT);

    // When I select template type
    dialog.getObjectType().selectOption('Tag');

    // And enter valid title
    dialog.enterTitle(TITLE);

    // And select template purpose
    dialog.getPimaryButton().click();

    // And select "uploadable"
    dialog.getPimaryButton().click();

    // title should be valid
    browser.wait(EC.not(EC.presenceOf(dialog.getTitleMessage())), DEFAULT_TIMEOUT);

    // And select the Ok button
    dialog.getOkButton().click();

    // And a success notification should be displayed
    let notification = page.getNotification();

    expect(notification.getMessage()).to.eventually.contains(TITLE);
    expect(notification.isSuccess()).to.eventually.be.true;

    // Then the template should be saved
    browser.executeScript('return window.savedTemplate').then(function (templateData) {
      expect(templateData).to.eql({
        forType: 'tag',
        title: TITLE,
        primary: false,
        purpose: 'creatable',
        sourceInstance: 'instanceId'
      });
    });

    // And the template dialog is closed
    dialog.waitUntilClosed();
  });

  it('should allow closing the config dialog', () => {
    dialog.cancel();
  });

  it('should not allow template saving when title is not provided', () => {
    dialog.enterTitle('');
    browser.wait(EC.not(EC.elementToBeClickable(dialog.getOkButton())), DEFAULT_TIMEOUT);
    browser.wait(EC.presenceOf(dialog.getTitleMessage()), DEFAULT_TIMEOUT);
  });

  it('should not allow selecting a type without definition', () => {
    dialog.getObjectType().open();
    expect(dialog.getObjectType().isOptionDisabled('Document')).to.eventually.be.true;
  });

  it('should not allow template saving and display message when title is not valid', () => {
    dialog.enterTitle('notUnique');

    browser.wait(EC.presenceOf(dialog.getTitleMessage()), DEFAULT_TIMEOUT);

    browser.wait(EC.not(EC.elementToBeClickable(dialog.getOkButton())), DEFAULT_TIMEOUT);
  });
});