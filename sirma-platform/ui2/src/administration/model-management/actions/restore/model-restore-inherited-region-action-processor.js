import {Injectable, Inject} from 'app/app';
import {ModelOperation} from 'administration/model-management/model/model-operation';
import {ModelChangeSetBuilder} from 'administration/model-management/services/builders/model-changeset-builder';

/**
 * Processor taking care of executing an operation of type {@link ModelRestoreInheritedRegionAction}
 * This processor is directly executes a restore operation for a given region inheriting that region
 * from the parent. Restoring the operation attaches back the overridden region that was present
 * before the action was executed. The action execution does a simple replacement of the overridden
 * region with the inherited when executed. Further more all overridden fields that are contained in
 * the restored region are taken out of it by removing the reference region id for each field.
 *
 * @author Svetlozar Iliev
 */
@Injectable()
@Inject(ModelChangeSetBuilder)
export class ModelRestoreInheritedRegionActionProcessor {

  constructor(modelChangeSetBuilder) {
    this.modelChangeSetBuilder = modelChangeSetBuilder;
  }

  execute(action) {
    let target = action.getModel();
    let context = action.getContext();
    let fields = action.getContainedFields();

    let inherited = target.getReference();
    fields.forEach(f => f.setRegionId(null));
    context.addRegion(inherited);
    return inherited;
  }

  restore(action) {
    let target = action.getModel();
    let context = action.getContext();
    let fields = action.getContainedFields();

    let region = target.getId();
    fields.forEach(f => f.setRegionId(region));
    context.addRegion(target);
    return target;
  }

  changeset(action) {
    return this.modelChangeSetBuilder.buildChangeSet(action.getModel(), ModelOperation.RESTORE);
  }
}
