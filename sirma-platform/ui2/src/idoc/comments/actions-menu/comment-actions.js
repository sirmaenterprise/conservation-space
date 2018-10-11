import {View, Component, Inject, Injectable, NgElement} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {Eventbus} from 'services/eventbus/eventbus';
import {ReloadRepliesEvent} from 'idoc/comments/events/reload-replies-event';
import {AfterCommentExpandedEvent} from 'idoc/comments/events/after-comment-expanded-event';
import {ActionsHelper} from 'idoc/actions/actions-helper';
import _ from 'lodash';
import 'components/dropdownmenu/dropdownmenu';
import commentActionsTemplate from './comment-actions.html!text';


@Component({
  selector: 'seip-comment-actions',
  properties: {
    'comment': 'comment',
    'config': 'config'
  }
})
@View({
  template: commentActionsTemplate
})
@Inject(PromiseAdapter, NgElement, Eventbus)
export class CommentActions {

  constructor(promiseAdapter, element, eventbus) {
    this.eventbus = eventbus;
    this.promiseAdapter = promiseAdapter;

    this.filterCriteria = ActionsHelper.getFilterCriteria(false, false);

    this.actionsMenuConfig = {
      loadItems: this.loadItems.bind(this),
      context: {comment: this.comment, config: this.config, currentObject: this.comment},
      buttonAsTrigger: true,
      triggerLabel: '',
      triggerClass: 'btn-xs button-ellipsis',
      wrapperClass: 'pull-right',
      triggerIcon: '<i class="fa fa-circle-column"></i>'
    };

    // When placed in ckeditor the bootstrap dropdown trigger stops working.
    // Fix it by initialising the trigger again.
    // Should wait some time because the button is in nested component.
    if (element.parents('.idoc-editor').length) {
      setTimeout(function () {
        element.find('.dropdown-toggle').dropdown();
      }, 250);
    }
  }

  loadItems() {
    let actions = this.comment.data.getActions();
    if (actions && actions.length) {
      this.eventbus.publish(new ReloadRepliesEvent(this.comment.data.getId()));
      return this.promiseAdapter.resolve(_.cloneDeep(ActionsHelper.extractActions(actions, this.filterCriteria)));
    }
    else {
      //When an image comment's dropdown is clicked the comment is not expanded and the actions are not loaded.
      //It fires event that expands the comment and waits for the actions to be loaded.
      return this.promiseAdapter.promise((resolve) => {
        this.subscribe = this.eventbus.subscribe(AfterCommentExpandedEvent, (event)=> {
          this.subscribe.unsubscribe();
          resolve(_.cloneDeep(ActionsHelper.extractActions(event[0], this.filterCriteria)));
        });
        this.eventbus.publish(new ReloadRepliesEvent(this.comment.data.getId()));
      });
    }
  }
}


