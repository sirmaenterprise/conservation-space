import {Component, View, Inject, NgElement, NgTimeout} from 'app/app';
import {ActionExecutor} from 'services/actions/action-executor';
import {ToTrustedHtml} from 'filters/to-trusted-html';
import _ from 'lodash';
import 'font-awesome/css/font-awesome.css!';

import template from './submenu.html!text';
import './submenu.css!';

@Component({
  selector: 'submenu',
  properties: {
    'config': 'config',
    'data': 'data'
  }
})
@View({
  template: template
})
@Inject(ActionExecutor, NgElement, NgTimeout)
export class Submenu {

  constructor(actionExecutor, element, $timeout) {
    this.actionExecutor = actionExecutor;
    this.element = element;
    this.$timeout = $timeout;
  }

  executeAction(item) {
    if (!item.disabled) {
      this.actionExecutor.execute(item, this.config.context);
    }
  }

  disableGroupClick(event) {
    event.stopPropagation();
  }

  /**
   * Calculates the position of the submenu.
   * Invoked when the element which should show the submenu is hovered.
   *
   * @param event the on mouse over event
   */
  calculateSubmenuPosition(event) {
    this.$timeout(() => {

      // The event's target is the <a> element but we need the parent <li>, where is the submenu.
      let triggerElement = $(event.target).parent();

      // Sometimes the the event target is the <span> in the <a>, so we need to get the parent again.
      if (triggerElement.prop('tagName') === 'A') {
        triggerElement = triggerElement.parent();
      }

      //Gets only the first submenu.
      let menu = triggerElement.find('.dropdown-menu').eq(0);
      let triggerElementOffset = triggerElement.offset();
      let top = 0;

      let submenuBottom = triggerElementOffset.top + triggerElement.height() + menu.height();
      let windowBottom = $(window).scrollTop() + $(window).height();

      //If the submenu's bottom is lower than the window's, brings up the submenu.
      if (submenuBottom > windowBottom) {
        top = windowBottom - submenuBottom;
      }
      let menuWidth = menu.width();
      let windowWidth = $(window).width();
      let left = triggerElement.width();

      // Calculates the left positioning based on trigger button location
      // If there is enough space on the right put it there
      if (triggerElementOffset.left + triggerElement.width() + menuWidth > windowWidth) {
        left = -menuWidth;
      }

      //When the submenu is positioned correctly changes its visibility.
      menu.css({
        'top': top + 'px',
        'left': left + 'px',
        'visibility': 'visible'
      });
    }, 0);
  }
}
