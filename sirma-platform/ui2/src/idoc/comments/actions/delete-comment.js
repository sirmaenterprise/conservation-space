import {Injectable, Inject} from 'app/app';
import {ActionHandler} from 'services/actions/action-handler';
import {Eventbus} from 'services/eventbus/eventbus';
import {CommentsHelper} from 'idoc/comments/comments-helper';

@Injectable()
@Inject(Eventbus)
export class DeleteCommentAction extends ActionHandler {

  constructor(eventbus) {
    super();
    this.eventbus = eventbus;
  }

  execute(action, context) {
    let comment = context.comment.data;
    let config = {
      comment: comment,
      commentId: comment.getId()
    };
    return context.config.dataProvider.deleteComment(config).then(() => {
      let tabId = context.config.tabId;
      let widgetId = context.config.widgetId;
      CommentsHelper.reloadComments(comment.isReply(), tabId, widgetId, this.eventbus);
    });

  }

}