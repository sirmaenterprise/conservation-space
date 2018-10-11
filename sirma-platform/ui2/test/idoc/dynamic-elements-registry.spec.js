import {DynamicElementsRegistry} from 'idoc/dynamic-elements-registry';
import {WidgetReadyEvent} from 'idoc/widget/widget-ready-event';
import {IdocReadyEvent} from 'idoc/idoc-ready-event';
import {EditorReadyEvent} from 'idoc/editor/editor-ready-event';
import {ImageReadyEvent} from 'idoc/events/image-ready-event';

describe('DynamicElementsRegistry', () => {
  let eventbus;
  let pluginService;
  let registry;

  beforeEach(() => {
    eventbus = {
      publish: sinon.spy(),
      subscribe: sinon.spy(function () {
        return {
          unsubscribe: sinon.spy()
        };
      })
    };

    pluginService = {
      getDefinitions: sinon.spy(() => [{name: 'widget1'}])
    };

    registry = new DynamicElementsRegistry((cb) => cb(), eventbus, pluginService);
  });

  it('should load available widgets', () => {
    expect(registry.availableWidgets).to.deep.eq(['widget1']);
  });

  describe('init()', () => {
    it('should initialize count and reset ready cache', () => {
      registry.registerEventHandlers = sinon.spy();

      registry.init();
      expect(registry.count).to.equal(0);
      // editors register themselves.
      registry.handleEditor('editor1234');
      registry.handleWidget('widget1', 'widget1234');
      expect(registry.count).to.equal(2);
      expect(registry.readyCache).to.deep.eq({editor1234: false, widget1234: false});
      expect(registry.registerEventHandlers.calledOnce).to.be.true;
    });
  });

  describe('reload()', () => {
    it('should call destroy method and then init', () => {
      registry.init = sinon.spy();
      registry.destroy = sinon.spy();
      registry.reload();

      expect(registry.destroy.calledOnce).to.be.true;
      expect(registry.init.calledOnce).to.be.true;
    });
  });

  describe('handleWidget($widget)', () => {
    beforeEach(()=>{
      registry.init();
    });

    it('should not increment count if widget does not have a name', () => {
      registry.handleWidget();
      expect(registry.count).to.eq(0);
    });

    it('should not increment count if widget is not available', () => {
      registry.handleWidget('widget2', 'widget123');
      expect(registry.count).to.eq(0);
    });

    it('should increment count', () => {
      registry.handleWidget('widget1', 'widget1234');
      expect(registry.count).to.eq(1);
    });
  });

  describe('#handeEditor', () => {
    it('should add editor', () => {
      registry.reload();
      registry.handleDynamicElementReadyEvent = sinon.spy();
      registry.handleEditor('editor1234');
      registry.handleEditorReadyEvent({editorName: 'editor1234'});
      expect(registry.handleDynamicElementReadyEvent.calledWith({id: 'editor1234'})).to.be.true;
    });
  });

  describe('handleImage($img)', () => {
    beforeEach(()=>{
      registry.init();
    });

    it('should register onload listener and increment count', () => {
      let element = [{}];
      element.on = sinon.spy();
      element.off = sinon.spy();

      registry.count = 1;
      registry.handleImage(element);
      expect(element.on.calledOnce).to.be.true;
      expect(element.on.getCall(0).args[0]).to.eq('load error');
      expect(registry.count).to.eq(2);
    });

    it('should register onload listener and increment count if loading of image is not completed', () => {
      let element = [{complete: false}];
      element.on = sinon.spy();
      element.off = sinon.spy();
      registry.count = 1;
      registry.handleImage(element);
      expect(element.on.calledOnce).to.be.true;
      expect(element.on.getCall(0).args[0]).to.eq('load error');
      expect(registry.count).to.eq(2);
    });

    it('should not register onload listener and increment count if loading of image is completed', () => {
      let element = [{complete: true}];
      element.on = sinon.spy();
      element.off = sinon.spy();
      registry.count = 1;
      registry.handleImage(element);
      expect(element.on.calledOnce).to.be.false;
      expect(registry.count).to.eq(1);
    });
  });

  describe('handleDynamicElementReadyEvent()', () => {
    it('should publish IdocReadyEvent when count reaches zero', () => {
      registry.count = 2;
      registry.publishIdocReadyEvent = sinon.spy();
      registry.handleDynamicElementReadyEvent({});

      expect(registry.publishIdocReadyEvent.called).to.be.false;

      registry.handleDynamicElementReadyEvent({});
      expect(registry.publishIdocReadyEvent.calledOnce).to.be.true;
    });
  });

  describe('handleWidgetReadyEvent(event)', () => {
    beforeEach(() => {
      let real = registry.handleDynamicElementReadyEvent.bind(registry);
      registry.handleDynamicElementReadyEvent = sinon.spy(real);
      registry.init();
    });

    it('should handle the event if not already handled', () => {
      registry.handleWidgetReadyEvent([{widgetId: 1}]);
      expect(registry.handleDynamicElementReadyEvent.calledOnce).to.be.true;
      expect(registry.readyCache[1]).to.be.true;
    });

    it('should not handle the event if already handled', () => {
      registry.count = 1;
      registry.readyCache[1] = true;
      registry.handleWidgetReadyEvent([{widgetId: 1}]);
      expect(registry.handleDynamicElementReadyEvent.calledOnce).to.be.true;
      expect(registry.count).to.eq(1);
    });
  });

  describe('registerEventHandlers()', () => {
    it('should register for events', () => {
      registry.registerEventHandlers();
      expect(registry.eventbus.subscribe.calledThrice, 'called Thrice').to.be.true;
      expect(registry.eventbus.subscribe.getCall(0).args[0], 'Image ready event').to.eq(ImageReadyEvent);
      expect(registry.eventbus.subscribe.getCall(1).args[0], 'Widget ready event').to.eq(WidgetReadyEvent);
      expect(registry.eventbus.subscribe.getCall(2).args[0], 'Editor ready event').to.eq(EditorReadyEvent);
      expect(registry.subscriptions.length, 'subscription length').to.eq(3);
    });
  });

  describe('publishIdocReadyEvent()', () => {
    it('should publish IdocReadyEvent', () => {
      registry.publishIdocReadyEvent();

      expect(registry.eventbus.publish.calledOnce).to.be.true;
      expect(registry.eventbus.publish.getCall(0).args[0] instanceof IdocReadyEvent).to.be.true;
    });
  });

  describe('destroy()', () => {
    it('should unsubscribe from all events', () => {
      registry.registerEventHandlers();
      registry.destroy();

      expect(registry.subscriptions.length).to.eq(3);
      expect(registry.subscriptions[0].unsubscribe.calledOnce);
      expect(registry.subscriptions[1].unsubscribe.calledOnce);
      expect(registry.subscriptions[2].unsubscribe.calledOnce);
    });
  });
});