import {Component, View, Inject, NgElement, NgTimeout} from 'app/app';
import {ActionExecutor} from 'services/actions/action-executor';
import {ToTrustedHtml} from 'filters/to-trusted-html';
import {Configurable} from 'components/configurable';
import {Logger} from 'services/logging/logger';
import './submenu';

import template from './dropdownmenu.html!text';
import 'font-awesome/css/font-awesome.css!';
import './dropdownmenu.css!';

/**
 * Intended to be used for dynamically loaded menu items. It can also be used with menu items registered for a particular
 * extension point. A menu item behavior can be added with an onclick callback provided trough the menu item plugin
 * definition and this menu implementation will call it when the menu item is selected. Optionally a sortComparator
 * callback method can be provided inside the config which will be used to order / sort the provided tabs accordingly
 *
 * The configuration is in format:
 * {
 *    placeholder: 'placeholder string that is used to narrow actions filtering according from where they are requested',
 *    loadItems: function that returns action definitions,
 *    buttonAsTrigger: true|false, // If the menu trigger should be button or link
 *    triggerLabel: 'menu label',
 *    triggerClass: 'btn btn-sm btn-success',
 *    wrapperClass: 'context-actions-menu',
 *    menuClass: 'is a css class that will be applied to the .dropdown-menu',
 *    switchlabel_onchange: true|false // Allows when menu item is selected the button label to be updated with the selected item label
 *    sortComparator: function(lhs, rhs) {
 *      return -1|0|1;
 *    }
 * }
 *
 * @author svelikov
 */
@Component({
  selector: 'seip-dropdown-menu',
  properties: {
    'config': 'config'
  }
})
@View({
  template: template
})
@Inject(NgElement, Logger, ActionExecutor, NgTimeout)
export class DropdownMenu extends Configurable {

  constructor($element, logger, actionExecutor, $timeout, config) {
    super({
      buttonAsTrigger: true,
      switchlabel_onchange: false
    });
    this.$timeout = $timeout;
    this.$element = $element;
    this.CONFIG = config || window.CONFIG;
    this.logger = logger;
    this.actionExecutor = actionExecutor;
    this.config.isLoading = false;
    this.data = {
      items: []
    };
    this.config.triggerIcon = this.config.triggerIcon || ' <span class="caret"></span>';

    if (this.config.extensionPoint) {
      this.data.items = PluginRegistry.get(this.config.extensionPoint);
      this.sortMenuItems();
    } else {
      // before dropdown to be shown we evaluate the actions and notify the angular for the changes in the model
      this.$element.on('show.bs.dropdown', () => {
        if (this.config.reloadMenu) {
          this.calculatePosition(this.$element);
        }

        //After the calculations for the position of the loading element are executed shows the element.
        this.loadMenuItems();
      }).on('click', 'li a', (evt) => {
        if (this.config.switchlabel_onchange) {
          let selectedItemText = $(evt.target).text();
          $('.btn:first-child', $element).html(selectedItemText + '&nbsp;' + this.config.triggerIcon).val(selectedItemText);
        }
      });
    }
  }

  ngOnDestroy() {
    this.$element.off('show.bs.dropdown');
    this.$element.off('click');
  }

  ngAfterViewInit() {
    if (this.config.reloadMenu) {
      this.$element.find('.dropdown-toggle').dropdown();
    }
  }


  executeAction(item) {
    if (!item.disabled) {
      this.actionExecutor.execute(item, this.config.context);
    }
  }

  /**
   * If the offset of the dropdown + its height is bigger than the windows' height, reduces its offset.
   * @param element trigger button for action menu
   */
  calculatePosition(triggerButton) {
    //calculates the position of the dropdown after all the rows are rendered
    this.$timeout(() => {
      let menu = triggerButton.find('.dropdown-menu');
      let triggerButtonPosition = triggerButton.position();
      let triggerButtonOffset = triggerButton.offset();
      let top = triggerButtonPosition.top + triggerButton.height();

      let dropdownBottom = triggerButtonOffset.top + triggerButton.height() + menu.height();

      let windowBottom = $(window).scrollTop() + $(window).height();

      if (dropdownBottom > windowBottom) {
        top = top - (dropdownBottom - windowBottom + 20);
      }

      let left = 0;
      let menuWidth = menu.width();
      // calculates the left positioning based on trigger button location
      if (triggerButtonPosition.left > menuWidth) {
        left = triggerButtonPosition.left - menuWidth;
      }

      menu.css({
        'top': top + 'px',
        'left': left + 'px'
      });
    });
  }

  /**
   * Invoke the provided service function to load menu items.
   */
  loadMenuItems() {
    if (!this.config.isLoading) {
      this.config.isLoading = true;
      this.config.loadItems().then((response) => {
        this.data.items = response;
        this.sortMenuItems();
        this.config.isLoading = false;
        if (this.config.reloadMenu) {
          this.calculatePosition(this.$element);
        }
      }, (error) => {
        this.config.isLoading = false;
        this.logger.error(error);
      });
    }
  }

  sortMenuItems() {
    if(this.config.sortComparator) {
      // sort the provided items using a custom function
      this.data.items.sort(this.config.sortComparator);
    }
  }

  reset() {
    if (this.config.extensionPoint) {
      return;
    }
    this.data.items.length = 0;
  }
}
