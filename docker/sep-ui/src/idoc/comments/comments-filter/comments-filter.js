import {Component, View, Inject, NgElement} from 'app/app';
import {DialogService} from 'components/dialog/dialog-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {CommentsFilteredEvent} from './comments-filtered-event';
import {CommentsFilterPanel} from './comments-filter-panel/comments-filter-panel';
import {EMPTY_FILTERS} from './comments-filter-const';
import _ from 'lodash';
import commentsFilterTemplate from './comments-filter.html!text';

@Component({
  selector: 'comments-filter',
  properties: {
    'config': 'config'
  }
})
@View({
  template: commentsFilterTemplate
})
@Inject(DialogService, Eventbus, NgElement)
export class CommentsFilter {
  constructor(dialogService, eventbus, element) {
    this.dialogService = dialogService;
    this.eventbus = eventbus;
    this.element = element;
  }

  ngAfterViewInit() {
    this.applyFilter();
  }

  isFilterActive() {
    return !_.isEqual(EMPTY_FILTERS, this.config.filters);
  }

  applyFilter() {
    if (this.isFilterActive()) {
      this.element.find('.btn-filter-comment').addClass('filtered');
    } else {
      this.element.find('.btn-filter-comment').removeClass('filtered');
    }
  }

  openFilterDialog() {
    this.dialogService.create(CommentsFilterPanel, {
      'config': this.config
    }, {
      header: 'comments.action.filter',
      showClose: true,
      modeless: true,
      buttons: [
        {
          id: 'Filter',
          label: 'comments.toolbar.button.filter',
          cls: 'btn-primary',
          dismiss: true,
          onButtonClick: (buttonId, componentScope) => {
            componentScope.commentsFilterPanel.saveToConfig();
            if (this.config.tabId) {
              this.eventbus.publish(new CommentsFilteredEvent(this.config.tabId));
            } else {
              this.eventbus.publish(new CommentsFilteredEvent(this.config.widgetId));
            }
            this.applyFilter();
          }
        },
        {
          id: 'Cancel',
          label: 'dialog.button.cancel',
          dismiss: true
        }
      ]
    });
  }
}
