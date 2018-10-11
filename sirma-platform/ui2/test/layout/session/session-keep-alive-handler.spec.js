import {SessionKeepAliveHandler} from 'layout/session/session-keep-alive-handler';
import {Configuration} from 'common/application-config';

describe('SessionKeepAliveHandler', () => {

  var _addActivityMonitors;

  function getConfigurationInstance() {
    var eventbus = {
      subscribe: () => {
      }
    };
    return new Configuration(undefined, eventbus);
  }

  beforeEach(() => {
    _addActivityMonitors = SessionKeepAliveHandler.prototype.addActivityMonitors;
    SessionKeepAliveHandler.prototype.addActivityMonitors = sinon.spy();
    SessionKeepAliveHandler.prototype.startSessionTimer = sinon.spy();
  });

  it('should unsubscribe from events when component is destroyed', () => {
    SessionKeepAliveHandler.prototype.addActivityMonitors = _addActivityMonitors;
    let unsubscribeSpy = sinon.spy();
    let eventbus = {
      subscribe: () => {
        return {
          unsubscribe: unsubscribeSpy
        };
      }
    };
    var config = getConfigurationInstance();
    config.configs = {};
    config.configs[Configuration.SESSION_TIMEOUT_PERIOD] = 1;
    config.configs[Configuration.SESSION_TIMEOUT] = true;
    var handler = new SessionKeepAliveHandler(config, null, mockWindowAdapter(), mockLocalStorageService(), eventbus);
    handler.ngOnInit();
    handler.ngOnDestroy();
    expect(unsubscribeSpy.calledOnce).to.be.true;
  });

  describe('ngOnInit', () => {
    var config = getConfigurationInstance();
    config.configs = {};
    config[Configuration.SESSION_TIMEOUT_PERIOD] = -1;
    config[Configuration.SESSION_TIMEOUT] = false;

    it('should not setup handler if redirect on timeout config is false', () => {
      config.configs[Configuration.SESSION_TIMEOUT_PERIOD] = 1;
      config.configs[Configuration.SESSION_TIMEOUT] = false;

      var handler = new SessionKeepAliveHandler(config, null, mockWindowAdapter(), mockLocalStorageService());
      handler.ngOnInit();

      expect(handler.addActivityMonitors.called).to.be.false;
      expect(handler.startSessionTimer.called).to.be.false;
    });

    it('should not setup handler if session ttl is less than 1', () => {
      config.configs[Configuration.SESSION_TIMEOUT_PERIOD] = -1;
      config.configs[Configuration.SESSION_TIMEOUT] = true;

      var handler = new SessionKeepAliveHandler(config, null, mockWindowAdapter(), mockLocalStorageService());
      handler.ngOnInit();

      expect(handler.addActivityMonitors.called).to.be.false;
      expect(handler.startSessionTimer.called).to.be.false;
    });

    it('should setup handler if both session ttl is greater than zero and redirect on timeout is true', () => {
      config.configs[Configuration.SESSION_TIMEOUT_PERIOD] = 1;
      config.configs[Configuration.SESSION_TIMEOUT] = true;

      var handler = new SessionKeepAliveHandler(config, null, mockWindowAdapter(), mockLocalStorageService());
      handler.ngOnInit();
      expect(handler.addActivityMonitors.called).to.be.true;
      expect(handler.startSessionTimer.called).to.be.true;
    });
  });

  describe('clearLocalStorageVars()', () => {
    var config = {get: () => {}};

    it('should remove all session timeout related props from local store', () => {
      var removeSpy = sinon.spy();
      var handler = new SessionKeepAliveHandler(config, null, null, mockLocalStorageService(null, null, removeSpy));

      expect(removeSpy.callCount).to.eq(2);
      expect(removeSpy.getCall(0).args[0]).to.eq('session.lastUserActivity');
      expect(removeSpy.getCall(1).args[0]).to.eq('session.timeout');
    });
  });

  describe('handleTimeoutEvent(event)', () => {
    var authSpy;
    var removeTokenSpy;
    var configService = {get: () => {}};

    beforeEach(() => {
      authSpy = sinon.spy();
      removeTokenSpy = sinon.spy();
    });

    it('should remove token and redirect to login page', () => {
      var handler = new SessionKeepAliveHandler(configService, mockAuthService(authSpy, removeTokenSpy), mockWindowAdapter(), mockLocalStorageService());
      handler.handleTimeoutEvent('session.timeout', 'true');
      expect(authSpy.called).to.be.true;
      expect(removeTokenSpy.called).to.be.true;
    });

    it('should not remove token and redirect to login page if not session timeout storage event', () => {
      var handler = new SessionKeepAliveHandler(configService, mockAuthService(authSpy, removeTokenSpy), mockWindowAdapter(), mockLocalStorageService());
      handler.handleTimeoutEvent('session.timeout');
      expect(authSpy.called).to.be.false;
      expect(removeTokenSpy.called).to.be.false;
    });


    it('should not remove token and redirect to login page if new value of storage event is not true (string)', () => {
      var handler = new SessionKeepAliveHandler(configService, mockAuthService(authSpy, removeTokenSpy), mockWindowAdapter(), mockLocalStorageService());
      handler.handleTimeoutEvent('session.timeout', true);
      expect(authSpy.called).to.be.false;
      expect(removeTokenSpy.called).to.be.false;
    });
  });

  describe('registerUserActivity()', () => {
    it('should cancel logout timeout after ping if there is one', () => {
      var windowAdapter = mockWindowAdapter();

      var time = new Date().getTime();
      var localStoreSetSpy = sinon.spy();
      var handler = new SessionKeepAliveHandler(null, null, windowAdapter, mockLocalStorageService(null, localStoreSetSpy, null));
      handler.timerId = '123';
      handler.registerUserActivity(time);

      expect(windowAdapter.window.clearTimeout.called).to.be.true;
      expect(handler.startSessionTimer.called).to.be.true;
      expect(handler.startSessionTimer.getCall(0).args[0]).to.eq(time);
      expect(localStoreSetSpy.called).to.be.true;
      expect(localStoreSetSpy.getCall(0).args[0]).to.eq('session.lastUserActivity');
      expect(localStoreSetSpy.getCall(0).args[1]).to.be.ok;
    });

    it('should not cancel logout timeout after ping if there is none', () => {
      var windowAdapter = mockWindowAdapter();

      var time = new Date().getTime();
      var handler = new SessionKeepAliveHandler(null, null, windowAdapter, mockLocalStorageService());
      handler.registerUserActivity(time);

      expect(windowAdapter.window.clearTimeout.called).to.be.false;
      expect(handler.startSessionTimer.called).to.be.true;
      expect(handler.startSessionTimer.getCall(0).args[0]).to.eq(time);
    });
  });

  function mockLocalStorageService(getSpy, setSpy, removeSpy) {
    return {
      get: getSpy ? getSpy : () => {},
      set: setSpy ? setSpy : () => {},
      remove: removeSpy ? removeSpy : () => {},
    };
  }

  function mockWindowAdapter() {
    return {
      window: {
        addEventListener: () => {},
        setTimeout: (cb) => {
          cb();
        },
        clearTimeout: sinon.spy()
      }
    };
  }

  function mockAuthService(authenticateSpy, removeTokenSpy) {
    return {
      authenticate: authenticateSpy,
      removeToken: removeTokenSpy
    };
  }

});