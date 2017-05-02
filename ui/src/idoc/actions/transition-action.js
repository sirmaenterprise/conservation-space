import {Injectable, Inject} from 'app/app';
import {InstanceAction} from 'idoc/actions/instance-action';
import {ActionsService} from 'services/rest/actions-service';
import {SaveDialogService} from 'idoc/save-idoc-dialog/save-dialog-service';
import {ValidationService} from 'form-builder/validation/validation-service';
import {InstanceRestService} from 'services/rest/instance-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {InstanceObject} from 'idoc/idoc-context';
import {InstanceRefreshEvent} from 'idoc/actions/events/instance-refresh-event';
import {Eventbus} from 'services/eventbus/eventbus';
import {Logger} from 'services/logging/logger';

const OPERATION = 'transition';

/**
 * Action handler for all object state changing actions.
 */
@Injectable()
@Inject(ActionsService, ValidationService, SaveDialogService, InstanceRestService, Eventbus, Logger, PromiseAdapter)
export class TransitionAction extends InstanceAction {

  constructor(actionsService, validationService, saveDialogService, instanceRestService, eventbus, logger, promiseAdapter) {
    super(logger);
    this.eventbus = eventbus;
    this.actionsService = actionsService;
    this.saveDialogService = saveDialogService;
    this.validationService = validationService;
    this.instanceRestService = instanceRestService;
    this.promiseAdapter = promiseAdapter;
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
    let models = {};
    return this.validationService.init()
      .then(() => {
        return this.getObjectModel(context, actionDefinition)
          .then((model) => {
            models = model;
            let id = context.currentObject.getId();
            let isValid = this.validationService.validate(models[id].models.validationModel,
              models[id].models.viewModel.flatModelMap, id);
            if (isValid) {
              return Promise.resolve();
            }
            return this.saveDialogService.openDialog({
              models: models,
              context: context,
              actionDefinition: actionDefinition,
              onFormValidated: this.onFormValidated
            });
          })
      }).then(() => {
        // work with the models returned by the service - there is the actual data
        return this.executeTransition(context, actionDefinition, models);
      }).catch((error) => {
        if(context.idocContext){
          //restore original models.
          context.idocContext.revertAllChanges();
        }
        if (error) {
          this.logger.error(error);
        }
      });
  }

  /**
   * @param context The action context
   * @param actionDefinition
   * @param models Models for the object for which this transition should be performed.
   */
  executeTransition(context, actionDefinition, models) {
    let currentObjectId = context.currentObject.getId();
    return this.actionsService.executeTransition(currentObjectId,
      this.buildActionPayload(actionDefinition, models[currentObjectId], OPERATION)).then((response) => {
      this.eventbus.publish(new InstanceRefreshEvent({
        actionDefinition: actionDefinition,
        response: response
      }));

      if(context.idocPageController) {
        context.idocPageController.checkPermissionsForEditAction();
      }
    });
  }

  getObjectModel(context, actionDefinition) {
    let id = context.currentObject.getId();
    let instanceLoader = this.instanceRestService.load(id);
    let modelsLoader = this.instanceRestService.loadModel(id, actionDefinition.action);

    return this.promiseAdapter.all([instanceLoader, modelsLoader]).then((results) => {
      let instanceData = results[0].data;
      let definitionData = results[1].data;

      definitionData.headers = instanceData.headers;

      let instanceObject = new InstanceObject(id, definitionData);
      instanceObject.mergePropertiesIntoModel(instanceData.properties);
      instanceObject.renderMandatory = true;
      let model = {};
      model[id] = instanceObject;
      return model;
    });
  }

  onFormValidated(okButton, data) {
    okButton.disabled = !data[0].isValid;
  }
}