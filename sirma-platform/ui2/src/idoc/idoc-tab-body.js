import {View, Component, Inject, NgScope, NgElement, NgCompile} from 'app/app';
import {IdocNavigation} from 'idoc/idoc-navigation/navigation';
import {Splitter} from 'components/splitter/splitter';
import {Editor} from 'idoc/editor/idoc-editor';
import {Eventbus} from 'services/eventbus/eventbus';
import {IdocTabOpenedEvent} from 'idoc/idoc-tabs/idoc-tab-opened-event';
import {NavigationEnabledEvent} from 'idoc/idoc-navigation/navigation-enabled-event';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import 'idoc/idoc-comments/idoc-comments';
import 'components/ui-preference/ui-preference';
import template from './idoc-tab-body.html!text';
import './idoc-tab-body.css!css';

@Component({
  selector: 'idoc-tab-body',
  properties: {
    tab: 'tab',
    context: 'context',
    tabsConfig: 'tabsConfig'
  }
})
@View({
  template: template
})
@Inject(NgScope, NgElement, NgCompile, Eventbus, WindowAdapter)
export class IdocTabBody {

  constructor($scope, $element, $compile, eventbus, windowAdapter) {
    this.$scope = $scope;
    this.$element = $element;
    this.$compile = $compile;
    this.eventbus = eventbus;
    this.windowAdapter = windowAdapter;

    this.uiPreferenceConfig = {
      sourceElements: {
        top: '.idoc-wrapper .fixed-container'
      },
      fillAvailableHeight: true,
      copyParentWidth: '.idoc-navigation-wrapper'
    };

    // edit mode should be determined before initializing the view
    this.tab.isEditMode = IdocTabBody.getIsEditMode(this.context, this.tab);
  }

  ngAfterViewInit() {
    this.$scope.$watch(() => {
      return this.tab.locked;
    }, () => {
      this.tab.isEditMode = IdocTabBody.getIsEditMode(this.context, this.tab);
    });

    if (this.tab.system && this.isActiveTab(this.tab)) {
      this.insertContent();
    }
    if (this.tab.system && !this.isActiveTab(this.tab)) {
      // lazy load the system tab when it gets opened, only the opened one
      this.subscription = this.eventbus.subscribe(IdocTabOpenedEvent, (tab) => {
        if (tab.id === this.tab.id && this.isActiveTab(tab)) {
          this.insertContent();
          this.subscription.unsubscribe();
          this.subscription = null;
        }
      });
    }
  }

  isActiveTab(tab) {
    return this.tabsConfig.activeTabId === tab.id;
  }

  insertContent() {
    var tabContentElement = this.$element.find('.system-tab-content');
    var content = $(this.tab.content);
    if (this.innerScope) {
      this.innerScope.$destroy();
    }
    this.innerScope = this.$scope.$new();
    var compiled = this.$compile(content)(this.innerScope);
    tabContentElement.append(compiled[0]);
  }

  // TODO: extract this inside an utility class and let the splitter use it in order to calculate the sizes
  generateSplitterConfig() {
    if (!this.tab.splitterConfig) {
      this.tab.splitterConfig = {
        setupSizes: {
          callback: () => {
            return {
              sizes: this.calculatePaneSizes(this.tab),
              minSize: 0
            };
          },
          arguments: [this.tab]
        },
        commands: {
          init: () => {
            return true;
          },
          destroy: () => {
            return [this.tab.showNavigation, this.tab.showComments];
          }
        }
      };
    }
    return this.tab.splitterConfig;
  }

  calculatePaneSizes(tab) {
    let sizes = [];
    let parentWidth = this.$element.width();
    if (tab.showNavigation) {
      this.eventbus.publish(new NavigationEnabledEvent(tab.id));
      sizes.push(100 * IdocTabBody.NAVIGATION_COLUMN_WIDTH / parentWidth);
    }
    let editorWidth = parentWidth - (tab.showNavigation ? IdocTabBody.NAVIGATION_COLUMN_WIDTH : 0) - (tab.showComments ? IdocTabBody.COMMENTS_COLUMN_WIDTH : 0);
    sizes.push(100 * editorWidth / parentWidth);
    if (tab.showComments) {
      sizes.push(100 * IdocTabBody.COMMENTS_COLUMN_WIDTH / parentWidth);
    }
    return sizes;
  }

  getEditorSelector(tabId) {
    return this.getTabSelector(tabId) + ' .' + Editor.editorClass;
  }

  getTocHolderSelector(tabId) {
    return this.getTabSelector(tabId) + ' .' + IdocNavigation.tocHolderClass;
  }

  getTabSelector(tabId) {
    return '#tab-' + tabId;
  }

  ngOnDestroy() {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }

    if (this.resizeHandler) {
      this.windowAdapter.window.removeEventListener('resize', this.resizeHandler);
    }
  }

  /**
   * Editor doesn't inherit document mode directly.
   * If document is in edit mode but tab is locked the editor should be in preview mode.
   * @returns {boolean} true if editor should be editable, false otherwise
   */
  static getIsEditMode(context, tab) {
    return context.isEditMode() ? !tab.locked : false;
  }
}

IdocTabBody.SPLITTER_WIDTH = 5;
IdocTabBody.NAVIGATION_COLUMN_WIDTH = 200;
IdocTabBody.COMMENTS_COLUMN_WIDTH = 250;
IdocTabBody.EDITOR_COLUMN_MIN_WIDTH = 300;