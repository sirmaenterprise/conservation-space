import {Injectable} from 'app/app';
import {ActionHandler} from 'services/actions/action-handler';

@Injectable()
export class ReplyCommentAction extends ActionHandler {

  constructor() {
    super();
  }

  execute(action, context) {
    let comment = context.comment.data;
    //Reply's target should have the same target as the comment container.
    let target = comment.getTarget();
    let config = {
      currentObject: {
        getId: function () {
          return target;
        }
      },
      tabId: context.config.tabId,
      replyTo: comment.getId(),
      widgetId: context.config.widgetId
    };

    context.config.commentContentDialog.createDialog(config);
  }
}