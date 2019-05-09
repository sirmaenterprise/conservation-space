import {Injectable} from 'app/app';
import {ModelRestoreInheritedControlAction} from './model-restore-inherited-control-action';

/**
 * Factory taking care of creating an action of type {@link ModelRestoreInheritedControlAction}
 * this action requires additional parameter to be provided representing the control to be restored.
 *
 * @author svelikov
 */
@Injectable()
export class ModelRestoreInheritedControlActionFactory {

  //@Override
  create(control) {
    return new ModelRestoreInheritedControlAction().setControlsToRestore([control]);
  }
}
