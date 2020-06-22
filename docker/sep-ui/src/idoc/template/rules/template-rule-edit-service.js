import {Injectable, Inject} from 'app/app';
import {DialogService} from 'components/dialog/dialog-service';
import {TemplateRuleEditor} from './template-rule-editor';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {TemplateService} from 'services/rest/template-service';
import {NotificationService} from 'services/notification/notification-service';
import {TranslateService} from 'services/i18n/translate-service';

/**
 * Opens dialog for editing a template rule and saves the rule when the dialog is accepted.
 */
@Injectable()
@Inject(DialogService, PromiseAdapter, TemplateService, NotificationService, TranslateService)
export class TemplateRuleEditorService {

  constructor(dialogService, promiseAdapter, templateService, notificationService, translateService) {
    this.dialogService = dialogService;
    this.promiseAdapter = promiseAdapter;
    this.templateService = templateService;
    this.notificationService = notificationService;
    this.translateService = translateService;
  }

  openRuleEditor(template, scope) {
    this.componentConfig = {
      config: {
        template
      }
    };

    this.instanceId = template.instanceId;

    return this.promiseAdapter.promise(resolve => {
      let dialogConfig = this.constructDialogConfiguration(resolve);

      scope.$watch(() => {
        return this.componentConfig.config.formAvailable;
      }, (value) => {
        dialogConfig.buttons[0].disabled = !value;
      });

      this.dialogService.create(TemplateRuleEditor, this.componentConfig, dialogConfig);
    });
  }

  onOkButtonClicked(resolve, dialogConfiguration) {
    this.templateService.editTemplateRules(this.instanceId, this.componentConfig.config.template.rules).then(() => {
      let message = this.translateService.translateInstant('idoc.template.rules.success');
      this.notificationService.success(message);

      dialogConfiguration.dismiss();
      resolve();
    });
  }

  constructDialogConfiguration(resolve) {
    return {
      showHeader: true,
      header: 'idoc.template.rules.dialog',
      buttons: [{
        id: DialogService.OK,
        label: 'dialog.button.ok',
        cls: 'btn-primary',
        onButtonClick: (button, dialogScope, dialogConfig) => {
          this.onOkButtonClicked(resolve, dialogConfig);
        },
        disabled: true
      },
      {
        id: DialogService.CANCEL,
        label: 'dialog.button.cancel',
        dismiss: true
      }]
    };
  }

}