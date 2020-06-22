import {View, Component, Inject, NgScope, NgCompile} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {MODE_PRINT, MODE_EDIT} from 'idoc/idoc-constants';
import 'components/resizableinput/resizable-input';
import 'components/dropdownmenu/dropdownmenu';
import _ from 'lodash';
import {PluginsService} from 'services/plugin/plugins-service';

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
@Inject(PromiseAdapter, PluginsService, NgScope, NgCompile)
export class IdocTab {

  constructor(promiseAdapter, pluginsService, $scope, $compile) {
    this.pluginsService = pluginsService;
    this.promiseAdapter = promiseAdapter;
    this.$scope = $scope;
    this.$compile = $compile;
    this.tabActions = this.createTabActions();
  }

  ngOnInit() {
    this.pluginsService.loadComponentModules(this.tab.tabNotificationExtensions, 'name').then((tabExtensions) => {
      let extensionHtml = '';
      Object.keys(tabExtensions).forEach((key) => {
        let extension = tabExtensions[key];
        extensionHtml += `<${extension.component}></${extension.component}>`;
      });
      if (this.innerScope) {
        this.innerScope.$destroy();
      }
      this.innerScope = this.$scope.$new();
      $('.' + this.tab.id + ' .tab-extensions').append(this.$compile(extensionHtml)(this.innerScope));
    });
  }

  createTabActions() {
    return {
      wrapperClass: 'idoc-tabs-menu',
      context: this.config,
      buttonAsTrigger: false,
      loadItems: () => {
        let menuItems = _.clone(PluginRegistry.get('idoc-tabs-menu-items'));
        if (this.tab.locked) {
          menuItems = menuItems.filter((menuItem) => {
            return menuItem.id !== 'deleteIdocTabs';
          });
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
