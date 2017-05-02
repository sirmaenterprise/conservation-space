import {Router} from 'adapters/router/router';
import {Eventbus} from 'services/eventbus/eventbus';
import {RouterStateChangeStartEvent} from 'common/router/router-state-change-start-event';
import {RouterStateChangeSuccessEvent} from 'common/router/router-state-change-success-event';
import {PromiseAdapterMock} from 'test/adapters/angular/promise-adapter-mock';
import {DialogService} from 'components/dialog/dialog-service';

describe('Router', () => {

  var state = {
    href: sinon.stub()
  };

  var rootScope = {
    subscriptions: [],
    $on(event, callback) {
      this.subscriptions[event] = callback;
    },
    $emit(event, toState, toParams, fromState, fromParams) {
      this.subscriptions[event](event, toState, toParams, fromState, fromParams);
    },
    reset() {
      this.subscriptions.splice(0, this.subscriptions.length);
    }
  };
  var modules = {};
  var pluginService = {
    loadPluginServiceModules: sinon.spy(() => {
      return Promise.resolve(modules);
    })
  };

  describe('on init', () => {
    it('should register $stateChangeStart event handler on init', () => {
      var rootScope = {
        $on: sinon.stub()
      };
      new Router(state, rootScope, new Eventbus(), pluginService);
      expect(rootScope.$on.getCall(0).args[0]).to.equal('$stateChangeStart');
    });

    it('should register $stateChangeSuccess event handler on init', () => {
      var rootScope = {
        $on: sinon.stub()
      };
      new Router(state, rootScope, new Eventbus(), pluginService);
      expect(rootScope.$on.getCall(1).args[0]).to.equal('$stateChangeSuccess');
    });
  });

  it('should publish RouterStateChangeStartEvent event on $stateChangeStart event if skipRouteInterrupt is true', (done) => {
    let eventbus = {
      publish: sinon.spy()
    };
    new Router(state, rootScope, eventbus, pluginService, undefined, undefined, PromiseAdapterMock.mockImmediateAdapter());
    let toState = {};
    rootScope.$emit('$stateChangeStart', toState, undefined, undefined, {skipRouteInterrupt: true});
    toState.resolve.pauseStateChange().then(() => {
      expect(eventbus.publish.getCall(0).args[0] instanceof RouterStateChangeStartEvent).to.be.true;
      done();
    }).catch(done);
  });

  it('should publish RouterStateChangeStartEvent event on $stateChangeStart event if Leave button is pressed in confirmation dialog', (done) => {
    let eventbus = {
      publish: sinon.spy()
    };
    let translateService = {
      translateInstant: (key) => {
        return key;
      }
    };
    let dialogService = {
      confirmation: (message, header, opts) => {
        let dialogConfig = {
          dismiss: () => {
          }
        };
        opts.onButtonClick(DialogService.YES, undefined, dialogConfig);
      }
    };
    let router = new Router(state, rootScope, eventbus, pluginService, dialogService, translateService, PromiseAdapterMock.mockImmediateAdapter());
    router.shouldInterrupt = () => {
      return true;
    };
    let toState = {};
    rootScope.$emit('$stateChangeStart', toState, undefined, undefined, {});
    toState.resolve.pauseStateChange().then(() => {
      expect(eventbus.publish.getCall(0).args[0] instanceof RouterStateChangeStartEvent).to.be.true;
      done();
    }).catch(done);
  });

  it('should not publish RouterStateChangeStartEvent event on $stateChangeStart event if Stay button is pressed in confirmation dialog', (done) => {
    let eventbus = {
      publish: sinon.spy()
    };
    let translateService = {
      translateInstant: (key) => {
        return key;
      }
    };
    let dialogService = {
      confirmation: (message, header, opts) => {
        let dialogConfig = {
          dismiss: () => {
          }
        };
        opts.onButtonClick(DialogService.CANCEL, undefined, dialogConfig);
      }
    };
    let router = new Router(state, rootScope, eventbus, pluginService, dialogService, translateService, PromiseAdapterMock.mockImmediateAdapter());
    router.shouldInterrupt = () => {
      return true;
    };
    let toState = {};
    rootScope.$emit('$stateChangeStart', toState, undefined, undefined, {});
    toState.resolve.pauseStateChange().catch(() => {
      expect(eventbus.publish).to.have.not.been.called;
      done();
    }).catch(done);
  });

  it('should publish RouterStateChangeSuccessEvent event on $stateChangeSuccess event', () => {
    let eventbus = {
      publish: sinon.spy()
    };
    new Router(state, rootScope, eventbus, pluginService);
    rootScope.$emit('$stateChangeSuccess');
    expect(eventbus.publish.getCall(0).args[0] instanceof RouterStateChangeSuccessEvent).to.be.true;
  });

  it('should provide the state url using $state', () => {
    const STATE_NAME = 'testState';
    const URL = '#/test';
    state.href.withArgs(STATE_NAME).returns(URL);

    var router = new Router(state, rootScope, null, pluginService);

    expect(router.getStateUrl(STATE_NAME)).to.equal(URL);
  });
});