import {Component, View, Inject} from 'app/app';
import {SaveIdocDialog} from 'idoc/save-idoc-dialog/save-idoc-dialog';
import {SaveIdocAction} from 'idoc/actions/save-idoc-action';
import {ActionExecutor} from 'services/actions/action-executor';
import saveIdocDialogTemplate from 'save-idoc-dialog-template!text';

@Component({
  selector: 'seip-save-idoc-dialog-stub',
  properties: {
    'config': 'config'
  }
})
@View({
  template: saveIdocDialogTemplate
})
@Inject(ActionExecutor, SaveIdocAction)
export class SaveIdocDialogStub {
  constructor(actionExecutor, saveIdocAction) {
    this.actionExecutor = actionExecutor;
    this.saveIdocAction = saveIdocAction;
  }
}
