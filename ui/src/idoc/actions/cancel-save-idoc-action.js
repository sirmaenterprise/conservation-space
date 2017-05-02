import {Injectable, Inject} from 'app/app';
import {Router} from 'adapters/router/router';
import {ActionHandler} from 'services/actions/action-handler';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import {StateParamsAdapter} from 'adapters/router/state-params-adapter';
import {MODE_PREVIEW} from 'idoc/idoc-constants';
import {InstanceAction} from 'idoc/actions/instance-action';
import {ValidationService} from 'form-builder/validation/validation-service';
import {ActionsService} from 'services/rest/actions-service';
import {IdocDraftService} from 'services/idoc/idoc-draft-service';
import {Logger} from 'services/logging/logger';
import {UNLOCK} from 'idoc/actions/action-constants';

@Injectable()
@Inject(Router, WindowAdapter, StateParamsAdapter, ActionsService, ValidationService, Logger, IdocDraftService)
export class CancelSaveIdocAction extends InstanceAction {

  constructor(router, windowAdapter, stateParamsAdapter, actionsService, validationService, logger, idocDraftService) {
    super(logger);
    this.router = router;
    this.windowAdapter = windowAdapter;
    this.stateParamsAdapter = stateParamsAdapter;
    this.actionsService = actionsService;
    this.validationService = validationService;
    this.idocDraftService = idocDraftService;
  }

  execute(action, context) {
    var currentObject = context.currentObject;
    var idocPageController = context.idocPageController;

    if (currentObject.isPersisted()) {
      idocPageController.stopDraftInterval();
      return this.idocDraftService.deleteDraft(context.idocContext)
        .then(() => { return this.validationService.init(); })
        .then(() => {
        context.idocContext.revertAllChanges();
        //revalidate in case there were invalid fields.
        context.idocContext.getAllSharedObjects().forEach((sharedObject)=> {
          this.validationService.validate(sharedObject.models.validationModel, sharedObject.models.viewModel.flatModelMap, sharedObject.id, false);
        });
        this.actionsService.unlock(context.currentObject.getId(), this.buildActionPayload(action, currentObject, UNLOCK)).then(() => {
          this.refreshInstance({id: context.currentObject.getId()}, context);
        });
        // revert content to initial content
        idocPageController.appendContent(currentObject.getContent());
        idocPageController.setViewMode(MODE_PREVIEW);
        this.router.navigate('idoc', this.stateParamsAdapter.getStateParams(), {reload: true, inherit: false, notify: false});
      });
    } else {
      this.windowAdapter.location.href = currentObject.getModels().returnUrl;
    }
  }
}