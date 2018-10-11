import {ModelAttributeMetaData} from 'administration/model-management/meta/model-attribute-meta';

/**
 * Represents a specific field meta data extending from {@link ModelAttributeMetaData}
 *
 * @author Svetlozar Iliev
 */
export class ModelFieldMetaData extends ModelAttributeMetaData {

  constructor(id) {
    super(id);
  }
}