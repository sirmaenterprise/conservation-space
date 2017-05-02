import {Inject, Injectable} from 'app/app';
import {DialogService} from 'components/dialog/dialog-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {ExtensionsPanel} from 'components/extensions-panel/extensions-panel';

/**
 * Service that allows to invoke the extensions panel inside a dialog.
 *
 * The service constructs a component scope out of provided configuration which will be used passed to the extensions
 * panel.
 *
 * The service accepts a dialog configuration which:
 *  - if undefined a default one will be assigned
 *  - if empty (e.g config = {}) - it will be extended with the default configuration to keep the reference
 *  - if not empty it will ignore the default one and use the provided
 *
 * <b>Important</b> To avoid changes to the provided configurations they should be cloned before that.
 * <b>Important</b> For contextual resolving, context must be provided,
 *
 * If the default dialog configuration is used, {@link ExtensionsDialogService#openDialog} will resolve the promise
 * with the extensions configuration map.
 *
 * @author Mihail Radkov
 */
@Injectable()
@Inject(DialogService, PromiseAdapter)
export class ExtensionsDialogService {

  constructor(dialogService, promiseAdapter) {
    this.dialogService = dialogService;
    this.promiseAdapter = promiseAdapter;
  }

  openDialog(config, context, dialogConfig) {
    var componentConfig = {
      'config': config,
      'context': context
    };

    return this.promiseAdapter.promise((resolve, reject) => {
      if (!dialogConfig) {
        dialogConfig = this.getDefaultDialogConfiguration(config, resolve, reject);
      } else if (Object.keys(dialogConfig).length < 1) {
        // Extends to keep the provided reference.
        Object.assign(dialogConfig, this.getDefaultDialogConfiguration(config, resolve, reject));
      }
      this.dialogService.create(ExtensionsPanel, componentConfig, dialogConfig);
    });
  }

  getDefaultDialogConfiguration(config, resolve, reject) {
    return {
      largeModal: true,
      showHeader: true,
      header: config ? config.header : undefined,
      helpTarget: config ? config.helpTarget : undefined,
      buttons: [{
        id: DialogService.OK,
        label: 'dialog.button.ok',
        cls: 'btn-primary'
      }, {
        id: DialogService.CANCEL,
        label: 'dialog.button.cancel'
      }],
      onButtonClick: function (buttonId, componentScope, dialogConfig) {
        if (buttonId === DialogService.OK) {
          dialogConfig.dismiss();
          resolve(componentScope.extensionsPanel.config.extensions);
        } else if (buttonId === DialogService.CANCEL) {
          dialogConfig.dismiss();
          reject(DialogService.CANCEL);
        }
      }
    };
  }
}