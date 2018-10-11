import {CommentInstance} from './comment-instance';
import {ReloadCommentsEvent} from './events/reload-comments-event';
import {ReloadRepliesEvent} from './events/reload-replies-event';

export class CommentsHelper {

  static sortByDate(comments) {
    return comments.sort(function (a, b) {
      var dateA = new Date(a.getModifiedDate());
      var dateB = new Date(b.getModifiedDate());
      return dateB - dateA;
    });
  };

  static convertToCommentInstances(comments) {
    return comments.map(function (comment) {
      return new CommentInstance(comment);
    });
  }

  static convertAndSort(comments) {
    return this.sortByDate(this.convertToCommentInstances(comments));
  }

  static reloadComments(reply, tabId, widgetId, eventbus) {
    if (reply) {
      if (tabId) {
        eventbus.publish(new ReloadRepliesEvent(tabId));
      } else {
        eventbus.publish(new ReloadRepliesEvent(widgetId));
      }
    } else {
      if (tabId) {
        eventbus.publish(new ReloadCommentsEvent(tabId));
      } else {
        eventbus.publish(new ReloadCommentsEvent(widgetId));
      }
    }
  }
}

CommentsHelper.SAVE_ACTION_ID = 'SAVE-COMMENT';
CommentsHelper.CANCEL_ACTION_ID = 'CANCEL';