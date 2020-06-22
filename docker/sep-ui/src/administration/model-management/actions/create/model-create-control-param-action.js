import {ModelAction} from 'administration/model-management/actions/model-action';

/**
 * Action representing a request for creating a new control param
 * inside a {@link ModelControl}. This action holds one property
 * and that property is meta model information needed to create the
 * control param. The meta model information contains data required
 * for an empty control param to be created.
 *
 * @author Stella D
 */
export class ModelCreateControlParamAction extends ModelAction {

  getMetaData() {
    return this.meta;
  }

  setMetaData(meta) {
    this.meta = meta;
    return this;
  }

  getAttributes() {
    return this.attributes;
  }

  setAttributes(attributes) {
    this.attributes = attributes;
    return this;
  }
}