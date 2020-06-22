import {ModelAction} from 'administration/model-management/actions/model-action';

/**
 * Action representing request for restoring provided inherited region.
 * Field to be restored from the parent model is provided to the action.
 * Additionally all fields contained in that region can also be provided.
 *
 * @author Svetlozar Iliev
 */
export class ModelRestoreInheritedRegionAction extends ModelAction {

  getContainedFields() {
    return this.fields;
  }

  setContainedFields(fields) {
    this.fields = fields;
    return this;
  }
}