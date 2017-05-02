'use strict';
let AddRelationSandboxPage = require('./add-relation-panel').AddRelationSandboxPage;

const DOCUMENT_TYPE = 'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document';

describe('AddRelationPanel', ()=> {

  var page = new AddRelationSandboxPage();

  beforeEach(() => {
    page.open();
  });

  it('should open dialog for one to one relation', () => {
    page.openDialog();
  });

  it('should have predefined object type', () => {
    let dialog = page.openDialog();
    var criteria = dialog.getSearchCriteria();
    expect(criteria.getSelectedValue(criteria.typesSelectElement)).to.eventually.deep.equal([DOCUMENT_TYPE]);
  });

  it('should have title for specific relation', () => {
    let dialog = page.openDialog();
    expect(dialog.getTitleText()).to.eventually.equal('Relation');
  });

  it('should have ok button', () => {
    let dialog = page.openDialog();
    expect(dialog.getOkButton().isDisplayed()).to.eventually.be.true;
  });

  it('should have disabled ok button ', () => {
    let dialog = page.openDialog();
    expect(dialog.getOkButton().isEnabled()).to.eventually.be.false;
  });

  it('should have cancel button', () => {
    let dialog = page.openDialog();
    expect(dialog.getCancelButton().isDisplayed()).to.eventually.be.true;
  });
});