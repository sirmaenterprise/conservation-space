import {ModelBase} from 'administration/model-management/model/model-base';

/**
 * Represents a concrete model element - action group, which is defined by an id.
 * Extends {@see ModelAttribute} to support attributes, accessor and mutator methods.
 * By default, it is assumed that the element is inherited from a parent definition. If necessary, the flag is corrected.
 *
 * @author T. Dossev
 */
export class ModelActionGroup extends ModelBase {

  constructor(id) {
    super(id);
    this.setInherited(true);
  }

  getInherited() {
    return this.inherited;
  }

  setInherited(inherited) {
    this.inherited = inherited;
  }
}