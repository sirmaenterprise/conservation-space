import {View, Component, Inject, NgElement} from 'app/app';
import {Eventbus} from 'services/eventbus/eventbus';
import {SessionStorageService} from 'services/storage/session-storage-service';
import $ from 'jquery';
import 'common/lib/jquery-ui/jquery-ui.min';
import 'common/lib/jquery-ui/jquery-ui.min.css!';
import sidebarTemplate from './sidebar.html!text';
import './sidebar.css!css';

const SIDEBAR_WIDTH = 'sep-sidebar-width';
const SIDEBAR_COLLAPSED_FLAG = 'sep-sidebar-collapsed';
const SIDEBAR_COLLAPSED_CLASS = 'collapsed';

@Component({
  selector: 'seip-sidebar',
  properties: {
    'context': 'context'
  }
})
@View({
  template: sidebarTemplate
})
@Inject(Eventbus, NgElement, SessionStorageService)
class Sidebar {

  constructor(eventbus, $element, sessionStorageService) {
    this.eventbus = eventbus;
    this.$element = $element;
    this.sessionStorageService = sessionStorageService;

    $element.resizable({
      handles: 'e',
      start: () => {
        $('body').addClass('dragging');
        $element.css('z-index', '100');
      },
      stop: (event, ui) => {
        $('body').removeClass('dragging');
        $element.css('z-index', '');
        sessionStorageService.set(SIDEBAR_WIDTH, ui.size.width);
      }
    });

    if (!this.sessionStorageService.getJson(SIDEBAR_COLLAPSED_FLAG, true)) {
      this.$element.addClass(SIDEBAR_COLLAPSED_CLASS);
    }
    this.toggleCollapse();
  }

  toggleCollapse() {
    this.$element.toggleClass(SIDEBAR_COLLAPSED_CLASS);
    if (this.$element.hasClass(SIDEBAR_COLLAPSED_CLASS)) {
      this.$element.resizable('disable');
      this.sessionStorageService.set(SIDEBAR_COLLAPSED_FLAG, true);
      // reset width on collapse
      this.$element.css('width', '');
      this.sessionStorageService.remove(SIDEBAR_WIDTH);
    } else {
      this.$element.resizable('enable');
      this.sessionStorageService.set(SIDEBAR_COLLAPSED_FLAG, false);
      if (this.sessionStorageService.get(SIDEBAR_WIDTH)) {
        this.$element.css('width', this.sessionStorageService.get(SIDEBAR_WIDTH));
      }
    }
  }

  ngOnDestroy() {
    if (this.$element && this.$element.data('ui-resizable')) {
      this.$element.resizable("destroy");
    }
  }
}
