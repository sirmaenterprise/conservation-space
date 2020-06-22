import _ from 'lodash';
import uuid from 'common/uuid';
import {MODE_PREVIEW, MODE_EDIT, MODE_PRINT} from 'idoc/idoc-constants';
import {EDIT_OPERATION_NAME} from 'services/rest/instance-service';
import {SharedObjectsRegistry} from 'idoc/shared-objects-registry';
import {SharedObjectUpdatedEvent} from 'idoc/shared-object-updated-event';
import {StatusCodes} from 'services/rest/status-codes';
import {CURRENT_OBJECT_TEMP_ID} from 'models/instance-object';
import {InstanceObject} from 'models/instance-object';

export const CURRENT_OBJECT_LOCK = 'currentObjectLock';

export class IdocContext {

  constructor(id, mode, instanceRestService, sessionStorageService, promiseAdapter, eventbus, router, pluginsService) {
    this.uuid = uuid();
    this.router = router;
    this.eventbus = eventbus;
    this.mode = mode || MODE_PREVIEW;
    this.modeling = false;
    this.id = id;
    // The current object data is stored inside the shared objects map and only its id is managed outside in this variable.
    this.currentObjectId = null;
    // This map contains all currently loaded and used inside the current idoc objects data wrapped inside InstanceObject
    // instances mapped to object ids.
    this.sharedObjects = {};

    this.sharedObjectsRegistry = new SharedObjectsRegistry(this.sharedObjects);

    this.instanceRestService = instanceRestService;
    this.pluginsService = pluginsService;
    this.sessionStorageService = sessionStorageService;
    this.promiseAdapter = promiseAdapter;

    // If an object is accessed multiple times before being loaded it will return the loader created the first time
    this.loaders = {};
    this.modelConverters = {};
  }

  getId() {
    return this.id;
  }

  getUUID() {
    return this.uuid;
  }

  getMode() {
    return this.mode;
  }

  setMode(newMode) {
    this.mode = newMode;
    return this;
  }

  isEditMode() {
    return this.mode === MODE_EDIT;
  }

  isPreviewMode() {
    return this.mode === MODE_PREVIEW;
  }

  isPrintMode() {
    return this.mode === MODE_PRINT;
  }

  isModeling() {
    return this.modeling;
  }

  setModeling(modeling) {
    this.modeling = modeling;
  }

  isShowTemplateSelector() {
    return this.showTemplateSelector;
  }

  setShowTemplateSelector(setShow) {
    this.showTemplateSelector = setShow;
  }

  setCurrentObjectId(id) {
    this.currentObjectId = id;
  }

  getCurrentObjectId() {
    return this.currentObjectId;
  }

  getSharedObjectsRegistry() {
    return this.sharedObjectsRegistry;
  }

  /**
   * Delete the current object stored under temp id before is was saved and map it with its actual id returned by the service.
   * @param id
   */
  updateTempCurrentObjectId(id) {
    this.sharedObjects[id] = this.sharedObjects[CURRENT_OBJECT_TEMP_ID];
    this.sharedObjects[id].id = id;
    this.sharedObjectsRegistry.registerWidget(CURRENT_OBJECT_LOCK, id, true);
    delete this.sharedObjects[CURRENT_OBJECT_TEMP_ID];
  }

  /**
   * Function to skip default HTTP interceptor in case that requested object(s) is(are) not found.
   * In that case the error is handled silently without logging and displaying an error notification to the user.
   * This can happen when some of the requested objects have been deleted.
   * @param response object returned for the request
   * @returns {*|boolean} true if response status is 404 NOT_FOUND
   */
  skipHttpInterceptor(response) {
    return response && response.status === StatusCodes.NOT_FOUND;
  }

  applyModelConverters(instanceObject) {
    // execute model converters on loaded models
    this.modelConverters.forEach((converter) => {
      converter.convert(instanceObject.getModels());
    });
  }

  /**
   * Loads object from the backend. This method cache loaders and if an object is currently loading its loader is reused
   * @param id identifier of the object to be loaded
   * @param loadView boolean flag indicating whether the view should be loaded or not. In the common case view is only needed for the current object
   * @returns {*} a promise which resolves with the loaded InstanceObject
   */
  loadObject(id, loadView) {
    if (this.loaders[id]) {
      return this.loaders[id];
    } else {
      let instanceLoader = this.instanceRestService.load(id, {skipInterceptor: this.skipHttpInterceptor});
      let instanceDefinitionLoader = this.instanceRestService.loadModel(id, EDIT_OPERATION_NAME, {skipInterceptor: this.skipHttpInterceptor});
      let instanceViewLoader = loadView === true ? this.instanceRestService.loadView(id, {skipInterceptor: this.skipHttpInterceptor}) : this.promiseAdapter.resolve();
      let modelConvertersLoader = this.pluginsService.loadPluginServiceModules('idoc-model-converter', 'name', true);
      let contextPathLoader = this.instanceRestService.loadContextPath(id, {skipInterceptor: this.skipHttpInterceptor});
      this.loaders[id] = this.promiseAdapter.all([instanceLoader, instanceDefinitionLoader, instanceViewLoader, modelConvertersLoader, contextPathLoader])
        .then(([instanceData, definitionData, viewData, modelConverters, context]) => {
          let idoc = instanceData.data;
          let models = definitionData.data;
          this.modelConverters = modelConverters;
          let content;
          if (viewData && viewData.data) {
            content = viewData.data;
          }
          let contextPath = context.data;
          delete this.loaders[id];

          // TODO: Remove models.headers and access them only via getHeader, through validationModel
          models.headers = idoc.headers;

          var instanceObject = new InstanceObject(id, models, content);
          instanceObject.setContextPath(contextPath);
          instanceObject.mergePropertiesIntoModel(idoc.properties);
          instanceObject.mergeHeadersIntoModel(idoc.headers);
          instanceObject.setThumbnail(idoc.thumbnailImage);
          instanceObject.setWriteAllowed(idoc.writeAllowed);
          this.applyModelConverters(instanceObject);
          return instanceObject;
        });
      return this.loaders[id];
    }
  }

  /**
   * Batch loads objects from the backend. If object is already loaded its reused
   * @param ids array with identifier of the objects to be loaded
   * @param config configuration which shows which instance properties to be loaded. If not provided all instance properties will be loaded
   * @returns {*} a promise which resolves with the loaded InstanceObject
   */
  loadObjects(ids, config) {
    config = _.defaults(config || {}, {skipInterceptor: this.skipHttpInterceptor});
    return this.promiseAdapter.all([
      this.instanceRestService.loadBatch(ids, config),
      this.instanceRestService.loadModels(ids, EDIT_OPERATION_NAME, config),
      this.pluginsService.loadPluginServiceModules('idoc-model-converter', 'name', true)
    ])
      .then((result) => {
        let instancesResponse = result[0].data;
        let modelsResponse = result[1].data;
        this.modelConverters = result[2];
        return instancesResponse.map((instanceResponse) => {
          let modelResponse = modelsResponse[instanceResponse.id];
          let models = {
            validationModel: modelResponse.validationModel,
            viewModel: modelResponse.viewModel,
            definitionId: modelResponse.definitionId,
            definitionLabel: modelResponse.definitionLabel,
            headers: instanceResponse.headers,
            instanceType: modelResponse.instanceType
          };
          let instanceObject = new InstanceObject(instanceResponse.id, models, instanceResponse.content, this.isPartiallyLoaded(config));
          instanceObject.setThumbnail(instanceResponse.thumbnailImage);
          instanceObject.setWriteAllowed(instanceResponse.writeAllowed);
          instanceObject.mergePropertiesIntoModel(instanceResponse.properties);
          instanceObject.mergeHeadersIntoModel(instanceResponse.headers);
          this.applyModelConverters(instanceObject);
          return instanceObject;
        });
      });
  }

  isPartiallyLoaded(config) {
    return _.get(config, 'params.properties', []).length > 0;
  }

  getCurrentObject() {
    return this.promiseAdapter.promise((resolve, reject) => {
      // object is already loaded
      if (this.currentObjectId) {
        resolve(this.sharedObjects[this.currentObjectId]);
        return;
      }

      if (this.id) {
        // load existing instance
        this.loadObject(this.id, true).then(result => {
          this.setCurrentObjectId(this.id);
          this.sharedObjects[this.id] = result;
          this.sharedObjectsRegistry.registerWidget(CURRENT_OBJECT_LOCK, this.currentObjectId);
          resolve(this.sharedObjects[this.currentObjectId]);
        }).catch((error) => {
          // Navigate to error page in case that current object no longer exists in the system
          if (error) {
            if (error.status === StatusCodes.NOT_FOUND) {
              this.router.navigate('error', {'key': IdocContext.ERROR_NOT_FOUND_KEY}, {location: 'replace'});
            } else if (error.status === StatusCodes.FORBIDDEN) {
              this.router.navigate('error', {'key': IdocContext.FORBIDDEN_KEY}, {location: 'replace'});
            }
          }
          reject(error);
        });
      } else {
        this.sharedObjects[CURRENT_OBJECT_TEMP_ID] = new InstanceObject(CURRENT_OBJECT_TEMP_ID, this.sessionStorageService.getJson('models'));
        this.setCurrentObjectId(CURRENT_OBJECT_TEMP_ID);
        this.sharedObjectsRegistry.registerWidget(CURRENT_OBJECT_LOCK, this.currentObjectId);
        resolve(this.sharedObjects[this.currentObjectId]);
      }
    });
  }

  /**
   * Load or get an object from map of shared objects
   * @param id of the object to be obtained
   * @param widgetId used to register obtained object with this widget.
   * Usage of this parameter is artificially enforced to prevent overlooking it and possible problems with stale objects or objects which disappear from shared objects map and their changes are reset.
   * If you're sure that obtained object shouldn't be registered to any widget pass null for widgetId.
   * @param reset if true widget will be deregistered from all previous objects (when objects displayed in the widget are changed). False if adding additional objects to the widget
   * @returns {*}
   */
  getSharedObject(id, widgetId, reset) {
    if (widgetId === undefined) {
      throw new Error('Widget id parameter is required. If you are sure that you don,\'t want to register requested object to any widget pass null for widgetId');
    }
    if (this.sharedObjectsRegistry.shouldResetObject(id, widgetId) && this.sharedObjects[id]) {
      this.sharedObjects[id].revertChanges();
    }
    return this.promiseAdapter.promise((resolve, reject) => {
      if (this.sharedObjects[id] && !this.sharedObjects[id].getPartiallyLoaded() && !this.sharedObjects[id].getShouldReload()) {
        this.sharedObjectsRegistry.registerWidget(widgetId, id, reset);
        resolve(this.sharedObjects[id]);
      } else {
        this.loadObject(id).then(result => {
          if (this.sharedObjects[id] && this.sharedObjects[id].getShouldReload()) {
            this.sharedObjects[id].updateLocalModel(result.getModels().validationModel);
            this.sharedObjects[id].setShouldReload(false);
          } else {
            this.sharedObjects[id] = this.sharedObjects[id] || result;
            if (this.sharedObjects[id].getPartiallyLoaded()) {
              this.mergeInstanceModels(this.sharedObjects[id], result);
            }
          }
          this.sharedObjectsRegistry.registerWidget(widgetId, id, reset);
          resolve(this.sharedObjects[id]);
        }).catch((result) => {
          if (result && result.status === StatusCodes.NOT_FOUND) {
            this.removeFromSharedObjects([id]);
          }
          reject(result);
        });
      }
    });
  }

  /**
   * Reload object details after save. Object details that might change on save:
   * - headers for object
   * - context path
   * - validation details: properties might be populated, or changed
   *
   * @param id object id
   * @returns Promise which resolves with object headers when they are loaded. Underlying object is also updated.
   */
  reloadObjectDetails(id) {
    return this.getSharedObject(id, null).then((sharedObject) => {
      return this.promiseAdapter.all([
        this.instanceRestService.load(id),
        this.instanceRestService.loadContextPath(id)
      ]).then((results) => {
        let idocData = results[0].data;
        let contextPath = results[1].data;
        sharedObject.setHeaders(idocData.headers);
        sharedObject.setContextPath(contextPath);
        sharedObject.mergePropertiesIntoModel(idocData.properties);
        sharedObject.mergeHeadersIntoModel(idocData.headers);
        sharedObject.setWriteAllowed(idocData.writeAllowed);
        this.eventbus.publish(new SharedObjectUpdatedEvent({
          objectId: sharedObject.getId()
        }));
        return sharedObject;
      });
    });
  }

  /**
   * Load objects by id or get an objects from map of shared objects
   * @param ids of shared objects to be returned
   * @param widgetId used to register obtained objects with this widget.
   * Usage of this parameter is artificially enforced to prevent overlooking it and possible problems with stale objects or objects which disappear from shared objects map and their changes are reset.
   * If you're sure that obtained objects shouldn't be registered to any widget pass null for widgetId.
   * @param reset if true widget will be deregistered from all previous objects (when objects displayed in the widget are changed). False if adding additional objects to the widget
   * @param config configuration which shows which instance properties to be loaded. If not provided all instance properties will be loaded
   * @returns [] promise which resolves when all requested and found shared objects are resolved
   */
  getSharedObjects(ids, widgetId, reset, config) {
    if (this.isEditMode() && this.isPartiallyLoaded(config)) {
      throw new Error('You can not load instance partially in edit mode');
    }
    if (widgetId === undefined) {
      throw new Error('Widget id parameter is required. If you are sure that you don\'t want to register requested object to any widget pass null for widgetId');
    }
    let notLoadedObjectsIds = ids.filter((id) => {
      return this.sharedObjects[id] === undefined || this.sharedObjects[id].getPartiallyLoaded() || this.sharedObjects[id].getShouldReload();
    });

    let result;
    if (notLoadedObjectsIds.length > 0) {
      result = this.loadObjects(notLoadedObjectsIds, config).then((loadedInstanceObjects) => {
        let loadedObjectIds = [];
        loadedInstanceObjects.forEach((loadedInstanceObject) => {
          let id = loadedInstanceObject.getId();
          loadedObjectIds.push(id);
          let currentObject = this.sharedObjects[id];
          if (currentObject && currentObject.getShouldReload()) {
            currentObject.updateLocalModel(loadedInstanceObject.getModels().validationModel);
            currentObject.setShouldReload(false);
          } else {
            currentObject = currentObject || loadedInstanceObject;
            this.sharedObjects[id] = currentObject;
            if (currentObject.getPartiallyLoaded()) {
              this.mergeInstanceModels(currentObject, loadedInstanceObject);
            }
          }
        });

        this.removeFromSharedObjects(_.difference(notLoadedObjectsIds, loadedObjectIds));
        return this.buildSharedObjectsResult(ids);
      }).catch((error) => {
        if (error && error.status === StatusCodes.NOT_FOUND) {
          this.removeFromSharedObjects(ids);
          return this.buildSharedObjectsResult(ids);
        } else {
          throw error;
        }
      });
    } else {
      result = this.promiseAdapter.resolve(this.buildSharedObjectsResult(ids));
    }
    result.then((result) => {
      let foundIds = result.data.map((instance) => {
        return instance.getId();
      });
      this.sharedObjectsRegistry.registerWidget(widgetId, foundIds, reset);
    });
    return result;
  }

  mergeInstanceModels(sharedInstance, loadedInstance) {
    sharedInstance.models.validationModel.addPropertiesToModel(loadedInstance.models.validationModel);
    sharedInstance.models.viewModel.addFieldsToModel(loadedInstance.models.viewModel.serialize().fields);
    sharedInstance.setPartiallyLoaded(loadedInstance.getPartiallyLoaded());
    sharedInstance.setHeaders(loadedInstance.getHeaders());
  }

  /**
   * Creates an object containing requested objects and an array with the ids of not found objects.
   * Call this after all objects are loaded and inserted into sharedObjects map.
   * @param ids of all requested objects
   * @returns {{data: Array, notFound: Array}} data contains all found InstanceObjects. Not found object ids are returned in notFound array
   */
  buildSharedObjectsResult(ids) {
    let notFoundSharedObjectIds = [];
    let returnedSharedObjects = _.filter(ids, (id) => {
      if (!this.sharedObjects[id]) {
        notFoundSharedObjectIds.push(id);
        return false;
      }
      return true;
    }).map((id) => {
      return this.sharedObjects[id];
    });
    return {
      data: returnedSharedObjects,
      notFound: notFoundSharedObjectIds
    };
  }

  /**
   * Convenience function to get all shared objects without registering them to any widget
   * @param onlyModified if true only modified objects plus current object are returned.
   * @param widgetId used to register obtained objects with this widget.
   * @returns {*}
   */
  getAllSharedObjects(onlyModified, widgetId) {
    // If the user wants all objects then non of them should be deregistered
    this.sharedObjectsRegistry.registerWidget(widgetId, Object.keys(this.sharedObjects));

    let registeredSharedObjects = [];
    Object.keys(this.sharedObjects).forEach((objectId) => {
      let changed = this.sharedObjects[objectId].isChanged();
      let isCurrentObject = this.currentObjectId === objectId;
      let isReferenced = this.sharedObjectsRegistry.isRegisteredToAnyWidget(objectId);

      if (isReferenced && (!onlyModified || changed || isCurrentObject)) {
        registeredSharedObjects.push(this.sharedObjects[objectId]);
      }
    });
    return registeredSharedObjects;
  }

  /**
   * Used only for testing purposes.
   * @param objects
   */
  setSharedObjects(objects) {
    this.sharedObjects = objects;
  }

  removeFromSharedObjects(ids) {
    ids.forEach((id) => {
      delete this.sharedObjects[id];
      this.sharedObjectsRegistry.deleteObjectFromRegistry(id);
    });
  }

  /**
   * If there are changes in any shared object model, the new data should be set in appropriate objects in context.
   *
   * @param objects plain js objects
   * {
        'OT210027': {
          definitionId: 'id',
          parentId: 'parentId',
          returnUrl: 'returnUrl',
          viewModel: { ... },
          validationModel: { ... }
        }
     }
   */
  mergeObjectsModels(objects) {
    Object.keys(objects).forEach((id) => {
      this.sharedObjects[id].mergeModelIntoModel(objects[id].models.validationModel);
    });
  }

  /**
   * Reverts changes for all shared objects
   */
  revertAllChanges() {
    this.getAllSharedObjects().forEach((sharedObject) => {
      sharedObject.revertChanges();
      sharedObject.mergeHeadersIntoModel(sharedObject.headers);
    });
  }

  /**
   * Sets forceReload property used to achieve widgets hard reload
   *
   * @param reload if true the shared object model will be updated if changed on the next properties load
   */
  setAllSharedObjectsShouldReload(reload) {
    this.getAllSharedObjects().forEach((sharedObject) => {
      sharedObject.setShouldReload(reload);
    });
  }

  static getRootContextWithReadAccess(contextPath) {
    if (contextPath) {
      let readAccessNode;
      for (let i = 0; i < contextPath.length; i++) {
        let currentNode = contextPath[i];
        if (currentNode.readAllowed) {
          readAccessNode = currentNode;
          break;
        }
      }
      return readAccessNode;
    }
  }

  /**
   * Walks the contextPath backwards, getting the root up until the user has read permissions.
   * Used for building the navigation tree, where there are cases of users not having permission
   * of every object in the tree and it needs to be build from wherever the user has permission
   * @param contextPath
   * @returns {*} root element
   */
  static getRootContextWithReadAccessInverted(contextPath) {
    if (contextPath) {
      let readAccessNode;
      for (let i = contextPath.length - 1; i > -1; i--) {
        let currentNode = contextPath[i];
        if (currentNode.readAllowed) {
          readAccessNode = currentNode;
        } else {
          break;
        }
      }
      return readAccessNode;
    }
  }
}

IdocContext.ERROR_NOT_FOUND_KEY = 'error.object.not.found';
IdocContext.FORBIDDEN_KEY = 'error.object.forbidden';
