import {View, Component, Inject, NgElement, NgScope} from 'app/app';
import {Configurable} from 'components/configurable';
import {ModelsService} from 'services/rest/models-service';
import {RestClient} from 'services/rest-client';
import {AuthenticationService} from 'security/authentication-service';
import {AUTHORIZATION} from 'services/rest/http-headers';
import {NotificationService} from 'services/notification/notification-service';
import {DialogService} from 'components/dialog/dialog-service';
import {TranslateService} from 'services/i18n/translate-service';
import {MomentAdapter} from 'adapters/moment-adapter';
import {Configuration} from 'common/application-config';
import {FileUploadIntegration} from 'file-upload/file-upload-integration';
import 'jquery-file-upload/js/vendor/jquery.ui.widget';
import 'jquery-file-upload';
import 'components/select/select';
import 'components/collapsible/collapsible-panel';

import _ from 'lodash';
import fileSaver from 'file-saver';

import './model-import.css!css';
import template from './model-import.html!text';

@Component({
  selector: 'seip-model-import',
  properties: {
    'config': 'config'
  }
})
@View({
  template
})
@Inject(ModelsService, RestClient, FileUploadIntegration, NotificationService, DialogService, TranslateService, NgElement, NgScope, MomentAdapter, Configuration, AuthenticationService)
export class ModelImport extends Configurable {

  constructor(modelsService, restClient, fileUploadIntegration, notificationService, dialogService, translateService, $element, $scope, momentAdapter, configuration, authenticationService) {
    super({});
    this.$scope = $scope;
    this.element = $element;

    this.momentAdapter = momentAdapter;
    this.configuration = configuration;

    this.restClient = restClient;
    this.fileUploadIntegration = fileUploadIntegration;

    this.modelsService = modelsService;
    this.dialogService = dialogService;
    this.translateService = translateService;
    this.notificationService = notificationService;
    this.authenticationService = authenticationService;
  }

  ngOnInit() {
    this.datePattern = this.configuration.get(Configuration.UI_DATE_FORMAT) + ' ' + this.configuration.get(Configuration.UI_TIME_FORMAT);
    // construct configurations
    this.createImportTypes();
    this.createSelectConfig();
    this.createUploadConfig();
    this.extractImportedModels();
  }

  extractImportedModels() {
    this.modelsService.getImportedModels().then(models => {
      this.makeDataHumanReadable(models);

      this.importedModels = models;
      this.definitions = models.definitions;
      this.templates = models.templates;
    });
  }

  createUploadConfig() {
    let type = this.selectConfig.defaultValue;
    let defaultImport = this.importTypes[type];
    this.setAllowedTypes(defaultImport.formats);

    this.element.fileupload({
      type: 'POST',
      add: (event, data) => {
        if (!this.uploadComponent) {
          this.uploadComponent = data;
        } else {
          // append the newly selected files
          this.uploadComponent.files = this.uploadComponent.files.concat(data.files);

          // prevent selecting particular file twice
          this.uploadComponent.files = _.uniq(this.uploadComponent.files, 'name');
        }
        this.uploadComponent.files = this.uploadComponent.files.filter(file => {
          return this.allowedTypes.find(type => file.name.endsWith(type));
        });

        // file upload is not within angular's scope so the digest has to be runned explicitly
        this.$scope.$digest();
      },
      url: defaultImport.url,
      headers: this.restClient.config.headers,
      singleFileUploads: false
    });
  }

  createImportTypes() {
    this.importTypes = {
      definition: {
        formats: ['.zip', '.xml', '.bpmn'],
        url: this.modelsService.getDefinitionImportUrl(),
        text: this.translateService.translateInstant('administration.models.import.type.definition')
      },
      ontology: {
        formats: ['.zip', '.ttl', '.xml', '.trig', '.ns', '.sparql'],
        url: this.modelsService.getOntologyImportUrl(this.config.tenantId),
        text: this.translateService.translateInstant('administration.models.import.type.ontology')
      }
    };
  }

  createSelectConfig() {
    this.selectConfig = {
      multiple: false,
      isDisabled: () => this.uploading,
      data: this.getImportTypes(this.importTypes),
      defaultValue: this.getDefaultType(this.importTypes)
    };
  }

  setUploadUrl(url) {
    this.element.fileupload('option', 'url', url);
  }

  setAllowedTypes(types) {
    this.allowedTypes = types;
    this.setAcceptFormats(types);
  }

  setAcceptFormats(types) {
    this.getFileInputElement().attr('accept', types.toString());
  }

  selectFiles() {
    this.getFileInputElement().click();
  }

  clearUploadFiles() {
    if (this.uploadComponent) {
      this.uploadComponent.files = [];
    }
  }

  clearUploadState() {
    this.uploading = false;
    this.clearUploadFiles();
  }

  onImportTypeSelect() {
    this.clearUploadState();
    let type = this.importSelection;
    let importType = this.importTypes[type];

    if (type && importType) {
      this.setUploadUrl(importType.url);
      this.setAllowedTypes(importType.formats);
    }
  }

  executeImport() {
    this.uploading = true;

    this.addAuthHeader().then(() => {
      this.fileUploadIntegration.submit(this.uploadComponent).done(() => {
        this.$scope.$apply(() => {
          this.clearUploadState();
          this.notificationService.success(this.getNotificationConfig('administration.models.import.successful'));
        });
      }).fail(error => {
        this.$scope.$apply(() => {
          // resolve the different response message contents based on the response type content.
          let messages = error.responseJSON ? error.responseJSON.messages : [error.responseText];
          let errorMessage = messages ? messages.reduce((result, current) => result + current + '<br/>', '') : '';

          this.clearUploadState();
          this.dialogService.error(errorMessage, this.translateService.translateInstant('administration.models.import.failed'));
        });
      });
    });
  }

  addAuthHeader() {
    return this.authenticationService.buildAuthHeader().then(authHeaderValue => {
      this.uploadComponent.headers = this.uploadComponent.headers || {};
      this.uploadComponent.headers[AUTHORIZATION] = authHeaderValue;
      return true;
    });
  }

  download() {
    let downloadRequest = {};

    if (this.allTemplatesSelected) {
      downloadRequest.allTemplates = true;
    } else {
      downloadRequest.templates = this.importedModels.templates.filter(template => template.selected)
        .map(template => template.id);
    }

    if (this.allDefinitionsSelected) {
      downloadRequest.allDefinitions = true;
    } else {
      downloadRequest.definitions = this.importedModels.definitions.filter(definition => definition.selected)
        .map(definition => definition.id);
    }

    this.modelsService.download(downloadRequest).then(result => this.saveFile(result));
  }

  downloadOntology() {
    this.modelsService.downloadOntology().then(result => this.saveFile(result));
  }

  saveFile(result) {
    let blob = new Blob([result.data], {
      type: 'application/octet-stream'
    });
    fileSaver.saveAs(blob, decodeURIComponent(result.fileName));
  }

  updateAll(modelsArray, value) {
    modelsArray.forEach(model => model.selected = value);

    this.recalculateState();
  }

  recalculateState() {
    let selectedTemplates = this.getSelected(this.importedModels.templates);
    this.allTemplatesSelected = selectedTemplates.length === this.importedModels.templates.length;

    let selectedDefinitions = this.getSelected(this.importedModels.definitions);
    this.allDefinitionsSelected = selectedDefinitions.length === this.importedModels.definitions.length;

    this.downloadAllowed = selectedTemplates.length > 0 || selectedDefinitions.length > 0;
  }

  getSelected(modelsArray) {
    return modelsArray.filter(model => model.selected);
  }

  makeDataHumanReadable(models) {
    if (models.definitions) {
      models.definitions.forEach(definition => {
        if (definition.modifiedOn) {
          definition.modifiedOn = this.momentAdapter.format(definition.modifiedOn, this.datePattern);
        }

        let abstractValueLabel = definition.abstract ? 'checkbox.checked' : 'checkbox.unchecked';
        this.translateService.translate(abstractValueLabel).then((translation) => {
          definition.abstract = translation;
        });
      });
    }

    if (models.templates) {
      models.templates.forEach(template => {
        if (template.modifiedOn) {
          template.modifiedOn = this.momentAdapter.format(template.modifiedOn, this.datePattern);
        }
      });
    }
  }

  filter(term) {
    if (!this.dataIndexed) {
      this.buildSearchIndex(this.importedModels);
      this.dataIndexed = true;
    }

    if (term === '') {
      this.definitions = this.importedModels.definitions;
      this.templates = this.importedModels.templates;

      return;
    }

    let filter = term.toLowerCase();

    this.definitions = this.importedModels.definitions.filter(definition => definition.index.indexOf(filter) != -1);
    this.templates = this.importedModels.templates.filter(template => template.index.indexOf(filter) != -1);
  }

  buildSearchIndex(models) {
    let cache = {};

    if (models.definitions) {
      models.definitions.forEach(definition => {
        definition.index = (definition.id + '||' + definition.fileName + '||' + definition.abstract + '||' +
          definition.title + '||' + definition.modifiedOn + '||' + this.getText(definition.modifiedBy, cache)).toLowerCase();
      });
    }

    if (models.templates) {
      models.templates.forEach(template => {
        template.index = (this.getText(template.title, cache) + '||' + template.purpose + '||' + template.primary + '||' +
          template.forObjectType + '||' + template.modifiedOn + '||' + this.getText(template.modifiedBy, cache)).toLowerCase();
      });
    }
  }

  getText(html, cache) {
    if (!html) {
      return '';
    }

    if (!cache[html]) {
      cache[html] = $(html).text();
    }

    return cache[html];
  }

  getDefaultType(importTypes) {
    return Object.keys(importTypes)[0];
  }

  getImportTypes(importTypes) {
    return Object.keys(importTypes).map(key => {
      return {id: key, text: importTypes[key].text};
    });
  }

  getNotificationConfig(label) {
    return {
      opts: {
        closeButton: false,
        hideOnHover: false
      },
      message: this.translateService.translateInstant(label)
    };
  }

  getFileInputElement() {
    return this.element.find('.file-input');
  }

  ngOnDestroy() {
    if (this.element.data('blueimp-fileupload')) {
      this.element.fileupload('destroy');
    }
  }

}