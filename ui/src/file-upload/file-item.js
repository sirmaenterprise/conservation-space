import {View, Component, Inject, NgScope} from 'app/app';
import _ from 'lodash';
import 'components/select/select';
import filesize from 'filesize';
import {InstanceRestService} from 'services/rest/instance-service';
import {ActionsService} from 'services/rest/actions-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {InstanceObject} from 'idoc/idoc-context';
import {TranslateService} from 'services/i18n/translate-service';
import {NotificationService, DEFAULT_POSITION} from 'services/notification/notification-service';
import {UploadCompletedEvent} from './events';
import {FileUploadIntegration} from './file-upload-integration';
import {InstanceCreateConfiguration} from 'create/instance-create-configuration';
import {UploadAllTriggeredEvent} from './upload-all-triggered-event';
import {ModelsService} from 'services/rest/models-service';

import 'filters/to-trusted-html';
import fileItemTemplate from './file-item.html!text';

@Component({
  selector: 'seip-file-upload-item',
  properties: {
    'fileUpload': 'file-upload',
    'entry': 'entry'
  },
  events: ['onValidityChange']
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
    this.events = [
      this.eventbus.subscribe(UploadAllTriggeredEvent, this.checkValidAndUpload.bind(this))
    ];
  }

  ngOnInit() {
    var fileExtension = this.getFileExtension(this.entry.file.name);

    this.config = {
      operation: 'create',
      renderMandatory: true,
      id: this.fileUpload.config.id,
      purpose: ModelsService.PURPOSE_UPLOAD,
      fileExtension: fileExtension,
      mimetype: this.entry.file.type,
      classFilter: this.fileUpload.config.classFilter,
      definitionFilter: this.fileUpload.config.definitionFilter
    };

    this.config.formConfig = {
      models: {
        parentId: this.fileUpload.config.parentId
      }
    };

    this.existingEntity = this.fileUpload.existingEntity;

    this.uploadEnabled = true;
    this.removeEnabled = true;
    this.uploadAllowed = true;

    if (this.existingEntity) {
      // when uploading a new version, the properties form is not shown
      this.valid = true;
      this.onValidityChange(this.buildValidityChangeEvent(true));
    }

    this.validateFileSize();
    this.$scope.$watch(() => {
      return this.fileUpload.config.errorMessage;
    }, () => {
      return this.onContextChanged();
    });
  }

  onContextChanged() {
    this.config.disabled = !!this.fileUpload.config.errorMessage;
  }

  checkValidAndUpload() {
    if (this.valid && this.uploadEnabled) {
      this.upload();
    }
  }

  getFileExtension(filename) {
    var dotIndex = filename.lastIndexOf(".");
    var isDotLastSymbol = dotIndex === (filename.length - 1);
    if (dotIndex !== -1 && !isDotLastSymbol) {
      return filename.substring(dotIndex + 1);
    }
    return null;
  }

  onFormLoaded(event) {
    // automatically set the document title and file name properties to the file name
    event.models.validationModel['title'].value = this.entry.file.name;
    event.models.validationModel['name'].value = this.entry.file.name;

    this.config.formConfig.models.validationModel.subscribe('modelValidated', (isValid) => {
      this.valid = isValid;
      this.onValidityChange(this.buildValidityChangeEvent(isValid));
    });

    this.instanceType = event.type;
    this.definitionId = event.models.definitionId;
  }

  validateFileSize() {
    var maxSize = this.fileUpload.config.maxFileSize;
    if (maxSize && this.entry.file.size > maxSize) {
      this.uploadEnabled = false;
      this.uploadAllowed = false;
      this.message = this.translateService.translateInstantWithInterpolation('fileupload.maxSizeExceeded', {
        max_size: filesize(maxSize)
      });
    }
  }

  upload() {
    var instanceObject = new InstanceObject(null, this.config.formConfig.models, null);
    // make a copy of the current data so the user changes durring upload won't take effect
    this.parentId = this.fileUpload.config.parentId;
    this.properties = instanceObject.getChangeset();
    this.uploadStarted = true;
    this.onValidityChange(this.buildValidityChangeEvent(false));

    var metadata = this.constructMetaData(this.instanceType, this.properties);
    this.entry.uploadControl.formData = {
      'metadata': JSON.stringify(metadata)
    };

    this.uploader = this.fileUploadIntegration.submit(this.entry.uploadControl).done((result)=> {
      _.merge(this.properties, result);
      this.onUploadComplete();
      this.$scope.$digest();
    }).fail(()=> {
      this.onFail();
      this.$scope.$digest();
    });

    this.uploadEnabled = false;
    this.message = null;
  }

  constructMetaData(instanceType, instanceProperties) {
    var metadata = {
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

    this.fileUpload.remove(this.entry)
  }

  buildValidityChangeEvent(valid) {
    return {
      event: {
        file: this.entry.file,
        valid: valid
      }
    };
  }

  onUploadComplete() {
    this.uploader = null;

    // prevent upload cancellation during instance creation
    this.removeEnabled = false;

    if (this.existingEntity) {
      var entityId = this.fileUpload.config.id;

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
    if (this.fileUpload.config.single && !this.existingEntity) {
      this.notificationService.success({
        opts: {
          closeButton: false,
          hideOnHover: false,
          positionClass: DEFAULT_POSITION
        },
        message: this.header
      });
    }

    this.eventbus.publish(new UploadCompletedEvent(entity));
  }

  onFail() {
    this.uploadStarted = false;
    this.uploadEnabled = true;
    this.removeEnabled = true;
    this.message = this.translateService.translateInstant('fileupload.fail');
  }

  ngOnDestroy() {
    for (let event of this.events) {
      event.unsubscribe();
    }
  }

}
