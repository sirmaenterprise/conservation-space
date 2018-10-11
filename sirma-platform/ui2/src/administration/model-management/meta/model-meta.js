import {Described} from 'administration/model-management/model/described';
import {ModelValidationRules} from 'administration/model-management/validation/model-validation-rules';

/**
 * Provides a description of a meta data which is used as a reference point.
 * Meta data stores arbitrary properties or attributes which are used to
 * provide the behaviour of a given model. Every model can be associated with
 * some type of meta data.
 *
 * @author Mihail Radkov
 * @author Svetlozar Iliev
 */
export class ModelMetaData extends Described {

  constructor(id) {
    super();
    this.id = id;
    this.validationRules = new ModelValidationRules();
  }

  getId() {
    return this.id;
  }

  setId(id) {
    this.id = id;
    return this;
  }

  getValidationRules() {
    return this.validationRules;
  }

  setValidationRules(rules) {
    this.validationRules = rules;
    return this;
  }

  seal() {
    this.validationRules.seal();
    // Using Object.freeze() to avoid reassigning existing fields which Object.seal() won't prevent.
    Object.freeze(this);
  }
}