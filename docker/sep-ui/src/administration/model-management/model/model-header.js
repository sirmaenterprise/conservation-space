import {ModelBase} from 'administration/model-management/model/model-base';
import {ModelAttribute} from 'administration/model-management/model/attributes/model-attribute';
import _ from 'lodash';

/**
 * Represents information about model definitions's header.
 *
 * @author Mihail Radkov
 */
export class ModelHeader extends ModelBase {

  getLabelAttribute() {
    return this.getAttribute(ModelAttribute.LABEL_ATTRIBUTE);
  }

  getHeaderTypeOptions() {
    return this.getAttribute(ModelAttribute.HEADER_TYPE_ATTRIBUTE).getMetaData().getOptions();
  }

  getHeaderTypeOption() {
    let headerId = this.getId();
    return _.find(this.getHeaderTypeOptions(), option => option.value === headerId);
  }
}