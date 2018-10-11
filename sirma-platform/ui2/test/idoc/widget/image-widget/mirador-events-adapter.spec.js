import {MiradorEventsAdapter} from 'idoc/widget/image-widget/mirador-integration/mirador-events-adapter';
import {WindowSlotAddedEvent} from 'idoc/widget/image-widget/mirador-integration/mirador-events';
import {WINDOW_SLOT_ADDED} from 'idoc/widget/image-widget/mirador-integration/mirador-events-adapter';

describe('MiradorEventsAdapter', () => {

  it('should subscribe to the mirador events', () => {
    let eventsEmitter = {
      subscribe: sinon.spy(),
      unsubscribe: sinon.spy()
    };
    eventsEmitter.publish = function (event, options) {
      return this.subscribe(event, options);
    };
    let eventbus = {subscribe: sinon.spy()};
    let widgetId = 'widgetId';

    new MiradorEventsAdapter(eventbus, eventsEmitter, widgetId);

    expect(eventsEmitter.subscribe.callCount).to.equal(3);
    eventsEmitter.publish(WINDOW_SLOT_ADDED[0], {'id': 'slotId'});
    expect(eventsEmitter.subscribe.args[0][0]).to.equal(WINDOW_SLOT_ADDED[0]);
  });

  it('should publish eventbus event', ()=> {
    let eventbus = {
      publish: sinon.spy(),
      subscribe: sinon.spy()
    };
    let eventsEmitter = {
      subscribe: sinon.spy()
    };

    let adapter = new MiradorEventsAdapter(eventbus, eventsEmitter, 'widgetId');
    adapter.publishEventbusEvent(WINDOW_SLOT_ADDED[1], {'id': 'publish'});

    expect(eventbus.publish.callCount).to.equal(1);
    expect(eventbus.publish.args[0][0].args[0]).to.equal('widgetId');
  });

  it('should publish mirador event', ()=> {
    let eventbus = {
      publish: sinon.spy(),
      subscribe: sinon.spy()
    };
    let eventsEmitter = {
      publish: sinon.spy(),
      subscribe: sinon.spy()
    };

    let adapter = new MiradorEventsAdapter(eventbus, eventsEmitter, 'widgetId');
    adapter.publishMiradorEvent(WINDOW_SLOT_ADDED[0], {'id': 'publish'});

    expect(eventsEmitter.publish.callCount).to.equal(1);
    expect(eventsEmitter.publish.args[0][0]).to.equal(WINDOW_SLOT_ADDED[0]);
  });

  it('should handle the annoShapeCreatedEvent', ()=> {
    let eventsEmitter = {
      publish: sinon.spy(),
      subscribe: sinon.spy()
    };

    let eventbus = {
      subscribe: sinon.spy()
    };
    let adapter = new MiradorEventsAdapter(eventbus, eventsEmitter, 'widgetId');

    adapter.publishEventbusEvent = sinon.spy();
    let options = {
      windowId: 'windowId'
    };
    adapter.handleAnnoShapeCreated({}, options);

    expect(adapter.publishEventbusEvent.callCount).to.equal(2);
    expect(adapter.currentSlotId).to.equal(options.windowId);
  });

  it('should destroy the events adapter', ()=> {
    let eventsEmitter = {
      publish: sinon.spy(),
      subscribe: sinon.spy(),
      unsubscribe: sinon.spy()
    };
    let eventbus = {
      publish: sinon.spy(),
      subscribe: function () {
        return {unsubscribe: sinon.spy()}
      }
    };
    let adapter = new MiradorEventsAdapter(eventbus, eventsEmitter, 'widgetId');
    adapter.slotToImageId = new Map();
    adapter.slotToImageId.set('first', 'asdf');
    adapter.slotToImageId.delete = sinon.spy();
    adapter.destroy();
    expect(adapter.eventbusEvents[0].unsubscribe.called).to.be.true;
  });

  it('should handle update annotation event', ()=> {
    let eventsEmitter = {
      publish: sinon.spy(),
      subscribe: sinon.spy(),
      unsubscribe: sinon.spy()
    };
    let eventbus = {
      publish: sinon.spy(),
      subscribe: function () {
        return {unsubscribe: sinon.spy()}
      }
    };
    let adapter = new MiradorEventsAdapter(eventbus, eventsEmitter, 'widgetId');
    adapter.publishMiradorEvent = sinon.spy();
    adapter.handleUpdateAnnotationListEvent(['widgetId']);
    expect(adapter.publishMiradorEvent.called).to.be.true;
  });

});