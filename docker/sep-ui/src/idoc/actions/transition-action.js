import {Injectable, Inject} from 'app/app';
import {InstanceAction} from 'idoc/actions/instance-action';
import {ActionsService} from 'services/rest/actions-service';
import {SaveDialogService} from 'idoc/save-idoc-dialog/save-dialog-service';
import {ValidationService} from 'form-builder/validation/validation-service';
import {InstanceRestService} from 'services/rest/instance-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {InstanceRefreshEvent} from 'idoc/actions/events/instance-refresh-event';
import {Eventbus} from 'services/eventbus/eventbus';
import {Logger} from 'services/logging/logger';
import {FormWrapper} from 'form-builder/form-wrapper';
import {ModelUtils} from 'models/model-utils';
import {TranslateService} from 'services/i18n/translate-service';
import {NotificationService} from 'services/notification/notification-service';

const OPERATION = 'transition';

/**
 * Action handler for all object state changing actions.
 */
@Injectable()
@Inject(ActionsService, ValidationService, SaveDialogService, InstanceRestService, Eventbus, Logger, PromiseAdapter, TranslateService, NotificationService)
export class TransitionAction extends InstanceAction {

  constructor(actionsService, validationService, saveDialogService, instanceRestService, eventbus, logger, promiseAdapter, translateService, notificationService) {
    super(logger);
    this.eventbus = eventbus;
    this.actionsService = actionsService;
    this.saveDialogService = saveDialogService;
    this.validationService = validationService;
    this.instanceRestService = instanceRestService;
    this.promiseAdapter = promiseAdapter;
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
    let models;
    let id;
    return this.promiseAdapter.promise((resolve, reject) => {
      this.validationService.init().then(() => {
        return this.getObjectModel(context, actionDefinition);
      }).then((model) => {
        models = model;
        id = context.currentObject.getId();
        return this.validationService.validate(models[id].models.validationModel,
          models[id].models.viewModel.flatModelMap, id, undefined, null, models[id].models.definitionId, models[id].models.id);
      }).then((isValid) => {
        let hasMandatoryFields = this.hasMandatoryFields(models[id].models.viewModel, models[id].models.validationModel);
        if (isValid && !hasMandatoryFields) {
          return this.promiseAdapter.resolve();
        }
        return this.saveDialogService.openDialog({
          models,
          context,
          actionDefinition,
          formConfig: {
            formViewMode: FormWrapper.FORM_VIEW_MODE_EDIT,
            renderMandatoryForState: true
          }
        });
      }).then(() => {
        // work with the models returned by the service - there is the actual data
        return this.executeTransition(context, actionDefinition, models).then((response) => {
          this.notificationService.success(this.translateService.translateInstant('action.notification.success') + actionDefinition.label);
          resolve(response);
          return response;
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
   * @param context The action context
   * @param actionDefinition
   * @param models Models for the object for which this transition should be performed.
   */
  executeTransition(context, actionDefinition, models) {
    let currentObjectId = context.currentObject.getId();
    return this.actionsService.executeTransition(currentObjectId, this.buildActionPayload(actionDefinition, models[currentObjectId], OPERATION))
      .then((response) => {
        this.eventbus.publish(new InstanceRefreshEvent({
          actionDefinition,
          response
        }));
        this.checkPermissionsForEditAction(context);
      });
  }

  getObjectModel(context, actionDefinition) {
    let id = context.currentObject.getId();
    return this.instanceRestService.loadInstanceObject(id, actionDefinition.action).then(instanceObject => {
      return {
        [id]: instanceObject
      };
    });
  }

  hasMandatoryFields(viewModel, validationModel) {
    let hasMandatoryField = false;
    ModelUtils.walkModelTree(viewModel.fields, validationModel, (fieldViewModel) => {
      if (hasMandatoryField) {
        return;
      }
      let validators = fieldViewModel.validators || [];
      hasMandatoryField = validators.some((validator) => {
        return validator.id === 'mandatory' && validator.isMandatoryForState;
      });
    });
    return hasMandatoryField;
  }
}