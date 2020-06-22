import {ModelAttribute} from 'administration/model-management/model/attributes/model-attribute';

/**
 * Represents a model of an attribute extending the {@link ModelAttribute}
 * This attribute is strictly restricted to supporting only a single value.
 * Such value is typically a standard primitive such as a string or a boolean
 *
 * @author Svetlozar Iliev
 */
export class ModelSingleAttribute extends ModelAttribute {

  constructor(id, type = 'string', value = null) {
    super(id, type, value);
  }
}
