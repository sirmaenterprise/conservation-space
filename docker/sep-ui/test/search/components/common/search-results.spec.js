import {SearchResults} from 'search/components/common/search-results';
import {NO_SELECTION, SINGLE_SELECTION, MULTIPLE_SELECTION} from 'search/search-selection-modes';

describe('SearchResults', () => {
  var results;
  beforeEach(()=> {
    SearchResults.prototype.config = undefined;
    results = new SearchResults();
  });

  it('should use default component configuration', () => {
    expect(results.config.selection).to.equal(NO_SELECTION);
    expect(results.config.linkRedirectDialog).to.be.false;
    expect(results.config.renderMenu).to.be.false;
  });

  it('should configure instance list configuration for the results', () => {
    SearchResults.prototype.config = {
      selectionHandler: () => {
        return 'selection handler result';
      },
      exclusions: ['emf:123'],
      renderMenu: true,
      placeholder: 'menu-placeholder'
    };

    results = new SearchResults();

    expect(results.searchResultsListConfig).to.exist;
    expect(results.searchResultsListConfig.selectionHandler).to.exist;
    expect(results.searchResultsListConfig.selectionHandler()).to.equal('selection handler result');
    expect(results.searchResultsListConfig.linkRedirectDialog).to.be.false;

    expect(results.searchResultsListConfig.exclusions).to.deep.equal(['emf:123']);
    expect(results.searchResultsListConfig.renderMenu).to.be.true;
    expect(results.searchResultsListConfig.placeholder).to.equal('menu-placeholder');
  });

  it('should properly define the selection mode for no selection', () => {
    SearchResults.prototype.config = {
      selection: NO_SELECTION
    };
    results = new SearchResults();
    expect(results.searchResultsListConfig.selectableItems).to.be.false;
    expect(results.searchResultsListConfig.singleSelection).to.be.false;
  });

  it('should properly define the selection mode for single selection', () => {
    SearchResults.prototype.config = {
      selection: SINGLE_SELECTION
    };
    results = new SearchResults();
    expect(results.searchResultsListConfig.selectableItems).to.be.true;
    expect(results.searchResultsListConfig.singleSelection).to.be.true;
  });

  it('should properly define the selection mode for multiple selection', () => {
    SearchResults.prototype.config = {
      selection: MULTIPLE_SELECTION
    };
    results = new SearchResults();
    expect(results.searchResultsListConfig.selectableItems).to.be.true;
    expect(results.searchResultsListConfig.singleSelection).to.be.false;
  });

});