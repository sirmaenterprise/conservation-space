import {ModelAction} from 'administration/model-management/actions/model-action';

/**
 * Action representing a request for creating a new control
 * inside a {@link ModelDefinition}. This action holds one property
 * and that property is meta model information needed to create the
 * control. The meta model information contains data required
 * for an empty control to be created.
 *
 * @author Stella D
 */
export class ModelCreateControlAction extends ModelAction {

  getMetaData() {
    return this.meta;
  }

  setMetaData(meta) {
    this.meta = meta;
    return this;
  }

  getId() {
    return this.id;
  }

  setId(id) {
    this.id = id;
    return this;
  }

  getOwningContext() {
    return this.definition;
  }

  setDefinition(definition) {
    this.definition = definition;
    return this;
  }
}