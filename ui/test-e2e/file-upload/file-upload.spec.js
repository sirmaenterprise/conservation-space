//max number of files?

var FileUploadSandboxPage = require('./file-upload').FileUploadSandboxPage;

const FILE_NAME = 'small-file.txt';

var page = new FileUploadSandboxPage();

describe('File upload', function () {

  var fileUpload;
  var entry;

  beforeEach(function () {
    // Given I have opened the file upload page
    fileUpload = page.open(false).getFileUpload();

    // When I add a file
    fileUpload.selectFile(FILE_NAME);
    entry = fileUpload.getEntry(1);
  });

  it('should disable the upload button until the user fills the form', function () {
    // Then the upload button should be disabled
    expect(entry.getUploadButton().isEnabled()).to.eventually.be.false;
  });

  it('should allow removing added files', function () {
    // And remove it
    entry.getRemoveButton().click();
    // Then I should NOT see the file
    expect(entry.isPresent()).to.eventually.be.false;
  });

  it('should automatically set title and name properties', function () {
    // And select document type
    entry.getSubType().selectOption('Common document');

    // Then the title field should contain the file name
    expect(entry.getForm().getInputText('title').getValue()).to.eventually.be.equal(FILE_NAME);
    // And the name field should contain the file name
    expect(entry.getForm().getInputText('name').getValue()).to.eventually.be.equal(FILE_NAME);
  });

  it('should show notification in single upload mode when the upload is complete', function () {
    // And select document type
    entry.getSubType().selectOption('Common document');

    // And fill the form
    entry.getForm().getInputText('description').setValue(undefined, 'test document');

    // And press the upload button
    entry.getUploadButton().click();

    // Then notification should be displayed
    fileUpload.getNotification().waitUntilOpened();

    // And the instance header is displayed
    expect(entry.getInstanceHeader().getText()).to.eventually.not.be.empty;
  });

  it('should cancel the ajax request when an item is removed during the upload process', function () {
    // And select document type
    entry.getSubType().selectOption('Common document');

    // And fill the form
    entry.getForm().getInputText('description').setValue(undefined, 'test document');

    // And press the upload button
    entry.getUploadButton().click();

    // And press the remove button when the upload is already started
    entry.getRemoveButton().click();

    // Then I should see a sign that the upload has bee aborted
    expect(browser.$('.file-upload-integration').getText()).to.eventually.equal('Upload aborted!');
  });

});

describe('File upload', function () {

  it('should disable the buttons on upload start', function () {
    // Given I have opened the file upload page
    var fileUpload = page.open(false, undefined, DEFAULT_TIMEOUT).getFileUpload();

    // When I add a file
    fileUpload.selectFile(FILE_NAME);
    var entry = fileUpload.getEntry(1);

    // And select document type
    entry.getSubType().selectOption('Common document');

    // And fill the form
    entry.getForm().getInputText('description').setValue(undefined, 'test document');

    // And press the upload button
    entry.getUploadButton().click();

    // Then the upload button should be disabled
    expect(entry.getUploadButton().isEnabled()).to.eventually.be.false;
  });

  it('should show message on upload error', function () {
    // Given I have opened the file upload page
    var fileUpload = page.open(true).getFileUpload();

    // When I add a file
    fileUpload.selectFile(FILE_NAME);
    var entry = fileUpload.getEntry(1);

    // And select document type
    entry.getSubType().selectOption('Common document');

    // And fill the form
    entry.getForm().getInputText('description').setValue(undefined, 'test document');

    // And press the upload button
    entry.getUploadButton().click();

    // I should see an error
    entry.getMessage().waitForText();
  });

  it('should validate file size', function () {
    // Given I have opened the file upload page
    var fileUpload = page.open(true).getFileUpload();

    // When I add a file with size that exceeds the allowed
    fileUpload.selectFile('file-upload.js');
    var entry = fileUpload.getEntry(1);

    // Then I should see an error message
    entry.getMessage().waitForText();

    // And the upload button should be disabled
    expect(entry.getUploadButton().isEnabled()).to.eventually.be.false;
  });

  it('should allow upload a content to an existing entity', function () {
    // Given I have opened the file upload page for existing entity
    var fileUpload = page.open(false, undefined, undefined, 'testId').getFileUpload();

    // When I select two files
    fileUpload.selectFile(FILE_NAME);
    fileUpload.selectFile(FILE_NAME);
    var entry = fileUpload.getEntry(1);

    // Then I should see only one file
    expect(fileUpload.getEntry(2).isPresent()).to.eventually.be.false;

    // I should not see the options for selecting document type

    // When I press the upload button
    entry.getUploadButton().click();

    // Then instance header should be displayed
    entry.waitForInstanceHeader();
    expect(entry.getInstanceHeader().getText()).to.eventually.not.be.empty;
  });

  it('should allow adding multiple files', function () {
    // Given I have opened the file upload page configured to support multiple upload
    var fileUpload = page.open(true, true).getFileUpload();

    // When I upload two files
    fileUpload.selectFile(FILE_NAME);
    fileUpload.selectFile(FILE_NAME);

    // Then I should see them both available for upload
    expect(fileUpload.getEntry(1).getFileName()).to.eventually.equal(FILE_NAME);
    expect(fileUpload.getEntry(2).getFileName()).to.eventually.equal(FILE_NAME);
  });

  it('should upload all the valid files', function () {
    // this is opened in a fresh page because (probably) the sandbox restart causes a race condition
    var fileUpload = new FileUploadSandboxPage().open(false, true).getFileUpload();

    fileUpload.selectFile(FILE_NAME);
    fileUpload.selectFile(FILE_NAME);

    var entry = fileUpload.getEntry(2);

    entry.getSubType().selectOption('Common document');
    entry.getForm().getInputText('description').setValue(undefined, 'test document');
    var uploadAllButton = fileUpload.getUploadAllButton();

    browser.wait(uploadAllButton.isEnabled(), DEFAULT_TIMEOUT);

    uploadAllButton.click();

    browser.wait(uploadAllButton.isDisabled(), DEFAULT_TIMEOUT);
  });
});