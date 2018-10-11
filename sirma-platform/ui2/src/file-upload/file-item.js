import {View, Component, Inject, NgScope} from 'app/app';
import _ from 'lodash';
import 'components/select/select';
import filesize from 'filesize';
import {InstanceRestService} from 'services/rest/instance-service';
import {ActionsService} from 'services/rest/actions-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {InstanceObject} from 'models/instance-object';
import {TranslateService} from 'services/i18n/translate-service';
import {NotificationService, DEFAULT_POSITION} from 'services/notification/notification-service';
import {UploadCompletedEvent} from './events';
import {BeforeIdocSaveEvent} from 'idoc/actions/events/before-idoc-save-event';
import {InstanceCreatedEvent} from 'idoc/events/instance-created-event';
import {FileUploadIntegration} from './file-upload-integration';
import {InstanceCreateConfiguration} from 'create/instance-create-configuration';
import {ModelsService} from 'services/rest/models-service';
import {TemplateDataPanel} from 'idoc/template/template-data-panel';
import {UPLOAD_ALL} from 'create/file-upload-panel';
import {CONTEXT_VALIDATED} from 'create/instance-create-panel';

import 'filters/to-trusted-html';
import fileItemTemplate from './file-item.html!text';

@Component({
  selector: 'seip-file-upload-item',
  properties: {
    // this is a reference to parent component (seip-file-upload) and caries its config
    'fileUpload': 'file-upload',
    // the selected file
    'entry': 'entry'
  },
  events: ['onValidityChange', 'onFileUploaded']
})
@View({
  template: fileItemTemplate
})
@Inject(Eventbus, NgScope, ActionsService, TranslateService, InstanceRestService, NotificationService, FileUploadIntegration, ModelsService)
export class FileUploadItem {
  constructor(eventbus, $scope, actionsService, translateService, instanceRestService, notificationService, fileUploadIntegration, modelsService) {
    this.$scope = $scope;
    this.instanceRestService = instanceRestService;
    this.actionsService = actionsService;
    this.notificationService = notificationService;
    this.fileUploadIntegration = fileUploadIntegration;
    this.modelsService = modelsService;
    this.translateService = translateService;
    this.eventbus = eventbus;
  }

  ngOnInit() {
    let fileExtension = this.getFileExtension(this.entry.file.name);

    this.config = {
      operation: 'create',
      renderMandatory: true,
      id: this.fileUpload.config.id,
      purpose: [ModelsService.PURPOSE_UPLOAD],
      templatePurpose: TemplateDataPanel.UPLOADABLE,
      fileExtension,
      mimetype: this.entry.file.type,
      classFilter: this.fileUpload.config.classFilter,
      definitionFilter: this.fileUpload.config.definitionFilter,
      showTemplateSelector: true,
      eventEmitter: this.fileUpload.config.eventEmitter
    };

    this.uploadAllSubscription = this.config.eventEmitter.subscribe(UPLOAD_ALL, this.checkValidAndUpload.bind(this));
    this.contextChangedSubscription = this.config.eventEmitter.subscribe(CONTEXT_VALIDATED, this.onContextValidated.bind(this));

    this.config.formConfig = {
      models: {
        parentId: this.fileUpload.config.parentId
      }
    };

    this.existingEntity = this.fileUpload.existingEntity;
    this.skipEntityUpdate = this.fileUpload.config.skipEntityUpdate;

    this.uploadEnabled = true;
    this.removeEnabled = true;
    this.uploadAllowed = true;

    if (this.existingEntity) {
      // when uploading a new version, the properties form is not shown
      this.valid = true;
      this.onValidityChange(this.buildValidityChangeEvent(true));
    }

    this.validateFileSize();
  }

  /**
   * Recives the received model and error message from the models service.
   * @param data response from the models service.
   */
  onContextValidated(data) {
    this.config.disabled = !!data.errorMessage;
  }

  checkValidAndUpload() {
    if (this.valid && this.uploadEnabled) {
      this.upload();
    }
  }

  getFileExtension(filename) {
    let dotIndex = filename.lastIndexOf('.');
    let isDotLastSymbol = dotIndex === (filename.length - 1);
    if (dotIndex !== -1 && !isDotLastSymbol) {
      return filename.substring(dotIndex + 1);
    }
    return null;
  }

  onFormLoaded(event) {
    // automatically set the document title and file name properties to the file name
    let title = event.models.validationModel['title'];
    let name = event.models.validationModel['name'];

    if (title) {
      title.value = this.entry.file.name;
    }

    if (name) {
      name.value = this.entry.file.name;
    }

    this.config.formConfig.models.validationModel.subscribe('modelValidated', (isValid) => {
      this.valid = isValid;
      this.onValidityChange(this.buildValidityChangeEvent(isValid));
    });

    this.instanceType = event.type;
    this.definitionId = event.models.definitionId;
  }

  validateFileSize() {
    let maxSize = this.fileUpload.config.maxFileSize;
    if (maxSize && this.entry.file.size > maxSize) {
      this.uploadEnabled = false;
      this.uploadAllowed = false;
      this.message = this.translateService.translateInstantWithInterpolation('fileupload.maxSizeExceeded', {
        max_size: filesize(maxSize)
      });
    }
  }

  upload() {
    let instanceObject = new InstanceObject(null, this.config.formConfig.models, null);
    // make a copy of the current data so the user changes durring upload won't take effect
    this.parentId = this.fileUpload.config.parentId;
    this.properties = instanceObject.getChangeset();
    this.uploadStarted = true;
    this.onValidityChange(this.buildValidityChangeEvent(false));

    let metadata = this.constructMetaData(this.instanceType, this.properties);
    this.entry.uploadControl.formData = {
      'metadata': JSON.stringify(metadata),
      'userOperation': this.fileUpload.config.userOperation
    };

    this.uploader = this.fileUploadIntegration.submit(this.entry.uploadControl).done((result) => {
      _.merge(this.properties, result);
      this.uploader = null;

      if (this.skipEntityUpdate) {
        this.onPersist(result);
      } else {
        this.onUploadComplete();
      }
      this.$scope.$digest();
    }).fail((data) => {
      if (data.responseJSON && data.responseJSON.message) {
        this.notifyForError(data.responseJSON.message);
      }
      this.onFail();
      this.$scope.$digest();
    });

    this.uploadEnabled = false;
    this.message = null;
  }

  notifyForError(message) {
    this.notificationService.error({
      opts: {
        closeButton: false,
        hideOnHover: false,
        positionClass: DEFAULT_POSITION
      },
      message
    });
  }

  constructMetaData(instanceType, instanceProperties) {
    let metadata = {
      'rdf:type': instanceType
    };
    _.merge(metadata, instanceProperties);
    return metadata;
  }

  remove() {
    if (this.uploader && this.uploadStarted) {
      this.uploader.abort();
      this.onValidityChange(this.buildValidityChangeEvent(true));
    } else {
      this.onValidityChange(this.buildValidityChangeEvent(false));
    }

    this.fileUpload.remove(this.entry);
  }

  buildValidityChangeEvent(valid) {
    return {
      event: {
        file: this.entry.file,
        valid
      }
    };
  }

  onUploadComplete() {
    // prevent upload cancellation during instance creation
    this.removeEnabled = false;

    if (this.existingEntity) {
      let entityId = this.fileUpload.config.id;

      let entity = {
        userOperation: this.fileUpload.config.userOperation,
        targetInstance: {
          properties: this.properties
        }
      };

      return this.actionsService.createOrUpdate(entityId, entity).then((response) => {
        this.onPersist(response.data);
      });
    } else {
      let entity = {
        definitionId: this.definitionId,
        parentId: this.parentId,
        properties: this.properties
      };
      this.eventbus.publish(new BeforeIdocSaveEvent(entity));
      return this.instanceRestService.create(entity).then((response) => {
        this.onPersist(response.data);
      });
    }
  }

  onPersist(entity) {
    this.complete = true;
    this.fileUpload.updateProgressBar(this.entry.file.id, 100);

    this.header = this.translateService.translateInstant('fileupload.successful');
    this.header += entity.headers.breadcrumb_header;

    // show header only on creation of single entity
    if (this.fileUpload.config.single && !this.existingEntity || this.fileUpload.config.showNotification) {
      this.notificationService.success({
        opts: {
          closeButton: false,
          hideOnHover: false,
          positionClass: DEFAULT_POSITION
        },
        message: this.header
      });
    }

    this.onFileUploaded({
      event: {
        instance: entity
      }
    });
    this.eventbus.publish(new UploadCompletedEvent(entity));
    this.eventbus.publish(new InstanceCreatedEvent({currentObject: entity}));
    this.uploadAllSubscription.unsubscribe();
    this.contextChangedSubscription.unsubscribe();
  }

  onFail() {
    this.uploadStarted = false;
    this.uploadEnabled = true;
    this.removeEnabled = true;
    this.message = this.translateService.translateInstant('fileupload.fail');
  }

  ngOnDestroy() {
    this.uploadAllSubscription.unsubscribe();
    this.contextChangedSubscription.unsubscribe();
  }
}
