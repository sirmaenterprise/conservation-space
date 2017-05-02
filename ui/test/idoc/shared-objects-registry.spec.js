import _ from 'lodash';
import { SharedObjectsRegistry } from 'idoc/shared-objects-registry';

describe('SharedObjectsRegistry', () => {
  let sharedObjectsRegistry;
  const WIDGET_ID_1 = 'widget-123456';
  const WIDGET_ID_2 = 'widget-999888';
  const OBJECT_ID_1 = 'emf:123456';
  const OBJECT_ID_2 = 'emf:999888';
  const OBJECT_ID_3 = 'emf:113311';

  beforeEach(() => {
    let sharedObjects = {
      OBJECT_ID_1: {},
      OBJECT_ID_2: {}
    };
    sharedObjectsRegistry = new SharedObjectsRegistry(sharedObjects);
  });

  describe('registerWidget()', () => {
    it('should return without changes if widgetId parameter is not defined', () => {
      expect(sharedObjectsRegistry.registerWidget()).to.be.undefined;
      expect(sharedObjectsRegistry.sharedObjectsRegistry).to.be.empty;
    });

    it('should return without changes if objectId parameter is not defined', () => {
      expect(sharedObjectsRegistry.registerWidget(WIDGET_ID_1)).to.be.undefined;
      expect(sharedObjectsRegistry.sharedObjectsRegistry).to.be.empty;
    });

    it('should call deregisterWidget if deregister is true', () => {
      let deregisterSpy = sinon.spy(sharedObjectsRegistry, 'deregisterWidget');
      sharedObjectsRegistry.registerWidget(WIDGET_ID_1, OBJECT_ID_1, true);
      expect(deregisterSpy.callCount).to.equal(1);
    });

    it('should register all given objectIds to the given widgetId if objectIds is array', () => {
      sharedObjectsRegistry.registerWidget(WIDGET_ID_1, [OBJECT_ID_1, OBJECT_ID_2]);
      expect(sharedObjectsRegistry.sharedObjectsRegistry.size).to.equal(2);
      expect(sharedObjectsRegistry.sharedObjectsRegistry.get(OBJECT_ID_1).has(WIDGET_ID_1)).to.be.true;
      expect(sharedObjectsRegistry.sharedObjectsRegistry.get(OBJECT_ID_2).has(WIDGET_ID_1)).to.be.true;
    });

    it('should register given objectId to the given widgetId if objectIds is string', () => {
      sharedObjectsRegistry.registerWidget(WIDGET_ID_1, OBJECT_ID_1);
      expect(sharedObjectsRegistry.sharedObjectsRegistry.size).to.equal(1);
      expect(sharedObjectsRegistry.sharedObjectsRegistry.get(OBJECT_ID_1).has(WIDGET_ID_1)).to.be.true;
    });
  });

  describe('registerWidgetToObject()', () => {
    it('should add new record to sharedObjectsRegistry if such doesn\'t exist', () => {
      expect(sharedObjectsRegistry.sharedObjectsRegistry).to.be.empty;
      sharedObjectsRegistry.registerWidgetToObject(WIDGET_ID_1, OBJECT_ID_1);
      expect(sharedObjectsRegistry.sharedObjectsRegistry.size).to.equal(1);
      expect(sharedObjectsRegistry.sharedObjectsRegistry.get(OBJECT_ID_1).has(WIDGET_ID_1)).to.be.true;
    });

    it('should add widgetId to existing record from sharedObjectsRegistry if such exists', () => {
      sharedObjectsRegistry.registerWidgetToObject(WIDGET_ID_1, OBJECT_ID_1);
      sharedObjectsRegistry.registerWidgetToObject(WIDGET_ID_2, OBJECT_ID_1);
      expect(sharedObjectsRegistry.sharedObjectsRegistry.size).to.equal(1);
      expect(sharedObjectsRegistry.sharedObjectsRegistry.get(OBJECT_ID_1).size).to.equal(2);
      expect(sharedObjectsRegistry.sharedObjectsRegistry.get(OBJECT_ID_1).has(WIDGET_ID_1)).to.be.true;
      expect(sharedObjectsRegistry.sharedObjectsRegistry.get(OBJECT_ID_1).has(WIDGET_ID_2)).to.be.true;
    });

    it('should remove widgetId from deleted widgets', () => {
      let deletedWidgetsSpy = sinon.spy(sharedObjectsRegistry.deletedWidgets, 'delete');
      sharedObjectsRegistry.registerWidgetToObject(WIDGET_ID_1, OBJECT_ID_1);
      expect(deletedWidgetsSpy.callCount).to.equal(1);
      expect(deletedWidgetsSpy.args[0][0]).to.equal(WIDGET_ID_1);
      deletedWidgetsSpy.reset();
    });
  });

  describe('deregisterWidget()', () => {
    it('should return without changes if widgetId parameter is not defined', () => {
      expect(sharedObjectsRegistry.deregisterWidget()).to.be.undefined;
      expect(sharedObjectsRegistry.sharedObjectsRegistry).to.be.empty;
    });

    it('should deregsiter widget from all objects if objectIds parameter is undefined', () => {
      sharedObjectsRegistry.sharedObjectsRegistry.set(OBJECT_ID_1, new Set([WIDGET_ID_1]));
      sharedObjectsRegistry.sharedObjectsRegistry.set(OBJECT_ID_2, new Set([WIDGET_ID_1, WIDGET_ID_2]));
      sharedObjectsRegistry.deregisterWidget(WIDGET_ID_1);
      expect(sharedObjectsRegistry.sharedObjectsRegistry.get(OBJECT_ID_1)).to.be.empty;
      expect(sharedObjectsRegistry.sharedObjectsRegistry.get(OBJECT_ID_2).size).to.equal(1);
      expect(sharedObjectsRegistry.sharedObjectsRegistry.get(OBJECT_ID_2).has(WIDGET_ID_1)).to.be.false;
    });

    it('should deregsiter widget from all given objectIds if objectIds is array', () => {
      sharedObjectsRegistry.sharedObjectsRegistry.set(OBJECT_ID_1, new Set([WIDGET_ID_1]));
      sharedObjectsRegistry.sharedObjectsRegistry.set(OBJECT_ID_2, new Set([WIDGET_ID_1, WIDGET_ID_2]));
      sharedObjectsRegistry.sharedObjectsRegistry.set(OBJECT_ID_3, new Set([WIDGET_ID_1]));
      sharedObjectsRegistry.deregisterWidget(WIDGET_ID_1, [OBJECT_ID_1, OBJECT_ID_2]);
      expect(sharedObjectsRegistry.sharedObjectsRegistry.get(OBJECT_ID_1).has(WIDGET_ID_1)).to.be.false;
      expect(sharedObjectsRegistry.sharedObjectsRegistry.get(OBJECT_ID_2).has(WIDGET_ID_1)).to.be.false;
      expect(sharedObjectsRegistry.sharedObjectsRegistry.get(OBJECT_ID_3).has(WIDGET_ID_1)).to.be.true;
    });

    it('should deregsiter widget from given objectId if objectIds is string', () => {
      sharedObjectsRegistry.sharedObjectsRegistry.set(OBJECT_ID_1, new Set([WIDGET_ID_1]));
      sharedObjectsRegistry.sharedObjectsRegistry.set(OBJECT_ID_2, new Set([WIDGET_ID_1, WIDGET_ID_2]));
      sharedObjectsRegistry.sharedObjectsRegistry.set(OBJECT_ID_3, new Set([WIDGET_ID_1]));
      sharedObjectsRegistry.deregisterWidget(WIDGET_ID_1, OBJECT_ID_2);
      expect(sharedObjectsRegistry.sharedObjectsRegistry.get(OBJECT_ID_1).has(WIDGET_ID_1)).to.be.true;
      expect(sharedObjectsRegistry.sharedObjectsRegistry.get(OBJECT_ID_2).has(WIDGET_ID_1)).to.be.false;
      expect(sharedObjectsRegistry.sharedObjectsRegistry.get(OBJECT_ID_3).has(WIDGET_ID_1)).to.be.true;
    });
  });

  describe('deregisterWidgetFromObject()', () => {
    it('should remove widgetId from sharedObjectsRegistry for given objectId (if another widget is registered with the given object it should remain in sharedObjects map)', () => {
      sharedObjectsRegistry.sharedObjectsRegistry.set(OBJECT_ID_2, new Set([WIDGET_ID_1, WIDGET_ID_2]));
      sharedObjectsRegistry.deregisterWidgetFromObject(WIDGET_ID_1, OBJECT_ID_2);
      expect(sharedObjectsRegistry.sharedObjectsRegistry.get(OBJECT_ID_2).size).to.equal(1);
      expect(sharedObjectsRegistry.sharedObjectsRegistry.get(OBJECT_ID_2).has(WIDGET_ID_2)).to.be.true;
    });

    it('should remove widgetId from sharedObjectsRegistry for given objectId and should delete the object from sharedObjects map if this is the last widget which references it', () => {
      sharedObjectsRegistry.sharedObjectsRegistry.set(OBJECT_ID_1, new Set([WIDGET_ID_1]));
      sharedObjectsRegistry.deregisterWidgetFromObject(WIDGET_ID_1, OBJECT_ID_1);
      expect(sharedObjectsRegistry.sharedObjects[OBJECT_ID_1]).to.be.undefined;
    });
  });

  it('onWidgetDelete() should deregister widget from all objects and add it to deleted widgets', () => {
    let deregisterWidgetSpy = sinon.spy(sharedObjectsRegistry, 'deregisterWidget');
    sharedObjectsRegistry.onWidgetDelete(WIDGET_ID_1);
    expect(deregisterWidgetSpy.callCount).to.equal(1);
    expect(deregisterWidgetSpy.args[0][0]).to.equal(WIDGET_ID_1);
    expect(sharedObjectsRegistry.deletedWidgets.has(WIDGET_ID_1)).to.be.true;
    deregisterWidgetSpy.reset();
  });

  it('isRegisteredToAnyWidget() should return true if object is registered with any widget', () => {
    expect(sharedObjectsRegistry.isRegisteredToAnyWidget(OBJECT_ID_1)).to.be.false;
    sharedObjectsRegistry.registerWidgetToObject(WIDGET_ID_1, OBJECT_ID_1);
    expect(sharedObjectsRegistry.isRegisteredToAnyWidget(OBJECT_ID_1)).to.be.true;
    expect(sharedObjectsRegistry.isRegisteredToAnyWidget(OBJECT_ID_2)).to.be.false;
  });

  it('shouldResetObject() should return true if object is not registered to any widget and widget is new (not deleted)', () => {
    sharedObjectsRegistry.registerWidgetToObject(WIDGET_ID_1, OBJECT_ID_1);
    sharedObjectsRegistry.onWidgetDelete(WIDGET_ID_1);
    expect(sharedObjectsRegistry.shouldResetObject(OBJECT_ID_1, WIDGET_ID_1)).to.be.false;
    expect(sharedObjectsRegistry.shouldResetObject(OBJECT_ID_1, WIDGET_ID_2)).to.be.true;
  });
});
