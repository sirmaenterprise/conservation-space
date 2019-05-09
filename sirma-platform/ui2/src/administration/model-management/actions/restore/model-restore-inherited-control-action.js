import {ModelAction} from 'administration/model-management/actions/model-action';

/**
 * Action representing request for restoring provided inherited controls.
 * Control to be restored from the parent model is provided to the action
 *
 * @author svelikov
 */
export class ModelRestoreInheritedControlAction extends ModelAction {

  setControlsToRestore(overridden) {
    this.overridden = overridden;
    return this;
  }

  getControlsToRestore() {
    return this.overridden;
  }
}