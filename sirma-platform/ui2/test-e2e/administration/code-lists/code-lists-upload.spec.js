'use strict';

let CodeListsSandbox = require('./code-lists.js').CodeListsSandbox;

const TEST_FILE = 'test.xls';

describe('Controlled vocabularies upload', () => {

  let sandbox;
  let codeListsUpload;
  beforeEach(() => {
    sandbox = new CodeListsSandbox();
    sandbox.open();
    codeListsUpload = sandbox.getCodeListsUpload();
  });

  it('should display a file upload button for opening a file select dialog', () => {
    expect(codeListsUpload.selectFileButton.isDisplayed()).to.eventually.be.true;
  });

  describe('When no file is selected', () => {
    it('should not allow any upload', () => {
      expect(codeListsUpload.updateCodeListsButton.isDisplayed()).to.eventually.be.false;
      expect(codeListsUpload.overwriteCodeListsButton.isDisplayed()).to.eventually.be.false;
    });
  });

  describe('When a file is selected', () => {
    beforeEach(() => codeListsUpload.selectFile(TEST_FILE));

    it('should display the file name in the file select button', () => {
      codeListsUpload.selectFile(TEST_FILE);
      expect(codeListsUpload.selectFileButton.getText()).to.eventually.equal('test.xls');
    });

    it('should allow to overwrite & update code lists', () => {
      expect(codeListsUpload.updateCodeListsButton.isDisplayed()).to.eventually.be.true;
      expect(codeListsUpload.overwriteCodeListsButton.isDisplayed()).to.eventually.be.true;
    });
  });

  describe('When overwriting or updating code lists', () => {
    it('should display a confirmation dialog', () => {
      codeListsUpload.selectFile(TEST_FILE);
      let dialog = codeListsUpload.overwriteCodeLists();
      dialog.closeModal();

      dialog = codeListsUpload.updateCodeLists();
      dialog.closeModal();
    });

    it('should render a success message', () => {
      codeListsUpload.selectFile(TEST_FILE);
      let dialog = codeListsUpload.overwriteCodeLists();
      dialog.ok();
      expect(codeListsUpload.isUploadSuccessful()).to.eventually.be.true;

      dialog = codeListsUpload.updateCodeLists();
      dialog.ok();
      expect(codeListsUpload.isUploadSuccessful()).to.eventually.be.true;
    });

    it('should render an error message', () => {
      // Reopening the page with fail flag
      sandbox.open(true);
      codeListsUpload = sandbox.getCodeListsUpload();
      codeListsUpload.selectFile(TEST_FILE);

      let dialog = codeListsUpload.overwriteCodeLists();
      dialog.ok();
      expect(codeListsUpload.isUploadSuccessful()).to.eventually.be.false;

      dialog = codeListsUpload.updateCodeLists();
      dialog.ok();
      expect(codeListsUpload.isUploadSuccessful()).to.eventually.be.false;
    });
  });
});