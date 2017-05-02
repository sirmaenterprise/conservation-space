import {Injectable, Inject} from 'app/app';
import {DialogService} from 'components/dialog/dialog-service';
import {TemplateDataPanel} from './template-data-panel';
import {TemplateService} from 'services/rest/template-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';

@Injectable()
@Inject(DialogService, TemplateService, PromiseAdapter)
export class TemplateConfigDialogService {

  constructor(dialogService, templateService, promiseAdapter) {
    this.dialogService = dialogService;
    this.templateService = templateService;
    this.promiseAdapter = promiseAdapter;
  }

  /**
   * Opens the dialog for template configuration.
   *
   * @param scope angular scope of the client component.
   * @param sourceInstance instance that is used as source for template creation.
   * @param typeFilter accepts a class name. if specified only that type and its subtypes will be present to the user.
   * @returns promise resolving with the configured template data.
   */
  openDialog(scope, sourceInstance, typeFilter) {
    return this.promiseAdapter.promise((resolve) => {
      var componentConfig = this.getComponentConfiguration(sourceInstance, typeFilter);
      var dialogConfig = this.getDialogConfiguration(scope, sourceInstance, componentConfig, resolve);
      this.dialogService.create(TemplateDataPanel, componentConfig, dialogConfig);
    });
  }

  getComponentConfiguration(sourceInstance, typeFilter) {
    var configuration = {
      config: {
        template: {}
      }
    };

    if (sourceInstance) {
      configuration.config.type = sourceInstance.getModels().definitionId;
    }

    if (typeFilter) {
      configuration.config.typeFilter = typeFilter;
    }

    return configuration;
  }

  getDialogConfiguration(scope, sourceInstance, componentConfig, resolve) {
    var dialogConfiguration = {
      showHeader: true,
      header: 'idoc.template.create',
      buttons: [{
        id: DialogService.OK,
        label: 'dialog.button.ok',
        cls: 'btn-primary',
        disabled: true,
        onButtonClick: (button, dialogScope, dialogConfig) => {
          this.okButtonHandler(sourceInstance, dialogConfig, componentConfig, resolve);
        }
      }, {
        id: DialogService.CANCEL,
        label: 'dialog.button.cancel',
        dismiss: true
      }]
    };

    scope.$watch(() => {
      return componentConfig.config.template.title;
    }, (value) => {
      dialogConfiguration.buttons[0].disabled = !this.isTitlePropertyValid(value);
    });

    return dialogConfiguration;
  }

  isTitlePropertyValid(title) {
    if (title) {
      var trimmed = title.trim();
      if (trimmed.length > 0 && trimmed.length < 128) {
        return true;
      }
    }
    return false;
  }

  okButtonHandler(sourceInstance, dialogConfiguration, componentConfig, resolve) {
    // Prevents spamming
    dialogConfiguration.buttons[0].disabled = true;

    var title = componentConfig.config.template.title.trim();
    var payload = {
      'forType': componentConfig.config.type,
      'title': title,
      'purpose': componentConfig.config.template.purpose,
      'primary': componentConfig.config.template.primary
    };

    if (sourceInstance) {
      payload.sourceInstance = sourceInstance.id;
    }

    dialogConfiguration.dismiss();

    resolve(payload);
  }

}