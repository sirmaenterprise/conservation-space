import {Component, View, Inject} from 'app/app';
import {Configurable} from 'components/configurable';
import {CreatePanelService} from 'services/create/create-panel-service';
import {InstanceCreatePanel} from 'create/instance-create-panel';
import {ModelsService} from 'services/rest/models-service';
import {WindowAdapter} from 'adapters/angular/window-adapter';

import template from './picker-create.html!text';

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
@Inject(CreatePanelService, WindowAdapter)
export class PickerCreate extends Configurable {
  constructor(createPanelService, windowAdapter) {
    super({
      controls: {
        showCancel: false,
        showCreateMore: false
      },
      forceCreate: true,
      useContext: true,
      predefinedTypes: [],
      purpose: ModelsService.PURPOSE_CREATE
    });

    this.windowAdapter = windowAdapter;
    this.createPanelService = createPanelService;
  }

  ngOnInit() {
    this.initInstancePanelConfig();
    this.initInstancePanelCallbacks();
  }

  initInstancePanelConfig() {
    let opts = {
      purpose: this.config.purpose,
      predefinedTypes: this.config.predefinedTypes,
      forceCreate: this.config.forceCreate,
      controls: this.config.controls,
      parentId: this.getContext(),
      returnUrl: this.windowAdapter.location.href
    };

    let config = this.createPanelService.getInstanceDialogConfig(opts);
    this.instancePanelConfig = this.createPanelService.getCreatePanelConfig(opts, config);
  }

  initInstancePanelCallbacks() {
    // add appropriate callbacks for create & configure to the instance create panel config
    this.instancePanelConfig.config.instanceCreatedCallback = this.onInstanceCreated.bind(this);
  }

  onInstanceCreated(instance) {
    this.config.selectionHandler(instance);
  }

  getContext() {
    return (this.context && this.config.useContext) ? this.context.getCurrentObjectId() : undefined;
  }
}
