import {Injectable, Inject} from 'app/app';
import {DialogService} from 'components/dialog/dialog-service';
import {TemplateDataPanel} from './template-data-panel';
import {TemplateService} from 'services/rest/template-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {FOR_OBJECT_TYPE, IS_PRIMARY_TEMPLATE, TEMPLATE_PURPOSE, TITLE} from './template-constants';

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
   * @param populatePropertiesFromSourceInstance If true, the template properties will be automatically populated taking their values from
   *                                     the source template instance. It should be fully loaded.
   * @param dialogHeader - the label key for the dialog header. Defined as constants in TemplateConfigDialogService.
   *                       TemplateConfigDialogService.CREATE_TEMPLATE_KEY is the default value.
   * @returns promise resolving with the configured template data.
   */
  openDialog(scope, sourceInstance, typeFilter, populatePropertiesFromSourceInstance, dialogHeader) {
    return this.promiseAdapter.promise((resolve) => {
      let componentConfig = this.getComponentConfiguration(sourceInstance, typeFilter, populatePropertiesFromSourceInstance);
      let dialogConfig = this.getDialogConfiguration(scope, sourceInstance, componentConfig, resolve, dialogHeader);
      this.dialogService.create(TemplateDataPanel, componentConfig, dialogConfig);
    });
  }

  getComponentConfiguration(sourceInstance, typeFilter, populatePropertiesFromSource) {
    let configuration = {
      template: {}
    };

    if (typeFilter) {
      configuration.typeFilter = typeFilter;
    }

    if (populatePropertiesFromSource && sourceInstance) {
      configuration.type = sourceInstance.properties[FOR_OBJECT_TYPE];
      configuration.template.title = sourceInstance.properties[TITLE];
      configuration.template.primary = sourceInstance.properties[IS_PRIMARY_TEMPLATE];
      configuration.template.purpose = this.getPropertyValue(sourceInstance.properties[TEMPLATE_PURPOSE]);
    } else if (sourceInstance) {
      // if no automatic populating is needed, set the forType to be source instance's definition type
      configuration.type = sourceInstance.getModels().definitionId;
    }

    return {
      config: configuration
    };
  }

  getPropertyValue(property) {
    // The provided instance may be loaded with properties as objects instead with plain values
    if (property && property.id) {
      return property.id;
    }
    return property;
  }

  getDialogConfiguration(scope, sourceInstance, componentConfig, resolve, header) {
    let dialogHeader = header || TemplateConfigDialogService.CREATE_TEMPLATE_KEY;
    let dialogConfiguration = {
      showHeader: true,
      header: dialogHeader,
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
      let trimmed = title.trim();
      if (trimmed.length > 0 && trimmed.length < 128) {
        return true;
      }
    }
    return false;
  }

  okButtonHandler(sourceInstance, dialogConfiguration, componentConfig, resolve) {
    // Prevents spamming
    dialogConfiguration.buttons[0].disabled = true;

    let title = componentConfig.config.template.title.trim();
    let payload = {
      forType: componentConfig.config.type,
      title,
      purpose: componentConfig.config.template.purpose,
      primary: componentConfig.config.template.primary
    };

    if (sourceInstance) {
      payload.sourceInstance = sourceInstance.id;
    }

    dialogConfiguration.dismiss();

    resolve(payload);
  }

}

TemplateConfigDialogService.CREATE_TEMPLATE_KEY = 'idoc.template.create';
TemplateConfigDialogService.SAVE_AS_TEMPLATE_KEY = 'idoc.template.save_as_template';
TemplateConfigDialogService.CLONE_TEMPLATE_KEY = 'idoc.template.clone';