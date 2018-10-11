/**
 * Created by tdossev on 15.2.2018 г..
 */
import {Injectable, Inject} from 'app/app';
import {Logger} from 'services/logging/logger';
import {InstanceAction} from 'idoc/actions/instance-action';
import {InstanceRestService} from 'services/rest/instance-service';
import {UploadDialogConfigurator} from 'idoc/actions/upload/upload-dialog-configurator'

/**
 * Action handler for upload new version operation
 */
@Injectable()
@Inject(Logger, InstanceRestService, UploadDialogConfigurator)
export class UploadNewVersionAction extends InstanceAction {

  constructor(logger, instanceRestService, uploadDialogConfigurator) {
    super(logger);
    this.instanceRestService = instanceRestService;
    this.uploadDialogConfigurator = uploadDialogConfigurator;
  }

  execute(actionDefinition, context) {
    return this.uploadDialogConfigurator.createDialog(actionDefinition, context.currentObject, this.instanceRestService.getContentUploadUrl(context.currentObject.id));
  }
}