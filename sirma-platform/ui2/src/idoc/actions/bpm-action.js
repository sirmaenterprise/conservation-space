import _ from 'lodash';
import {Injectable, Inject} from 'app/app';
import {TransitionAction} from 'idoc/actions/transition-action';
import {ActionsService} from 'services/rest/actions-service';
import {FormWrapper} from 'form-builder/form-wrapper';
import {SaveDialogService} from 'idoc/save-idoc-dialog/save-dialog-service';
import {ValidationService} from 'form-builder/validation/validation-service';
import {InstanceRestService} from 'services/rest/instance-service';
import {BpmService} from 'services/rest/bpm-service';
import {InstanceObject} from 'models/instance-object';
import {Logger} from 'services/logging/logger';
import {Eventbus} from 'services/eventbus/eventbus';
import {TranslateService} from 'services/i18n/translate-service';
import {NotificationService} from 'services/notification/notification-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {StatusCodes} from 'services/rest/status-codes';
import {InstanceRefreshEvent} from 'idoc/actions/events/instance-refresh-event';

const OPERATION = 'bpmTransition';

/**
 * Action handler for all BPMTranstions.
 */
@Injectable()
@Inject(ActionsService, ValidationService, SaveDialogService, InstanceRestService, Eventbus, Logger, PromiseAdapter, BpmService, TranslateService, NotificationService)
export class BpmTransitionAction extends TransitionAction {

  constructor(actionsService, validationService, saveDialogService, instanceRestService, eventbus, logger, promiseAdapter, bpmService, translateService, notificationService) {
    super(actionsService, validationService, saveDialogService, instanceRestService, eventbus, logger, promiseAdapter);
    this.bpmService = bpmService;
    this.translateService = translateService;
    this.notificationService = notificationService;
  }

  /**
   * - Fetch mandatory fields for the object for given state
   * - If there are uncompleted mandatory fields open a dialog with object details form
   * - OK button in dialog is executing the transition
   * - OK button should be disabled until all mandatory fields are completed
   * - Cancel button is rejecting and dismissing the dialog
   *
   * @param actionDefinition
   * @param context
   */
  execute(actionDefinition, context) {
    return this.promiseAdapter.promise((resolve, reject) => {
      return this.validationService.init()
        .then(() => {
          return this.getObjectModel(context, actionDefinition)
            .then((model) => {
              this.invalidObjects = model;
              if (_.isEmpty(model)) {
                return;
              }
              return this.saveDialogService.openDialog({
                models: model,
                context,
                actionDefinition
              });
            });
        }).then(() => {
          // work with the models returned by the service - there is the actual data
          return this.executeTransition(context, actionDefinition, this.invalidObjects).then(() => {
            resolve();
          });
        }).catch((error) => {
          if (context.idocContext) {
            //restore original models.
            context.idocContext.revertAllChanges();
          }
          if (error) {
            this.logger.error(error);
          }
          reject();
        });
    });
  }

  /**
   * Executes the bpmTransition.
   *
   * @param context The action context
   * @param actionDefinition the action definition
   * @param models Models for the object for which this transition should be performed.
   */
  executeTransition(context, actionDefinition, models) {
    let currentObjectId = context.currentObject.getId();
    let actionPayload = this.bpmService.buildBPMActionPayload(currentObjectId, actionDefinition, models, OPERATION);
    return this.bpmService.executeTransition(currentObjectId, actionPayload).then((response) => {
      let createdObjectHeader = this.translateService.translateInstantWithInterpolation('bpm.transition', {
        transitionId: actionDefinition.label
      });

      if (response.data.length > 0) {
        createdObjectHeader += this.translateService.translateInstant('bpm.created');
      }

      response.data.forEach((item) => {
        createdObjectHeader += item.headers.breadcrumb_header + '<br>';
      });

      this.notificationService.success(createdObjectHeader);
      this.notifyOnUpdate(undefined, response);

      return response;
    }, (response) => {
      if (response.status === StatusCodes.SERVER_ERROR && response.data && response.data.labelId) {
        let message = this.translateService.translateInstant(response.data.labelId);
        this.notificationService.warning(message);
      } else {
        this.notificationService.error(this.translateService.translateInstant('error.generic'));
      }
    });
  }

  /**
   * Retrieves the object model for the next instances in the bpm transition chain
   * also gets the models for the current instance so we validate any missing properties.
   *
   * @param context
   *           the current context
   * @param actionDefinition
   *       the actionDefinition
   */
  getObjectModel(context, actionDefinition) {
    let id = context.currentObject.getId();

    return this.bpmService.loadModel(id, actionDefinition.action).then((response) => {
      let models = {};

      Object.keys(response.data).forEach((id) => {
        let instance = response.data[id];
        //Modify the label to have span tags to avoid errors.
        let header = '<span>' + instance.model.definitionLabel + '</span>';
        instance.model.headers = {
          compact_header: header,
          breadcrumb_header: header,
          default_header: header
        };
        let instanceObject = this.generateInstance(id, instance);
        //We need to merge the data for the current instance which can be found in the Db
        instanceObject.mergePropertiesIntoModel(instance.instance.properties);
        instanceObject.models.id = instance.instance.id;
        instanceObject.models.definitionId = instance.instance.definitionId;
        // As the model don't give any information if the instance is new or is persisted, a separated flag is added.
        instanceObject.models.isNewInstance = true;
        models[instance.instance.id] = instanceObject;
      });
      let result = {};
      let currentInstance;
      Object.keys(models).forEach((id) => {
        let instance = models[id];
        if (instance.id === context.currentObject.getId()) {
          currentInstance = instance;
        } else if (instance.hasMandatory()) {
          result[id] = models[id];
        }
      });

      return {result, instance: currentInstance, models};
    }).then((response) => {
      let instance = response.instance;
      let result = response.result;
      let models = response.models;
      if (instance) {
        return this.validationService.validate(instance.models.validationModel, instance.models.viewModel.flatModelMap, instance.id, undefined, null, instance.models.definitionId, instance.models.id).then((isValid) => {
          if (!isValid) {
            result[id] = models[id];
          }
          return result;
        });
      }
      return result;
    });
  }

  generateInstance(id, instance) {
    return new InstanceObject(id, instance.model);
  }

  notifyOnUpdate(actionDefinition, response) {
    response.config.data.id = response.config.data.currentInstance;
    this.eventbus.publish(new InstanceRefreshEvent({
      actionDefinition,
      response: {
        data: response.config.data
      }
    }));
  }
}