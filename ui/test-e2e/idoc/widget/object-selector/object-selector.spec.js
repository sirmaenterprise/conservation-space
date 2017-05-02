var ObjectSelector = require('./object-selector.js').ObjectSelector;
var BasicSearchCriteria = require('../../../search/components/common/basic-search-criteria.js').BasicSearchCriteria;
var MixedSearchCriteria = require('../../../search/components/common/mixed-search-criteria.js');
var AdvancedSearch = require('../../../search/components/advanced/advanced-search.js').AdvancedSearch;
var SandboxPage = require('../../../page-object').SandboxPage;

const BUTTON_EXCLUDE_CURRENT_OPTION = '#excludeCurrent';
const BUTTON_HIDE_OBJECT_OPTIONS = '#hideObjectOptions';
const BUTTON_SHOW_INCLUDE_CURRENT = '#showIncludeCurrent';

describe('ObjectSelector', () => {
  var objectSelector;
  var page = new SandboxPage();
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
    objectSelector.getSearch().clickSearch();
    objectSelector.getSearch().results.clickResultItem(1);
    objectSelector.getSearch().results.getResultsInputs().then((items) => {
      expect(items[1].getAttribute('checked')).to.eventually.equal('true');
    });
  });

  it('search results should not be selectable if search object mode is "automatically"', () => {
    objectSelector.selectObjectSelectionMode(ObjectSelector.AUTOMATICALLY);
    objectSelector.getSearch().clickSearch();
    expect(objectSelector.getSearch().results.getResultsInputs()).to.eventually.be.empty;
  });

  it('should show include current object checkbox when automatically selecting and configured to do so', () => {
    $(BUTTON_SHOW_INCLUDE_CURRENT).click();

    objectSelector.selectObjectSelectionMode(ObjectSelector.AUTOMATICALLY);
    objectSelector.clickIncludeCurrent();
  });

  it('should restore the toolbar criteria', () => {
    objectSelector.selectObjectSelectionMode(ObjectSelector.AUTOMATICALLY);
    objectSelector.getSearch().selectOrderBy('dcterms\\:title').results.waitUntilOpened();
    var toolbar = objectSelector.getSearch().toolbar;
    var orderBy = toolbar.getOrderByValue();
    return Promise.all([
      expect(orderBy).to.eventually.equal('dcterms:title'),
    ]);
  });

  it('should display current object header if search object mode is "current object"', () => {
    objectSelector.currentObjectHeader = 'test';
    objectSelector.selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
    expect(objectSelector.isSearchPresent()).to.eventually.be.false;
    expect(objectSelector.isCurrentObjectHeaderPresent()).to.eventually.be.true;
  });

  it('should assign default search criteria and clear the rest after pressing the clear button', () => {
    var criteria = new BasicSearchCriteria($(BasicSearchCriteria.COMPONENT_SELECTOR));
    criteria.waitUntilVisible();
    criteria.waitForSelectedOption('rel:that');
    return criteria.clearCriteria().then(() => {
      criteria.waitForSelectedOption('current_object');
      expect(criteria.getSelectedValue(criteria.typesSelectElement)).to.eventually.deep.equal([]);
      expect(criteria.getSelectedValue(criteria.relationshipsSelectElement)).to.eventually.deep.equal([]);
      expect(criteria.getSelectedValue(criteria.contextSelectElement)).to.eventually.deep.equal(['current_object']);
      expect(criteria.getSelectedValue(criteria.createdBySelectElement)).to.eventually.deep.equal([]);
    });
  });

  it('should not display option if configured', () => {
    expect(objectSelector.isObjectSelectionModePresent(ObjectSelector.CURRENT_OBJECT)).to.eventually.be.true;
    $(BUTTON_EXCLUDE_CURRENT_OPTION).click();
    expect(objectSelector.isObjectSelectionModePresent(ObjectSelector.CURRENT_OBJECT)).to.eventually.be.false;
  });

  it('should preserve the search mode when object select mode is changed after search', () => {
    var mixedSearchCriteria = new MixedSearchCriteria();
    mixedSearchCriteria.clickOption(mixedSearchCriteria.getAdvancedSearchOption());

    var advancedSearch = new AdvancedSearch(mixedSearchCriteria.getAdvancedSearchForm());
    advancedSearch.search();
    objectSelector.selectObjectSelectionMode(ObjectSelector.AUTOMATICALLY);

    return expect(mixedSearchCriteria.getCheckedOption().getAttribute('value')).to.eventually.equal('advanced');
  });

  it('should not render selector options when configured with renderOptions=false', () => {
    $(BUTTON_HIDE_OBJECT_OPTIONS).click();
    expect(objectSelector.areObjectSelectorOptionsPresent()).to.eventually.be.false;
  });
});