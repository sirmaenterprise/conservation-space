import {UiPreference} from 'components/ui-preference/ui-preference';
import {StateParamsAdapterMock} from 'test/adapters/angular/state-params-adapter-mock';

describe('UiPreference', () => {
  let uiPreference;
  beforeEach(() => {
    let stateParamsAdapterMock = StateParamsAdapterMock.mockAdapter();
    // avoid component initialization
    stateParamsAdapterMock.setStateParam('mode', 'print');
    uiPreference = new UiPreference(undefined, stateParamsAdapterMock);
  });

  it('should deregister resize listeners on destroy', () => {
    uiPreference.resizeListeners = [
      sinon.spy(),
      sinon.spy()
    ];
    uiPreference.ngOnDestroy();

    uiPreference.resizeListeners.forEach((resizeListener) => {
      expect(resizeListener.callCount).to.equals(1);
    });
  });

  describe('validateConfig', () => {
    it('should throw error if both copyElementWidth and copyParentWidth are set as they are mutually exclusive', () => {
      uiPreference.uiPreferenceConfig = {
        copyElementWidth: true,
        copyParentWidth: true
      };
      expect(() => uiPreference.validateConfig()).to.throws(Error, 'copyElementWidth and copyParentWidth configurations are mutually exclusive.');
    });

    it('should throw error if both copyElementWidth or copyParentWidth and fillAvailableWidth are set as they are mutually exclusive', () => {
      uiPreference.uiPreferenceConfig = {
        copyElementWidth: true,
        fillAvailableWidth: true
      };
      expect(() => uiPreference.validateConfig()).to.throws(Error, 'copyElementWidth or copyParentWidth and fillAvailableWidth configurations are mutually exclusive.');
    });
  });
});
