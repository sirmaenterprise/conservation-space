var Dialog = require('./dialog');
var ContextualHelp = require('../help/contextual-help').ContextualHelp;
var SandboxPage = require('../../page-object').SandboxPage;
var hasClass = require('../../test-utils').hasClass;

var page = new SandboxPage();

describe('Dialog service', function () {

  var modalDialogElement;
  var modalDialog;

  beforeEach(() => {
    page.open('/sandbox/components/dialog');
  });

  afterEach(() => {
    browser.wait(EC.stalenessOf(modalDialogElement), DEFAULT_TIMEOUT);
  });

  it('should show error dialog', function () {
    element(by.buttonText('Error')).click();
    modalDialogElement = element(by.className('modal'));
    modalDialog = new Dialog(modalDialogElement);
    modalDialog.waitUntilOpened();

    expect(modalDialog.isPresent()).to.eventually.be.true;
    var modalTitle = modalDialog.getTitleElement();
    expect(modalTitle.getText()).to.eventually.contain('Error!');
    var modalBody = modalDialog.getBodyElement();
    expect(modalBody.getText()).to.eventually.contain('Error message');
    var buttons = modalDialog.getButtons();
    expect(buttons).to.eventually.have.length(1);
    modalDialog.closeModal();
  });

  it('should show confirm dialog', function () {
    element(by.buttonText('Confirm')).click();
    modalDialogElement = element(by.className('modal'));
    modalDialog = new Dialog(modalDialogElement);
    modalDialog.waitUntilOpened();

    expect(modalDialog.isPresent()).to.eventually.be.true;
    var modalTitle = modalDialog.getTitleElement();
    expect(modalTitle.getText()).to.eventually.contain('Confirm');
    var modalBody = modalDialog.getBodyElement();
    expect(modalBody.getText()).to.eventually.contain('Confirmation message');
    var buttons = modalDialog.getButtons();
    expect(buttons).to.eventually.have.length(3);
    modalDialog.closeModal();
  });

  it('should show notify dialog', function () {
    element(by.buttonText('Notify')).click();
    modalDialogElement = element(by.className('modal'));
    modalDialog = new Dialog(modalDialogElement);
    modalDialog.waitUntilOpened();

    expect(modalDialog.isPresent()).to.eventually.be.true;
    var modalTitle = modalDialog.getTitleElement();
    expect(modalTitle.getText()).to.eventually.contain('Notification');
    var modalBody = modalDialog.getBodyElement();
    expect(modalBody.getText()).to.eventually.contain('Notification message');
    var buttons = modalDialog.getButtons();
    expect(buttons).to.eventually.have.length(1);
    modalDialog.closeModal();
  });

  it('should show custom component dialog by selector', function () {
    element(by.buttonText('Custom component by selector')).click();
    modalDialogElement = element(by.className('modal'));
    modalDialog = new Dialog(modalDialogElement);
    modalDialog.waitUntilOpened();

    expect(modalDialog.isPresent()).to.eventually.be.true;
    var modalTitle = modalDialog.getTitleElement();
    expect(modalTitle.getText()).to.eventually.contain('Custom component dialog');
    var buttons = modalDialog.getButtons();
    expect(buttons).to.eventually.have.length(2);
    // Verify content
    var triggerButton = element.all(by.css('#datetime-picker-1 span')).first();
    var icon = triggerButton.element(by.tagName('span'));
    expect(hasClass(icon, 'fa-calendar')).to.eventually.be.false;
    expect(hasClass(icon, 'fa-clock-o')).to.eventually.be.true;
    // modal is closed via the X button on the top right corner
    modalDialog.closeModal();
    // but it should be handled by the buttonClicked handler, which changes the info-span.
    // CMF-21668
    expect(element(by.css('.info-span')).getText()).to.eventually.equal('CANCEL');
  });

  it('should show custom component dialog by component', function () {
    element(by.buttonText('Custom component by component')).click();
    modalDialog = new Dialog(modalDialogElement);
    modalDialog.waitUntilOpened();

    expect(modalDialog.isPresent()).to.eventually.be.true;
    var modalTitle = modalDialog.getTitleElement();
    expect(modalTitle.getText()).to.eventually.contain('Custom component dialog');
    var buttons = modalDialog.getButtons();
    expect(buttons).to.eventually.have.length(1);
    // Verify content
    var triggerButton = element.all(by.css('#datetime-picker-2 span')).first();
    icon = triggerButton.element(by.tagName('span'));
    expect(hasClass(icon, 'fa-calendar')).to.eventually.be.true;
    expect(hasClass(icon, 'fa-clock-o')).to.eventually.be.false;
    modalDialog.closeModal();
  });

  it('should show dialog without a header', function () {
    element(by.buttonText('No header dialog')).click();
    modalDialogElement = element(by.className('modal'));
    modalDialog = new Dialog(modalDialogElement);
    modalDialog.waitUntilOpened();

    expect(modalDialog.isPresent()).to.eventually.be.true;
    var modalHeader = modalDialog.getHeaderElement();
    browser.wait(EC.invisibilityOf(modalHeader), DEFAULT_TIMEOUT);
    modalDialog.getCloseButton().click();
  });

  it('should close existing dialogs on route change', function () {
    // When I open the dialog
    element(by.buttonText('No header dialog')).click();
    modalDialogElement = element(by.className('modal'));
    modalDialog = new Dialog(modalDialogElement);
    modalDialog.waitUntilOpened();

    // And I change the route (the link will be overlayed by the dialog)
    browser.executeScript('$("#view1-link").click(); $("#view1-link").trigger("click")');

    // Then I should not see the modal panel
    browser.wait(EC.invisibilityOf(modalDialogElement), DEFAULT_TIMEOUT);
    browser.wait(EC.not(EC.presenceOf(modalDialogElement)), DEFAULT_TIMEOUT);
  });

  it('should support modeless dialog', function () {
    // When I open the modeless dialog
    element(by.buttonText('Modeless dialog')).click();
    modalDialogElement = element(by.className('modal'));
    modalDialog = new Dialog(modalDialogElement);
    modalDialog.waitUntilOpened();

    // Then the dialog should be draggable
    browser.wait(EC.presenceOf($('.ui-draggable.modal-dialog')), DEFAULT_TIMEOUT);

    // And the button for modeless dialog should still be clickable
    browser.wait(EC.visibilityOf(element(by.buttonText('Error'))), DEFAULT_TIMEOUT);
    // For some reason the browser cannot click on the close button
    browser.executeScript('$(".close").click()');
  });

  it('should open second modal and maintain the ability to scroll in the modal', ()=> {
    var body = element(by.tagName('body'));
    element(by.buttonText('Confirm')).click();
    modalDialogElement = element(by.className('modal'));
    modalDialog = new Dialog(modalDialogElement);
    modalDialog.waitUntilOpened();
    expect(hasClass(body, 'modal-open')).to.eventually.be.true;
    modalDialog.closeModal();
    expect(hasClass(body, 'modal-open')).to.eventually.be.false;
    element(by.buttonText('Custom component by selector')).click();
    modalDialogElement = element(by.className('modal'));
    modalDialog = new Dialog(modalDialogElement);
    modalDialog.waitUntilOpened();
    expect(hasClass(body, 'modal-open')).to.eventually.be.true;
    modalDialog.closeModal();
  });

  it('should render a contextual help in the dialog header if provided with a help target', () => {
    element(by.buttonText('Dialog with a contextual help')).click();
    modalDialogElement = element(by.className('modal'));
    modalDialog = new Dialog(modalDialogElement);
    modalDialog.waitUntilOpened();

    var helpElement = modalDialog.getHelpElement();
    var helpComponent = new ContextualHelp(helpElement);
    expect(helpComponent.isRendered()).to.eventually.be.true;

    modalDialog.closeModal();
  });

  it('should render a warning message in the dialog header if provided with configuration', () => {
    element(by.buttonText('Dialog with a warning')).click();
    modalDialogElement = element(by.className('modal'));
    modalDialog = new Dialog(modalDialogElement);
    modalDialog.waitUntilOpened();

    var messageElement = modalDialog.getWarningMessageElement();
    var popoverBody = modalDialog.getWarningPopoverBody();
    var popoverTitle = modalDialog.getWarningPopoverTitle();

    expect(messageElement.isPresent()).to.eventually.be.true;
    expect(popoverTitle).to.eventually.eq('This is a popover title');
    expect(popoverBody).to.eventually.eq('This is a popover body message');
    modalDialog.closeModal();
  });
});


