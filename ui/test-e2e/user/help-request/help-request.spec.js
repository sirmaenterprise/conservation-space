var HelpRequestSandbox = require('./help-request').HelpRequestSandbox;

describe('HelpRequest', ()=> {
  var page = new HelpRequestSandbox();

  beforeEach(() => {
    page.open();
  });

  it('should open help-request dialog', () => {
    var dialog = page.openDialog();
    expect(dialog.isPresent()).to.eventually.be.true;
    expect(element(by.id('subject-wrapper')).isDisplayed()).to.eventually.be.true;
    expect(element(by.id('type-wrapper')).isDisplayed()).to.eventually.be.true;
    expect(element(by.id('message-wrapper')).isDisplayed()).to.eventually.be.false;
  });

  it('should check all conditions of send button to be enable', () => {
    var dialog = page.openDialog();
    expect(dialog.isSendButtonEnable(), 'all fields are emty').to.eventually.be.false;
    dialog.setSubject("Subject of the request");
    expect(dialog.isSendButtonEnable(), 'subject is filled').to.eventually.be.false;
    dialog.selectOption("Question");
    expect(dialog.isSendButtonEnable(), 'subject and type are filled').to.eventually.be.false;
    dialog.addContentToEditor("Detailed description of the request");
    expect(dialog.isSendButtonEnable(), 'all fields are filed').to.eventually.be.true;

    dialog.clearSubject();
    expect(dialog.isSendButtonEnable(), 'only subject is not filled').to.eventually.be.false;
    dialog.setSubject("Subject of the request");
    expect(dialog.isSendButtonEnable(), 'all fields are filed').to.eventually.be.true;

    dialog.clearEditor();
    expect(dialog.isSendButtonEnable(), 'only description is not filled').to.eventually.be.false;
    dialog.addContentToEditor("Detailed description of the request");
    expect(dialog.isSendButtonEnable(), 'all fields are filed').to.eventually.be.true;

    dialog.clearOption();
    expect(dialog.isSendButtonEnable(), 'only type is not filled').to.eventually.be.false;
  });

  it('should close dialog after cancel button is clicked', () => {
    var dialog = page.openDialog();
    expect(dialog.isPresent()).to.eventually.be.true;
    dialog.cancel();
    expect(dialog.isPresent()).to.eventually.be.false;
  });
});