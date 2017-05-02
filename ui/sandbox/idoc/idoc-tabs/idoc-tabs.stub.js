import {Component, View} from 'app/app';
import 'johnny/jquery-sortable';
import {IdocTabs} from 'idoc/idoc-tabs/idoc-tabs';
import {UrlUtils} from 'common/url-utils';
import template from './idoc-tabs.stub.html!text';

@Component({
  selector: 'idoc-tabs-stub'
})
@View({
  template: template
})
export class IdocTabsStub {

  constructor() {
    this.tabsConfig = {
      activeTabId: 'id_1'
    };
    let tabs = [];
    for (let i = 1; i <= 3; i++) {
      this.addTab(tabs, i);
    }

    tabs[0].default = true;
    tabs[0].showNavigation = true;
    tabs[0].showComments = true;

    tabs[1].default = false;
    tabs[1].showNavigation = true;
    tabs[1].showComments = false;

    tabs[2].default = false;
    tabs[2].showNavigation = false;
    tabs[2].showComments = false;

    this.tabsConfig.tabs = tabs;
    this.mode = 'edit';
    this.tabsConfig.tabsCounter = 4;

    var params = '?' + window.location.hash.substring(2);
    var systemTab = UrlUtils.getParameter(params, 'addSystemTab');

    if (systemTab) {
      //insert system tab
      this.addTab(tabs, 'system');

      tabs[3].default = false;
      tabs[3].system = true;
      tabs[3].showNavigation = false;
      tabs[3].showComments = false;

      this.tabsConfig.tabsCounter++;
    }

  }

  setMode(mode) {
    this.mode = mode;
  }

  addTab(tabs, name) {
    tabs.push({
      id: 'id_' + name,
      title: 'Tab ' + name,
      content: 'Content ' + name
    });
  }
}