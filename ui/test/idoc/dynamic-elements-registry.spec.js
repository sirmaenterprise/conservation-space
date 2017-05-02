import {DynamicElementsRegistry} from 'idoc/dynamic-elements-registry';
import {WidgetReadyEvent} from 'idoc/widget/widget-ready-event';
import {IdocReadyEvent} from 'idoc/idoc-ready-event';
import {EditorReadyEvent} from 'idoc/editor/editor-ready-event';
import {ImageReadyEvent} from 'idoc/events/image-ready-event';
import {Eventbus} from 'services/eventbus/eventbus';

const html = `
  <div>
    <div widget="widget1" class="widget"></div>
    <div widget="widget2" class="widget"></div>
    <div class="widget"></div>
    <img />

    <section data-id="first-tab">
      <img />
      <div widget="widget1" class="widget"></div>
    </section>

    <section data-id="second-tab">
      <div widget="widget1" class="widget"></div>
      <div widget="widget2" class="widget"></div>
      <div class="widget"></div>
      <img />
    </section>

    <section data-id="third-tab">
      <div widget="widget3" class="widget"></div>
      <img />
    </section>
  </div>
`;

describe('DynamicElementsRegistry', () => {
  let eventbus;
  let pluginService;
  let registry;
  let dom;

  beforeEach(() => {
    dom = $(html);
    eventbus = {
      publish: sinon.spy(),
      subscribe: sinon.spy(function() {
        return {
          unsubscribe: sinon.spy()
        };
      })
    };

    pluginService = {
      getDefinitions: sinon.spy((ext) => [{name: 'widget1'}])
    };

    registry = new DynamicElementsRegistry((cb) => cb(), eventbus, pluginService);
    registry.readyCache = {};
  });

  it('should load available widgets', () => {
    expect(registry.availableWidgets).to.deep.eq(['widget1']);
  });

  describe('init(dom, tab)', () => {
    it('should initialize count and reset ready cache', () => {
      registry.readyCache = new Date().getTime();
      registry.registerEventHandlers = sinon.spy();

      registry.init(dom, ['first-tab']);
      expect(registry.count).to.eq(2);
      expect(registry.readyCache).to.deep.eq({});
      expect(registry.registerEventHandlers.calledOnce).to.be.true;
    });
  });

  describe('reload(dom, tab)', () => {
    it('should call destroy method and then init', () => {
      registry.init = sinon.spy();
      registry.destroy = sinon.spy();

      registry.reload('this is the dom', 'this is the tab');

      expect(registry.destroy.calledOnce).to.be.true;
      expect(registry.init.calledOnce).to.be.true;
      expect(registry.init.calledWith('this is the dom', 'this is the tab')).to.be.true;
      expect(registry.init.calledAfter(registry.destroy)).to.be.true;
    });
  });

  describe('countElements(dom, tab)', () => {
    it('should scan the entire dom if tab id is falsy', () => {
      registry.countElements(dom);
      expect(registry.count).to.eq(5);

      registry.countElements(dom, null);
      expect(registry.count).to.eq(5);

      registry.countElements(dom, false);
      expect(registry.count).to.eq(5);

      registry.countElements(dom, '');
      expect(registry.count).to.eq(5);

      registry.countElements(dom, 0);
      expect(registry.count).to.eq(5);
    });

    it('should scan only the provided tab', () => {
      registry.countElements(dom, ['first-tab']);
      expect(registry.count).to.eq(2);

      registry.countElements(dom, ['first-tab', 'second-tab']);
      expect(registry.count).to.eq(4);
    });
  });

  describe('handleWidget($widget)', () => {
    beforeEach(() => {
      registry.count = 1;
    });

    it('should not increment count if widget does not have a name', () => {
      registry.handleWidget($('<div></div>'));
      expect(registry.count).to.eq(1);
    });

    it('should not increment count if widget is not available', () => {
      registry.handleWidget($('<div widget="widget2"></div>'));
      expect(registry.count).to.eq(1);
    });

    it('should increment count', () => {
      registry.handleWidget($('<div widget="widget1"></div>'));
      expect(registry.count).to.eq(2);
    });
  });

  describe('handleImage($img)', () => {
    it('should register onload listener and increment count', () => {
      let element = [{}];
      element.on = sinon.spy();

      registry.count = 1;
      registry.handleImage(element);
      expect(element.on.calledOnce).to.be.true;
      expect(element.on.getCall(0).args[0]).to.eq('load error');
      expect(registry.count).to.eq(2);
    });

    it('should register onload listener and increment count if loading of image is not completed', () => {
      let element = [{complete: false}];
      element.on = sinon.spy();
      registry.count = 1;
      registry.handleImage(element);
      expect(element.on.calledOnce).to.be.true;
      expect(element.on.getCall(0).args[0]).to.eq('load error');
      expect(registry.count).to.eq(2);
    });

    it('should not register onload listener and increment count if loading of image is completed', () => {
      let element = [{complete: true}];
      element.on = sinon.spy();
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

      expect(registry.eventbus.subscribe.calledThrice).to.be.true;
      expect(registry.eventbus.subscribe.getCall(0).args[0]).to.eq(ImageReadyEvent);
      expect(registry.eventbus.subscribe.getCall(1).args[0]).to.eq(WidgetReadyEvent);
      expect(registry.eventbus.subscribe.getCall(2).args[0]).to.eq(EditorReadyEvent);
      expect(registry.subscriptions.length).to.eq(3);
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