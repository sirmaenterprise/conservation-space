import {Component, View, Inject} from 'app/app';
import {Configurable} from 'components/configurable';
import {CreatePanelService} from 'services/create/create-panel-service';
import {InstanceCreatePanel} from 'create/instance-create-panel';
import {Eventbus} from 'services/eventbus/eventbus';
import {ModelsService} from 'services/rest/models-service';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import {InstanceCreatedEvent} from 'idoc/events/instance-created-event';
import {InstanceCreateConfigurationEvent} from 'create/instance-create-configuration-event';
import _ from 'lodash';

import template from './picker-create.html!text';

export const NOTHING_TO_CREATE_MESSAGE = 'picker.create.none';

/**
 * Wrapper component designed to configure {@link InstanceCreatePanel}.
 * The main purpose of this tab extension is to be able to create objects of given predefined types
 * on the fly if they are not found by the picker. The wrapper component filters creatable object types based on the
 * predefinedTypes property, and configures the create instance panel to force create objects
 *
 * @author Svetlozar Iliev
 */
@Component({
  selector: 'picker-create',
  properties: {
    config: 'config',
    context: 'context'
  }
})
@View({
  template: template
})
@Inject(Eventbus, CreatePanelService, WindowAdapter)
export class PickerCreate extends Configurable {
  constructor(eventbus, createPanelService, windowAdapter) {
    super({
      controls: {
        showCancel: false,
      },
      forceCreate: true,
      useRootContext: true,
      predefinedTypes: [],
      purpose: ModelsService.PURPOSE_CREATE,
      nothingToCreateMessage: NOTHING_TO_CREATE_MESSAGE
    });

    this.eventbus = eventbus;
    this.windowAdapter = windowAdapter;
    this.createPanelService = createPanelService;
  }

  ngOnInit() {
    this.initInstancePanelConfig();
    this.subscribeToInstanceCreated();
    this.subscribeToInstanceConfiguration();
  }

  initInstancePanelConfig() {
    let opts = {
      predefinedTypes: this.config.predefinedTypes,
      forceCreate: this.config.forceCreate,
      controls: this.config.controls,
      parentId: (this.context && this.config.useRootContext) ? this.context.getCurrentObjectId() : undefined,
      returnUrl: this.windowAdapter.location.href
    };

    let config = this.createPanelService.getInstanceDialogConfig(opts);
    this.instancePanelConfig = this.createPanelService.getPropertiesConfig(opts, config.models, config.dialogConfig, config.suggestedPropertiesMap);
  }

  subscribeToInstanceConfiguration() {
    this.instanceConfigurationEvent = this.eventbus.subscribe(InstanceCreateConfigurationEvent, (event) => {
      let models = event[0].models;
      this.showPanel = models && models.length > 0;
    });
  }

  subscribeToInstanceCreated() {
    this.instanceCreatedEvent = this.eventbus.subscribe(InstanceCreatedEvent, (event) => {
      this.config.selectionHandler(event[0].currentObject);
    });
  }

  ngOnDestroy() {
    if (this.instanceCreatedEvent) {
      this.instanceCreatedEvent.unsubscribe();
    }

    if (this.instanceConfigurationEvent) {
      this.instanceConfigurationEvent.unsubscribe();
    }
  }
}
