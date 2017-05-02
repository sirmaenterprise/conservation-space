import {MixedSearchCriteria} from 'search/components/common/mixed-search-criteria';
import {SearchMediator} from 'search/search-mediator';
import {QueryBuilder} from 'search/utils/query-builder';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {OPEN_SAVED_SEARCH_EVENT} from 'search/components/saved/saved-search-select/saved-search-select';

describe('MixedSearchCriteria', () => {

  afterEach(() => {
    MixedSearchCriteria.prototype.config = undefined;
  });

  it('should not render contextual help by default', () => {
    var queryBuilder = new QueryBuilder({});
    MixedSearchCriteria.prototype.config = {
      searchMediator: new SearchMediator({}, queryBuilder),
      searchMode: SearchCriteriaUtils.ADVANCED_MODE
    };
    var mixedCriteria = new MixedSearchCriteria();
    expect(mixedCriteria.config.renderHelp).to.be.false;
  });

  it('should obtain the query builder if it is a basic search', () => {
    var queryBuilder = new QueryBuilder({});
    MixedSearchCriteria.prototype.config = {
      searchMediator: new SearchMediator({}, queryBuilder)
    };
    var mixedCriteria = new MixedSearchCriteria();
    expect(mixedCriteria.basicSearchQueryBuilder).to.equal(queryBuilder);
    expect(mixedCriteria.config.searchMediator.searchMode).to.equal(SearchCriteriaUtils.BASIC_MODE);
  });

  it('should obtain the query builder if it is an advanced search', () => {
    var queryBuilder = new QueryBuilder({});
    MixedSearchCriteria.prototype.config = {
      searchMediator: new SearchMediator({}, queryBuilder),
      searchMode: SearchCriteriaUtils.ADVANCED_MODE
    };
    var mixedCriteria = new MixedSearchCriteria();
    expect(mixedCriteria.advancedSearchQueryBuilder).to.equal(queryBuilder);
    expect(mixedCriteria.config.searchMediator.searchMode).to.equal(SearchCriteriaUtils.ADVANCED_MODE);
  });

  it('should construct a query builder for basic search if it is an advanced search', () => {
    var queryBuilder = new QueryBuilder({});
    MixedSearchCriteria.prototype.config = {
      searchMediator: new SearchMediator({}, queryBuilder),
      searchMode: SearchCriteriaUtils.ADVANCED_MODE
    };
    var mixedCriteria = new MixedSearchCriteria();
    expect(mixedCriteria.basicSearchQueryBuilder).to.exist;
  });

  it('should construct a query builder for advanced search if it is a basic search', () => {
    var queryBuilder = new QueryBuilder({});
    MixedSearchCriteria.prototype.config = {
      searchMediator: new SearchMediator({}, queryBuilder)
    };
    var mixedCriteria = new MixedSearchCriteria();
    expect(mixedCriteria.advancedSearchQueryBuilder).to.exist;
  });

  describe('onSwitch', () => {
    it('should assign the basic search query builder if the current mode is for basic search', () => {
      var advancedSearchQueryBuilder = new QueryBuilder({});
      MixedSearchCriteria.prototype.config = {
        searchMediator: new SearchMediator({}, advancedSearchQueryBuilder),
        searchMode: SearchCriteriaUtils.ADVANCED_MODE
      };
      var mixedCriteria = new MixedSearchCriteria();
      var basicSearchQueryBuilder = mixedCriteria.basicSearchQueryBuilder;

      mixedCriteria.config.searchMode = SearchCriteriaUtils.BASIC_MODE;
      mixedCriteria.onSwitch();
      expect(mixedCriteria.config.searchMediator.queryBuilder).to.equal(basicSearchQueryBuilder);
      expect(mixedCriteria.config.searchMediator.searchMode).to.equal(SearchCriteriaUtils.BASIC_MODE);
    });

    it('should assign the advanced search query builder if the current mode is for advanced search', () => {
      var basicSearchQueryBuilder = new QueryBuilder({});
      MixedSearchCriteria.prototype.config = {
        searchMediator: new SearchMediator({}, basicSearchQueryBuilder),
        searchMode: SearchCriteriaUtils.BASIC_MODE
      };
      var mixedCriteria = new MixedSearchCriteria();
      var advancedSearchQueryBuilder = mixedCriteria.advancedSearchQueryBuilder;

      mixedCriteria.config.searchMode = SearchCriteriaUtils.ADVANCED_MODE;
      mixedCriteria.onSwitch();
      expect(mixedCriteria.config.searchMediator.queryBuilder).to.equal(advancedSearchQueryBuilder);
      expect(mixedCriteria.config.searchMediator.searchMode).to.equal(SearchCriteriaUtils.ADVANCED_MODE);
    });

    it('should clear the current search results', () => {
      var queryBuilder = new QueryBuilder({});
      MixedSearchCriteria.prototype.config = {
        searchMediator: new SearchMediator({}, queryBuilder)
      };
      var mixedCriteria = new MixedSearchCriteria();
      mixedCriteria.clearResults = sinon.spy();
      mixedCriteria.onSwitch();
      expect(mixedCriteria.clearResults.calledOnce).to.be.true;
    });
  });

  describe('isBasicSearchMode()', () => {
    it('should tell if the current mode is basic search', () => {
      var queryBuilder = new QueryBuilder({});
      MixedSearchCriteria.prototype.config = {
        searchMediator: new SearchMediator({}, queryBuilder),
        searchMode: SearchCriteriaUtils.BASIC_MODE
      };
      var mixedCriteria = new MixedSearchCriteria();
      expect(mixedCriteria.isBasicSearchMode()).to.be.true;
    });

    it('should tell if the current mode is advanced search', () => {
      var queryBuilder = new QueryBuilder({});
      MixedSearchCriteria.prototype.config = {
        searchMediator: new SearchMediator({}, queryBuilder),
        searchMode: SearchCriteriaUtils.ADVANCED_MODE
      };
      var mixedCriteria = new MixedSearchCriteria();
      expect(mixedCriteria.isBasicSearchMode()).to.be.false;
    });

    it('should tell if the current mode is external search', () => {
      var queryBuilder = new QueryBuilder({});
      MixedSearchCriteria.prototype.config = {
        searchMediator: new SearchMediator({}, queryBuilder),
        searchMode: SearchCriteriaUtils.EXTERNAL_MODE
      };
      var mixedCriteria = new MixedSearchCriteria();
      expect(mixedCriteria.isBasicSearchMode()).to.be.false;
      expect(mixedCriteria.isExternalSearchMode()).to.be.true;
    });
  });

  describe('registerSearchLoadListener()', () => {
    it('should register a mediator listener when a saved search is loaded', () => {
      var queryBuilder = new QueryBuilder({});
      MixedSearchCriteria.prototype.config = {
        searchMediator: new SearchMediator({}, queryBuilder),
        searchMode: SearchCriteriaUtils.BASIC_MODE
      };

      var mixedCriteria = new MixedSearchCriteria();
      var openListeners = mixedCriteria.config.searchMediator.listeners[OPEN_SAVED_SEARCH_EVENT];

      expect(openListeners).to.exist;
      expect(openListeners.length).to.equal(1);
    });

    it('should set the new search mode and rebuild the query builders', () => {
      var queryBuilder = new QueryBuilder({});
      MixedSearchCriteria.prototype.config = {
        searchMediator: new SearchMediator({}, queryBuilder),
        searchMode: SearchCriteriaUtils.EXTERNAL_MODE
      };

      var mixedCriteria = new MixedSearchCriteria();
      mixedCriteria.initQueryBuilders = sinon.spy();

      var openListeners = mixedCriteria.config.searchMediator.listeners[OPEN_SAVED_SEARCH_EVENT];
      var openListener = openListeners[0];

      openListener({
        searchMode: SearchCriteriaUtils.ADVANCED_MODE
      });

      expect(mixedCriteria.config.searchMode).to.equal(SearchCriteriaUtils.ADVANCED_MODE);
      expect(mixedCriteria.initQueryBuilders.calledOnce).to.be.true;
    });
  });
});
