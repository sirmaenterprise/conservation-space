import _ from 'lodash';

/**
 * A registry that maps widgets to context objects. Each time a context object is requested by a widget, the widget must register itself with that object.
 * When an object is no longer referenced by any widget it is removed from the context with all its changes.
 * <b>IMPORTANT! This class has very limited usage outside of idoc context class. Don't use it unless 100% sure what you're doing.</b>
 */
export class SharedObjectsRegistry {
  constructor(sharedObjects) {
    this.sharedObjects = sharedObjects;
    this.sharedObjectsRegistry = new Map();
    this.deletedWidgets = new Set();
  }

  /**
   * Register widget to given object ids
   * @param widgetId id of the widget to be registered
   * @param objectIds either an array with object ids or a string with single object id
   * @param reset if true all previous registrations for the given widget will be deregistered
   * (note that if there is previous registration to object in objectIds array it will be preserved)
   */
  registerWidget(widgetId, objectIds, reset) {
    if (!widgetId || !objectIds) {
      return;
    }
    if (reset === true) {
      this.deregisterWidget(widgetId, undefined, objectIds);
    }

    objectIds = _.isArray(objectIds) ? objectIds : [objectIds];
    objectIds.forEach((objectId) => {
      this.registerWidgetToObject(widgetId, objectId);
    });
  }

  /**
   * Register widget to given object. <b>For internal usage.</b>
   * @param widgetId
   * @param objectId
   */
  registerWidgetToObject(widgetId, objectId) {
    // remove widget id from deleted widgets if it was deleted. This may happen on undo or on widget drag and drop which deletes the widget and then recreates it
    this.deletedWidgets.delete(widgetId);

    let sharedObjectRegister = this.sharedObjectsRegistry.get(objectId);
    if (sharedObjectRegister === undefined) {
      sharedObjectRegister = new Set();
      this.sharedObjectsRegistry.set(objectId, sharedObjectRegister);
    }
    sharedObjectRegister.add(widgetId);
  }

  /**
   * Deregister widget from given objects
   * @param widgetId id of the widget to be deregistered
   * @param objectIds either an array with object ids or a string with single object id. To deregister widget from all previous objects pass undefined or null as objectIds
   * @param excludeObjectIds an array of object ids which registration to be preserved if objectsIds is undefined (i.e. deregister from all objects except excluded)
   */
  deregisterWidget(widgetId, objectIds, excludeObjectIds) {
    if (!widgetId) {
      return;
    }
    if (!objectIds) {
      // deregister from all objects
      for (let objectId of this.sharedObjectsRegistry.keys()) {
        if (!excludeObjectIds || excludeObjectIds.indexOf(objectId) === -1) {
          this.deregisterWidgetFromObject(widgetId, objectId);
        }
      }
    }
    objectIds = _.isArray(objectIds) ? objectIds : [objectIds];
    // deregister from list of objects
    objectIds.forEach((objectId) => {
      this.deregisterWidgetFromObject(widgetId, objectId);
    });
  }

  /**
   * Deregister widget from give object. <b>For internal usage.</b>
   * @param widgetId
   * @param objectId
   */
  deregisterWidgetFromObject(widgetId, objectId) {
    let sharedObjectRegister = this.sharedObjectsRegistry.get(objectId);
    if (sharedObjectRegister) {
      sharedObjectRegister.delete(widgetId);
    }
  }

  /**
   * Should be called on widget delete. It will deregister the widget from all objects and will add it to deleted widgets set
   * @param widgetId
   */
  onWidgetDelete(widgetId) {
    // deregister from all objects
    this.deregisterWidget(widgetId);
    this.deletedWidgets.add(widgetId);
  }

  /**
   * Returns true if object is registered to any widget, false otherwise
   * @param objectId
   * @returns {V|boolean}
   */
  isRegisteredToAnyWidget(objectId) {
    let sharedObjectRegister = this.sharedObjectsRegistry.get(objectId);
    return !!sharedObjectRegister && sharedObjectRegister.size > 0;
  }

  /**
   * Return true if object should be reset in the context.
   * This should happen when object is not registered to any widget and if the widget is brand new i.e. not in deleted widgets set
   * @param objectId id of the object we are registering
   * @param widgetId id of the widget to which we register the object
   * @returns {boolean}
   */
  shouldResetObject(objectId, widgetId) {
    return !this.isRegisteredToAnyWidget(objectId) && !this.deletedWidgets.has(widgetId);
  }

  /**
   * Deletes object from registry map.
   * This should happen when object is removed from idoc-context's sharedObjects.
   * @param objectId id to be deleted
   * @returns {boolean} result of the operation
   */
  deleteObjectFromRegistry(objectId) {
    return this.sharedObjectsRegistry.delete(objectId);
  }
}
