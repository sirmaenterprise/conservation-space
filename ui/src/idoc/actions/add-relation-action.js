import {Injectable, Inject} from 'app/app';
import {AddRelationService} from 'idoc/actions/add-relation-service';
import {ActionHandler} from 'services/actions/action-handler';

@Injectable()
@Inject(AddRelationService)
export class AddRelationAction extends ActionHandler {
  constructor(addRelationService) {
    super();
    this.addRelationService = addRelationService;
  }

  execute(action, context) {
    return this.addRelationService.openDialog(context.currentObject.id, context.scope, context.idocContext, action);
  }
}