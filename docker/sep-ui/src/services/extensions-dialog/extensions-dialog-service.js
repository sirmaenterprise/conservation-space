import {Inject, Injectable} from 'app/app';
import {DialogService} from 'components/dialog/dialog-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {ExtensionsPanel} from 'components/extensions-panel/extensions-panel';
import _ from 'lodash';

/**
 * Service that allows to invoke the extensions panel inside a dialog.
 *
 * The service constructs a component scope out of provided configuration which will be used passed to the extensions
 * panel.
 *
 * The service accepts a dialog configuration which:
 *  - if undefined a default one will be assigned
 *  - if defined, it will ensure default values for top level keys
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
    let componentConfig = {
      config,
      context
    };
    dialogConfig = dialogConfig || {};

    return this.promiseAdapter.promise((resolve, reject) => {
      _.defaultsDeep(dialogConfig, this.getDefaultDialogConfiguration(config, resolve, reject));
      this.dialogService.create(ExtensionsPanel, componentConfig, dialogConfig);
    });
  }

  getDefaultDialogConfiguration(config, resolve, reject) {
    return {
      largeModal: true,
      showHeader: true,
      header: config ? config.header : undefined,
      modalCls: config ? config.modalCls : undefined,
      helpTarget: config ? config.helpTarget : undefined,
      warningPopover: config ? config.warningPopover : undefined,
      warningMessage: config ? config.warningMessage : undefined,
      buttons: [{
        id: DialogService.OK,
        label: 'dialog.button.ok',
        cls: 'btn-primary'
      }, {
        id: DialogService.CANCEL,
        label: 'dialog.button.cancel'
      }],
      onButtonClick(buttonId, componentScope, dialogConfig) {
        if (buttonId === DialogService.OK) {
          resolve(componentScope.extensionsPanel.config.extensions);
          dialogConfig.dismiss();
        } else if (buttonId === DialogService.CANCEL) {
          reject(DialogService.CANCEL);
          dialogConfig.dismiss();
        }
      },
      onClose: () => {
        reject();
      }
    };
  }
}
