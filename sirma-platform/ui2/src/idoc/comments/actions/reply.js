import {Injectable, Inject} from 'app/app';
import {ActionHandler} from 'services/actions/action-handler';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {CommentsHelper} from 'idoc/comments/comments-helper';

@Injectable()
@Inject(PromiseAdapter)
export class ReplyCommentAction extends ActionHandler {

  constructor(promiseAdapter) {
    super();
    this.promiseAdapter = promiseAdapter;
  }

  execute(action, context) {
    return this.promiseAdapter.promise((resolve, reject) => {
      let comment = context.comment.data;
      //Reply's target should have the same target as the comment container.
      let target = comment.getTarget();
      let config = {
        currentObject: {
          getId() {
            return target;
          }
        },
        tabId: context.config.tabId,
        replyTo: comment.getId(),
        widgetId: context.config.widgetId,
        onClose: (buttonId) => {
          if (buttonId === CommentsHelper.SAVE_ACTION_ID) {
            resolve();
          } else {
            reject();
          }
        }
      };
      context.config.commentContentDialog.createDialog(config);
    });
  }
}
