import {Injectable} from 'app/app';
import {ModelValidateAttributeAction} from './model-validate-attribute-action';

/**
 * Factory taking care of creating an action of type {@link ModelValidateAttributeAction}
 *
 * @author Svetlozar Iliev
 */
@Injectable()
export class ModelValidateAttributeActionFactory {

  create() {
    return new ModelValidateAttributeAction();
  }
}
