import {SearchComponent} from 'search/components/common/search-component';

describe('SearchComponent', () => {

  it('should initialize even if no configuration object is provided', () => {
    expect(() => {
      new SearchComponent();
    }).to.not.throw(TypeError);
  });

  describe('isLockedOrDisabled(component)', () => {

    var searchComponent;
    beforeEach(() => {
      searchComponent = new SearchComponent({});
    });

    it('should properly determine that the component is not locked or disabled', () => {
      expect(searchComponent.isLockedOrDisabled()).to.be.false;
    });

    it('should properly determine that the component is disabled', () => {
      searchComponent.config.disabled = true;
      expect(searchComponent.isLockedOrDisabled()).to.be.true;
    });

    it('should properly determine that the component is locked if the configuration contains that component', () => {
      searchComponent.config.locked = ['testComponent'];
      expect(searchComponent.isLockedOrDisabled('testComponent')).to.be.true;
    });

    it('should properly determine that the component is not locked if the configuration does not contain that component', () => {
      searchComponent.config.locked = ['testComponent'];
      expect(searchComponent.isLockedOrDisabled('testComponent2')).to.be.false;
    });

  });

});