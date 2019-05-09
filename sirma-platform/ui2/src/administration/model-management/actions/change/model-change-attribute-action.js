import {ModelAction} from 'administration/model-management/actions/model-action';

/**
 * Action representing a change or modification in a model attribute. This action
 * holds the context in which the change has occurred and the actual model attribute
 * Further more the action holds information about the state of the attribute which
 * can be inherited or not from a parent model of the same type.
 *
 * @author Svetlozar Iliev
 */
export class ModelChangeAttributeAction extends ModelAction {

  isInherited() {
    return !!this.inherited;
  }

  setInherited(inherited) {
    this.inherited = inherited;
    return this;
  }

  getOwningContext() {
    return this.owner;
  }

  setOwningContext(owner) {
    this.owner = owner;
    return this;
  }
}