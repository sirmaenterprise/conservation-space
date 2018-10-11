import {Injectable, Inject} from 'app/app';
import {DialogService} from 'components/dialog/dialog-service';

/**
 * This dummy action handler is assigned to all not implemented actions because it was required to show all off them.
 */
@Injectable()
@Inject(DialogService)
export class DummyAction {

  constructor(dialogService) {
    this.dialogService = dialogService;
  }

  execute() {
    this.dialogService.error('Not implemented action!');
    throw new Error('Not implemented action!');
  }
}