import {Component, View, Inject} from 'app/app';
import {Configurable} from 'components/configurable';
import 'components/contextselector/context-selector';
import 'create/instance-create-configuration';
import {ModelsService} from 'services/rest/models-service';
import {TranslateService} from 'services/i18n/translate-service';
import {NotificationService} from 'services/notification/notification-service';
import {TemplateDataPanel} from 'idoc/template/template-data-panel';
import {EventEmitter} from 'common/event-emitter';
import {InstanceContextService} from 'services/idoc/instance-context-service';
import {ModelUtils} from 'models/model-utils';
import {CONTEXT_CHANGED_EVENT} from 'components/contextselector/context-selector';
import {TEMPLATE_ID} from 'idoc/template/template-constants';

import 'idoc/template/idoc-template-selector';
import _ from 'lodash';
import template from './instance-create-panel.html!text';
import './instance-create-panel.css!';

export const CONTEXT_VALIDATED = 'ContextValidated';

/**
 * Component which wraps the InstanceCreateConfiguration component and is meant to create a new instance.
 * Additionally this component exposes two callback methods inside it's config which are triggered when:
 *  - instanceCreatedCallback - a new instance has been created. And will be triggered each time a new instance is created
 */
@Component({
  selector: 'instance-create-panel',
  properties: {
    'config': 'config'
  }
})
@View({
  template
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
      templatePurpose: TemplateDataPanel.CREATABLE,
      createButtonLabel: 'instance.create.panel.create',
      eventEmitter: new EventEmitter(),
      showTemplateSelector: true
    });
    this.notificationService = notificationService;
    this.successfulCreationLabel = translateService.translateInstant('instance.create.panel.create.success');
    this.createAnother = this.config.forceCreate;

    this.config.contextSelectorDisabled = this.config.formConfig.models.contextSelectorDisabled || false;
    this.config.parentId = this.config.formConfig.models.parentId;
    this.contextChangedHandler = this.config.eventEmitter.subscribe(CONTEXT_CHANGED_EVENT, (id) => {
      this.config.formConfig.models.parentId = id;
    });

    this.contextValidatedSubscription = this.config.eventEmitter.subscribe(CONTEXT_VALIDATED, (data) => {
      this.config.disableCreate = data.errorMessage ? true :
        (this.config.existingInContext ? InstanceContextService.validateExistenceInContext(this.config.formConfig.models.parentId, this.config.existingInContext) : false);
    });
  }

  create() {
    let onCreate = this.config.onCreate;
    if (_.isFunction(onCreate)) {
      if (this.createAnother && !this.config.formConfig.models.validationModel[TEMPLATE_ID].value.results[0]) {
        ModelUtils.updateObjectProperty(this.config.formConfig.models.validationModel, TEMPLATE_ID, this.createTeplateUsed);
      }
      this.isCreating = true;
      onCreate().then((instance) => {
        this.createdObjectHeader = this.successfulCreationLabel + instance.headers.breadcrumb_header;
        this.notificationService.success(this.createdObjectHeader);
        this.callOnInstanceCreatedCallback(instance);

        if (!this.createAnother) {
          if (this.config.openInNewTab) {
            this.openInNewTab(instance);
          } else {
            this.open(instance);
          }
        } else {
          this.createTeplateUsed = instance.properties[TEMPLATE_ID].results[0];
        }
      }).finally(() => {
        this.isCreating = false;
      });
    }
  }

  callOnInstanceCreatedCallback(instance) {
    let createdCallback = this.config.instanceCreatedCallback;

    if (createdCallback) {
      createdCallback(instance);
    }
  }

  openInNewTab(instance) {
    this.execute(this.config.onOpenInNewTab, instance);
  }

  open(instance) {
    this.execute(this.config.onOpen, instance);
  }

  cancel() {
    this.execute(this.config.onCancel);
  }

  execute(callback, param) {
    if (_.isFunction(callback)) {
      callback(param);
    }
  }

  isModelValid() {
    return this.config.formConfig.models.definitionId !== null && this.config.formConfig.models.validationModel !== null && this.config.formConfig.models.validationModel.isValid;
  }

  hasNoDefinition() {
    return this.config.formConfig.models.definitionId === null || !this.config.formConfig.models.validationModel.isValid;
  }

  isCreateDisabled() {
    return this.isCreating || this.config.disableCreate || (this.createAnother ? !this.isModelValid() : this.hasNoDefinition());
  }

  ngOnDestroy() {
    this.contextChangedHandler.unsubscribe();
    this.contextValidatedSubscription.unsubscribe();
  }
}
