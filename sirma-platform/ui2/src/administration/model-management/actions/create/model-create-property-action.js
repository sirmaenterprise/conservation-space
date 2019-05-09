import {ModelAction} from 'administration/model-management/actions/model-action';

/**
 * Action representing a request for creating a new model property
 * inside a {@link ModelDefinition}. This action holds one property
 * and that property is meta model information needed to create the
 * actual property. The meta information contains data required
 * for an empty property to be created.
 *
 * @author Svetlozar Iliev
 */
export class ModelCreatePropertyAction extends ModelAction {

  getMetaData() {
    return this.meta;
  }

  setMetaData(meta) {
    this.meta = meta;
    return this;
  }
}