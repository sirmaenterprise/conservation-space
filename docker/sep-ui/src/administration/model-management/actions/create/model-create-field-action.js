import {ModelAction} from 'administration/model-management/actions/model-action';

/**
 * Action representing a request for creating a new model field
 * inside a {@link ModelDefinition}. This action holds one property
 * and that property is meta model information needed to create the
 * actual field. The meta model information contains data required
 * for an empty field to be created.
 *
 * @author Svetlozar Iliev
 */
export class ModelCreateFieldAction extends ModelAction {

  getMetaData() {
    return this.meta;
  }

  setMetaData(meta) {
    this.meta = meta;
    return this;
  }
}