import {Component, Inject, View} from 'app/app';
import {Configurable} from 'components/configurable';
import {DialogService} from 'components/dialog/dialog-service';
import {PREVIEW} from 'administration/code-lists/manage/code-manage-modes';
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
  events: ['onChange']
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
    this.setInitialValuesOfDescriptions();
    this.dialogService.create(CodeDescriptions, this.getDescriptionsComponentConfig(), this.getDialogConfiguration());
  }

  setInitialValuesOfDescriptions() {
    if (!this.isPreviewMode()) {
      this.initialDescriptions = {};
      Object.keys(this.code.descriptions).forEach(lang => {
        let description = this.code.descriptions[lang];
        this.initialDescriptions[lang] = {
          name: description.name,
          comment: description.comment
        };
      });
    }
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
      buttons: this.getButtonsConfiguration(),
      onButtonClick: (buttonId, componentScope, dialogConfig) => {
        if (buttonId === DialogService.CANCEL) {
          this.revertChangesInDescriptions();
        }
        dialogConfig.dismiss();
      }
    };
  }

  revertChangesInDescriptions() {
    Object.keys(this.code.descriptions).forEach(lang => {
      this.code.descriptions[lang].name = this.initialDescriptions[lang].name;
      this.code.descriptions[lang].comment = this.initialDescriptions[lang].comment;
    });
  }

  getButtonsConfiguration() {
    if (this.isPreviewMode()) {
      return [this.dialogService.createButton(DialogService.CLOSE, 'dialog.button.close')];
    }
    return [
      this.dialogService.createButton(DialogService.CONFIRM, 'dialog.button.save', true),
      this.dialogService.createButton(DialogService.CANCEL, 'dialog.button.cancel')
    ];
  }

  isPreviewMode() {
    return this.mode === PREVIEW;
  }

}