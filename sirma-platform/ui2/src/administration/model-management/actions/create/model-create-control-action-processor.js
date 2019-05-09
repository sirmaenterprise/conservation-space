import {Injectable, Inject} from 'app/app';
import {ModelOperation} from 'administration/model-management/model/model-operation';
import {ModelChangeSetBuilder} from 'administration/model-management/services/builders/model-changeset-builder';

import {ModelManagementUtility} from 'administration/model-management/utility/model-management-utility';
import {ModelManagementCopyService} from 'administration/model-management/services/model-management-copy-service';

/**
 * Processor taking care of executing an action of type {@link ModelCreateControlAction}.
 * The execute stage of the action appends the newly created control to the provided context
 * which in this case should be of type {@link ModelField} and initialized the parent
 * of the created control to the context to which it was appended. The restore operation stage
 * takes the created control and detaches it from the context and removes the parent reference
 * off of it. The change set unconditionally collects all attributes for the created control
 * regardless of their current dirty state.
 *
 * @author Stella D
 */
@Injectable()
@Inject(ModelChangeSetBuilder)
export class ModelCreateControlActionProcessor {

  constructor(modelChangeSetBuilder) {
    this.modelChangeSetBuilder = modelChangeSetBuilder;
  }

  execute(action) {
    let control = action.getModel();
    let context = action.getContext();
    if (ModelManagementUtility.isInherited(context, action.getOwningContext())) {
      // the current model is actually the inherited one, create a copy of it
      context = ModelManagementCopyService.createCopiedModel(context, action.getOwningContext());
    }
    context.addControl(control);
    control.setParent(context);
    return control;
  }

  restore(action) {
    let control = action.getModel();
    let context = action.getContext();
    if (ModelManagementUtility.isInherited(context, action.getOwningContext())) {
      // the coppied model should be replaced with the inherited one
      ModelManagementCopyService.addToModel(action.getOwningContext(), context);
      return control;
    }
    control.setParent(null);
    context.removeControl(control);
    return control;
  }

  changeset(action) {
    return this.modelChangeSetBuilder.buildChangeSets(action.getModel().getAttributes(), ModelOperation.MODIFY);
  }
}
