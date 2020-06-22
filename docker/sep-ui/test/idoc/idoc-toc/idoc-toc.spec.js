import {IdocToC} from 'idoc/idoc-toc/idoc-toc';
import {stub} from 'test/test-utils';
import {Eventbus} from 'services/eventbus/eventbus';
import {AfterIdocContentModelUpdateEvent} from 'idoc/events/after-idoc-content-model-update-event';
import {IdocContentSanitizedEvent} from 'idoc/events/idoc-content-sanitized-event';

describe('Tests for idoc toc', function () {
  let toc;

  before(function () {
    fixture.setBase('test/idoc/idoc-toc');
  });

  beforeEach(function () {
    let config = {
      source: 'deleteCollapseContainer',
      eventbus: {
        instance: new MockEventbus(),
        channel: 'mock'
      },
      tab: {
        id: '123'
      }
    };
    toc = new IdocToC(config);
    this.template = fixture.load('idoc-toc-dom-manipulator.html');
  });

  after(function () {
    fixture.cleanup();
  });

  it('should remove collapse container dom element and unsubscribe from all events', function () {
    toc.destroy();
    toc.events.forEach(function (event) {
      expect(event.unsubscribe.called).to.be.true;
    });
    expect($('#deleteCollapseContainer').length).to.be.zero;
  });

  it('should subscribe for AfterIdocContentModelUpdateEvent and IdocContentSanitizedEvent if editor is opened', function () {
    toc.config.source = '<div id="1"></div>';
    toc.config.eventbus.instance = stub(Eventbus);
    toc.subscribeAndRefreshToC('1');

    expect(toc.config.eventbus.instance.subscribe.callCount).to.equal(2);
    expect(toc.config.eventbus.instance.subscribe.calledWith(AfterIdocContentModelUpdateEvent)).to.be.true;
    expect(toc.config.eventbus.instance.subscribe.calledWith(IdocContentSanitizedEvent)).to.be.true;
  });

  it('should not subscribe for AfterIdocContentModelUpdateEvent or IdocContentSanitizedEvent if not same editor is opened', function () {
    toc.config.source = '<div id="2"></div>';
    toc.config.eventbus.instance = stub(Eventbus);
    toc.subscribeAndRefreshToC('1');
    expect(toc.config.eventbus.instance.subscribe.callCount).to.equal(0);
  });

});

class MockEventbus {
  subscribe() {
    return {
      unsubscribe: sinon.spy()
    };
  }
}