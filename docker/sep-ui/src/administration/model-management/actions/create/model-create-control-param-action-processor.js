import {Injectable, Inject} from 'app/app';
import {ModelOperation} from 'administration/model-management/model/model-operation';
import {ModelChangeSetBuilder} from 'administration/model-management/services/builders/model-changeset-builder';

/**
 * Processor taking care of executing an action of type {@link ModelCreateControlParamAction}.
 * The execute stage of the action appends the newly created control param to the provided context
 * which in this case should be of type {@link ModelControl} and initialized the parent
 * of the created control param to the context to which it was appended. The restore operation stage
 * takes the created control param and detaches it from the context and removes the parent reference
 * off of it. The change set unconditionally collects all attributes for the created control param
 * regardless of their current dirty state.
 *
 * @author Stella D
 */
@Injectable()
@Inject(ModelChangeSetBuilder)
export class ModelCreateControlParamActionProcessor {

  constructor(modelChangeSetBuilder) {
    this.modelChangeSetBuilder = modelChangeSetBuilder;
  }

  execute(action) {
    let controlParam = action.getModel();
    let context = action.getContext();
    context.addControlParam(controlParam);
    controlParam.setParent(context);
    return controlParam;
  }

  restore(action) {
    let controlParam = action.getModel();
    let context = action.getContext();

    controlParam.setParent(null);
    context.removeControlParam(controlParam);
    return controlParam;
  }

  changeset(action) {
    return this.modelChangeSetBuilder.buildChangeSets(action.getModel().getAttributes(), ModelOperation.MODIFY);
  }
}
