import {ModelBase} from 'administration/model-management/model/model-base';
import {ModelList} from './model-list';
import {ModelActionExecution} from 'administration/model-management/model/model-action-execution';

/**
 * Represents a concrete model element - action, which is defined by an id.
 * Extends {@see ModelAttribute} to support attributes, accessor and mutator methods.
 * By default, it is assumed that the element is inherited from a parent definition. If necessary, the flag is corrected.
 *
 * @author T. Dossev
 */
export class ModelAction extends ModelBase {

  constructor(id) {
    super(id);
    this.setInherited(true);
    this.actionExecutions = new ModelList();
  }

  getActionExecutions() {
    return this.actionExecutions.getModels();
  }

  getDirtyActionExecutions() {
    return ModelBase.getDirtyModels(this.getOwnActionExecutions());
  }

  getOwnActionExecutions() {
    return this.getOwnModels(this.getActionExecutions());
  }

  getActionExecution(actionExecutionId) {
    return this.actionExecutions.getModel(actionExecutionId);
  }

  addActionExecution(actionExecution) {
    if (actionExecution instanceof ModelActionExecution) {
      this.actionExecutions.insert(actionExecution);
    }
    return this;
  }

  getInherited() {
    return this.inherited;
  }

  setInherited(inherited) {
    this.inherited = inherited;
  }

  copyFrom(src) {
    super.copyFrom(src);
    this.actionExecutions.copyFrom(src.actionExecutions);
    this.setInherited(src.getInherited());
    return this;
  }

  isValid() {
    return super.isValid() && ModelBase.areModelsValid(this.getDirtyActionExecutions());
  }

  isDirty() {
    return super.isDirty() || ModelBase.areModelsDirty(this.getOwnActionExecutions()) || ModelBase.areModelsDirty(this.getAttributes());

  }

  isOwningModels() {
    return super.isOwningModels() || !!this.getOwnActionExecutions().length;
  }
}