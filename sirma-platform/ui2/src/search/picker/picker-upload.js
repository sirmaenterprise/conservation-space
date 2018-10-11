import {Component, Inject, View} from 'app/app';
import {Configurable} from 'components/configurable';
import {CreatePanelService} from 'services/create/create-panel-service';
import {FileUploadPanel} from 'create/file-upload-panel';
import {ModelsService} from 'services/rest/models-service';
import {WindowAdapter} from 'adapters/angular/window-adapter';

import template from './picker-upload.html!text';

/**
 * Wrapper component designed to configure {@link FileUploadPanel}.
 * The main purpose of this tab extension is to be able to upload objects of given predefined types
 * on the fly if they are not found by the picker. The wrapper component filters uploadable object types based on the
 * predefinedTypes property, and configures the file upload panel
 *
 * @author Svetlozar Iliev
 */
@Component({
  selector: 'picker-upload',
  properties: {
    config: 'config',
    context: 'context'
  }
})
@View({
  template: template
})
@Inject(CreatePanelService, WindowAdapter)
export class PickerUpload extends Configurable {
  constructor(createPanelService, windowAdapter) {
    super({
      controls: {
        showCancel: false
      },
      useContext: true,
      predefinedTypes: [],
      purpose: ModelsService.PURPOSE_UPLOAD
    });

    this.windowAdapter = windowAdapter;
    this.createPanelService = createPanelService;
  }

  ngOnInit() {
    this.initUploadPanelConfig();
    this.initUploadPanelCallbacks();
  }

  initUploadPanelConfig() {
    let opts = {
      purpose: this.config.purpose,
      predefinedTypes: this.config.predefinedTypes,
      controls: this.config.controls,
      parentId: this.getContext(),
      returnUrl: this.windowAdapter.location.href
    };

    let config = this.createPanelService.getInstanceDialogConfig(opts);
    this.uploadPanelConfig = this.createPanelService.getUploadPanelConfig(opts, config);
  }

  initUploadPanelCallbacks() {
    //add appropriate callbacks for upload & configure to the instance upload panel config
    this.uploadPanelConfig.config.fileUploadedCallback = this.onFileUploaded.bind(this);
  }

  onFileUploaded(instance) {
    this.config.selectionHandler(instance);
  }

  getContext() {
    return (this.context && this.config.useContext) ? this.context.getCurrentObjectId() : undefined;
  }
}
