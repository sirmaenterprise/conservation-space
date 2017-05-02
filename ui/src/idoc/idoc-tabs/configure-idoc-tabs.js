import {Injectable, Inject} from 'app/app';
import {ActionHandler} from 'services/actions/action-handler';
import {DialogService} from 'components/dialog/dialog-service';
import {ConfigureTabDialog} from './configure-tab-dialog';
import shortid from 'shortid';

@Injectable()
@Inject(DialogService)
export class ConfigureIdocTabs extends ActionHandler {

  constructor(dialogService) {
    super();
    this.dialogService = dialogService;
  }

  execute(actionDefinition, context) {
    this.initContext(context);
    let tab = $.grep(this.context.tabs, function (e) {
      return e.id === context.activeTabId;
    });
    this.openConfigureTabDialog(tab[0]);
  }

  initContext(context) {
    this.context = context;
  }

  openConfigureTabDialog(tab) {
    this.dialogService.create(ConfigureTabDialog, this.getPropertiesConfig(tab), this.getDialogConfig(tab));
  }

  getDialogConfig(tab) {
    this.dialogConfig = {
      header: 'idoc.tabs.configdialog.header',
      showClose: true,
      buttons: [
        {
          id: ConfigureIdocTabs.SAVE,
          label: 'idoc.tabs.config.button.save',
          cls: 'btn-primary',
          disabled: false,
          dismiss: true,
          onButtonClick: (buttonId, componentScope) => {
            this.dialogConfig.buttons[0].disabled = true;
            if (tab.id) {
              this.updateTab(tab, componentScope.configureTabDialog);
            } else {
              this.addTab(componentScope.configureTabDialog);
            }
            this.dialogConfig.dismiss();
          }
        },
        {
          id: ConfigureIdocTabs.CANCEL,
          label: 'idoc.tabs.config.button.cancel',
          dismiss: true
        }
      ]
    };
    return this.dialogConfig;
  }

  getPropertiesConfig(tab) {
    if (!tab.id) {
      tab.title = 'Tab' + this.context.tabsCounter;
      tab.showComments = true;
      tab.showNavigation = true;
      tab.revision = ConfigureIdocTabs.DEFAULT_REVISION;
      tab.locked = false;
    }

    if (!tab.revision) {
      tab.revision = ConfigureIdocTabs.DEFAULT_REVISION;
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

  addTab(tabConfig) {
    let newTab = {
      id: shortid.generate(),
      title: tabConfig.tabTitle,
      showNavigation: tabConfig.showNavigation,
      showComments: tabConfig.showComments,
      revision: tabConfig.revision,
      locked: tabConfig.locked
    };

    this.context.tabs.push(newTab);
    this.setActiveTab(newTab);
    this.context.tabsCounter = ++this.context.tabsCounter;
  }

  updateTab(tab, newValue) {
    tab.title = newValue.tabTitle;
    tab.showNavigation = newValue.showNavigation;
    tab.showComments = newValue.showComments;
    tab.revision = newValue.revision;
    tab.locked = newValue.locked;
  }

  setActiveTab(tab) {
    this.context.activeTabId = tab.id;
  }
}

ConfigureIdocTabs.SAVE = 'SAVE';
ConfigureIdocTabs.CANCEL = 'CANCEL';
ConfigureIdocTabs.DEFAULT_REVISION = 'exportable';