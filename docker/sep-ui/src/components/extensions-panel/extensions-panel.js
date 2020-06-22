import {View, Component, Inject, NgScope, NgElement} from 'app/app';
import {PluginsService} from 'services/plugin/plugins-service';
import {Configurable} from 'components/configurable';
import 'components/tabs/tabs';
import _ from 'lodash';

import template from './extensions-panel.html!text';
import './extensions-panel.css!';

/**
 * Component that allows different components registered as extension points to be embedded in it with navigation
 * between them.
 *
 * The extension points can provide a <code>label</code> property which will be considered a key for translation.
 * Every provided extension point is compiled and inserted in the DOM lazily for best performance.
 *
 * There is a configuration property for providing every extension point with configuration object. Every extension
 * should have a component property <code>config</code> if to be passed down its configuration.
 *
 * There are two configuration properties for filtering extensions:
 * 1) <code>exclusions</code> - an array of extension names that will be excluded
 * 2) <code>inclusions</code> - an array of extension names that will be preserved
 *
 * Custom tab configurations can be provided via <code>tabs</code> map in the configuration. Currently only postfix
 * function is supported.
 *
 * <b>Important</b>: If you don't want the extensions panel to change the actual configuration objects -> provide copies!
 *
 * @author Mihail Radkov
 */
@Component({
  selector: 'seip-extensions-panel',
  properties: {
    'config': 'config',
    'context': 'context'
  },
  events: ['onTabChanged']
})
@View({
  template
})
@Inject(NgScope, NgElement, '$compile', PluginsService)
export class ExtensionsPanel extends Configurable {

  constructor($scope, $element, $compile, pluginsService) {
    super({
      extensions: {},
      exclusions: [],
      inclusions: [],
      tabs: {}
    });

    if (!this.config.extensionPoint) {
      throw new Error('Missing required configuration: extensionPoint');
    }

    this.$scope = $scope;
    this.$element = $element;
    this.$compile = $compile;

    this.extensions = {};
    this.compiledExtensions = [];

    pluginsService.loadComponentModules(this.config.extensionPoint, 'name').then((modules) => this.configureExtensions(modules));
  }

  handleOnTabChanged(newTab, oldTab, config) {
    this.onTabChanged && this.onTabChanged({newTab, oldTab, config});
  }

  configureExtensions(modules) {
    this.extensions = modules;
    if (this.extensions) {
      this.filterExcludedExtensions(this.extensions, this.config.exclusions);
      this.filterIncludedExtensions(this.extensions, this.config.inclusions);
      this.tabsConfig = this.getExtensionTabsConfig(this.extensions);
    }
  }

  /**
   * Removes all extensions declared in the exclusions array.
   */
  filterExcludedExtensions(extensions, exclusions) {
    Object.keys(extensions).forEach((key) => {
      if (exclusions.indexOf(key) > -1) {
        delete extensions[key];
      }
    });
  }

  /**
   * Leaves all extensions declared in the inclusions array.
   */
  filterIncludedExtensions(extensions, inclusions) {
    if (inclusions.length > 0) {
      Object.keys(extensions).forEach((key) => {
        if (inclusions.indexOf(key) < 0) {
          delete extensions[key];
        }
      });
    }
  }

  getExtensionTabsConfig(extensions) {
    let tabs = [];
    Object.keys(extensions).forEach((key) => {
      let extension = extensions[key];
      let tabConfig = _.defaultsDeep(this.config.tabs[key] || {}, {
        id: extension.name,
        label: extension.label,
        classes: extension.name
      });
      tabs.push(tabConfig);
    });

    let tabsConfig = {
      tabs,
      classes: 'nav-stacked nav-left',
      sortComparator: this.config.sortComparator
    };

    if (this.shouldUseDefaultTab(extensions)) {
      tabsConfig.activeTab = this.config.defaultTab;
    } else if (tabs.length > 0) {
      tabsConfig.activeTab = tabs[0].id;
    }
    return tabsConfig;
  }

  shouldUseDefaultTab(extensions) {
    return this.config.defaultTab && _.includes(Object.keys(extensions), this.config.defaultTab);
  }

  showExtension(extensionName) {
    if (this.tabsConfig.activeTab === extensionName) {
      if (this.compiledExtensions.indexOf(extensionName) < 0) {
        this.compileExtension(extensionName);
      }
      return true;
    }
    return false;
  }

  compileExtension(extensionName) {
    let extension = this.extensions[extensionName];
    if (extension) {
      let extensionsElement = this.$element.find('.extension.' + extensionName);

      if (!this.config.extensions[extensionName]) {
        this.config.extensions[extensionName] = {};
      }

      let extensionTag = `<${extension.component} config="extensionsPanel.config.extensions['${extension.name}']" context="extensionsPanel.context"></${extension.component}>`;
      let compiledExtension = this.$compile(extensionTag)(this.$scope)[0];

      extensionsElement.append(compiledExtension);
      this.compiledExtensions.push(extensionName);
    }
  }

  showNavigation() {
    return Object.keys(this.extensions).length > 1;
  }
}