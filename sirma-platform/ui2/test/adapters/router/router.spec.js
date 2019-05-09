import {Router} from 'adapters/router/router';
import {Eventbus} from 'services/eventbus/eventbus';
import {PluginsService} from 'services/plugin/plugins-service';
import {RouterStateChangeStartEvent} from 'common/router/router-state-change-start-event';
import {RouterStateChangeSuccessEvent} from 'common/router/router-state-change-success-event';
import {stub} from 'test/test-utils';
import {PromiseStub} from 'test/promise-stub';
import {stubConfirmationDialogService} from 'test/components/dialog/confirmation-dialog-service.stub';

describe('Router', () => {

  let state;
  let rootScope;
  let pluginsService;
  let eventbus;
  let router;

  beforeEach(() => {
    state = {
      href: sinon.stub()
    };
    let subscriptions = [];
    rootScope = {
      $on: sinon.spy((event, callback) => {
        subscriptions[event] = callback;
      }),
      $emit(event, toState, toParams, fromState, fromParams) {
        subscriptions[event](event, toState, toParams, fromState, fromParams);
      },
      reset() {
        subscriptions.splice(0, subscriptions.length);
      }
    };
    pluginsService = stub(PluginsService);
    pluginsService.loadPluginServiceModules.returns(Promise.resolve({}));
    eventbus = stub(Eventbus);

    // Initialize service
    router = new Router(state, rootScope, eventbus, pluginsService, stubConfirmationDialogService(), PromiseStub);
  });

  function hasFiredStateChangeStartEvent() {
    // expect(eventbus.publish.calledOnce).to.be.true;
    expect(eventbus.publish.getCall(0).args[0] instanceof RouterStateChangeStartEvent).to.be.true;
  }

  function hasFiredStateChangeSuccessEvent() {
    // expect(eventbus.publish.calledOnce).to.be.true;
    expect(eventbus.publish.getCall(0).args[0] instanceof RouterStateChangeSuccessEvent).to.be.true;
  }

  function hasNotFiredStateChangeStartEvent() {
    expect(eventbus.publish.called).to.be.false;
  }

  it('should register $stateChangeStart event handler on init', () => {
    expect(rootScope.$on.getCall(0).args[0]).to.equal('$stateChangeStart');
  });

  it('should register $stateChangeSuccess event handler on init', () => {
    expect(rootScope.$on.getCall(1).args[0]).to.equal('$stateChangeSuccess');
  });

  it('should publish RouterStateChangeStartEvent event on $stateChangeStart event if skipRouteInterrupt is true', () => {
    let toState = {};
    rootScope.$emit('$stateChangeStart', toState, undefined, undefined, {skipRouteInterrupt: true});
    toState.resolve.pauseStateChange();
    hasFiredStateChangeStartEvent();
  });

  it('should publish RouterStateChangeStartEvent event on $stateChangeStart event if Leave button is pressed in confirmation dialog', () => {
    router.shouldInterrupt = () => true;
    let toState = {};
    rootScope.$emit('$stateChangeStart', toState, undefined, undefined, {});
    toState.resolve.pauseStateChange();
    hasFiredStateChangeStartEvent();
  });

  it('should not publish RouterStateChangeStartEvent event on $stateChangeStart event if Stay button is pressed in confirmation dialog', () => {
    router.shouldInterrupt = () => true;
    router.confirmationDialogService = stubConfirmationDialogService(false);
    let toState = {};
    rootScope.$emit('$stateChangeStart', toState, undefined, undefined, {});
    toState.resolve.pauseStateChange();
    hasNotFiredStateChangeStartEvent();
  });

  it('should publish RouterStateChangeSuccessEvent event on $stateChangeSuccess event', () => {
    rootScope.$emit('$stateChangeSuccess');
    hasFiredStateChangeSuccessEvent();
  });

  it('should provide the state url using $state', () => {
    const STATE_NAME = 'testState';
    const URL = '#/test';
    state.href.withArgs(STATE_NAME).returns(URL);
    expect(router.getStateUrl(STATE_NAME)).to.equal(URL);
  });

  it('should properly navigate with options when reload is false', () => {
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
    router.$state.params = {};
    router.$state.go = sinon.spy();
    router.onStateChangeSuccess = sinon.spy();

    router.navigate('state', 'params', {reload: true});

    expect(router.$state.go.calledOnce).to.be.true;
    expect(router.eventbus.publish.calledOnce).to.be.false;
    expect(router.onStateChangeSuccess.calledOnce).to.be.false;
  });
});