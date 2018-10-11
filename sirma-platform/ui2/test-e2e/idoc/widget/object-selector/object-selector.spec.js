'use strict';

let ObjectSelector = require('./object-selector.js').ObjectSelector;
let SandboxPage = require('../../../page-object').SandboxPage;

const BUTTON_EXCLUDE_CURRENT_OPTION = '#excludeCurrent';
const BUTTON_HIDE_OBJECT_OPTIONS = '#hideObjectOptions';
const BUTTON_SHOW_INCLUDE_CURRENT = '#showIncludeCurrent';

describe('ObjectSelector', () => {

  let objectSelector;
  let page = new SandboxPage();
  beforeEach(function () {
    page.open('/sandbox/idoc/widget/object-selector');
    objectSelector = new ObjectSelector();
    objectSelector.waitUntilOpened();
  });

  it('should select (Manually select object) by default', () => {
    expect(objectSelector.getSelectObjectMode()).to.eventually.equal(ObjectSelector.MANUALLY);
  });

  it('should show search if select object mode is manually or automatically', () => {
    expect(objectSelector.getSelectObjectMode()).to.eventually.equal(ObjectSelector.MANUALLY);
    expect(objectSelector.isSearchPresent()).to.eventually.be.true;
    objectSelector.selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
    expect(objectSelector.isSearchPresent()).to.eventually.be.false;
    objectSelector.selectObjectSelectionMode(ObjectSelector.AUTOMATICALLY);
    expect(objectSelector.isSearchPresent()).to.eventually.be.true;
  });

  it('search results should be selectable if search object mode is "manually"', () => {
    expect(objectSelector.getSelectObjectMode()).to.eventually.equal(ObjectSelector.MANUALLY);
    let search = objectSelector.getSearch();
    search.getCriteria().getSearchBar().search();
    search.getResults().waitForResults();
    search.getResults().clickResultItem(1);
    search.getResults().getResultsInputs().then((items) => {
      expect(items[1].getAttribute('checked')).to.eventually.equal('true');
    });
  });

  it('search results should not be selectable if search object mode is "automatically"', () => {
    objectSelector.selectObjectSelectionMode(ObjectSelector.AUTOMATICALLY);
    let search = objectSelector.getSearch();
    search.getCriteria().getSearchBar().search();
    expect(objectSelector.getSearch().getResults().getResultsInputs()).to.eventually.be.empty;
  });

  it('should show include current object checkbox when automatically selecting and configured to do so', () => {
    $(BUTTON_SHOW_INCLUDE_CURRENT).click();

    objectSelector.selectObjectSelectionMode(ObjectSelector.AUTOMATICALLY);
    objectSelector.clickIncludeCurrent();
  });

  it('should restore the toolbar criteria', () => {
    objectSelector.selectObjectSelectionMode(ObjectSelector.AUTOMATICALLY);
    let search = objectSelector.getSearch();
    let toolbar = search.getToolbar();
    let orderbar = toolbar.getOrderToolbar();

    orderbar.selectOrderByOption(2);
    expect(orderbar.getOrderByOption()).to.eventually.equal('Title');
  });

  it('should display current object header if search object mode is "current object"', () => {
    objectSelector.currentObjectHeader = 'test';
    objectSelector.selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
    expect(objectSelector.isSearchPresent()).to.eventually.be.false;
    expect(objectSelector.isCurrentObjectHeaderPresent()).to.eventually.be.true;
  });

  it('should not display option if configured', () => {
    expect(objectSelector.isObjectSelectionModePresent(ObjectSelector.CURRENT_OBJECT)).to.eventually.be.true;
    $(BUTTON_EXCLUDE_CURRENT_OPTION).click();
    expect(objectSelector.isObjectSelectionModePresent(ObjectSelector.CURRENT_OBJECT)).to.eventually.be.false;
  });

  it('should preserve the search mode when object select mode is changed after search', () => {
    let search = objectSelector.getSearch();

    search.getCriteria().getSearchBar().toggleOptions().openAdvancedSearch();
    search.getCriteria().getAdvancedSearch().search();

    objectSelector.selectObjectSelectionMode(ObjectSelector.AUTOMATICALLY);
    objectSelector.getSearch().getCriteria().getAdvancedSearch().waitUntilVisible();
  });

  it('should not render selector options when configured with renderOptions=false', () => {
    $(BUTTON_HIDE_OBJECT_OPTIONS).click();
    expect(objectSelector.areObjectSelectorOptionsPresent()).to.eventually.be.false;
  });
});