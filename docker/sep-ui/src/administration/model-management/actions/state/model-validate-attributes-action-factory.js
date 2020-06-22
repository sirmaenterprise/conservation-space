import {Injectable} from 'app/app';
import {ModelValidateAttributesAction} from './model-validate-attributes-action';

/**
 * Factory taking care of creating an action of type {@link ModelValidateAttributesAction}
 *
 * @author Svetlozar Iliev
 */
@Injectable()
export class ModelValidateAttributesActionFactory {

  create() {
    return new ModelValidateAttributesAction();
  }
}
