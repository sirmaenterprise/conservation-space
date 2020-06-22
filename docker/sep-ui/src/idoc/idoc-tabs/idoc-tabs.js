import {View, Component, Inject, NgScope, NgElement} from 'app/app';
import {MODE_EDIT} from 'idoc/idoc-constants';
import {Eventbus} from 'services/eventbus/eventbus';
import 'idoc/idoc-tabs/idoc-tab';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import $ from 'jquery';
import {DragAndDrop} from 'components/draganddrop/drag-and-drop';
import {TabsService} from 'idoc/idoc-tabs/tabs-service';
import idocTabsTemplate from './idoc-tabs.html!text';
import './idoc-tabs.css!';

@Component({
  selector: 'seip-idoc-tabs',
  properties: {
    'config': 'config',
    'mode': 'mode',
    'context': 'context'
  }
})
@View({
  template: idocTabsTemplate
})
@Inject(NgScope, NgElement, Eventbus, PromiseAdapter, TabsService)
export class IdocTabs {

  constructor($scope, $element, eventbus, promiseAdapter, tabsService) {
    this.$scope = $scope;
    this.element = $element;
    this.eventbus = eventbus;
    this.promiseAdapter = promiseAdapter;
    this.tabsService = tabsService;

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
      onDragStart($item, container, _super) {
        itemIndex = $item.index();
        _super($item, container);
      },
      onDrop($item, container, _super) {
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

    this.$scope.$watch(() => {
      return this.isDraggAllowed();
    }, (allowDrag) => {
      if (allowDrag) {
        DragAndDrop.enable($element);
      } else {
        DragAndDrop.disable($element);
      }
    });
  }

  configureNewTab() {
    return this.promiseAdapter.promise((resolve, reject) => {
      var tab = {};
      tab.userDefined = !this.context.isModeling();
      this.tabsService.openConfigureTabDialog(tab, this.config, resolve, reject);
    });
  }

  isDraggAllowed() {
    return this.isEditMode() && this.config.tabs.length > 1;
  }

  isEditMode() {
    return this.mode === MODE_EDIT;
  }
}
