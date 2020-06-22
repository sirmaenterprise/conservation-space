import {Injectable} from 'app/app';
import {ModelChangeAttributeAction} from './model-change-attribute-action';

/**
 * Factory taking care of creating an action of type {@link ModelChangeAttributeAction}
 *
 * @author Svetlozar Iliev
 */
@Injectable()
export class ModelChangeAttributeActionFactory {

  create() {
    return new ModelChangeAttributeAction();
  }

  evaluate(action) {
    // default binding for the owning context
    action.setOwningContext(action.getContext());
  }
}
