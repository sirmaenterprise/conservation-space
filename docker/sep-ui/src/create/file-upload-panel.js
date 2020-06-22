import {View, Component} from 'app/app';
import {Configurable} from 'components/configurable';
import 'components/contextselector/context-selector';
import 'file-upload/file-upload';
import _ from 'lodash';
import {ModelsService} from 'services/rest/models-service';
import {EventEmitter} from 'common/event-emitter';
import {CONTEXT_VALIDATED} from 'create/instance-create-panel';

import './file-upload-panel.css!css';
import template from './file-upload-panel.html!text';

export const UPLOAD_ALL = 'UploadAll';
export const ADD_UPLOAD_REMAINING_MESSAGE_COMMAND = 'AddUploadRemainingMessageCommand';
export const REMOVE_UPLOAD_REMAINING_MESSAGE_COMMAND = 'RemoveUploadRemainingMessageCommand';

@Component({
  selector: 'file-upload-panel',
  properties: {
    'config': 'config'
  },
  events: ['onValidityChange']
})
@View({
  template
})

export class FileUploadPanel extends Configurable {

  constructor() {
    super({
      controls: {
        showCancel: true,
        showUploadAll: true
      },
      purpose: ModelsService.PURPOSE_UPLOAD,
      eventEmitter: new EventEmitter()
    });
  }

  ngOnInit() {
    this.registerEventHandlers();
    this.errorMessages = new Set();
  }

  registerEventHandlers() {
    this.addErrorMessageHandler = this.registerAddErrorMessageHandler();
    this.removeErrorMessageHandler = this.registerRemoveErrorMessageHandler();
    this.contextChangedSubscription = this.config.eventEmitter.subscribe(CONTEXT_VALIDATED, this.onContextValidated.bind(this));

  }

  registerAddErrorMessageHandler() {
    return this.config.eventEmitter.subscribe(ADD_UPLOAD_REMAINING_MESSAGE_COMMAND, (errorMessage) => {
      this.errorMessages.add(errorMessage);
    });
  }

  registerRemoveErrorMessageHandler() {
    return this.config.eventEmitter.subscribe(REMOVE_UPLOAD_REMAINING_MESSAGE_COMMAND, (errorMessage) => {
      this.errorMessages.delete(errorMessage);
    });
  }

  cancel() {
    let onCancel = this.config.onCancel;
    if (_.isFunction(onCancel)) {
      onCancel();
    }
  }

  onValidityChanged(event) {
    if(!this.isContextInvalid) {
      this.uploadEnabled = event.valid;
    }
  }

  onContextValidated(data) {
    this.isContextInvalid = !!data.errorMessage;
    this.uploadEnabled = !data.errorMessage;
  }

  uploadAll() {
    this.config.eventEmitter.publish(UPLOAD_ALL);
  }

  getErrorMessages() {
    return [...this.errorMessages];
  }

  ngOnDestroy() {
    if (this.config.eventEmitter) {
      this.config.eventEmitter.unsubscribeAll();
    }
    this.errorMessages = null;
  }
}
