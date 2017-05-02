import _ from 'lodash';
import base64 from 'common/lib/base64';
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
import {MODE_PREVIEW} from 'idoc/idoc-constants';
import {FormWrapper} from 'form-builder/form-wrapper';
import {InstanceUtils} from 'instance/utils';
import {UNLOCK} from 'idoc/actions/action-constants';
import {SELECT_OBJECT_AUTOMATICALLY} from 'idoc/widget/object-selector/object-selector';
import {SearchResolverService} from 'services/resolver/search-resolver-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {IdocDraftService} from 'services/idoc/idoc-draft-service';
import {UserService} from 'services/identity/user-service';
import {Configuration} from 'common/application-config';

const STATE_PARAM_ID = 'id';

@Injectable()
@Inject(Eventbus, InstanceRestService, Logger, ValidationService, NotificationService, TranslateService,
  StateParamsAdapter, Router, SaveDialogService, ActionsService, SearchResolverService, PromiseAdapter,
  IdocDraftService, UserService, Configuration)
export class SaveIdocAction extends InstanceAction {

  constructor(eventbus, instanceRestService, logger, validationService, notificationService, translateService,
              stateParamsAdapter, router, saveDialogService, actionsService, searchResolverService, promiseAdapter,
              idocDraftService, userService, configuration) {
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
        models = this.getInvalidObjectsModels(context);
        if (Object.keys(models).length === 0) {
          return Promise.resolve();
        }
        context.idocPageController.animateSaveButton(false);
        return this.saveDialogService.openDialog({
          models: models,
          context: context,
          actionDefinition: action,
          onFormValidated: this.allowSave()
        });
      })
      .then(() => {
        return this.save(context, models)
          .catch((error)=> {
            context.idocPageController.disableSaveButton(false);
            this.logger.error(error);
          });
      })
      .catch((error) => {
        context.idocPageController.disableSaveButton(false);
        if (error) {
          this.notificationService.error(error);
          this.logger.error(error);
        }
      });
  }

  save(context, models) {
    context.idocContext.mergeObjectsModels(models);

    this.eventbus.publish(new BeforeIdocSaveEvent());

    var payloadResolver = this.getSaveObjectPayload(context);
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
    if (updatingCurrentUser) {
      this.userService.getCurrentUser(true).then((currentUser) => {
        this.translateService.changeLanguage(currentUser.language || this.configuration.get('system.language'));
      })
    }
    return this.reloadIdoc(response, id, context);
  }

  reloadIdoc(response, id, context){
      // default method - template method pattern
  }

  stopDraftInterval(context){
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
        data.content = context.idocPageController.getIdocContent();
        return this.extractDynamicCriteriaMap(data.content, context).then((dynamicQueries) => {
          data.dynamicQueries = dynamicQueries;
          return this.promiseAdapter.resolve(data);
        });
      } else {
        return this.promiseAdapter.resolve(data);
      }
    });
    return this.promiseAdapter.all(objectDataResolvers);
  }

  /**
   * Extracts a map with all dynamic queries in all widgets ('Select object automatically' is selected in widget config)
   * @param content idoc content
   * @param context idoc context
   * @returns a promise which resolves with {{}} a map with criteria id (at root level) as key and criteria itself as value
   */
  extractDynamicCriteriaMap(content, context) {
    let criteriaResolvers = [];
    let widgets = $(content).find('.widget');
    widgets.each((index, widget) => {
      let widgetConfigEncoded = $(widget).attr('config');
      if (widgetConfigEncoded) {
        let widgetConfig = JSON.parse(base64.decode(widgetConfigEncoded));
        if (SELECT_OBJECT_AUTOMATICALLY === widgetConfig.selectObjectMode && widgetConfig.criteria) {
          let criteria = _.cloneDeep(widgetConfig.criteria);
          let criteriaResolver = this.getDynamicCriteriaResolver(criteria, context);
          criteriaResolvers.push(criteriaResolver);
        }
      }
    });
    return this.promiseAdapter.all(criteriaResolvers).then((resolvedCriterias) => {
      let dynamicCriteriaMap = {};
      resolvedCriterias.forEach((resolvedCriteria) => {
        dynamicCriteriaMap[resolvedCriteria.id] = resolvedCriteria;
      });
      return dynamicCriteriaMap;
    });
  }

  getDynamicCriteriaResolver(criteria, context) {
    return this.searchResolverService.resolve(criteria, context.idocContext).then(() => {
      return criteria;
    });
  }

  /**
   * If for any shared object the validation service returns that it has invalid data, then save action should not be
   * enabled.
   *
   * button: The ok button.
   * data: Event payload object.
   */
  allowSave() {
    return (button, data) => {
      this.invalidObjectsMap[data[0].id] = data[0].isValid;
      let invalid = Object.keys(this.invalidObjectsMap).some((id) => {
        return !this.invalidObjectsMap[id];
      });
      button.disabled = invalid;
    };
  }

  getInvalidObjectsModels(context) {
    this.invalidObjectsMap = {};
    let invalidObjects = {};
    var sharedObjects = context.idocContext.getAllSharedObjects(true);
    sharedObjects.forEach((sharedObject) => {
      let id = sharedObject.getId();
      if (!id) {
        return;
      }
      var objectModels = sharedObject.getModels();
      let isValid = this.validationService.validate(objectModels.validationModel,
        objectModels.viewModel.flatModelMap, id);
      if (!isValid) {
        this.invalidObjectsMap[id] = false;
        invalidObjects[id] = {
          models: {
            id: id,
            definitionId: objectModels.definitionId,
            // Clone model so if user press Cancel in the dialog changes will not be applied
            validationModel: objectModels.validationModel.clone(),
            viewModel: objectModels.viewModel,
            headers: sharedObject.getHeaders()
          },
          formViewMode: FormWrapper.FORM_VIEW_MODE_EDIT,
          renderMandatory: true
        };
      }
    });
    return invalidObjects;
  }

  /**
   * Reloads silently iDoc form i.e. data, contexts, location are updated but the form is not reloaded
   */
  reloadIDocSilently(response, currentObjectId, context) {
    return this.actionsService.unlock(currentObjectId, this.buildActionPayload({}, context.currentObject, UNLOCK)).then(() => {
      this.notificationService.success(this.translateService.translateInstant('idoc.save.notification.success'));
      response.data.forEach((object) => this.updateDetails(object, currentObjectId, context));
    });
  }

  updateDetails(object, currentObjectId, context) {
    if (object.id === currentObjectId) {
      context.id = object.id;
      context.currentObject.setId(object.id);
      this.stateParamsAdapter.setStateParam(STATE_PARAM_ID, object.id);
    }
    this.refreshInstance(object, context);
  }

  afterInstanceRefreshHandler(context) {
    context.idocPageController.setViewMode(MODE_PREVIEW);
    context.idocPageController.checkPermissionsForEditAction();
    this.router.navigate('idoc', this.stateParamsAdapter.getStateParams(), {notify: true, skipRouteInterrupt: true});
  }
}

SaveIdocAction.PURPOSE = 'emf:purpose';
SaveIdocAction.MIMETYPE = 'mimetype';
SaveIdocAction.VERSION = 'emf:version';