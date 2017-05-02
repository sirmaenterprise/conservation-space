import {Component, View, Inject} from 'app/app';
import {Configurable} from 'components/configurable';
import {ContextSelector} from 'components/contextselector/context-selector';
import {InstanceCreateConfiguration} from 'create/instance-create-configuration';
import {ModelsService} from 'services/rest/models-service';
import {TranslateService} from 'services/i18n/translate-service';
import {NotificationService} from 'services/notification/notification-service';
import _ from 'lodash';
import template from './instance-create-panel.html!text';
import './instance-create-panel.css!';

@Component({
  selector: 'instance-create-panel',
  properties: {
    'config': 'config'
  }
})
@View({
  template: template
})
@Inject(TranslateService, NotificationService)
export class InstanceCreatePanel extends Configurable {

  constructor(translateService, notificationService) {
    super({
      controls: {
        showCreate: true,
        showCancel: true,
        showCreateMore: true
      },
      forceCreate: false,
      purpose: [ModelsService.PURPOSE_CREATE],
      createButtonLabel: 'instance.create.panel.create'
    });

    this.notificationService = notificationService;
    this.successfulCreationLabel = translateService.translateInstant('instance.create.panel.create.success');
    this.createAnother = this.config.forceCreate;
  }

  execute() {
    if (this.createAnother) {
      this.create();
    } else {
      this.open();
    }
  }

  create() {
    let onCreate = this.config.onCreate;
    if (_.isFunction(onCreate)) {
      this.isCreating = true;
      onCreate().then((instance) => {
        this.createdObjectHeader = this.successfulCreationLabel + instance.headers.breadcrumb_header;
        this.notificationService.success(this.createdObjectHeader);
      }).finally(() => {
        this.isCreating = false;
      });
    }
  }

  open() {
    let onOpen = this.config.onOpen;
    if (_.isFunction(onOpen)) {
      onOpen();
    }
  }

  cancel() {
    let onCancel = this.config.onCancel;
    if (_.isFunction(onCancel)) {
      onCancel();
    }
  }

  isModelValid() {
    return this.config.formConfig.models.definitionId === null && this.config.formConfig.models.validationModel === null ||
      !this.config.formConfig.models.validationModel.isValid;
  }

  hasNoDefinition() {
    return this.config.formConfig.models.definitionId === null;
  }

}
