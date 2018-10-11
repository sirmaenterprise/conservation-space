import {SearchBarOptions} from 'search/components/search-bar/search-bar-options';
import {Configuration} from 'common/application-config';
import {KEY_ENTER} from 'common/keys';
import {SavedSearchesLoader} from 'search/components/saved/saved-searches-loader';

import {stub} from 'test/test-utils';
import {PromiseStub} from 'test/promise-stub';
import {stubSearchService} from 'test/services/rest/search-service-mock';

describe('SearchBarOptions', () => {
  let options;
  beforeEach(() => {
    options = new SearchBarOptions(stubSearchService(), stubConfiguration());
    options.ngOnInit();
    // Stubbing it for easier testing.
    options.savedSearchesLoader = stubSavedSearchesLoader();
  });

  describe('ngOnInit()', () => {
    it('construct a configuration for the rendered headers of the saved searches with redirect suppressing', () => {
      expect(options.headerConfig).to.deep.equal({preventLinkRedirect: true});
    });

    it('should trigger a saved search filtering', () => {
      // Because we stub the real one after ngOnInit, we gotta verify with the search service
      expect(options.searchService.search.calledOnce).to.be.true;
    });
  });

  describe('onKeyPressed()', () => {
    it('should filter saved searches with the current terms if the pressed key is Enter', () => {
      options.savedSearchFilter = 'projects';
      options.onKeyPressed({keyCode: KEY_ENTER});
      expect(options.savedSearchesLoader.filterSavedSearches.calledOnce).to.be.true;
      expect(options.savedSearchesLoader.filterSavedSearches.calledWith(options.savedSearchFilter)).to.be.true;
    });

    it('should not filter saved searches if the pressed key is different than Enter', () => {
      options.onKeyPressed({keyCode: KEY_ENTER + 1});
      expect(options.savedSearchesLoader.filterSavedSearches.called).to.be.false;
    });
  });

  describe('filterSavedSearches', () => {
    it('should use the loader to filter saved searches with the current terms', () => {
      let savedSearches = [{id: 'emf:123'}, {id: 'emf:456'}];
      options.savedSearchesLoader = stubSavedSearchesLoader(savedSearches);
      options.savedSearchFilter = 'projects';
      options.filterSavedSearches();
      expect(options.savedSearches).to.deep.equal(savedSearches);
    });
  });

  describe('selectSavedSearch(savedSearch)', () => {
    it('should invoke the component event onSearchSelected with the selected mode', () => {
      let savedSearch = {id: 'emf:123'};
      options.onSearchSelected = sinon.spy();
      options.selectSavedSearch(savedSearch);
      expect(options.onSearchSelected.calledOnce).to.be.true;
      expect(options.onSearchSelected.getCall(0).args[0]).to.deep.equal({savedSearch});
    });
  });

  describe('changeMode(mode)', () => {
    it('should invoke the component event onModeSelected with the selected mode', () => {
      let mode = 'advanced';
      options.onModeSelected = sinon.spy();
      options.changeMode(mode);
      expect(options.onModeSelected.calledOnce).to.be.true;
      expect(options.onModeSelected.getCall(0).args[0]).to.deep.equal({mode});
    });
  });

  describe('renderExternalSearch()', () => {
    it('should not render it if none of the systems is enabled', () => {
      expect(options.renderExternalSearch()).to.be.false;
    });

    it('should render it at least one of the systems is enabled', () => {
      options.configuration = stubConfiguration(true, false);
      expect(options.renderExternalSearch()).to.be.true;
      options.configuration = stubConfiguration(false, true);
      expect(options.renderExternalSearch()).to.be.true;
    });
  });

  function stubConfiguration(isDamEnabled = false, isCmsEnabled = false) {
    let stubbedConfiguration = stub(Configuration);
    stubbedConfiguration.get.withArgs(Configuration.EAI_DAM_ENABLED).returns(isDamEnabled);
    stubbedConfiguration.get.withArgs(Configuration.EAI_CMS_ENABLED).returns(isCmsEnabled);
    return stubbedConfiguration;
  }

  function stubSavedSearchesLoader(values = []) {
    let stubbedLoader = stub(SavedSearchesLoader);
    stubbedLoader.filterSavedSearches.returns(PromiseStub.resolve({values}));
    return stubbedLoader;
  }

});