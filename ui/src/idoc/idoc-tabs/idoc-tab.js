import {View, Component, Inject} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {MODE_PRINT, MODE_EDIT} from 'idoc/idoc-constants';
import 'components/resizableinput/resizable-input';
import 'components/dropdownmenu/dropdownmenu';
import _ from 'lodash';
import idocTabTemplate from './idoc-tab.html!text';
import './idoc-tab.css!';

@Component({
  selector: 'seip-idoc-tab',
  properties: {
    'config': 'config',
    'mode': 'mode',
    'tab': 'tab'
  }
})
@View({
  template: idocTabTemplate
})
@Inject(PromiseAdapter)
export class IdocTab {

  constructor(promiseAdapter) {
    this.promiseAdapter = promiseAdapter;
    this.tabActions = this.createTabActions();
  }

  createTabActions() {
    return {
      wrapperClass: 'idoc-tabs-menu',
      context: this.config,
      buttonAsTrigger: false,
      loadItems: () => {
        let menuItems = _.clone(PluginRegistry.get('idoc-tabs-menu-items'));
        if (this.tab.locked) {
          let filteredMenuItems = menuItems.filter((menuItem) => {
            return menuItem.id !== 'deleteIdocTabs';
          });
          return this.promiseAdapter.resolve(filteredMenuItems);
        }
        return this.promiseAdapter.resolve(menuItems);
      }
    };
  }

  enableEdit() {
    this.editable = this.isActiveTab() && this.isEditMode() && !this.tab.locked;
  }

  isActiveTab() {
    return this.tab.id === this.config.activeTabId;
  }

  setActiveTab() {
    let tabChanged = this.config.activeTabId !== this.tab.id;
    if (tabChanged) {
      this.config.activeTabId = this.tab.id;
    }
  }

  isEditMode() {
    return this.mode === MODE_EDIT;
  }

  isPrintMode() {
    return this.mode === MODE_PRINT;
  }
}
