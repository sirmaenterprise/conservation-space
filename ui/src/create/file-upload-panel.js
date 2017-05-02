import {View, Component, Inject} from 'app/app';
import {Configurable} from 'components/configurable';
import {ContextSelector} from 'components/contextselector/context-selector';
import {FileUpload} from 'file-upload/file-upload';
import {Eventbus} from 'services/eventbus/eventbus';
import {UploadAllTriggeredEvent} from 'file-upload/upload-all-triggered-event';
import _ from 'lodash';
import {ModelsService} from 'services/rest/models-service';

import './file-upload-panel.css!css';
import template from './file-upload-panel.html!text';

@Component({
  selector: 'file-upload-panel',
  properties: {
    'config': 'config'
  }
})
@View({
  template: template
})
@Inject(Eventbus, ModelsService)
export class FileUploadPanel extends Configurable {

  constructor(eventbus, modelsService) {
    super({
      purpose: ModelsService.PURPOSE_UPLOAD
    });
    this.modelsService = modelsService;
    this.eventbus = eventbus;
    this.config.onContextSelected = (contextId) => {
      this.onContextSelected(contextId)
    };
    this.onContextSelected(this.config.parentId);
  }

  onContextSelected(contextId) {
    this.modelsService.getModels(this.config.purpose, contextId, this.config.mimetype, this.config.fileExtension).then((models)=> {
      this.config.errorMessage = models.errorMessage;
    });
  }

  cancel() {
    let onCancel = this.config.onCancel;
    if (_.isFunction(onCancel)) {
      onCancel();
    }
  }

  onValidityChanged(event) {
    this.uploadEnabled = event.valid;
  }


  uploadAll() {
    this.eventbus.publish(new UploadAllTriggeredEvent());
  }
}