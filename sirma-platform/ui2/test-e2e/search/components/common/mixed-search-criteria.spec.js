'use strict';

const SANDBOX_URL = 'sandbox/search/components/common/mixed-search-criteria';

let MixedSearchCriteriaSandbox = require('./mixed-search-criteria').MixedSearchCriteriaSandbox;
let AdvancedSearchKeywordCriteria = require('../advanced/advanced-search').AdvancedSearchKeywordCriteria;
let AdvancedSearchRelationCriteria = require('../advanced/advanced-search').AdvancedSearchRelationCriteria;
let ObjectPickerDialog = require('../../../picker/object-picker').ObjectPickerDialog;

// Types used in the stubbed sandbox
const TYPES = ['http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document', 'MS210001'];

describe('Search criteria ', () => {

  let page = new MixedSearchCriteriaSandbox();
  let criteria;

  beforeEach(() => {
    page.open(SANDBOX_URL);
    criteria = page.getCriteria();
  });

  it('should open the search bar by default', () => {
    criteria.getSearchBar().waitUntilOpened();
  });

  it('should be able to display the advanced search form', () => {
    criteria.getSearchBar().toggleOptions().openAdvancedSearch();
    criteria.getAdvancedSearch().waitUntilVisible();
  });

  describe('When advanced search is opened', () => {
    it('should have the option to close it', () => {
      criteria.getSearchBar().toggleOptions().openAdvancedSearch();
      criteria.closeAdvancedSearch();
      criteria.getSearchBar().waitUntilOpened();
    });

    it('should populate the advanced search with the search bar criteria', () => {
      let searchBar = criteria.getSearchBar();
      searchBar.getMultiTypeSelect().selectFromMenuByValue(TYPES[0]);
      searchBar.getMultiTypeSelect().selectFromMenuByValue(TYPES[1]);
      searchBar.typeFreeText('abc 123');

      searchBar.openContextPicker();
      let pickerDialog = new ObjectPickerDialog();
      let search = pickerDialog.getObjectPicker().getSearch();
      // Selected id is '1'
      search.getResults().clickResultItem(0);
      pickerDialog.ok();

      criteria.getSearchBar().toggleOptions().openAdvancedSearch();
      let advancedSearch = criteria.getAdvancedSearch();
      let section = advancedSearch.getSection(0);

      expect(section.getObjectTypeSelectValue()).to.eventually.deep.equal(TYPES);
      section.getCriteriaRowForGroup(1, 0, 0).then((row) => {
        let keywordCriteria = new AdvancedSearchKeywordCriteria(row.valueColumn, true);
        expect(keywordCriteria.getValue()).to.eventually.equal('abc 123');
      });
      section.getCriteriaRowForGroup(1, 0, 1).then((row) => {
        let relationCriteria = new AdvancedSearchRelationCriteria(row.valueColumn);
        expect(relationCriteria.getValueMenu().getSelectedValue()).to.eventually.deep.equal(['1']);
      });
    });
  });

  describe('When a predefined criteria is simple enough for the search bar', () => {
    it('should visualize it in the search bar', () => {
      page.setSimpleCriteria();
      let searchBar = criteria.getSearchBar();
      expect(searchBar.getMultiTypeSelect().getSelectedValue()).to.eventually.deep.equal(TYPES);
      expect(searchBar.getFreeTextValue()).to.eventually.equal('abc 123');
      expect(searchBar.getSelectedContext()).to.eventually.equal('1');
    });
  });

  describe('When the criteria cannot be rendered by the search bar', () => {
    it('should automatically visualize it in the advanced search form', () => {
      page.setComplexCriteria();
      criteria.getAdvancedSearch().waitUntilVisible();
    });
  });

});
