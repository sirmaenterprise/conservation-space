//max number of files?

var FileUploadSandboxPage = require('./file-upload').FileUploadSandboxPage;
var Search = require('../search/components/search.js').Search;
var Dialog = require('../components/dialog/dialog');

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

  it('should load predefined file', function () {
    // Given I have opened the file upload page
    var fileUpload = page.open(true, undefined, undefined, undefined, true).getFileUpload();

    // Then I should see one file automatically selected
    var entry = fileUpload.getEntry(1);
    expect(fileUpload.getEntry(1).isPresent()).to.eventually.be.true;

    // Then I should not see select file and remove file buttons
    expect(entry.getRemoveButton().isPresent()).to.eventually.be.false;
    expect(entry.getSelectButton().isPresent()).to.eventually.be.false;

    // Then I should see default type selected
    expect(entry.getType().getSelectedValue()).to.eventually.equal('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document');
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

  it('should set default type based on document mimetype', function () {
    // Given I have opened the file upload page
    var fileUpload = page.open(false).getFileUpload();

    // When I select file
    fileUpload.selectFile(FILE_NAME);
    var entry = fileUpload.getEntry(1);

    // Then I should see default type selected
    expect(entry.getType().getSelectedValue()).to.eventually.equal('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document');
  });

  it('should not change selected by user type', function () {
    // Given I have opened the file upload page
    var fileUpload = page.open(false, false, false, null).getFileUpload();

    // When I select file
    fileUpload.selectFile(FILE_NAME);
    // Then the object type is resolved automatically to documentinstance
    var entry = fileUpload.getEntry(1);
    expect(entry.getType().getSelectedValue()).to.eventually.equal('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document');

    // When I select "Approval document" as for subtype
    entry.getSubType().selectOption('Approval document');

    // And I change the context
    var contextSelector = fileUpload.getContextSelector();
    contextSelector.clickSelectButton();

    var search = new Search($(Search.COMPONENT_SELECTOR));
    search.getCriteria().getSearchBar().search();

    var results = search.getResults();
    results.waitForResults();
    results.clickResultItem(1);

    var dialog = new Dialog($('.modal-dialog.modal-lg.ui-draggable'));
    dialog.ok();
    dialog.waitUntilClosed();

    // Then I expect the subtype to remain unchanged (MS210001 == Approval document)
    expect(entry.getSubType().getSelectedValue()).to.eventually.equal('MS210001');
  });

  describe('File upload files count limitation', function () {
    beforeEach(function () {
      // Given I have opened the file upload page configured to support multiple upload
      fileUpload = page.open(false, true).getFileUpload();

      // I upload 7 files
      fileUpload.selectFile(FILE_NAME);
      fileUpload.selectFile(FILE_NAME);
      fileUpload.selectFile(FILE_NAME);
      fileUpload.selectFile(FILE_NAME);
      fileUpload.selectFile(FILE_NAME);
      fileUpload.selectFile(FILE_NAME);
      fileUpload.selectFile(FILE_NAME);
    });

    it('should allow adding multiple files beyond limitation', function () {
      // Then I should see 5 available for upload
      expect(fileUpload.getEntry(1).getFileName()).to.eventually.equal(FILE_NAME);
      expect(fileUpload.getEntry(2).getFileName()).to.eventually.equal(FILE_NAME);
      expect(fileUpload.getEntry(3).getFileName()).to.eventually.equal(FILE_NAME);
      expect(fileUpload.getEntry(4).getFileName()).to.eventually.equal(FILE_NAME);
      expect(fileUpload.getEntry(5).getFileName()).to.eventually.equal(FILE_NAME);

      // Then I should see limitation warning
      expect(fileUpload.getErrorMessage().getText()).to.eventually.equal('No more than 5 files could be selected for upload. After you finish with these uploads, you will automatically continue with the next 2 files!');
    });

    it('should add pending file after file removal', function () {
      // When I remove file
      fileUpload.getEntry(1).getRemoveButton().click();

      // Then I should see updated limitation warning
      browser.wait(EC.textToBePresentInElement(fileUpload.getErrorMessage(), 'No more than 5 files could be selected for upload. After you finish with these uploads, you will automatically continue with the next 1 files!'), DEFAULT_TIMEOUT);
    });

    it('should remove message if upload files are under limit', function () {
      // When I remove 2 files
      fileUpload.getEntry(1).getRemoveButton().click();
      fileUpload.getEntry(1).getRemoveButton().click();

      // Then I should not see limitation warning
      expect(fileUpload.getErrorMessage().isPresent()).to.eventually.be.false;
    });

    it('should add to pending files new added files', function () {
      // When I add new file
      fileUpload.selectFile(FILE_NAME);
      // Then I should see updated limitation warning
      browser.wait(EC.textToBePresentInElement(fileUpload.getErrorMessage().getText(), 'No more than 5 files could be selected for upload. After you finish with these uploads, you will automatically continue with the next 3 files!'), DEFAULT_TIMEOUT);

      // When I add new files
      fileUpload.selectFile(FILE_NAME);
      fileUpload.selectFile(FILE_NAME);
      fileUpload.selectFile(FILE_NAME);
      // Then I should see updated limitation warning
      browser.wait(EC.textToBePresentInElement(fileUpload.getErrorMessage().getText(), 'No more than 5 files could be selected for upload. After you finish with these uploads, you will automatically continue with the next 6 files!'), DEFAULT_TIMEOUT);
    });
  })
});
