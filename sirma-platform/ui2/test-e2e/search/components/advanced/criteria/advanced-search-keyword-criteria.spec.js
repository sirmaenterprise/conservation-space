'use strict';

let SearchSandbox = require('../../search').SearchSandbox;
let AdvancedSearchKeywordCriteria = require('../advanced-search').AdvancedSearchKeywordCriteria;

describe('AdvancedSearchKeywordCriteria', () => {

  let sandbox = new SearchSandbox();

  let search;
  let advancedSearchSection;
  let keywordCriteria;

  beforeEach(() => {
    search = sandbox.open().getSearch();
    let criteria = search.getCriteria();
    criteria.getSearchBar().toggleOptions().openAdvancedSearch();
    let advancedSearch = criteria.getAdvancedSearch();
    advancedSearchSection = advancedSearch.getSection(0);

    return advancedSearchSection.getCriteriaRowForGroup(1, 0, 0).then((row) => {
      keywordCriteria = new AdvancedSearchKeywordCriteria(row.valueColumn);
    });
  });

  describe('When typing in the keyword field', () => {
    it('should set the order to relevance if there is entered text', () => {
      keywordCriteria.getInput().setValue(undefined, 'test');
      expect(search.getToolbar().getOrderToolbar().getOrderByOption()).to.eventually.equal('Relevance');
    });

    it('should return previous order and disable relevance if there is no entered text', () => {
      keywordCriteria.getInput().setValue(undefined, 'test');
      keywordCriteria.getInput().setValue(undefined, '');
      expect(search.getToolbar().getOrderToolbar().getOrderByOption()).to.eventually.equal('Modified On');
    });
  });

  describe('When choosing another property', () => {
    it('should return previous order and disable relevance', () => {
      keywordCriteria.getInput().setValue(undefined, 'test');
      return advancedSearchSection.getCriteriaRowForGroup(1, 0, 0).then((row) => {
        row.changeProperty('title');
        row.waitForOperatorSelectToRender();

        expect(search.getToolbar().getOrderToolbar().getOrderByOption()).to.eventually.equal('Modified On');
      });
    });
  });

  describe('When choosing another object type', () => {
    it('should return previous order and disable relevance', () => {
      keywordCriteria.getInput().setValue(undefined, 'test');
      return advancedSearchSection.addObjectType('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document').then(() => {
        expect(search.getToolbar().getOrderToolbar().getOrderByOption()).to.eventually.equal('Modified On');
      });
    });
  });

  describe('When clearing the criteria', () => {
    it('should return previous order and disable relevance', () => {
      keywordCriteria.getInput().setValue(undefined, 'test');
      search.getCriteria().getAdvancedSearch().clear();
      expect(search.getToolbar().getOrderToolbar().getOrderByOption()).to.eventually.equal('Modified On');
    });
  });
});