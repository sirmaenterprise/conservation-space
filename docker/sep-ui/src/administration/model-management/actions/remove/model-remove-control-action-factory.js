import {Injectable} from 'app/app';
import {ModelRemoveControlAction} from './model-remove-control-action';

/**
 * Factory taking care of creating an action of type {@link ModelRemoveControlAction}
 *
 * @author Stella D
 */
@Injectable()
export class ModelRemoveControlActionFactory {

  create() {
    return new ModelRemoveControlAction();
  }
}
