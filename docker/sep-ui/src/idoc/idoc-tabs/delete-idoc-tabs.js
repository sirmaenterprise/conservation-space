import {Injectable, Inject} from 'app/app';
import {ActionHandler} from 'services/actions/action-handler';
import {DialogService} from 'components/dialog/dialog-service';
import {TranslateService} from 'services/i18n/translate-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';

@Injectable()
@Inject(DialogService, TranslateService, PromiseAdapter)
export class DeleteIdocTabs extends ActionHandler {

  constructor(dialogService, translateService, promiseAdapter) {
    super();
    this.dialogService = dialogService;
    this.translateService = translateService;
    this.promiseAdapter = promiseAdapter;
  }

  execute(actionDefinition, context) {
    return this.promiseAdapter.promise((resolve, reject) => {
      this.initContext(context);

      let systemTabs = this.context.tabs.filter(function (tab) {
        return tab.system;
      });

      if ((this.context.tabs.length - systemTabs.length) === 1) {
        this.dialogService.notification(this.translateService.translateInstant('idoc.tabs.delete.cannot.be.performed'));
        reject();
      } else {
        let tab = $.grep(this.context.tabs, function (e) {
          return e.id === context.activeTabId;
        });
        this.confirmDeleteTab(tab[0], context, resolve, reject);
      }
    });
  }

  initContext(context) {
    this.context = context;
  }

  deleteTab(tab) {
    let index = this.context.tabs.indexOf(tab);
    let nextActiveTabId;
    index + 1 === this.context.tabs.length ? nextActiveTabId = index - 1 : nextActiveTabId = index + 1;
    this.context.activeTabId = this.context.tabs[nextActiveTabId].id;
    this.context.tabs.splice(index, 1);
  }

  confirmDeleteTab(tab, context, resolve, reject) {
    this.dialogService.confirmation(this.translateService.translateInstant('idoc.tabs.delete.confirmation'), null,
      {
        buttons: [
          {
            id: DeleteIdocTabs.CONFIRM_DELETE_TAB,
            label: this.translateService.translateInstant('idoc.tabs.delete.confirm'),
            cls: 'btn-primary'
          },
          {
            id: DeleteIdocTabs.CANCEL_DELETE_TAB,
            label: this.translateService.translateInstant('idoc.tabs.delete.cancel')
          }
        ],
        onButtonClick: (buttonID, componentScope, dialogConfig) => {
          let confirm = false;
          if (buttonID === DeleteIdocTabs.CONFIRM_DELETE_TAB) {
            this.deleteTab(tab);
            context.currentObject.setDirty(true);
            resolve();
          } else {
            reject();
          }
          dialogConfig.dismiss();
          return confirm;
        }
      });
  }
}
DeleteIdocTabs.CONFIRM_DELETE_TAB = 'idoc-tab-confirm-delete';
DeleteIdocTabs.CANCEL_DELETE_TAB = 'idoc-tab-cancel-delete';