var ObjectPickerSandbox = require('./object-picker').ObjectPickerSandbox;
var UPLOAD_EXTENSION = require('./object-picker').UPLOAD_EXTENSION;
var FileUploadPanel = require('../file-upload/file-upload').FileUploadPanel;

const FILE_NAME = 'test-file.png';

describe('Uploading in Object picker', () => {

  var picker;
  var uploadTab;
  var fileUploadPanel;

  var page = new ObjectPickerSandbox();

  beforeEach(() => {
    page.open();
    picker = page.getEmbeddedPicker();

    uploadTab = picker.getExtensionTab(UPLOAD_EXTENSION);
    uploadTab.click();

    var uploadTabContent = picker.getExtension(UPLOAD_EXTENSION);
    fileUploadPanel = new FileUploadPanel(uploadTabContent);
  });

  it('should add uploaded instance into the basket', () => {
    fileUploadPanel.selectFile(FILE_NAME);
    var entry = fileUploadPanel.getEntry(1);
    entry.getSubType().selectOption('Common document');
    entry.getForm().getInputText('description').setValue(undefined, 'test document');
    entry.getUploadButton().click();
    picker.waitForBasketCount(1);
  });

  it('should display an upload tab', () => {
    expect(uploadTab.isDisplayed()).to.eventually.be.true;
  });

  it('should have the upload button visible and disabled when file is selected', () => {
    fileUploadPanel.selectFile(FILE_NAME);
    var entry = fileUploadPanel.getEntry(1);
    expect(entry.getUploadButton().isPresent()).to.eventually.be.true;
    expect(entry.getUploadButton().isDisabled()).to.eventually.be.true;
  });

  it('should have the upload all button visible & disabled when no files are selected', () => {
    expect(fileUploadPanel.getUploadAllButton().isPresent()).to.eventually.be.true;
    expect(fileUploadPanel.getUploadAllButton().isDisabled()).to.eventually.be.true;
  });

  it('should have the cancel button hidden & not present by default', () => {
    expect(fileUploadPanel.getCloseButton().isPresent()).to.eventually.be.false;
  });
});