import {DateService} from 'services/i18n/date-service';
import {LanguageChangeSuccessEvent} from 'services/i18n/language-change-success-event';
import {MockEventbus} from 'test/test-utils';

describe('Test Date service', function() {
  var filterResult;
  var filter = function() {
    return function() {
      return filterResult;
    }
  };

  var eventbus = new MockEventbus();

  // spy for eventbus.unsubscribe calls
  var eventbusUnsubscribeSpy = sinon.spy(eventbus, 'unsubscribe');

  // the service we actually want to test
  var dateService = new DateService(filter, eventbus);

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
  beforeEach(function() {
    eventbus.reset();
    scope.reset();
    eventbusUnsubscribeSpy.reset();
  });

  it('Test format date without binding', function() {
    filterResult = 'date';
    expect(dateService.formatDate('testDate')).to.be.equal('date');
  });

  it('Test if format date throws Error for binding', function() {
    expect(function() { dateService.formatDate('testDate', '', scope); }).to.throw(Error);
  });

  it('Test format date with binding', function() {
    filterResult = 'date';
    var controller = {};
    expect(dateService.formatDate('testDate', '', scope, controller, 'formattedDate')).to.be.equal('date');
    expect(controller.formattedDate).to.be.equal('date');

    filterResult = 'modified date';
    eventbus.publish(new LanguageChangeSuccessEvent());

    expect(controller.formattedDate).to.be.equal('modified date');

    scope.$emit('$destroy');
    expect(eventbusUnsubscribeSpy.callCount).to.equal(1);
    expect(eventbusUnsubscribeSpy.getCall(0).args[0]).to.be.equal(LanguageChangeSuccessEvent.name);
  });
});
