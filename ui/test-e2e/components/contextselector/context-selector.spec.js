var ContextSelector = require('./context-selector');
var ObjectPicker = require('../../components/extensions-panel/extensions-panel');
var Dialog = require('../dialog/dialog');
var Search = require('../../search/components/search');
var SandboxPage = require('../../page-object').SandboxPage;

const DIALOG = '.modal-dialog';
const EXTENSIONS_PANEL = '.extensions-panel';
const DIALOG_PICKER = `${DIALOG} ${EXTENSIONS_PANEL}`;

var page = new SandboxPage();

describe('Test for context selector component ', function () {

  var contextSelector;

  beforeEach(()=> {
    page.open('sandbox/components/contextselector');
    contextSelector = new ContextSelector(element(by.className('context-selector')));
  });

  it('should show default context when loaded', function () {
    expect(contextSelector.getClearContextButton().isPresent()).to.eventually.be.true;
    expect(contextSelector.getSelectButton().isPresent()).to.eventually.be.true;
    expect(contextSelector.getContextIdText()).to.eventually.equal('test_id');
  });

  it('should clear context when clear context (X) button is clicked', function () {
    contextSelector.clickClearContextButton();
    expect(contextSelector.getClearContextButton().isPresent()).to.eventually.be.false;
    expect(contextSelector.getSelectButton().isPresent()).to.eventually.be.true;
    expect(contextSelector.getContextPathText()).to.eventually.equal('No Context');
    expect(contextSelector.getContextIdText()).to.eventually.equal('');
  });
  it('should change context when instance from picker is selected', function () {
    contextSelector.clickSelectButton();

    var objectPicker = new ObjectPicker($(DIALOG_PICKER));
    objectPicker.waitUntilOpened();
    var objectPickerDialog = new Dialog($(DIALOG));
    objectPickerDialog.waitUntilOpened();
    search = new Search(DIALOG_PICKER);
    search.waitUntilOpened();

    search.clickSearch();
    search.results.clickResultItem(0);
    objectPickerDialog.getOkButton().click();
    //for some reason, in the sandbox, the model does not update until after the select button is clicked again.
    browser.sleep(1000)
    contextSelector.clickSelectButton();

    expect(contextSelector.getClearContextButton().isPresent()).to.eventually.be.true;
    expect(contextSelector.getSelectButton().isPresent()).to.eventually.be.true;
    expect(contextSelector.getContextIdText()).to.eventually.equal('aa873a4d-ccb2-4878-8a68-6be03deb2e7d');
  });

});
