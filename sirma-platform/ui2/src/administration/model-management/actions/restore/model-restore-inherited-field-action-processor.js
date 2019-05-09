import {Injectable, Inject} from 'app/app';
import {ModelOperation} from 'administration/model-management/model/model-operation';
import {ModelChangeSetBuilder} from 'administration/model-management/services/builders/model-changeset-builder';

/**
 * Processor taking care of executing an operation of type {@link ModelRestoreInheritedFieldAction}
 * This processor is directly executes a restore operation for a given field inheriting that field
 * from the parent. Restoring the operation attaches back the overridden field that was present
 * before the action was executed. The action execution does a simple replacement of the overridden
 * field with the inherited when executed.
 *
 * @author Svetlozar Iliev
 */
@Injectable()
@Inject(ModelChangeSetBuilder)
export class ModelRestoreInheritedFieldActionProcessor {

  constructor(modelChangeSetBuilder) {
    this.modelChangeSetBuilder = modelChangeSetBuilder;
  }

  execute(action) {
    let target = action.getModel();
    let context = action.getContext();

    let inherited = target.getReference();
    context.addField(inherited);
    return inherited;
  }

  restore(action) {
    let target = action.getModel();
    let context = action.getContext();

    context.addField(target);
    return target;
  }

  changeset(action) {
    return this.modelChangeSetBuilder.buildChangeSet(action.getModel(), ModelOperation.RESTORE);
  }
}
