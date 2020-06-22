import {Injectable} from 'app/app';
import {ModelRestoreInheritedRegionAction} from './model-restore-inherited-region-action';
import {ModelManagementUtility} from 'administration/model-management/utility/model-management-utility';

/**
 * Factory taking care of creating an action of type {@link ModelRestoreInheritedRegionAction}
 * this action requires additional parameter to be provided representing the field to be restored.
 * Additionally the action computes and evaluates all fields that are currently related to that
 * region or contained inside it.
 *
 * @author Svetlozar Iliev
 */
@Injectable()
export class ModelRestoreInheritedRegionActionFactory {

  //@Override
  create() {
    return new ModelRestoreInheritedRegionAction();
  }

  evaluate(action) {
    let region = action.getModel();
    let context = action.getContext();

    let contained = context.getFields().filter(f => f.getRegionId() === region.getId());
    let owned = contained.filter(f => !ModelManagementUtility.isInherited(f, context));

    action.setContainedFields(owned);
  }
}
