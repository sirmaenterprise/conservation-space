import {Injectable, Inject} from 'app/app';
import {ActionHandler} from 'services/actions/action-handler';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {CommentsHelper} from 'idoc/comments/comments-helper';

@Injectable()
@Inject(PromiseAdapter)
export class EditCommentAction extends ActionHandler {

  constructor(promiseAdapter) {
    super();
    this.promiseAdapter = promiseAdapter;
  }

  execute(action, context) {
    return this.promiseAdapter.promise((resolve, reject) => {
      let comment = context.comment.data;
      let config = {
        comment: comment,
        widgetId: context.config.widgetId,
        tabId: context.config.tabId,
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