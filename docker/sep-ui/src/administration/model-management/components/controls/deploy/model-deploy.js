import {View, Component, Inject} from 'app/app';
import {Configurable} from 'components/configurable';
import {DialogService} from 'components/dialog/dialog-service';
import {TranslateService} from 'services/i18n/translate-service';
import {NotificationService} from 'services/notification/notification-service';

import {ModelListValidationView} from 'administration/model-management/components/list/model-list-validation-view';
import 'components/button/button';

import template from './model-deploy.html!text';

/**
 * Simple action button component which is responsible for displaying and triggering the
 * process of model deployment. This component opens a dialog window and prompts the user
 * to select the specific models to be deployed. The two most important features of this
 * component are the request and confirmed component events.
 *
 * - onDeployRequested - is triggered when the user requests a deployment, this event should
 * return a promise with the models which are available for deployment. It can optionally
 * contain a list of pre-selected (by default) models for deployment.
 *
 * - onDeployConfirmed - is triggered when the user confirms the desired models for deployment
 * a {@link ModelList} is sent as a payload back to the caller containing selected models for
 * deployment.
 *
 * @author Svetlozar Iliev
 */
@Component({
  selector: 'model-deploy',
  properties: {
    'enabled': 'enabled'
  },
  events: ['onDeployRequested', 'onDeployConfirmed']
})
@View({
  template
})
@Inject(DialogService, NotificationService, TranslateService)
export class ModelDeploy extends Configurable {

  constructor(dialogService, notificationService, translateService) {
    super({
      primary: true,
      label: 'administration.models.management.deploy.changes'
    });
    this.dialogService = dialogService;
    this.notificationService = notificationService;
    this.translateService = translateService;
  }

  ngOnInit() {
    this.message = this.translateService.translateInstant('administration.models.management.deploy.success.message');
  }

  onModelDeploy() {
    if (!this.isDeployEnabled()) {
      return;
    }

    this.startDeploying();
    return this.onDeployRequested().then(deploymentRequest => {
      this.sortModelsForDeployment(deploymentRequest);
      this.dialogConfig = this.getDialogConfiguration(deploymentRequest);
      this.deployConfig = this.getModelDeployConfiguration(deploymentRequest);
      this.dialogService.create(ModelListValidationView, this.deployConfig, this.dialogConfig);
    }).finally(() => this.finishDeploying());
  }

  onListAction() {
    let selected = this.deployConfig.config.selected;
    this.dialogConfig.buttons[0].disabled = !this.hasSelectedModels(selected);
  }

  getModelDeployConfiguration(deploymentRequest) {
    return {
      config: {
        selectableItems: true,
        singleSelection: false,
        selected: deploymentRequest.getSelectedModels()
      },
      models: deploymentRequest.getModels(),
      report: deploymentRequest.getValidationReport(),
      onAction: this.onListAction.bind(this)
    };
  }

  getDialogConfiguration(deploymentRequest) {
    let selectedModels = deploymentRequest.getSelectedModels();
    return {
      largeModal: true,
      header: 'administration.models.management.deploy.dialog.header',
      buttons: [this.createConfirmButton(selectedModels), this.createCloseButton()],
      onButtonClick: (buttonId, componentScope, dialogConfig) => {
        if (buttonId === DialogService.CONFIRM && this.hasSelectedModels(selectedModels)) {
          this.startDeploying();
          this.onDeployConfirmed({deploymentRequest})
            .then(() => this.notifyForDeployment())
            .finally(() => this.finishDeploying());
        }
        dialogConfig.dismiss();
      }
    };
  }

  sortModelsForDeployment(deploymentRequest) {
    deploymentRequest.getModels().sort((left, right) => {
      let lhsLabel = left.getDescription().getValue();
      let rhsLabel = right.getDescription().getValue();
      return lhsLabel.localeCompare(rhsLabel);
    });
  }

  startDeploying() {
    this.deploying = true;
  }

  finishDeploying() {
    this.deploying = false;
  }

  notifyForDeployment() {
    this.notificationService.success(this.message);
  }

  createConfirmButton(selected) {
    let button = this.dialogService.createButton(DialogService.CONFIRM, 'dialog.button.confirm', true);
    button.disabled = !this.hasSelectedModels(selected);
    return button;
  }

  createCloseButton() {
    return this.dialogService.createButton(DialogService.CLOSE, 'dialog.button.close');
  }

  hasSelectedModels(models) {
    return !!models && models.getModels().length > 0;
  }

  isDeployEnabled() {
    return !this.deploying && this.enabled;
  }
}
