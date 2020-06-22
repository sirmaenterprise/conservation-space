import {Component, View} from 'app/app';
import {Configurable} from 'components/configurable';
import 'filters/to-trusted-html';
import _ from 'lodash';

import './tabs.css!css';
import tabsTemplate from './tabs.html!text';

/**
 * Reusable component for bootstrap tabs. Allows custom styling to the whole component and to every tab.
 *
 * The component changes the activeTab in the configuration. Optionally a sortComparator callback method can be provided
 * inside the config which will be used to order / sort the provided tabs accordingly.
 *
 * If no active tab is configured, the first one will be used as a default choice.
 *
 * Example configuration:
 *  config: {
 *    tabs: [{
 *      id: 'firstTab',
 *      label: 'First tab',
 *      classes: 'big'
 *    }, {
 *      id: 'second',
 *      label: 'labels.tabs.second'
 *    }],
 *    activeTab: 'firstTab',
 *    classes: 'horizontal',
 *    sortComparator: function(lhs, rhs) {
 *       return -1|0|1;
 *    }
 *  }
 *
 * The component fires the <code>onTabChanged</code> event when a tab has been manually changed. The event carries
 * the new and old tabs along with the tabs configuration to allow to restore the active tab if needed.
 *
 *  Additionally, every tab will consider a postfix function that will be invoked and the results bind into
 *  HTML after the tab's title.
 */
@Component({
  selector: 'seip-tabs',
  properties: {
    'config': 'config'
  },
  events: ['onTabChanged']
})
@View({
  template: tabsTemplate
})
export class Tabs extends Configurable {

  constructor() {
    super({
      classes: ''
    });
  }

  ngOnInit() {
    this.setActiveTab();
    this.sortTabs();
  }

  setActiveTab() {
    if (!this.config.activeTab && this.config.tabs) {
      this.config.activeTab = this.config.tabs[0].id;
    }
  }

  switchTab(newTab) {
    let oldTab = _.find(this.config.tabs, tab => tab.id === this.config.activeTab);
    this.config.activeTab = newTab.id;
    this.onTabChanged && this.onTabChanged({newTab, oldTab, config: this.config});
  }

  isActive(tab) {
    return this.config.activeTab === tab.id;
  }

  sortTabs() {
    if (this.config.sortComparator) {
      //sort provided tabs using custom function
      this.config.tabs.sort(this.config.sortComparator);
    }
  }
}