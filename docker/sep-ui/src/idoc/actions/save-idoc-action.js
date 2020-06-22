import {Injectable, Inject} from 'app/app';
import {Router} from 'adapters/router/router';
import {Logger} from 'services/logging/logger';
import {Eventbus} from 'services/eventbus/eventbus';
import {InstanceAction} from 'idoc/actions/instance-action';
import {SaveDialogService} from 'idoc/save-idoc-dialog/save-dialog-service';
import {BeforeIdocSaveEvent} from 'idoc/actions/events/before-idoc-save-event';
import {AfterIdocSaveEvent} from 'idoc/actions/events/after-idoc-save-event';
import {TranslateService} from 'services/i18n/translate-service';
import {InstanceRestService} from 'services/rest/instance-service';
import {StateParamsAdapter} from 'adapters/router/state-params-adapter';
import {ValidationService} from 'form-builder/validation/validation-service';
import {NotificationService} from 'services/notification/notification-service';
import {ActionsService} from 'services/rest/actions-service';
import {InstanceUtils} from 'instance/utils';
import {SearchResolverService} from 'services/resolver/search-resolver-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {IdocDraftService} from 'services/idoc/idoc-draft-service';
import {UserService} from 'security/user-service';
import {Configuration} from 'common/application-config';
import {IdocSanitizer} from '../idoc-sanitizer';
import {InstanceContextService} from 'services/idoc/instance-context-service';

@Injectable()
@Inject(Eventbus, InstanceRestService, Logger, ValidationService, NotificationService, TranslateService,
  StateParamsAdapter, Router, SaveDialogService, ActionsService, SearchResolverService, PromiseAdapter,
  IdocDraftService, UserService, Configuration, InstanceContextService)
export class SaveIdocAction extends InstanceAction {

  constructor(eventbus, instanceRestService, logger, validationService, notificationService, translateService,
              stateParamsAdapter, router, saveDialogService, actionsService, searchResolverService, promiseAdapter,
              idocDraftService, userService, configuration, instanceContextService) {
    super(logger);
    this.router = router;
    this.eventbus = eventbus;
    this.saveDialogService = saveDialogService;
    this.translateService = translateService;
    this.validationService = validationService;
    this.stateParamsAdapter = stateParamsAdapter;
    this.instanceRestService = instanceRestService;
    this.notificationService = notificationService;
    this.actionsService = actionsService;
    this.searchResolverService = searchResolverService;
    this.promiseAdapter = promiseAdapter;
    this.idocDraftService = idocDraftService;
    this.userService = userService;
    this.configuration = configuration;
    this.instanceContextService = instanceContextService;
  }

  /**
   * - Check all objects in context if they are valid
   * -- If valid, object is saved
   * -- If not valid
   * --- Open dialog with the wrong objects to allow user to edit them
   * --- If all is ok and user closes the dialog with OK, then save object
   * --- If cancel is used then object is not saved
   *
   * @param action
   * @param context
   */
  execute(action, context) {
    let models = {};
    return this.validationService.init()
      .then(() => {
        return this.getInvalidObjectsModels(context);
      }).then((invalidModels) => {
        models = invalidModels;
        if (Object.keys(models).length === 0) {
          return Promise.resolve();
        }

        context.idocActionsController.animateSaveButton(false);
        return this.saveDialogService.openDialog({
          models,
          context,
          actionDefinition: action
        });
      }).then(() => {
        return this.save(context, models)
          .catch((error) => {
            context.idocActionsController.disableSaveButton(false);
            this.logger.error(error);
          });
      })
      .catch((error) => {
        context.idocActionsController.disableSaveButton(false);
        if (error) {
          this.notificationService.error(error);
          this.logger.error(error);
        }
      });
  }

  save(context, models) {
    context.idocContext.mergeObjectsModels(models);

    this.eventbus.publish(new BeforeIdocSaveEvent());

    let payloadResolver = this.getSaveObjectPayload(context);
    return payloadResolver.then((payload) => {
      let updatingCurrentUser = SaveIdocAction.containsCurrentUserModel(payload, this.userService.getCurrentUserId());

      if (context.currentObject.isPersisted()) {
        return this.instanceRestService.updateAll(payload).then(response => {
          this.stopDraftInterval(context);
          return this.idocDraftService.deleteDraft(context.idocContext).then(() => {
            return this.afterUpdate(response, context.currentObject.getId(), context, updatingCurrentUser);
          });
        });
      } else {
        // Even if creating a new object its possible to have referenced updated objects through a widget. So on every save
        // all already persisted object ids are collected in order to allow after the save to find which is the new object
        // in the response - its id won't be present in the collected ids.
        let persistedObjectIds = SaveIdocAction.getPersistedObjectsIds(payload);
        let currentObject = payload[0];
        currentObject.parentId = context.currentObject.getModels().parentId;
        return this.instanceRestService.updateAll(payload).then(response => {
          // after the save, find out the object that is not present in the set of object ids
          // that one should be the current object with its brand new id
          let persistedCurrentObject = SaveIdocAction.findTheCurrentObject(response.data, persistedObjectIds);
          context.idocContext.setCurrentObjectId(persistedCurrentObject.id);
          context.idocContext.updateTempCurrentObjectId(persistedCurrentObject.id);
          return this.afterUpdate(response, persistedCurrentObject.id, context, updatingCurrentUser);
        });
      }
    });
  }

  static containsCurrentUserModel(models, currentUserId) {
    return models.some((model) => {
      return model.id === currentUserId;
    });
  }

  /**
   * Execute a common flow during every save operation. A template method is called during this flow in order to allow
   * concrete implementations to hook some logic in the flow.
   *
   * @param response
   * @param id
   * @param context
   * @param updatingCurrentUser If the save operation is going to update the current user.
   */
  afterUpdate(response, id, context, updatingCurrentUser) {
    this.eventbus.publish(new AfterIdocSaveEvent());
    this.executeUpdate(response, id, context, updatingCurrentUser);
  }

  executeUpdate(response, id, context, updatingCurrentUser) {
    if (updatingCurrentUser) {
      this.userService.getCurrentUser(true).then((currentUser) => {
        this.translateService.changeLanguage(currentUser.language || this.configuration.get('system.language'));
      });
    }
    return this.reloadIdoc(response, id, context);
  }

  reloadIdoc(response, id, context) {
    // default method - template method pattern
  }

  stopDraftInterval(context) {
    // default method - template method pattern
  }

  static findTheCurrentObject(responseData, persistedObjectIds) {
    let found = null;
    responseData.forEach((object) => {
      if (!persistedObjectIds.has(object.id)) {
        found = object;
        return;
      }
    });
    return found;
  }

  static getPersistedObjectsIds(payload) {
    let set = new Set();
    payload.forEach((object) => {
      if (!InstanceUtils.isTempId(object.id)) {
        set.add(object.id);
      }
    });
    return set;
  }

  getSaveObjectPayload(context) {
    let currentObject = context.currentObject;
    let currentObjectId = currentObject.getId();
    let objectDataResolvers = context.idocContext.getAllSharedObjects(true).map((object) => {
      let data = {
        definitionId: object.getModels().definitionId,
        properties: object.getChangeset()
      };
      // for not persisted instance we don't pass the temp id
      if (object.isPersisted()) {
        data.id = object.id;
      }
      if (object.getId() === currentObjectId) {
        if (currentObject.getModels().purpose) {
          data.properties[SaveIdocAction.PURPOSE] = currentObject.getModels().purpose;
          data.properties[SaveIdocAction.MIMETYPE] = 'text/html';
        }
        data.content = IdocSanitizer.sanitize(context.idocPageController.getIdocContent(true));

        // sets the version mode for the executed action, ATM it will be set only for current object
        this.setOperationSpecificProperties(data, currentObject);
      }
      return this.promiseAdapter.resolve(data);
    });
    return this.promiseAdapter.all(objectDataResolvers);
  }

  getInvalidObjectsModels(context) {
    let modifiedInstanceObjects = context.idocContext.getAllSharedObjects(true);
    return this.validate(modifiedInstanceObjects).then((invalidInstanceObjects) => {
      return this.validateExistingInContext(modifiedInstanceObjects, invalidInstanceObjects).then(() => {
        let invalidObjectModels = {};
        invalidInstanceObjects.forEach((invalidInstanceObject) => {
          invalidObjectModels[invalidInstanceObject.getId()] = SaveIdocAction.getInvalidObject(invalidInstanceObject);
        });
        return invalidObjectModels;
      });
    });
  }

  /**
   * Validates <code>modifiedInstanceObjects</code>.
   * @param modifiedInstanceObjects with instance objects form <code>modifiedInstanceObjects</code> which validation failed.
   */
  validate(modifiedInstanceObjects = []) {
    // Executes validation of all modified instance objects.
    return this.validationService.validateAll(modifiedInstanceObjects).then((validationResults) => {
      // Filters all valid instance objects.
      return modifiedInstanceObjects.filter(modifiedInstanceObject => {
        return !validationResults[modifiedInstanceObject.getId()].isValid;
      });
    });
  }

  validateExistingInContext(modifiedInstanceObjects = [], invalidInstanceObjects = []) {
    // Filters already known invalid instance objects.
    let validInstanceObjects = modifiedInstanceObjects.filter((modifiedInstanceObject) => {
      return !invalidInstanceObjects[modifiedInstanceObject.getId()];
    });
    // Executes existence in context validation for all valid instance objects.
    return this.instanceContextService.validateExistingInContextAll(validInstanceObjects).then((validationResults) => {
      // Filters all valid instance objects.
      let invalidExistingInContextInstanceObjects = validInstanceObjects.filter((validInstanceObject) => {
        return !validationResults[validInstanceObject.getId()].isValid;
      });
      // add all invalided instance objects to array with invalided instance objects.
      invalidInstanceObjects.push(...invalidExistingInContextInstanceObjects);
    });
  }

  static getInvalidObject(instanceObject) {
    let instanceObjectModels = instanceObject.getModels();
    return {
      models: {
        id: instanceObject.getId(),
        definitionId: instanceObjectModels.definitionId,
        // Clone model so if user press Cancel in the dialog changes will not be applied
        validationModel: instanceObjectModels.validationModel.clone(),
        viewModel: instanceObjectModels.viewModel,
        headers: instanceObject.getHeaders()
      }
    };
  }

  /**
   * Sets version mode property to the passed object. This mode controls the creation of the objects versions, when
   * they are saved. The save actions should create new versions for the objects only when they are executed for the
   * first time after the user enters edit mode, every save after that should update the latest version, without
   * creating new.<br />
   * Currently the version mode is set to 'MINOR', when the object is opened for edit. After that the version mode is
   * overridden by the back-end to 'UPDATE' and send back to the clint. That way the latest version will be updated,
   * until the object is switched between preview and edit mode again. <br />
   * The method will set the version mode to 'UPDATE' by default, if it is not passed(by the back-end or another logic)
   * as a top level property of the models for the passed instance object.
   * This method could be used to set other properties as well. <br />
   * Note that version mode property is temporary and it won't be persisted anywhere.
   *
   * @param object to which will be set the properties
   * @param {InstanceObject} instanceObject contains all available instance data
   */
  setOperationSpecificProperties(object, instanceObject) {
    let versionMode = instanceObject.getModels()[SaveIdocAction.VERSION_MODE];
    object[SaveIdocAction.VERSION_MODE] = versionMode ? versionMode : SaveIdocAction.UPDATE;
  }
}

SaveIdocAction.PURPOSE = 'emf:purpose';
SaveIdocAction.MIMETYPE = 'mimetype';
SaveIdocAction.VERSION = 'emf:version';
SaveIdocAction.VERSION_MODE = '$versionMode$';
SaveIdocAction.MINOR = 'MINOR';
SaveIdocAction.UPDATE = 'UPDATE';
