'use strict';

let ContextSelector = require('./context-selector');
let ObjectPickerDialog = require('../../picker/object-picker').ObjectPickerDialog;
let SandboxPage = require('../../page-object').SandboxPage;

let page = new SandboxPage();

describe('Test for context selector component ', function () {

  beforeEach(()=> {
    page.open('sandbox/components/contextselector');
  });

  describe('initialization of context selector', () =>{
    it('should enable both buttons when selection mode is WITHOUT_CONTEXT', () => {
      let contextSelector = new ContextSelector(element(by.css(ContextSelector.SELECTOR_CONTEXT_SELECTOR_SELECTION_MODE_WITHOUT_CONTEXT)));
      expect(contextSelector.getClearContextButton().isEnabled()).to.eventually.be.true;
      expect(contextSelector.getSelectButton().isEnabled()).to.eventually.be.false;
    });

    it('should enable both buttons when selection mode is IN_CONTEXT', () => {
      let contextSelector = new ContextSelector(element(by.css(ContextSelector.SELECTOR_CONTEXT_SELECTOR_SELECTION_MODE_IN_CONTEXT)));
      expect(contextSelector.getClearContextButton().isEnabled()).to.eventually.be.false;
      expect(contextSelector.getSelectButton().isEnabled()).to.eventually.be.true;
    });

    it('should enable both buttons when selection mode is  BOTH', () => {
      let contextSelector = new ContextSelector(element(by.css(ContextSelector.SELECTOR_CONTEXT_SELECTOR_SELECTION_MODE_BOTH)));
      expect(contextSelector.getClearContextButton().isEnabled()).to.eventually.be.true;
      expect(contextSelector.getSelectButton().isEnabled()).to.eventually.be.true;
    });

    it('should enable both buttons when selection mode is not passed as configuration', () => {
      let contextSelector = new ContextSelector(element(by.css(ContextSelector.SELECTOR_CONTEXT_SELECTOR)));
      expect(contextSelector.getClearContextButton().isEnabled()).to.eventually.be.true;
      expect(contextSelector.getSelectButton().isEnabled()).to.eventually.be.true;
    });
  });

  it('should show default context when loaded', function () {
    let contextSelector = new ContextSelector(element(by.css(ContextSelector.SELECTOR_CONTEXT_SELECTOR)));
    expect(contextSelector.getClearContextButton().isPresent()).to.eventually.be.true;
    expect(contextSelector.getSelectButton().isPresent()).to.eventually.be.true;
    expect(contextSelector.getContextIdText()).to.eventually.equal('test_id');
  });

  it('should clear context when clear context (X) button is clicked', function () {
    let contextSelector = new ContextSelector(element(by.css(ContextSelector.SELECTOR_CONTEXT_SELECTOR)));
    contextSelector.clickClearContextButton();
    expect(contextSelector.getClearContextButton().isPresent()).to.eventually.be.false;
    expect(contextSelector.getSelectButton().isPresent()).to.eventually.be.true;
    expect(contextSelector.getContextPathText()).to.eventually.equal('No Context');
    expect(contextSelector.getContextIdText()).to.eventually.equal('');
  });

  it('should change context when instance from picker is selected', function () {
    let contextSelector = new ContextSelector(element(by.css(ContextSelector.SELECTOR_CONTEXT_SELECTOR)));
    contextSelector.clickSelectButton();

    let pickerDialog = new ObjectPickerDialog();
    let picker = pickerDialog.getObjectPicker();
    let search = picker.getSearch();
    search.getCriteria().getSearchBar().search();
    search.getResults().clickResultItem(0);
    pickerDialog.ok();

    expect(contextSelector.getClearContextButton().isPresent()).to.eventually.be.true;
    expect(contextSelector.getSelectButton().isPresent()).to.eventually.be.true;
    expect(contextSelector.getContextIdText()).to.eventually.equal('aa873a4d-ccb2-4878-8a68-6be03deb2e7d');
  });
});
