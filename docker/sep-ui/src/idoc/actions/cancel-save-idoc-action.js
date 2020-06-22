import {Injectable, Inject} from 'app/app';
import {Router} from 'adapters/router/router';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import {StateParamsAdapter} from 'adapters/router/state-params-adapter';
import {MODE_PREVIEW} from 'idoc/idoc-constants';
import {InstanceAction} from 'idoc/actions/instance-action';
import {ValidationService} from 'form-builder/validation/validation-service';
import {ActionsService} from 'services/rest/actions-service';
import {IdocDraftService} from 'services/idoc/idoc-draft-service';
import {Logger} from 'services/logging/logger';
import {NotificationService} from 'services/notification/notification-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {TranslateService} from 'services/i18n/translate-service';
import {DialogService} from 'components/dialog/dialog-service';
import {UNLOCK} from 'idoc/actions/action-constants';
import _ from 'lodash';

@Injectable()
@Inject(Router, WindowAdapter, StateParamsAdapter, ActionsService, ValidationService, Logger, IdocDraftService, NotificationService, PromiseAdapter, TranslateService, DialogService)
export class CancelSaveIdocAction extends InstanceAction {

  constructor(router, windowAdapter, stateParamsAdapter, actionsService, validationService, logger, idocDraftService, notificationService, promiseAdapter, translateService, dialogService) {
    super(logger);
    this.router = router;
    this.windowAdapter = windowAdapter;
    this.stateParamsAdapter = stateParamsAdapter;
    this.actionsService = actionsService;
    this.validationService = validationService;
    this.idocDraftService = idocDraftService;
    this.notificationService = notificationService;
    this.promiseAdapter = promiseAdapter;
    this.translateService = translateService;
    this.dialogService = dialogService;
  }

  execute(action, context) {
    let idocPageController = context.idocPageController;

    return this.promiseAdapter.promise((resolve, reject) => {
      if (idocPageController.currentObject.isDirty() || this.isSharedObjectDirty(idocPageController)) {
        let confirmationMessage = this.translateService.translateInstant('exit.file.without.save.confirmation.message');
        this.dialogService.confirmation(confirmationMessage, null, {
          buttons: [
            {
              id: DialogService.CONFIRM,
              label: this.translateService.translateInstant('dialog.button.yes'),
              cls: 'btn-primary',
              disabled: this.disabled
            },
            {
              id: DialogService.CANCEL,
              label: this.translateService.translateInstant('dialog.button.no')
            }
          ],
          onButtonClick: (buttonID, componentScope, dialogConfig) => {
            if (buttonID === DialogService.CONFIRM) {
              dialogConfig.dismiss();
              return this.processCancelOperation(context, action, resolve, reject);
            } else {
              dialogConfig.dismiss();
              reject();
            }
          }
        });
      } else {
        return this.processCancelOperation(context, action, resolve, reject);
      }
    });
  }

  isSharedObjectDirty(idocPageController) {
    return _.some(idocPageController.context.sharedObjects, (object) => {
      return object.isChanged();
    });
  }

  processCancelOperation(context, action, resolve, reject) {
    if (context.currentObject.isPersisted()) {
      context.idocPageController.stopDraftInterval();
      return this.idocDraftService.deleteDraft(context.idocContext)
        .then(() => {
          return this.validationService.init();
        })
        .then(() => {
          context.idocContext.revertAllChanges();
          let validators = [];
          //revalidate in case there were invalid fields.
          context.idocContext.getAllSharedObjects().forEach((sharedObject) => {
            validators.push(this.validationService.validate(sharedObject.models.validationModel, sharedObject.models.viewModel.flatModelMap,
              sharedObject.id, false, null, sharedObject.models.definitionId, sharedObject.models.id));
          });

          if (validators.length) {
            return this.promiseAdapter.all(validators);
          } else {
            return this.promiseAdapter.resolve();
          }
        }).then(() => {
          // revert content to initial content
          context.idocPageController.appendContent(context.currentObject.getContent());
          context.idocPageController.setViewMode(MODE_PREVIEW);
          this.router.navigate('idoc', this.stateParamsAdapter.getStateParams(), {
            reload: true,
            inherit: false,
            notify: false
          });

        }).then(() => {
          return this.actionsService.unlock(context.idocContext.getCurrentObjectId(), this.buildActionPayload(action, context.currentObject, UNLOCK));
        }).then(() => {
          context.currentObject.setDirty(false);
          resolve();
          return this.refreshInstance({id: context.idocContext.getCurrentObjectId()}, context);
        }).catch((error) => {
          this.notificationService.error(error);
          reject(error);
        });
    } else {
      this.windowAdapter.location.href = context.currentObject.getModels().returnUrl;
      resolve();
    }
  };
}
