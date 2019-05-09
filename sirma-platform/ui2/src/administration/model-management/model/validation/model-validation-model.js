import {ModelValidationRules} from 'administration/model-management/model/validation/model-validation-rules';
import {ModelRestrictions} from 'administration/model-management/model/model-restrictions';

/**
 * Model representing a composition of several properties used to represent the validation model
 *
 * - affected - represents the affected models, affected models are such that require validation when
 * the current model has been changed or it's state has been modified in any way
 *
 * - restrictions - represents various static restrictions that can be applied to the current model
 * they can be also used to obtain information about the initial restriction state of a model
 *
 * - validationRules - collection of rules which are executed during the validation cycle for the
 * current model, based on these rules the model state can be altered
 *
 * @author Svetlozar Iliev
 */
export class ModelValidationModel {

  constructor() {
    this.affected = [];
    this.restrictions = new ModelRestrictions();
    this.validationRules = new ModelValidationRules();
  }

  getAffected() {
    return this.affected;
  }

  setAffected(affected) {
    this.affected = affected;
    return this;
  }

  getValidationRules() {
    return this.validationRules;
  }

  getRestrictions() {
    return this.restrictions;
  }

  seal() {
    Object.freeze(this.affected);
    this.validationRules.seal();
    this.restrictions.seal();
    Object.freeze(this);
  }
}