import { QuickSearch } from 'search/components/quick-search';
import { KEY_ENTER } from 'common/keys';

describe('QuickSearch', () => {
  describe('search()', () => {
    it('should call default onSearch callback if none is configured', () => {
      let navigateSpy = sinon.spy();
      QuickSearch.prototype.metaText = 'test';

      new QuickSearch({ navigate: navigateSpy }).search();

      expect(navigateSpy.called).to.be.true;
      expect(navigateSpy.getCall(0).args[0]).to.eq('search');
      expect(navigateSpy.getCall(0).args[1]).to.deep.eq({ metaText: 'test' });
      expect(navigateSpy.getCall(0).args[2]).to.deep.eq({ reload: true });
    });

    it('should call the configured onSearch callback', () => {
      let onSearchSpy = sinon.spy();
      QuickSearch.prototype.config = { onSearch: onSearchSpy };
      new QuickSearch().search();
      expect(onSearchSpy.called).to.be.true;
    });

    it('should clear metaText after search', () => {
      var quickSearch = new QuickSearch();
      quickSearch.search();
      expect(quickSearch.metaText).to.be.null;
    });
  });

  describe('onKeypress(event)', () => {
    let searchSpy = sinon.spy();

    beforeEach(() => {
      QuickSearch.prototype.config = { onSearch: searchSpy };
      searchSpy.reset();
    });

    it('should call search when enter is pressed', () => {
      new QuickSearch().onKeypress({ keyCode: KEY_ENTER });
      expect(searchSpy.called).to.be.true;
    });

    it('should not call search when enter is not pressed', () => {
      new QuickSearch().onKeypress({ keyCode: 1 });
      expect(searchSpy.called).to.be.false;
    });
  });
});
