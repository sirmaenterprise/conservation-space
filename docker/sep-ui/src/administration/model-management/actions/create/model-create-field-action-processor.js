import {Injectable, Inject} from 'app/app';
import {ModelOperation} from 'administration/model-management/model/model-operation';
import {ModelChangeSetBuilder} from 'administration/model-management/services/builders/model-changeset-builder';
import {ModelManagementUtility} from 'administration/model-management/utility/model-management-utility';

/**
 * Processor taking care of executing an action of type {@link ModelCreateFieldAction}.
 * The execute stage of the action appends the newly created field to the provided context
 * which in this case should be of type {@link ModelDefinition} and initialized the parent
 * of the created field to the context to which it was appended. The restore operation stage
 * takes the created field and detaches it from the context and removes the parent reference
 * off of it. The change set unconditionally collects all attributes for the created field
 * regardless of their current dirty state.
 *
 * @author Svetlozar Iliev
 */
@Injectable()
@Inject(ModelChangeSetBuilder)
export class ModelCreateFieldActionProcessor {

  constructor(modelChangeSetBuilder) {
    this.modelChangeSetBuilder = modelChangeSetBuilder;
  }

  execute(action) {
    let field = action.getModel();
    let context = action.getContext();

    context.addField(field);
    field.setParent(context);
    return field;
  }

  restore(action) {
    let field = action.getModel();
    let context = action.getContext();

    field.setParent(null);
    context.removeField(field);
    return field;
  }

  changeset(action) {
    // TODO: this should potentially be a single CREATE operation handled by the backend using model path
    return this.modelChangeSetBuilder.buildChangeSets(this.getAttributes(action), ModelOperation.MODIFY);
  }

  getAttributes(action) {
    // make sure to collect all not empty, not dirty attributes from the model to ensure the model completeness
    return action.getModel().getNotDirtyAttributes().filter(a => !ModelManagementUtility.isAttributeEmpty(a));
  }
}
