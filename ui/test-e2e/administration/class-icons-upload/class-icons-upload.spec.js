var ClassIconsUploadSandboxPage = require('./class-icons-uploader').ClassIconsUploadSandboxPage;

var page = new ClassIconsUploadSandboxPage();

describe('ClassIconsUpload', ()=> {

  var dialog;
  beforeEach(() => {
    page.open();
    dialog = page.openDialog();
  });

  it('should open the icons upload dialog', ()=> {
    expect(dialog.getUploaders().count()).to.eventually.equal(4);
    expect(dialog.getOkButton().isDisplayed()).to.eventually.be.true;
    expect(dialog.getOkButton().isEnabled()).to.eventually.be.false;
    expect(dialog.getCancelButton().isDisplayed()).to.eventually.be.true;
    expect(dialog.getCancelButton().isDisplayed()).to.eventually.be.true;
  });

  it('should select the mandatory icons and enable their upload', () => {
    var uploader = dialog.getUploader(0);

    uploader.selectIcon(true);
    expect(uploader.getSelectedIcon().isDisplayed()).to.eventually.be.true;
    expect(dialog.getOkButton().isEnabled()).to.eventually.be.false;

    uploader = dialog.getUploader(3);
    uploader.selectIcon(true);
    expect(uploader.getSelectedIcon().isDisplayed()).to.eventually.be.true;
    expect(dialog.getOkButton().isEnabled()).to.eventually.be.true;
  });

  it('should select wrong file and show an error', () => {
    var uploader = dialog.getUploader(2);
    uploader.selectIcon(false);
    expect(uploader.getErrorMessage().isDisplayed()).to.eventually.be.true;
  });

  it('should close the dialog', ()=> {
    dialog.cancel();
    expect(dialog.isPresent()).to.eventually.be.false;
  });

});