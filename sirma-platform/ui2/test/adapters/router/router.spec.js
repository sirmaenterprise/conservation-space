import {Router} from 'adapters/router/router';
import {Eventbus} from 'services/eventbus/eventbus';
import {RouterStateChangeStartEvent} from 'common/router/router-state-change-start-event';
import {RouterStateChangeSuccessEvent} from 'common/router/router-state-change-success-event';
import {stub} from 'test/test-utils';
import {PromiseStub} from 'test/promise-stub';
import {stubConfirmationDialogService} from 'test/components/dialog/confirmation-dialog-service.stub';

describe('Router', () => {

  let state = {
    href: sinon.stub()
  };

  let rootScope = {
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
  let modules = {};
  let pluginService = {
    loadPluginServiceModules: sinon.spy(() => {
      return Promise.resolve(modules);
    })
  };

  describe('on init', () => {
    it('should register $stateChangeStart event handler on init', () => {
      let rootScope = {
        $on: sinon.stub()
      };
      new Router(state, rootScope, new Eventbus(), pluginService, stubConfirmationDialogService(), PromiseStub);
      expect(rootScope.$on.getCall(0).args[0]).to.equal('$stateChangeStart');
    });

    it('should register $stateChangeSuccess event handler on init', () => {
      let rootScope = {
        $on: sinon.stub()
      };
      new Router(state, rootScope, new Eventbus(), pluginService, stubConfirmationDialogService(), PromiseStub);
      expect(rootScope.$on.getCall(1).args[0]).to.equal('$stateChangeSuccess');
    });
  });

  it('should publish RouterStateChangeStartEvent event on $stateChangeStart event if skipRouteInterrupt is true', (done) => {
    let eventbus = {
      publish: sinon.spy()
    };
    new Router(state, rootScope, eventbus, pluginService, stubConfirmationDialogService(), PromiseStub);
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
    let router = new Router(state, rootScope, eventbus, pluginService, stubConfirmationDialogService(), PromiseStub);
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
    let router = new Router(state, rootScope, eventbus, pluginService, stubConfirmationDialogService(false), PromiseStub);
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
    new Router(state, rootScope, eventbus, pluginService, stubConfirmationDialogService(), PromiseStub);
    rootScope.$emit('$stateChangeSuccess');
    expect(eventbus.publish.getCall(0).args[0] instanceof RouterStateChangeSuccessEvent).to.be.true;
  });

  it('should provide the state url using $state', () => {
    const STATE_NAME = 'testState';
    const URL = '#/test';
    state.href.withArgs(STATE_NAME).returns(URL);

    let router = new Router(state, rootScope, null, pluginService, stubConfirmationDialogService(), PromiseStub);

    expect(router.getStateUrl(STATE_NAME)).to.equal(URL);
  });

  it('should properly navigate with options when reload is false', () => {
    let router = new Router(state, rootScope, stub(Eventbus), pluginService, stubConfirmationDialogService(), PromiseStub);
    router.$state = {
      params: {},
      go: sinon.spy(),
      current: {
        name: 'current-state'
      }
    };
    router.onStateChangeSuccess = sinon.spy();
    router.getStateByName = sinon.spy((state) => {
      return state;
    });

    router.navigate('state', 'params', {reload: false});
    // the provided reload: false option transitions to a notify: false
    expect(router.$state.go.calledOnce).to.be.true;
    expect(router.$state.go.calledWith('state', 'params', {reload: false, notify: false})).to.be.true;
    // the actual event should be fired through onStateChangeSuccess
    expect(router.onStateChangeSuccess.calledOnce).to.be.true;
    expect(router.onStateChangeSuccess.calledWith('$stateChangeSuccess', 'state', 'params')).to.be.true;
  });

  it('should properly navigate with options when reload is true', () => {
    let router = new Router(state, rootScope, stub(Eventbus), pluginService, stubConfirmationDialogService(), PromiseStub);
    router.$state.params = {};
    router.$state.go = sinon.spy();
    router.onStateChangeSuccess = sinon.spy();

    router.navigate('state', 'params', {reload: true});

    expect(router.$state.go.calledOnce).to.be.true;
    expect(router.eventbus.publish.calledOnce).to.be.false;
    expect(router.onStateChangeSuccess.calledOnce).to.be.false;
  });
});