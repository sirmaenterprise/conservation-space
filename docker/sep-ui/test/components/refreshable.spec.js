import {Refreshable} from 'components/refreshable';
import {Eventbus} from 'services/eventbus/eventbus';
import {SharedObjectUpdatedEvent} from 'idoc/shared-object-updated-event';

describe('Refreshable', () => {
  let eventbus = {
    subscribe: () => {}
  };

  it('should throw error on init if the refresh method is not implemeneted in widget', () => {
    expect(() => {
      new RefreshableWidgetNoRefreshMethod(eventbus)
    }).to.throw(Error, /Must implement refresh method in component!/);
  });

  it('should subscribe to an object updated event on init', () => {
    let spySubscribe = sinon.spy(eventbus, 'subscribe');
    new RefreshableWidget(eventbus);
    expect(spySubscribe.callCount).to.equal(1);
  });

  it('should invoke the refresh if the object updated event is fired', () => {
    let bus = new Eventbus();
    let spyRefresh = sinon.spy(RefreshableWidget.prototype, 'refresh');
    new RefreshableWidget(bus);
    bus.publish(new SharedObjectUpdatedEvent({ objectId: 'emf:123456' }));
    expect(spyRefresh.callCount).to.equal(1);
    expect(spyRefresh.getCall(0).args[0]).to.eql({ objectId: 'emf:123456' });
  });

  it('should unsubscribe from sharedObjectUpdateEvent when widget is destroyed', () => {
    let bus = new Eventbus();
    let widget = new RefreshableWidget(bus);
    let spyUnsubscribe = sinon.spy(widget.sharedObjectUpdateEvent, 'unsubscribe');
    widget.ngOnDestroy();
    expect(spyUnsubscribe.callCount).to.equal(1)
  });
});

class RefreshableWidgetNoRefreshMethod extends Refreshable {
  constructor(eventbus) {
    super(eventbus);
  }
}

class RefreshableWidget extends Refreshable {
  constructor(eventbus) {
    super(eventbus);
  }

  refresh(data) {

  }
}