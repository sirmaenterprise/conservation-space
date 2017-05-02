import {View, Component, Inject, NgScope, NgElement} from 'app/app';
import {ConfigureIdocTabs} from 'idoc/idoc-tabs/configure-idoc-tabs';
import {MODE_EDIT} from 'idoc/idoc-constants';
import {Eventbus} from 'services/eventbus/eventbus';
import {IdocTab} from 'idoc/idoc-tabs/idoc-tab';
import $ from 'jquery';
import {DragAndDrop} from 'components/draganddrop/drag-and-drop';
import idocTabsTemplate from './idoc-tabs.html!text';
import './idoc-tabs.css!';

@Component({
  selector: 'seip-idoc-tabs',
  properties: {
    'config': 'config',
    'mode': 'mode'
  }
})
@View({
  template: idocTabsTemplate
})
@Inject(NgScope, NgElement, ConfigureIdocTabs, Eventbus)
export class IdocTabs {

  constructor($scope, $element, configureIdocTabs, eventbus) {
    this.$scope = $scope;
    this.element = $element;
    this.configureIdocTabs = configureIdocTabs;
    this.eventbus = eventbus;

    let tabs = this.config.tabs;

    let itemIndex;
    this.config.tabsCounter = this.config.tabsCounter || tabs.length + 1;

    DragAndDrop.makeDraggable($element, {
      exclude: '.menu-item',
      addTabButton: '.add-tab-button',
      // prevent drag to start on click in FF and Chrome
      helper: 'clone',
      delay: 50,
      // set $item relative to cursor position
      onDragStart: function ($item, container, _super) {
        itemIndex = $item.index();
        _super($item, container);
      },
      onDrop: function ($item, container, _super) {
        _super($item, container);

        // This is needed to prevent new entered tab to be placed at wrong position (after the last but moved tab)
        let spliceIndex = $item.index() > 0 ? 0 : 1;
        let isCurrentPosition = $item.index() === itemIndex;
        let isLastPosition = $item.index() === tabs.length;
        if (!isCurrentPosition && !isLastPosition) {
          spliceIndex = itemIndex;
        }

        tabs.splice($item.index(), 0, tabs.splice(spliceIndex, 1)[0]);
        $scope.$apply();
        tabs.splice(spliceIndex, 0, tabs.splice(itemIndex, 1)[0]);
        $scope.$apply();
        $(this.addTabButton).appendTo(container.el);
      }
    });

    this.$scope.$watch(()=> {
      return this.isDraggAllowed();
    }, (allowDrag) => {
      if (allowDrag) {
        DragAndDrop.enable($element);
      } else {
        DragAndDrop.disable($element);
      }
    });
  }

  openConfigureTabDialog() {
    this.configureIdocTabs.initContext(this.config);
    this.configureIdocTabs.openConfigureTabDialog({});
  }

  isDraggAllowed() {
    return this.isEditMode() && this.config.tabs.length > 1;
  }

  isEditMode() {
    return this.mode === MODE_EDIT;
  }
}
