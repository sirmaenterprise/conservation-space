import {Component, View, Inject, NgTimeout} from 'app/app';
import {CommentExpandedEvent} from './events/comment-expanded-event';
import {ReloadRepliesEvent} from 'idoc/comments/events/reload-replies-event';
import 'idoc/comments/actions-menu/comment-actions';
import {CommentsRestService} from 'services/rest/comments-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {MomentAdapter} from 'adapters/moment-adapter';
import {Configuration} from 'common/application-config';
import {CommentInstance} from 'idoc/comments/comment-instance';
import {ReplyCommentAction} from 'idoc/comments/actions/reply';
import {IconsService} from 'services/icons/icons-service';
import {AfterCommentExpandedEvent} from 'idoc/comments/events/after-comment-expanded-event';

import 'user/avatar/user-avatar';
import 'dotdotdot';
import 'font-awesome/css/font-awesome.css!';
import './comment.css!';
import template from './comment.html!text';

const USER_ICON_NAME = 'user';
const USER_ICON_SIZE = 64;
const COMMENT_DESCRIPTION_CLASS = '.comment-description';
const COMMENT_REPLY_TOOLBAR_CLASS = '.comment-reply-toolbar';

@Component({
  selector: 'seip-comment',
  properties: {
    'config': 'config',
    'comment': 'comment'
  }
})
@View({template})
@Inject(MomentAdapter, Configuration, ReplyCommentAction, IconsService, Eventbus, NgTimeout, CommentsRestService)
export class Comment {
  constructor(momentAdapter, configuration, replyCommentAction, iconsService, eventbus, $timeout, commentsService) {
    this.momentAdapter = momentAdapter;
    this.configuration = configuration;
    this.eventbus = eventbus;
    this.replyCommentAction = replyCommentAction;
    this.iconsService = iconsService;
    this.commentsService = commentsService;
    this.$timeout = $timeout;
    this.date = this.formatDate(this.comment.getModifiedDate());
    this.defaultUserIcon = this.getDefaultUserIcon();

    this.commentId = this.comment.getId().split(':')[1];
    this.commentUser = {id: this.comment.getAuthorId()};

    this.actionsContext = {
      data: this.comment
    };

    this.replyActionContext = {
      config: this.config,
      comment: {
        data: this.comment
      }
    };

    this.events = [];
    if (!this.comment.isReply()) {
      this.events.push(this.eventbus.subscribe(CommentExpandedEvent, this.handleCommentExtendedEvent.bind(this)));
      this.events.push(this.eventbus.subscribe(ReloadRepliesEvent, this.handleReloadRepliesEvent.bind(this)));
    }
  }

  handleCommentExtendedEvent(event) {
    if (this.comment.getId() !== event[0] && this.expanded && !this.config.context.isPrintMode()) {
      this.collapseComment();
    }
  }

  handleReloadRepliesEvent(event) {
    //When image comment's dropdown is clicked the event's propagation is stopped and the comment isn't expanded.
    //In this case an event is fired with the comment id.
    if (!this.expanded && event[0] === this.comment.getId()) {
      this.expandComment();
    } else {
      this.obtainReplies(event);
    }
  }

  /**
   * By default all the comments are collapsed.
   */
  ngAfterViewInit() {
    if (!this.comment.isReply()) {
      if (this.config.widgetId) {
        this.commentContainer = $(`#${this.config.widgetId} #${this.commentId}`);
      } else {
        this.commentContainer = $(`#${this.commentId}`);
      }

      this.commentDescriptionContainer = this.commentContainer.find(COMMENT_DESCRIPTION_CLASS);
      if (this.comment.expanded || this.config.context.isPrintMode()) {
        this.expandComment();
      } else {
        this.collapseComment();
      }
    }
  }

  /**
   * Collapses the expanded comment when another comment is selected.
   */
  collapseComment() {
    this.expanded = false;
    this.commentDescriptionContainer.addClass('collapsed expanded');
    this.commentContainer.find(COMMENT_REPLY_TOOLBAR_CLASS).css({'display': 'none'});
    this.commentDescriptionContainer.dotdotdot({
      ellipsis: '... ',
      wrap: 'word'
    });

    let spanContent = this.commentDescriptionContainer.find(' span');
    // if different font size is used, the height and line-height of the description container must be resized according to the ellipsed span height.
    if (spanContent.length) {
      let spanContentHeight = spanContent.height();
      this.commentDescriptionContainer.css({
        'height': spanContentHeight + 'px',
        'line-height': spanContentHeight / 1.2 + 'px'
      });
    }
  }

  getDefaultUserIcon() {
    return this.iconsService.getIconForInstance(USER_ICON_NAME, USER_ICON_SIZE);
  }

  createReply() {
    this.replyCommentAction.execute(null, this.replyActionContext);
  }

  formatDate(date) {
    let pattern = this.configuration.get(Configuration.UI_DATE_FORMAT);
    pattern += ' ' + this.configuration.get(Configuration.UI_TIME_FORMAT);
    return this.momentAdapter.format(date, pattern);
  }

  /**
   * Expands the comment if the comment isn't already expanded. Removes the pointer cursor.
   * Shows the full text of the comment and the reply toolbar.
   */
  expandComment() {
    if (!this.expanded && !this.comment.isReply()) {
      this.expanded = true;
      this.obtainReplies();
      this.commentContainer.removeClass('expanded');
      this.commentContainer.find(COMMENT_REPLY_TOOLBAR_CLASS).css({'display': 'block'});
      this.commentDescriptionContainer.trigger('destroy');
      this.commentDescriptionContainer.removeClass('collapsed');
      this.eventbus.publish(new CommentExpandedEvent(this.comment.getId()));
    }
  }

  obtainReplies(event) {
    if (this.shouldReloadReplies(event)) {
      this.commentsService.loadReplies(this.comment.getId()).then((response) => {
        this.actionsContext.data = new CommentInstance(response.data);
        if (response.data && response.data['emf:reply']) {
          let replyInstances = response.data['emf:reply'].map(function (reply) {
            return new CommentInstance(reply, true);
          });
          this.replies = replyInstances;
        } else {
          delete this.replies;
        }
        this.eventbus.publish(new AfterCommentExpandedEvent(this.actionsContext.data.getActions()));
      });
    }
  }

  shouldReloadReplies(event) {
    if (this.expanded && !this.comment.isReply()) {
      if (event && event[0]) {
        if (event[0] === this.config.tabId || event[0] === this.config.widgetId) {
          return true;
        }
      } else {
        return true;
      }
    }
    return false;
  }

  ngOnDestroy() {
    for (let event of this.events) {
      event.unsubscribe();
    }
  }

}
