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

  it('should call unlock via ajax', () => {
    let authenticationServiceMock = {
      getToken: ()=> {return "123"}
    };
    let navigationRetainment = new NavigationRetainment(windowAdapter, mockRouter(false), translateService,
      IdocMocks.mockStateParamsAdapter("emf:123", MODE_EDIT), authenticationServiceMock);
    let spySubscribe = sinon.spy($, 'ajax');
    navigationRetainment.unlock();
    let expected =  {
      type: 'POST',
      contentType: 'application/vnd.seip.v2+json',
      accept: 'application/vnd.seip.v2+json',
      url: '/remote/api/instances/emf:123/actions/unlock',
      async: false,
      headers: {Authorization: 'Bearer 123'}
    };
    expect(spySubscribe.getCall(0).args[0]).to.deep.equal(expected);
  });
});

function mockRouter(interrupt) {
  return {
    shouldInterrupt: ()=> {
      return interrupt;
    }
  }
}