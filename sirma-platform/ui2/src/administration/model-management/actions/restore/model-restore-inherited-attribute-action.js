import {ModelAction} from 'administration/model-management/actions/model-action';

/**
 * Action representing request for restoring provided inherited attributes.
 * Attributes to be restored from the parent model are provided to the action
 *
 * @author Svetlozar Iliev
 */
export class ModelRestoreInheritedAttributeAction extends ModelAction {

  setAttributesToRestore(overridden) {
    this.overridden = overridden;
    return this;
  }

  getAttributesToRestore() {
    return this.overridden;
  }
}