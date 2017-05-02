import {Component, View, Inject} from 'app/app';
import {DialogService} from 'components/dialog/dialog-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {CommentsFilteredEvent} from './comments-filtered-event';
import {CommentsFilterPanel} from './comments-filter-panel/comments-filter-panel';
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
@Inject(DialogService, Eventbus)
export class CommentsFilter {
  constructor(dialogService, eventbus) {
    this.dialogService = dialogService;
    this.eventbus = eventbus;
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
