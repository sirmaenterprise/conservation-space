import {TranslateService} from 'services/i18n/translate-service';
import {LanguageChangeSuccessEvent} from 'services/i18n/language-change-success-event';

describe('Test Translate service', function () {
  //mock translate adapter
  var translateAdapter = {
    translate() {
    },
    instant() {
    },
  };

  // stubs for translate adapter methods to be later called with different arguments
  var instantStub = sinon.stub(translateAdapter, 'instant');
  var translateStub = sinon.stub(translateAdapter, 'translate');

  // mock eventbus
  var eventbus = {
    subscriptions: [],
    subscribe(type, callback) {
      this.subscriptions[type.name] = callback;
      return type.name;
    },
    publish(type) {
      this.subscriptions[type.name]();
    },
    unsubscribe(func) {
    },
    reset() {
      this.subscriptions.splice(0, this.subscriptions.length);
    }
  };

  // spy for eventbus.unsubscribe calls
  var eventbusUnsubscribeSpy = sinon.spy(eventbus, 'unsubscribe');

  // the service we actually want to test
  var translateService = new TranslateService(translateAdapter, eventbus);

  // mock scope to test scope.on($destroy,...) call
  var scope = {
    subscriptions: [],
    $on(event, callback) {
      this.subscriptions[event] = callback;
    },
    $emit(event) {
      this.subscriptions[event]();
    },
    reset() {
      this.subscriptions.splice(0, this.subscriptions.length);
    }
  };

  // reset all mocks and spies
  beforeEach(function () {
    eventbus.reset();
    scope.reset();
    eventbusUnsubscribeSpy.reset();
  });

  it('Test instant translate without binding', function () {
    const LABEL = 'Dashboard';
    instantStub.withArgs('dashboard').returns(LABEL);
    expect(translateService.translateInstant('dashboard')).to.equal(LABEL);
  });

  it('Test instant translate with interpolation', function () {
    const LABEL = 'Hello ';
    const PARAMTERS = {
      name: 'World'
    };

    instantStub.withArgs('hello', PARAMTERS).returns(LABEL + PARAMTERS.name);
    expect(translateService.translateInstantWithInterpolation('hello', PARAMTERS)).to.equal(LABEL + PARAMTERS.name);
  });

  it('Test if instant translate throws Error for missing argument', function () {
    expect(function () {
      translateService.translateInstant('dashboard', scope);
    }).to.throw(Error);
  });

  it('Test instant translate with binding to component variable', function () {
    var withMenuArg = instantStub.withArgs('menu');
    withMenuArg.returns('Меню');
    var controller = {};
    var result = translateService.translateInstant('menu', scope, controller, 'menu');
    expect(result).to.be.equal('Меню');
    expect(controller.menu).to.be.equal('Меню');

    // Check that binding is properly updated after LanguageChangeSuccessEvent
    withMenuArg.returns('Главно меню');
    eventbus.publish(LanguageChangeSuccessEvent);
    expect(controller.menu).to.be.equal('Главно меню');

    scope.$emit('$destroy');
    expect(eventbusUnsubscribeSpy.callCount).to.equal(1);
    expect(eventbusUnsubscribeSpy.getCall(0).args[0]).to.be.equal(LanguageChangeSuccessEvent.name);
  });

  it('Test translate (promised) without binding', function (done) {
    var promise = new Promise(function (resolve, reject) {
      resolve('Работен плот');
    });
    translateStub.withArgs('dashboard').returns(promise);
    translateService.translate('dashboard').should.eventually.equal('Работен плот').notify(done);
  });

  it('Test if translate (promised) throws Error for missing argument', function () {
    expect(function () {
      translateService.translate('dashboard', scope);
    }).to.throw(Error);
  });

  it('Test translate (promised) with binding to component variable', function (done) {
    var promise = new Promise(function (resolve, reject) {
      resolve('Меню');
    });
    var withMenuArg = translateStub.withArgs('menu');
    withMenuArg.returns(promise);
    var controller = {};

    translateService.translate('menu', scope, controller, 'menu').then(function (value) {
      value.should.equal('Меню');
      controller.menu.should.equal('Меню');
      done();
    }).catch(done);
  });

  it('Test translate (promised) with binding to component variable after LanguageChangeSuccessEvent', function (done) {
    var promise = new Promise(function (resolve, reject) {
      resolve('Меню');
    });
    var withMenuArg = translateStub.withArgs('menu');
    withMenuArg.returns(promise);
    var controller = {};

    translateService.translate('menu', scope, controller, 'menu');

    promise = new Promise(function (resolve, reject) {
      resolve('Главно меню');
    });

    // change what will be returned after next call
    withMenuArg.returns(promise);

    // publish LanguageChangeSuccessEvent to trigger call of translateAdapter.translate()
    eventbus.publish(LanguageChangeSuccessEvent);

    setTimeout(function () {
      controller.menu.should.equal('Главно меню');
      done();
    }, 1);

    scope.$emit('$destroy');
    expect(eventbusUnsubscribeSpy.callCount).to.equal(1);
    expect(eventbusUnsubscribeSpy.getCall(0).args[0]).to.be.equal(LanguageChangeSuccessEvent.name);
  });
});
