import {NavigationRetainment} from 'idoc/idoc-navigation/navigation-retainment';
import {IdocMocks} from '../idoc-mocks';
import {MODE_EDIT, MODE_PREVIEW} from 'idoc/idoc-constants';

describe('NavigationRetainment', ()=> {

  let windowAdapter = {
    window: {
      onbeforeunload: sinon.spy(),
      removeEventListener: sinon.spy()
    }
  };

  let translateService = {
    translateInstant: ()=> {
      return 'router.interrupt.dialog.message';
    }
  };
  describe('retain', () => {
    it('should retain', () => {
      let navigationRetainment = new NavigationRetainment(windowAdapter, mockRouter(true), translateService);
      expect(navigationRetainment.retain()).to.be.equal('router.interrupt.dialog.message');
    });

    it('should not retain', () => {
      let navigationRetainment = new NavigationRetainment(windowAdapter, mockRouter(false), translateService);
      expect(navigationRetainment.retain()).to.be.undefined;
    });
  });

  it('should calculate if document must be unlocked', () => {
    let navigationRetainment = new NavigationRetainment(windowAdapter, mockRouter(true), translateService, IdocMocks.mockStateParamsAdapter("emf:123", MODE_EDIT));
    expect(navigationRetainment.isLocked()).to.be.true;
    navigationRetainment = new NavigationRetainment(windowAdapter, mockRouter(true), translateService, IdocMocks.mockStateParamsAdapter("emf:123", MODE_PREVIEW));
    expect(navigationRetainment.isLocked()).to.be.false;
  });

  it('should call unlock', () => {
    let actionsService = {
      unlock: sinon.stub()
    };

    let navigationRetainment = new NavigationRetainment(windowAdapter, mockRouter(false), translateService,
      IdocMocks.mockStateParamsAdapter('emf:123', MODE_EDIT), actionsService);

    navigationRetainment.unlock();

    expect(actionsService.unlock.calledWith('emf:123')).to.be.true;
  });

  function mockRouter(interrupt) {
    return {
      shouldInterrupt: ()=> {
        return interrupt;
      }
    };
  }

});