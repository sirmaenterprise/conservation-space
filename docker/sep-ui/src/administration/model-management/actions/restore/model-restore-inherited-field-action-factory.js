import {Injectable} from 'app/app';
import {ModelRestoreInheritedFieldAction} from './model-restore-inherited-field-action';

/**
 * Factory taking care of creating an action of type {@link ModelRestoreInheritedFieldAction}
 * this action requires additional parameter to be provided representing the field to be restored.
 *
 * @author Svetlozar Iliev
 */
@Injectable()
export class ModelRestoreInheritedFieldActionFactory {

  //@Override
  create() {
    return new ModelRestoreInheritedFieldAction();
  }
}
