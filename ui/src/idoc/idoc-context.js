import _ from 'lodash';
import uuid from 'common/uuid';
import {MODE_PREVIEW, MODE_EDIT, MODE_PRINT} from 'idoc/idoc-constants';
import {SharedObjectsRegistry} from 'idoc/shared-objects-registry';
import {SharedObjectUpdatedEvent} from 'idoc/shared-object-updated-event';
import {HEADER_COMPACT} from 'instance-header/header-constants';
import {StatusCodes} from 'services/rest/status-codes';
import {ModelUtils} from 'models/model-utils';
import {InstanceModel} from 'models/instance-model';
import {DefinitionModel} from 'models/definition-model';
import {DefinitionModelProperty} from 'models/definition-model';
import * as angularAdapter from 'adapters/angular/angular-adapter';

// used as dummy widget id to ensure that current object would never be deregistered
export const CURRENT_OBJECT_TEMP_ID = 'currentObjectTempId';
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
      let instanceDefinitionLoader = this.instanceRestService.loadModel(id, 'editDetails', {skipInterceptor: this.skipHttpInterceptor});
      let instanceViewLoader = loadView === true ? this.instanceRestService.loadView(id, {skipInterceptor: this.skipHttpInterceptor}) : this.promiseAdapter.resolve();
      let modelConvertersLoader = this.pluginsService.loadPluginServiceModules('idoc-model-converter', 'name', true);
      let contextPathLoader = this.instanceRestService.loadContextPath(id, {skipInterceptor: this.skipHttpInterceptor});
      this.loaders[id] = this.promiseAdapter.all([instanceLoader, instanceDefinitionLoader, instanceViewLoader, modelConvertersLoader, contextPathLoader])
        .then((result) => {
          let idoc = result[0].data;
          let models = result[1].data;
          this.modelConverters = result[3];
          let content;
          if (result[2] && result[2].data) {
            content = result[2].data;
          }
          let contextPath = result[4].data;
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
        this.instanceRestService.loadModels(ids, 'editDetails', config),
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
              this.router.navigate('error', {'key': IdocContext.ERROR_NOT_FOUND_KEY});
            } else if (error.status === StatusCodes.FORBIDDEN) {
              this.router.navigate('error', {'key': IdocContext.FORBIDDEN_KEY});
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
      if (this.sharedObjects[id] && !this.sharedObjects[id].partiallyLoaded) {
        this.sharedObjectsRegistry.registerWidget(widgetId, id, reset);
        resolve(this.sharedObjects[id]);
      } else {
        this.loadObject(id).then(result => {
          this.sharedObjects[id] = this.sharedObjects[id] || result;
          if (this.sharedObjects[id].partiallyLoaded) {
            this.mergeInstanceModels(this.sharedObjects[id], result);
          }
          this.sharedObjectsRegistry.registerWidget(widgetId, id, reset);
          resolve(this.sharedObjects[id]);
        }).catch(reject);
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
        this.instanceRestService.loadModel(id, 'editDetails'),
        this.instanceRestService.loadContextPath(id)
      ]).then((results) => {
        let idocData = results[0].data;
        let modelsData = results[1].data;
        let contextPath = results[2].data;
        sharedObject.setHeaders(idocData.headers);
        sharedObject.setContextPath(contextPath);
        // TODO: Test and remove updateLocalModel method
        sharedObject.updateLocalModel(modelsData.validationModel);
        sharedObject.mergePropertiesIntoModel(idocData.properties);
        sharedObject.mergeHeadersIntoModel(idocData.headers);
        this.eventbus.publish(new SharedObjectUpdatedEvent({
          objectId: sharedObject.getId()
        }));
        return sharedObject;
      });
    });
  }

  /**
   * Load objects by id or get an objects from map of shared objects
   * @param ids of shared objects to be returned (optional)
   * @param widgetId used to register obtained objects with this widget.
   * Usage of this parameter is artificially enforced to prevent overlooking it and possible problems with stale objects or objects which disappear from shared objects map and their changes are reset.
   * If you're sure that obtained objects shouldn't be registered to any widget pass null for widgetId.
   * @param reset if true widget will be deregistered from all previous objects (when objects displayed in the widget are changed). False if adding additional objects to the widget
   * @param config configuration which shows which instance properties to be loaded. If not provided all instance properties will be loaded
   * @returns [] either a promise which resolves when all requested shared objects are resolved
   * or it returns all shared objects immediately if no ids are passed as parameter
   */
  getSharedObjects(ids, widgetId, reset, config) {
    if (this.isEditMode() && this.isPartiallyLoaded(config)) {
      throw new Error('You can not load instance partially in edit mode');
    }
    if (widgetId === undefined) {
      throw new Error('Widget id parameter is required. If you are sure that you don\'t want to register requested object to any widget pass null for widgetId');
    }
    let notLoadedObjectsIds = ids.filter((id) => {
      return this.sharedObjects[id] === undefined || this.sharedObjects[id].partiallyLoaded;
    });

    let result;
    if (notLoadedObjectsIds.length > 0) {
      result = this.loadObjects(notLoadedObjectsIds, config).then((loadedInstanceObjects) => {
        loadedInstanceObjects.forEach((loadedInstanceObject) => {
          this.sharedObjects[loadedInstanceObject.id] = this.sharedObjects[loadedInstanceObject.id] || loadedInstanceObject;
          if (this.sharedObjects[loadedInstanceObject.id].partiallyLoaded) {
            this.mergeInstanceModels(this.sharedObjects[loadedInstanceObject.id], loadedInstanceObject);
          }
        });
        return this.buildSharedObjectsResult(ids);
      }).catch((error) => {
        if (error && error.status === StatusCodes.NOT_FOUND) {
          return this.buildSharedObjectsResult(ids);
        } else {
          throw error;
        }
      });
    } else {
      result = this.promiseAdapter.resolve(this.buildSharedObjectsResult(ids));
    }
    result.then(() => {
      this.sharedObjectsRegistry.registerWidget(widgetId, ids, reset);
    });
    return result;
  }

  mergeInstanceModels(sharedInstance, loadedInstance) {
    sharedInstance.models.validationModel.addPropertiesToModel(loadedInstance.models.validationModel);
    sharedInstance.models.viewModel.addFieldsToModel(loadedInstance.models.viewModel.serialize().fields);
    sharedInstance.partiallyLoaded = loadedInstance.partiallyLoaded;
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
      if (this.sharedObjectsRegistry.isRegisteredToAnyWidget(objectId)
        && (!onlyModified || this.sharedObjects[objectId].isChanged() || this.currentObjectId === objectId)) {
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
}

export class InstanceObject {
  constructor(id, models, content, partiallyLoaded) {
    this.id = id;
    this.models = models;
    this.content = content;
    this.partiallyLoaded = partiallyLoaded;
    if (models) {
      models.id = id;
      this.headers = models.headers || [];
      this.instanceType = models.instanceType;
      if (models.validationModel && !(models.validationModel instanceof InstanceModel)) {
        // push headers in view and validation models
        Object.keys(this.headers).forEach((header) => {
          this.models.validationModel[header] = {
            value: this.headers[header],
            defaultValue: this.headers[header]
          };
          this.models.viewModel.fields.push({
            'identifier': header,
            'dataType': 'text',
            'displayType': 'SYSTEM',
            'isDataProperty': true,
            'control': {
              'identifier': 'INSTANCE_HEADER'
            }
          });
        });
        this.models.validationModel = new InstanceModel(models.validationModel);
      }
      if (models.viewModel && !(models.viewModel instanceof DefinitionModel)) {
        this.models.viewModel = new DefinitionModel(this.models.viewModel);
      }
    }
  }

  getModels() {
    return this.models;
  }

  setModels(newModels) {
    this.models = newModels;
    return this;
  }

  getContent() {
    return this.content;
  }

  setContent(newContent) {
    this.content = newContent;
    return this;
  }

  getId() {
    return this.id;
  }

  setId(newId) {
    this.id = newId;
    return this;
  }

  getHeader(type) {
    return this.headers && this.headers[type || HEADER_COMPACT];
  }

  getHeaders() {
    return this.headers;
  }

  setHeaders(headers) {
    this.headers = headers;
  }

  getInstanceType() {
    return this.instanceType;
  }

  setThumbnail(thumbnail) {
    this.thumbnail = thumbnail;
  }

  getThumbnail() {
    return this.thumbnail;
  }

  setWriteAllowed(writeAllowed) {
    this.writeAllowed = writeAllowed;
  }

  getWriteAllowed() {
    return this.writeAllowed;
  }

  isPersisted() {
    return !!this.getId() && this.getId() !== CURRENT_OBJECT_TEMP_ID;
  }

  isLocked() {
    return this.models.validationModel.lockedBy && this.models.validationModel.lockedBy.value !== undefined;
  }

  setContextPath(contextPath) {
    this.contextPath = contextPath;
  }

  getContextPath() {
    return this.contextPath;
  }

  getContextPathIds() {
    let path = this.getContextPath();
    if (path && path.length) {
      return this.getContextPath().map((item) => {
        return item.id;
      });
    }
    return [];
  }

  getPropertyValue(propertyName) {
    let validationModel = _.get(this, 'models.validationModel');
    if (validationModel && validationModel[propertyName]) {
      return validationModel[propertyName].value;
    }
  }

  /**
   * Sets the provided map of properties into the validation model of the instance. The properties are converted before
   * being stored in the validation model.
   *
   * This operation will be considered as a change set in the model!
   * If a property does not exist in the validation model it will not be set!
   *
   * @param properties - map of instance properties
   */
  setPropertiesValue(properties) {
    if (this.models.validationModel && properties) {
      let flatViewModelMap = ModelUtils.flatViewModel(this.models.viewModel);
      Object.keys(properties).forEach((key) => {
        if (this.models.validationModel[key]) {
          var newValue = properties[key];
          InstanceObject.setIncomingPropertyValue(flatViewModelMap.get(key), this.models.validationModel[key], newValue);
        }
      });
    }
  }

  /**
   * Generates properties change set based on the existing validation and view model for this instance.
   *
   * @param forDraft - if true changed values are returned as is without modifications
   * @returns {{}} a map with property name and propery value for all changed properties
   */
  getChangeset(forDraft) {
    let changeSet = {};
    if (!this.models.validationModel) {
      return changeSet;
    }
    let flatViewModelMap = ModelUtils.flatViewModel(this.models.viewModel);
    Object.keys(this.models.validationModel.serialize()).forEach((propertyName) => {
      let propertyViewModel = flatViewModelMap.get(propertyName);
      let propertyValidationModel = this.models.validationModel[propertyName];
      // Changeset for draft should be unchanged so it can be restored as is
      let propertyValues = forDraft ? propertyValidationModel : InstanceObject.convertPropertyValues(propertyViewModel, propertyValidationModel);
      let areEqual = angularAdapter.equals(propertyValues.defaultValue, propertyValues.value);
      // When value is cleared by the user through the UI or by condition assume the value is changed if the defaultValue
      // is defined which means the field initially had some value. Otherwise if the field didn't have value initially
      // then the user has completed the value, but later removed it again, then as result the value should not be
      // considered as changed because it was initially not defined: value=null and defaultValue=null
      if (!propertyValues.value && propertyValues.defaultValue) {
        // for different type values return:
        // string: null
        // object (like user): null
        // boolean (checkboxes): can not be erased from the UI - always true|false
        // date|datetime: null - dates are sent as strings so if deleted from UI, return nulls
        // !!!array (multiselect fields) - probably null?
        changeSet[propertyName] = null;
      } else if (!areEqual && propertyValues.value) {
        changeSet[propertyName] = propertyValues.value;
      }
    });
    return changeSet;
  }

  /**
   * Iterates the model and returns true the first time when a changed property is found
   * @returns {boolean}
   */
  isChanged() {
    if (!this.models.validationModel) {
      return false;
    }
    let flatViewModelMap = ModelUtils.flatViewModel(this.models.viewModel);
    let index = _.findIndex(Object.keys(this.models.validationModel.serialize()), (propertyName) => {
      let propertyViewModel = flatViewModelMap.get(propertyName);
      let propertyValidationModel = this.models.validationModel[propertyName];
      let propertyValues = InstanceObject.convertPropertyValues(propertyViewModel, propertyValidationModel);

      return !angularAdapter.equals(propertyValues.defaultValue, propertyValues.value) && (!InstanceObject.isNil(propertyValues.defaultValue) || !InstanceObject.isNil(propertyValues.value));
    });
    return index !== -1;
  }

  /**
   * Checks if the given model has any mandatory fields.
   *
   * @return true if there is any mandatory field, false otherwise.
   */
  hasMandatory() {
    return this.checkMandatory(this.models.viewModel);
  }

  checkMandatory(model) {
    return !model.fields ? model.isMandatory : model.fields.some((field) => {
      return this.checkMandatory(field);
    });
  }

  static isNil(value) {
    return value === null || value === undefined;
  }

  /**
   * Converts property values to format used when saving the instance.
   * We work with InstanceObjects when dealing with object properties but only URIs are send to the server
   * @param propertyViewModel
   * @param propertyValidationModel
   * @returns {{defaultValue: *, value: *}}
   */
  static convertPropertyValues(propertyViewModel, propertyValidationModel) {
    let defaultValue = propertyValidationModel.defaultValue;
    let value = propertyValidationModel.value;

    if (InstanceObject.isObjectProperty(propertyViewModel)) {
      defaultValue = InstanceObject.formatObjectPropertyValue(defaultValue);
      value = InstanceObject.formatObjectPropertyValue(value);
    }

    return {
      defaultValue,
      value
    };
  }

  /**
   * Converts and sets property value into validation model.
   * @param propertyViewModel
   * @param propertyValidationModel
   * @param newValue
   */
  static setIncomingPropertyValue(propertyViewModel, propertyValidationModel, newValue) {
    if (InstanceObject.isCodelistProperty(propertyViewModel)) {
      if (newValue instanceof Array) {
        let value = [];
        let valueLabel = [];
        newValue.forEach((item) => {
          value.push(item.id);
          valueLabel.push(item.text);
        });
        propertyValidationModel.value = value;
        propertyValidationModel.valueLabel = valueLabel.join(', ');
      } else {
        propertyValidationModel.value = newValue.id;
        propertyValidationModel.valueLabel = newValue.text;
      }
    } else {
      propertyValidationModel.value = newValue;
    }
  }

  static isObjectProperty(viewModel) {
    return viewModel && !viewModel.isDataProperty;
  }

  static isCodelistProperty(viewModel) {
    return viewModel && !!viewModel.codelist;
  }

  /**
   * Converts object property value into proper format to be send to the server.
   * Relations are stored in the model in JSON format used for storing instances. When sending to the server only their ids are send.
   * @param rawValue
   * @returns {*}
   */
  static formatObjectPropertyValue(rawValue) {
    if (rawValue) {
      return rawValue.map((relationTarget) => {
        return relationTarget.id;
      });
    } else {
      // If default value is undefined but user opens the picker and does not select an object then defaultValue will be undefined and value will be [] and property will be considered changed.
      // This will fix described problem.
      return [];
    }
  }

  updateLocalModel(updatedModel) {
    Object.keys(this.models.validationModel.serialize()).filter((propertyName) => {
      return updatedModel.hasOwnProperty(propertyName);
    }).forEach((propertyName) => {
      let isUpdatedProperty = updatedModel[propertyName].value && (updatedModel[propertyName].value !== this.models.validationModel[propertyName].value);
      if (isUpdatedProperty) {
        this.models.validationModel[propertyName].value = updatedModel[propertyName].value;
      }
      // Clone in case that value is not primitive
      this.models.validationModel[propertyName].defaultValue = _.cloneDeep(this.models.validationModel[propertyName].serialize().value);
      let isFieldWithLabel = this.models.validationModel[propertyName].valueLabel || updatedModel[propertyName].valueLabel;
      if (isFieldWithLabel) {
        this.models.validationModel[propertyName].valueLabel = updatedModel[propertyName].valueLabel;
      }
    });
  }

  /**
   * This is usually used to merge models after idoc save action where some object properties might be changed or
   * populated on the serverside.
   *
   * @param properties
   */
  mergePropertiesIntoModel(properties) {
    if (this.models.validationModel) {
      let flatViewModelMap = ModelUtils.flatViewModel(this.models.viewModel);
      Object.keys(this.models.validationModel.serialize()).forEach((propertyName) => {

        if (properties && properties[propertyName] !== undefined) {
          let newValue = properties[propertyName];
          InstanceObject.setIncomingPropertyValue(flatViewModelMap.get(propertyName), this.models.validationModel[propertyName], newValue);
        }
        this.models.validationModel[propertyName].defaultValue = _.cloneDeep(this.models.validationModel[propertyName].value);
        if (this.models.validationModel[propertyName].valueLabel) {
          this.models.validationModel[propertyName].defaultValueLabel = _.cloneDeep(this.models.validationModel[propertyName].valueLabel);
        }
      });
      if (properties) {
        Object.keys(properties).forEach((propertyName) => {
          if (!this.models.validationModel[propertyName]) {
            // Add dummy property into validation model for properties which does not exist in the model
            this.models.validationModel[propertyName] = {
              value: properties[propertyName],
              defaultValue: properties[propertyName]
            };
          }
        });
      }
    }
  }

  /**
   * Headers are available in instance but not in model anymore so their values have to be filled in validation model.
   */
  mergeHeadersIntoModel(headers) {
    if (headers) {
      Object.keys(headers).forEach((header) => {
        this.models.validationModel[header].value = headers[header];
        this.models.validationModel[header].defaultValue = _.cloneDeep(this.models.validationModel[header].value);
      });
    }
  }

  /**
   * Used when there are changes in a separated/cloned model that should be merged in the original model. This usually
   * happens if there is a cloned version of the model for example when idoc is saved and there is invalid data in an
   * object which causes the object details to be rendered in a window. For that window a cloned model is used in order
   * to allow the cancel operation to not require any sort of data revert but just dismiss the cloned model.
   *
   * @param newModel
   */
  mergeModelIntoModel(newModel) {
    Object.keys(this.models.validationModel.serialize()).forEach((propertyName) => {
      if (newModel.hasOwnProperty(propertyName) && newModel[propertyName].value !== this.models.validationModel[propertyName].value) {
        this.models.validationModel[propertyName].value = newModel[propertyName].value;
      }
    });
  }

  revertChanges() {
    Object.keys(this.models.validationModel.serialize()).forEach((propertyName) => {
      this.assignDefaultValue(this.models.validationModel[propertyName], 'value', 'defaultValue');

      if (this.models.validationModel[propertyName].defaultValueLabel) {
        this.assignDefaultValue(this.models.validationModel[propertyName], 'valueLabel', 'defaultValueLabel');
      }
    });
  }

  assignDefaultValue(propertyModel, valueKey, defaultValueKey) {
    let clonedDefaultValue = _.cloneDeep(propertyModel[defaultValueKey]);
    if (propertyModel[valueKey] instanceof Array) {
      propertyModel[valueKey].splice(0);
      if (clonedDefaultValue) {
        propertyModel[valueKey].push(...clonedDefaultValue);
      }
    } else {
      propertyModel[valueKey] = clonedDefaultValue;
    }
  }

  isVersion() {
    return !!this.getPropertyValue('isVersion');
  }
}

IdocContext.ERROR_NOT_FOUND_KEY = 'error.object.not.found';
IdocContext.FORBIDDEN_KEY = 'error.object.forbidden';
