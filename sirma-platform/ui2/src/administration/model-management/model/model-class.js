import {ModelBase} from 'administration/model-management/model/model-base';

/**
 * Represents a model of a class extending the {@link ModelBase} with a list
 * of subTypes which are required to be of type {@link ModelDefinition}.
 *
 * @author Svetlozar Iliev
 */
export class ModelClass extends ModelBase {

  constructor(id, parent) {
    super(id, parent);
  }
}
