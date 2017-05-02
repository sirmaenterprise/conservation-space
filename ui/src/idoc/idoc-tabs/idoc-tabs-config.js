import {IdocTabOpenedEvent} from './idoc-tab-opened-event';
import _ from 'lodash';

export class TabsConfig {
  constructor(eventbus, currentObject) {
    this.tabs = [];
    this.eventbus = eventbus;
    this.currentObject = currentObject;
  }

  set activeTabId(activeTabId) {
    this._activeTabId = activeTabId;
    this._activeTab = this.getActiveTab();
    if (this._activeTab) {
      this._activeTab.shouldRender = true;
      this.eventbus.publish(new IdocTabOpenedEvent(this._activeTab));
    }
  }

  get activeTabId() {
    return this._activeTabId;
  }

  get activeTab() {
    if (!this._activeTab) {
      this._activeTab = this.getActiveTab();
    }
    return this._activeTab;
  }

  set activeTab(activeTab) {
    this._activeTab = activeTab;
  }

  getActiveTab() {
    return _.find(this.tabs, (tab) => {
      return tab.id === this._activeTabId;
    });
  }
}