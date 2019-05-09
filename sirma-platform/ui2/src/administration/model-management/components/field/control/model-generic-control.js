import {ModelAttribute} from 'administration/model-management/model/attributes/model-attribute';

/**
 * Generic control class responsible for providing common logic which extends to
 * all of the different model control types.
 *
 * @author svelikov
 */
export class ModelGenericControl {

  getParamValue(paramName) {
    let controlParam = this.control.getControlParam(paramName);
    return controlParam && controlParam.getAttribute(ModelAttribute.VALUE_ATTRIBUTE);
  }
}