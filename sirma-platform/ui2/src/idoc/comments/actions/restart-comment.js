import {Injectable, Inject} from 'app/app';
import {ActionHandler} from 'services/actions/action-handler';
import {Eventbus} from 'services/eventbus/eventbus';
import {CommentsHelper} from 'idoc/comments/comments-helper';

@Injectable()
@Inject(Eventbus)
export class RestartCommentAction extends ActionHandler {

  constructor(eventbus) {
    super();
    this.eventbus = eventbus;
  }

  execute(action, context) {
    let comment = context.comment.data;
    comment.removeActions();
    comment.setRestartAction();

    let config = {
      instanceId: comment.getId(),
      comment: comment,
      content: comment.getComment()
    };
    return context.config.dataProvider.updateComment(config).then(() => {
      CommentsHelper.reloadComments(comment.isReply(), context.config.tabId, context.config.widgetId, this.eventbus);
    });
  }
}