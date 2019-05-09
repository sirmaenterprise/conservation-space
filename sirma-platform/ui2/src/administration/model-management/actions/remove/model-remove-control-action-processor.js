import {Injectable, Inject} from 'app/app';
import {ModelOperation} from 'administration/model-management/model/model-operation';
import {ModelChangeSetBuilder} from 'administration/model-management/services/builders/model-changeset-builder';

/**
 * Processor taking care of executing an action of type {@link ModelRemoveControlAction}.
 * The execute stage of the action removes control by setting it's it to null
 * The restore operation stage takes the removed control and attach it back to it's parent.
 *
 * @author Stella D
 */
@Injectable()
@Inject(ModelChangeSetBuilder)
export class ModelRemoveControlActionProcessor {

  constructor(modelChangeSetBuilder) {
    this.modelChangeSetBuilder = modelChangeSetBuilder;
  }

  execute(action) {
    let control = action.getModel();
    let context = action.getContext();
    control.setParent(context);
    context.removeControl(control);
    return control;
  }

  restore(action) {
    let control = action.getModel();
    let context = action.getContext();
    context.addControl(control);
    control.setParent(context);
    return control;
  }

  changeset(action) {
    return this.modelChangeSetBuilder.buildChangeSet(action.getModel(), ModelOperation.REMOVE);
  }
}
