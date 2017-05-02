import {View, Component, Inject, Injectable, NgElement} from 'app/app';
import commentActionsTemplate from './comment-actions.html!text';
import 'components/dropdownmenu/dropdownmenu';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {Eventbus} from 'services/eventbus/eventbus';
import {ReloadRepliesEvent} from 'idoc/comments/events/reload-replies-event';
import {AfterCommentExpandedEvent} from 'idoc/comments/events/after-comment-expanded-event';
import _ from 'lodash';

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
    this.collectImplementedHandlers();
    this.eventbus = eventbus;
    this.promiseAdapter = promiseAdapter;
    this.actions = {
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
      return this.promiseAdapter.resolve(_.cloneDeep(this.buildAvailableActions(actions)));
    }
    else {
      //When an image comment's dropdown is clicked the comment is not expanded and the actions are not loaded.
      //It fires event that expands the comment and waits for the actions to be loaded.
      return new Promise((resolve) => {
        this.subscribe = this.eventbus.subscribe(AfterCommentExpandedEvent, (event)=> {
          this.subscribe.unsubscribe();
          let availableActions = this.buildAvailableActions(event[0]);
          resolve(_.cloneDeep(availableActions));
        });
        this.eventbus.publish(new ReloadRepliesEvent(this.comment.data.getId()));
      });
    }
  }

  buildAvailableActions(actions) {
    return actions.map((action)=> {
      var handlerName = action.serverOperation + 'Action';
      return {
        // The user operation is the actual operation that user requests. For example if user requests operation
        // 'approve', then the server operation would be 'transition'. This way a group of identical operations
        // might be handled by a single action handler.
        action: action.userOperation,
        // Defined action handler: resolved on the server.
        // If the server operation is 'transition', then action handler would be TransitionAction.
        // Action handlers are registered as plugins for the 'actions' extension point and are resolved by name.
        name: handlerName,
        label: action.label,
        disabled: action.disabled,
        confirmationMessage: action.confirmationMessage,
        extensionPoint: 'actions',
        configuration: action.configuration,
        id: action.userOperation,
        tooltip:action.tooltip
      };
    });
  }

  collectImplementedHandlers() {
    this.actionHandlers = PluginRegistry.get('actions').reduce(function (total, current) {
      total[current.name] = current;
      return total;
    });
  }
}


