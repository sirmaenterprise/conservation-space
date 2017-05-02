import {Component, View} from 'app/app';
import {Configurable} from 'components/configurable';
import {ToTrustedHtml} from 'filters/to-trusted-html';
import './tabs.css!css';
import tabsTemplate from './tabs.html!text';

/**
 * Reusable component for bootstrap tabs. Allows custom styling to the whole component and to every tab.
 *
 * For native bootstrap switching, there should be DOM elements that correspond to the declared targets in
 * the provided configuration. If this is not preferred, the component changes the activeTab in the configuration.
 *
 * If no active tab is configured, the first one will be used as a default choice.
 *
 * Example configuration:
 *  config: {
 *    tabs: [{
 *      id: 'firstTab'
 *      label: 'First tab'
 *      target: '.pane > .firstTab'
 *      classes: 'big'
 *    }, {
 *      id: 'second'
 *      label: 'labels.tabs.second'
 *      target: '.pane > .secondTab'
 *    }],
 *    activeTab: 'firstTab',
 *    classes: 'horizontal'
 *  }
 *
 *  Additionally, every tab will consider a postfix function that will be invoked and the results bind into
 *  HTML after the tab's title.
 */
@Component({
  selector: 'seip-tabs',
  properties: {
    'config': 'config'
  }
})
@View({
  template: tabsTemplate
})
export class Tabs extends Configurable {
  constructor() {
    let defaultConfig = {
      classes: ''
    };
    super(defaultConfig);

    if(!this.config.activeTab && this.config.tabs) {
      this.config.activeTab = this.config.tabs[0].id;
    }
  }

  switchTab(tab) {
    this.config.activeTab = tab.id;
  }

  isActive(tab) {
    return this.config.activeTab === tab.id;
  }
}