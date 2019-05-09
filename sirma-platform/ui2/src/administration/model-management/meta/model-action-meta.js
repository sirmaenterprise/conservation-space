import {ModelAttributeMetaData} from 'administration/model-management/meta/model-attribute-meta';

/**
 * Represents a specific action meta data extending from {@link ModelAttributeMetaData}
 *
 * @author B.Tonchev
 */
export class ModelActionMetaData extends ModelAttributeMetaData {

  constructor(id) {
    super(id);
  }
}