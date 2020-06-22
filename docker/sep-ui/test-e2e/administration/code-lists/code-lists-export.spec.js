'use strict';

let CodeListsSandbox = require('./code-lists.js').CodeListsSandbox;

const CODE_LISTS_FILE = 'Controlled-Vocabularies-List.xls';

describe('Controlled vocabularies export', () => {

  let sandbox;
  let codeListsExport;

  beforeEach(() => {
    sandbox = new CodeListsSandbox();
    sandbox.open();
    codeListsExport = sandbox.getCodeListsExport();
  });

  it('should have the code lists export button visible & present', () => {
    expect(codeListsExport.codeListExportButton.isPresent()).to.eventually.be.true;
    expect(codeListsExport.codeListExportButton.isDisplayed()).to.eventually.be.true;
  });

  it('should properly export code lists file', () => {
    codeListsExport.exportCodeLists();
    codeListsExport.waitExportedCodeLists();
    expect(codeListsExport.getExportedFileName()).to.eventually.equal(CODE_LISTS_FILE);
  });
});