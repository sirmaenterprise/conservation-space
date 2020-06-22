import {Injectable} from 'app/app';
import {ModelRestoreInheritedHeaderAction} from './model-restore-inherited-header-action';

/**
 * Factory taking care of creating an action of type {@link ModelRestoreInheritedHeaderAction}
 * this action requires additional parameter to be provided representing the header to be restored.
 *
 * @author Svetlozar Iliev
 */
@Injectable()
export class ModelRestoreInheritedHeaderActionFactory {

  //@Override
  create() {
    return new ModelRestoreInheritedHeaderAction();
  }
}
