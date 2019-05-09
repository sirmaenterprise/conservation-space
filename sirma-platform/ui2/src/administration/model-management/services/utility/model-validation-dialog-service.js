import {Inject, Injectable} from 'app/app';
import {DialogService} from 'components/dialog/dialog-service';

import {ModelList} from 'administration/model-management/model/model-list';
import {ModelValidationReport} from 'administration/model-management/model/validation/model-validation-report';
import {ModelErrors} from 'administration/model-management/components/errors/model-errors';
import {ModelListValidationView} from 'administration/model-management/components/list/model-list-validation-view';

/**
 * Service responsible for displaying an error dialog when an action fails.
 * It is used to show validation errors during save. There are two types of
 * error dialogs: the first one displays a list of error messages and the
 * second one displays model list with validation log.
 *
 * The reject caught from the rest service can have status code 400 or 409.
 * If the status is 400, this service will show the second dialog containing
 * model list with validation log. If the status is 409, simple error dialog
 * wih messages will be shown.
 *
 * @author Radoslav Dimitrov
 */
@Injectable()
@Inject(DialogService)
export class ModelValidationDialogService {

  constructor(dialogService) {
    this.dialogService = dialogService;
    this.initErrorMapper();
  }

  initErrorMapper() {
    this.errorMapper = {};
    this.errorMapper[ModelValidationDialogService.CONFLICT_ERROR_STATUS] = (data) => this.showConflictErrorsDialog(data);
    this.errorMapper[ModelValidationDialogService.BAD_REQUEST_ERROR_STATUS] = (data) => this.showInvalidModelsDialog(data);
  }

  create(reject, modelStore) {
    this.store = modelStore;
    this.errorMapper[reject.status](reject.data);
  }

  showConflictErrorsDialog(data) {
    let errors = [data.message];
    Object.values(data.errors).forEach(value => errors.push(value.message));
    this.dialogService.create(ModelErrors, {errors}, this.getErrorDialogConfig());
  }

  showInvalidModelsDialog(report) {
    let modelsToDisplay = new ModelList();
    report.nodes.forEach(model => {
      model = this.store.getModel(model.id);
      model && modelsToDisplay.insert(model);
    });

    this.dialogService.create(ModelListValidationView, this.getInvalidModelsConfig(report, modelsToDisplay),
      this.getErrorDialogConfig());
  }

  getErrorDialogConfig() {
    return {
      largeModal: true,
      header: 'administration.models.management.save.fail.message',
      headerCls: 'error',
      showClose: true
    };
  }

  getInvalidModelsConfig(validationReport, modelsToDisplay) {
    return {
      config: {
        selectableItems: false,
        skipValidModels: true
      },
      models: modelsToDisplay,
      report: new ModelValidationReport(validationReport)
    };
  }
}

ModelValidationDialogService.CONFLICT_ERROR_STATUS = 409;
ModelValidationDialogService.BAD_REQUEST_ERROR_STATUS = 400;