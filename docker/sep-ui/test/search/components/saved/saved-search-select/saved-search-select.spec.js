import {SavedSearchSelect} from 'search/components/saved/saved-search-select/saved-search-select';
import {SavedSearchesLoader} from 'search/components/saved/saved-searches-loader';
import {SearchMediator} from 'search/search-mediator';
import {QueryBuilder} from 'search/utils/query-builder';
import {stub} from 'test/test-utils';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';

describe('SavedSearchSelect', () => {

  var savedSearchSelect;
  beforeEach(() => {
    savedSearchSelect = new SavedSearchSelect(mockElement(), mock$scope, mockTimeout(), {});
    savedSearchSelect.config.searchMediator = new SearchMediator(undefined, new QueryBuilder({}));
    savedSearchSelect.ngOnInit();
    savedSearchSelect.savedSearchesLoader = stub(SavedSearchesLoader);
  });

  describe('ngOnInit()', () => {
    it('should define selectConfig', () => {
      expect(savedSearchSelect.config.selectConfig).to.be.define;
    });
  });

  describe('getDefaultConfig()', () => {
    it('should return default config', () => {
      var config = savedSearchSelect.getDefaultConfig();
      expect(config.delay).to.be.equals(500);
      expect(config.multiple).to.be.false;
      expect(config.listeners).to.be.defined;
    });
  });

  describe('savedSearchLoader()', () => {
    it('should invoke the loader with the provided filtering terms', () => {
      let filterStub = savedSearchSelect.savedSearchesLoader.filterSavedSearches;

      savedSearchSelect.savedSearchLoader();
      expect(filterStub.calledWith('')).to.be.true;

      savedSearchSelect.savedSearchLoader({});
      expect(filterStub.calledWith('')).to.be.true;

      savedSearchSelect.savedSearchLoader({data: {}});
      expect(filterStub.calledWith('')).to.be.true;

      savedSearchSelect.savedSearchLoader({data: {q: 'test'}});
      expect(filterStub.calledWith('test')).to.be.true;
    });
  });

  describe('savedSearchConverter()', () => {
    it('should return empty result if there are no records', () => {
      var converter = savedSearchSelect.savedSearchConverter({values: []});
      expect(converter.length).to.be.equals(0);
    });

    it('should return converted records', () => {
      var records = {
        values: [{
          id: 'id',
          properties: {
            searchCriteria: '{\"criteria\" : {\"id":\"id",\"rules\":[]}}'
          }
        }]
      };
      var converted = savedSearchSelect.savedSearchConverter(records);
      expect(converted[0].id).to.be.equals('id');
      expect(converted[0].criteria.id).to.be.equals('id');
    });
  });

  describe('openSavedSearch()', () => {
    it('should clear selected search when open new one', () => {
      savedSearchSelect.selectedSearch = {};
      savedSearchSelect.openSavedSearch({params: {data: {}}});
      expect(savedSearchSelect.selectedSearch).to.be.undefined;
    });
  });

  describe('applyClosingListener()', () => {
    it('should catch select2 closing event', () => {
      var spy = savedSearchSelect.$element.on;
      expect(spy.callCount).to.equal(1);
      expect(spy.getCall(0).args[0]).to.be.equals('select2:closing');
    });
  });

  describe('triggerSavedSearchSelect()', () => {
    it('should trigger save search select', () => {
      savedSearchSelect.triggerSavedSearchSelect();
      var spyFind = savedSearchSelect.$element.find;
      var spySelect2 = spyFind.getCall(0).returnValue.select2;
      expect(spyFind.callCount).to.equal(1);
      expect(spySelect2.callCount).to.equal(1);
      expect(spySelect2.getCall(0).args[0]).to.equal('open');
    });
  });
});

function mockTimeout() {
  return (fn) => {
    fn();
  };
}

function mockElement(element) {
  return {
    find: sinon.spy(()=> {
      if (element) {
        return element
      }
      return {
        select2: sinon.spy()
      };
    }),
    on: sinon.spy()
  };
}