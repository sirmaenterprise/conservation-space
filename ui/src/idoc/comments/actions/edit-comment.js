import {Injectable} from 'app/app';
import {ActionHandler} from 'services/actions/action-handler';

@Injectable()
export class EditCommentAction extends ActionHandler {

  constructor() {
    super();
  }

  execute(action, context) {
    let comment = context.comment.data;
    let config = {
      comment: comment,
      widgetId: context.config.widgetId,
      tabId: context.config.tabId
    };
    context.config.commentContentDialog.createDialog(config);
  }
}