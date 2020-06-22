import {Injectable, Inject} from 'app/app';
import {DialogService} from 'components/dialog/dialog-service';
import {ConfigureTabDialog} from 'idoc/idoc-tabs/configure-tab-dialog';
import _ from 'lodash';
import shortid from 'shortid';


@Injectable()
@Inject(DialogService)
export class TabsService {

  constructor(dialogService) {
    this.dialogService = dialogService;
  }

  openConfigureTabDialog(tab, context, resolve, reject) {
    this.dialogService.create(ConfigureTabDialog, this.getPropertiesConfig(tab, context), this.getDialogConfig(tab, context, resolve, reject));
  }

  getDialogConfig(tab, context, resolve, reject) {
    this.dialogConfig = {
      header: 'idoc.tabs.configdialog.header',
      showClose: true,
      buttons: [
        {
          id: TabsService.SAVE,
          label: 'idoc.tabs.config.button.save',
          cls: 'btn-primary',
          disabled: false,
          dismiss: true,
          onButtonClick: (buttonId, componentScope) => {
            this.dialogConfig.buttons[0].disabled = true;
            if (tab.id) {
              this.updateTab(tab, componentScope.configureTabDialog);
            } else {
              this.addTab(tab, componentScope.configureTabDialog, context);
            }
            this.dialogConfig.dismiss();
            context.currentObject.setDirty(true);
            resolve();
          }
        },
        {
          id: TabsService.CANCEL,
          label: 'idoc.tabs.config.button.cancel',
          dismiss: true,
          onButtonClick: () => {
            reject();
          }
        }
      ]
    };
    return this.dialogConfig;
  }

  getPropertiesConfig(tab, context) {
    if (!tab.id) {
      tab.title = 'Tab' + context.tabsCounter;
      tab.showComments = true;
      tab.showNavigation = true;
      tab.revision = TabsService.DEFAULT_REVISION;
      tab.locked = false;
    }

    if (!tab.revision) {
      tab.revision = TabsService.DEFAULT_REVISION;
    }

    return {
      config: {
        tabTitle: tab.title,
        showComments: tab.showComments,
        showNavigation: tab.showNavigation,
        revision: tab.revision,
        locked: tab.locked
      }
    };
  }

  addTab(tab, tabConfig, context) {
    let newTab = _.merge({}, tab, {
      id: shortid.generate(),
      title: tabConfig.tabTitle,
      showNavigation: tabConfig.showNavigation,
      showComments: tabConfig.showComments,
      revision: tabConfig.revision,
      locked: tabConfig.locked
    });

    context.tabs.push(newTab);
    this.setActiveTab(newTab, context);
    context.tabsCounter = ++context.tabsCounter;
  }

  updateTab(tab, newValue) {
    tab.title = newValue.tabTitle;
    tab.showNavigation = newValue.showNavigation;
    tab.showComments = newValue.showComments;
    tab.revision = newValue.revision;
    tab.locked = newValue.locked;
  }

  setActiveTab(tab, context) {
    context.activeTabId = tab.id;
  }

}


TabsService.SAVE = 'SAVE';
TabsService.CANCEL = 'CANCEL';
TabsService.DEFAULT_REVISION = 'exportable';