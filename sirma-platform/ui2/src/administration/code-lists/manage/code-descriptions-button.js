import {Component, Inject, View} from 'app/app';
import {Configurable} from 'components/configurable';
import {DialogService} from 'components/dialog/dialog-service';
import {CodeDescriptions} from 'administration/code-lists/manage/code-descriptions';
import template from './code-descriptions-button.html!text';

const DESCRIPTIONS_HELP_TARGET = 'administration.code.lists.descriptions';

/**
 * Helper component for opening a dialog with the descriptions for a provided code list.
 *
 * @author Mihail Radkov
 */
@Component({
  selector: 'code-descriptions-button',
  properties: {
    'config': 'config',
    'code': 'code',
    'mode': 'mode'
  },
  events: ['onClose', 'onChange']
})
@View({
  template
})
@Inject(DialogService)
export class CodeDescriptionsButton extends Configurable {

  constructor(dialogService) {
    super({
      renderLabel: false
    });
    this.dialogService = dialogService;
  }

  openDescriptions() {
    this.dialogService.create(CodeDescriptions, this.getDescriptionsComponentConfig(), this.getDialogConfiguration());
  }

  getDescriptionsComponentConfig() {
    return {
      onChange: () => this.onChange(),
      descriptions: this.code.descriptions,
      mode: this.mode
    };
  }

  getDialogConfiguration() {
    return {
      header: `${this.code.id} ${this.code.description.name}`,
      largeModal: true,
      helpTarget: DESCRIPTIONS_HELP_TARGET,
      buttons: [this.dialogService.createButton(DialogService.CLOSE, 'dialog.button.close')],
      onButtonClick: (buttonId, componentScope, dialogConfig) => {
        dialogConfig.dismiss();
        this.onClose();
      }
    };
  }

}