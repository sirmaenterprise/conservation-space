import {WindowAdapter, TARGET_BLANK} from 'adapters/angular/window-adapter';

describe('WindowAdapter', () => {

  describe('navigate(url)', () => {
    it('should set the url to window.location.href', () => {
      var realWindow = {location: {}};
      var adapter = new WindowAdapter(realWindow, {});

      adapter.navigate('/test');
      expect(realWindow.location.href).to.eq('/test');
    });
  });

  describe('openInNewTab(url)', () => {
    it('should open the given URL in another tab', () => {
      var window = {
        open: sinon.spy(),
        location: {}
      };
      var adapter = new WindowAdapter(window, {});

      adapter.openInNewTab('#/idoc/');
      expect(window.open.calledOnce).to.be.true;
      expect(window.open.getCall(0).args[0]).to.equal('#/idoc/');
      expect(window.open.getCall(0).args[1]).to.equal(TARGET_BLANK);
    });
  });
});