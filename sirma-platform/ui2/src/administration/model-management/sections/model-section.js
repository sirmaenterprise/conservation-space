import {Configurable} from 'components/configurable';

import {ModelValidateAttributesAction} from 'administration/model-management/actions/state/model-validate-attributes-action';
import {ModelValidateAttributeAction} from 'administration/model-management/actions/state/model-validate-attribute-action';
import {ModelChangeAttributeAction} from 'administration/model-management/actions/change/model-change-attribute-action';

import {ModelManagementUtility} from 'administration/model-management/utility/model-management-utility';
import _ from 'lodash';

/**
 * Common base class representing a single model section used to edit, save or preview model data.
 * This class defines several methods which must be overridden by every class which extends from it.
 * Such Methods are for example methods related to saving or reverting changes made to the model
 * inside the section, a boolean method which defines if a section is dirty or not.
 *
 * This generic section internally works with component events. When a subclass extends from this
 * section it can provide various component events which if present are going to be utilized and
 * triggered, if a given event is not present it won't be triggered or utilized. Supported events
 * are the following:
 *
 * - onModelSave - triggered when a save operation is executed, the payload of this events is collection of models to save
 * - onModelAttributeChange - triggered every time an attribute's value is changed, the payload is the attribute itself
 * - onSectionStateChange - triggered when the entire section transitions from dirty to not dirty state or back to dirty, payload is the state
 * - onModelStateChange - triggered when a model transitions from dirty to not dirty state or back to dirty, payload is the model and it's state
 *
 * @author Svetlozar Iliev
 */
export class ModelSection extends Configurable {

  constructor(config) {
    super(config || {});
    this.modelStates = {};
    this.modelSaving = false;

    // create a list of mandatory functions to override
    this.ensureFunctionOverride('getSectionModels');

    // attach the specific validation policies bound to the given actions for the generic section
    this.insertValidation(ModelChangeAttributeAction.getType(), ModelValidateAttributeAction);
  }

  // Core methods for working with the model section

  isSectionDirty() {
    return this.isSectionDirtyForModels(this.getPreparedModels());
  }

  isSaveAllowed() {
    return this.isSaveAllowedForModels(this.getPreparedModels());
  }

  saveAllChanges() {
    return this.saveChangesForModels(this.getPreparedModels());
  }

  revertAllChanges() {
    return this.revertChangesForModels(this.getPreparedModels());
  }

  afterModelChange() {
    this.notifyForSectionStateChange();
  }

  // Utility methods for working with the model section

  saveChangesForModels(models) {
    let saved = this.notifyForModelsSave(models);
    let processor = m => this.processActions(m, true, false);
    return saved ? saved.finally(() => processor(models)) : processor(models);
  }

  revertChangesForModels(models) {
    let reverted = this.notifyForModelsCancel(models);
    let processor = m => this.processActions(m, true, true);
    return reverted ? reverted.finally(() => processor(models)) : processor(models);
  }

  isSaveAllowedForModels(models) {
    return this.isSectionDirtyForModels(models) && models.every(model => this.isModelValid(model));
  }

  isSaveAllowedForModel(model) {
    return model && this.isSectionDirtyForModel(model) && this.isModelValid(model);
  }

  isSectionDirtyForModels(models) {
    return models.some(model => this.isSectionDirtyForModel(model));
  }

  isSectionDirtyForModel(model) {
    return model && !this.isSectionSaving() && !!this.getModelState(model).isDirty;
  }

  isModelValid(model) {
    return model.isValid();
  }

  isModelDirty(model, target = null) {
    // if the context has any actions associated with it
    let actions = model && this.extractActions(model);

    if (target && actions) {
      // if target is specified then check the dirty state only for it
      // if it is is present either as target or context of an action
      return actions.some(a =>
        ModelManagementUtility.isModelsEqualById(a.getModel(), target) ||
        ModelManagementUtility.isModelsEqualById(a.getContext(), target));
    }
    return actions && actions.length > 0;
  }

  // Internally used methods for controlling the model section

  /**
   * Method notifying when a save operation is requested. The save operation requires a
   * number of models to be provided as a payload to the method. These models are those
   * that are required to be saved, usually stored or cached beforehand inside the section.
   *
   * @param models - the array of models to be saved during the execution
   * @param resolve - callback executed when the save is resolved, optional
   * @param reject - callback executed when the save is rejected, optional
   */
  notifyForModelsSave(models, resolve, reject) {
    let actions = [];
    models.forEach(model => actions = actions.concat(this.extractActions(model)));

    if (actions && actions.length && this.onModelSave) {
      this.enterSavingState();
      return this.onSavePerform(actions)
        .then(res => {
          resolve && resolve(res);
          this.onSaveResolve(actions);
        })
        .catch(err => {
          reject && reject(err);
          this.onSaveReject(actions);
          throw err;
        })
        .finally(() => {
          this.exitSavingState();
        });
    }
  }

  /**
   * Method notifying when a cancel operation is performed on the section. The cancel operation
   * carries an array of models for which the cancel is performed. Usually these models are the
   * models provided and supported by the section. See {@link ModelSection#getSectionModels}
   *
   * @param models - models that are canceled, or requested canceling through the section
   */
  notifyForModelsCancel(models) {
    if (models && this.onModelCancel) {
      // try to notify models cancelation
      return this.onModelCancel({models});
    }
  }

  /**
   * Method notifying when a model load is required. This method performs model loading such that
   * if the requested model is not already loaded it will be prepared and loaded thus completely
   * ready for use.
   *
   * @param model - the model for which to perform the loading
   * @param navigate - flag which determines if after the load section requires navigation to this model
   */
  notifyForModelLoaded(model, navigate = true) {
    if (model && this.onModelLoad) {
      // try to notify model navigation
      return this.onModelLoad({model, navigate});
    }
  }

  /**
   * Method providing a public entry point to notify the section state has been changed. A change in the
   * state section can occur if one or more models owned and managed by this section is changed or it's
   * state is modified in any way.
   */
  notifyForSectionStateChange() {
    // fetch current section dirty state
    let isDirty = this.isSectionDirty();
    if (isDirty !== this.isDirty) {
      this.isDirty = isDirty;
      this.onSectionStateChange && this.onSectionStateChange({isDirty});
    }
  }

  /**
   * Method providing a public entry point to notify that a given model requires a state re-compute. The
   * state re-compute is usually required when a model is changed or the present context is also changed.
   *
   * @param model - the model for which to recompute the state
   * @param context - the context in which the model is currently present
   */
  notifyForModelStateCalculation(model, context) {
    if (model && context) {
      this.notifyForModelActionCreateAndExecute(ModelValidateAttributesAction, model, context);
    }
  }

  /**
   * Method providing a public entry point to notify the section that a given attribute has been changed
   * for a given context. The context is usually a model of some base owning type such as a class or a
   * definition model. Upon processing the method returns the resulting attribute which can differ from
   * the one provided as argument or can be the same. The attribute can differ from the one provided in
   * case the attribute was previously inherited, inherited attributes are such that are not owned by
   * the provided context as argument to this method.
   *
   * @param attribute - the currently attribute being changed
   * @param context - the context in which the attribute is being changed
   */
  notifyForModelAttributeChange(attribute, context) {
    if (attribute && context) {
      if (context === ModelManagementUtility.getOwningModel(attribute) && !this.isAttributeDirty(attribute, context)) {
        // attribute is not dirty so we can restore any changes made to it
        attribute = this.computeModelAttributeRestore(attribute, context);
      } else {
        // attribute is dirty in someway so we can process those changes
        attribute = this.computeModelAttributeModify(attribute, context);
      }
      this.notifyForModelStateChange(context);
    }
    this.notifyForSectionStateChange();
    return attribute;
  }

  /**
   * Method providing a public entry point to notify that a given model's state has been changed. That
   * is evaluated by checking the dirty state of the model. A dirty state for a given model is such
   * that a number of actions have been performed or executed upon that model.
   *
   * @param model - the model for which to notify that a state change might have occurred
   */
  notifyForModelStateChange(model) {
    // fetch the current model dirty state
    let isDirty = this.isModelDirty(model);
    if (this.getModelState(model).isDirty !== isDirty) {
      this.getModelState(model).isDirty = isDirty;
      this.onModelStateChange && this.onModelStateChange({model, isDirty});
    }
  }

  /**
   * Notifies for action creation and execution type of action and number arguments to be used
   * to create the given action type. As a result this method returns the created action and
   * not the result produced due to the action execution.
   *
   * @param type - the type of the action extending off of {@link ModelAction}
   * @param args - arguments to be passed during action creation
   * @returns {ModelAction} - instance of the given model action type
   */
  notifyForModelActionCreateAndExecute(type, ...args) {
    let action = this.notifyForModelActionCreate(type, ...args);
    this.notifyForModelActionExecute([action]);
    return action;
  }

  /**
   * Notifies for action creation for a given type of action and number arguments to be used
   * to create the given action type.
   *
   * @param type - the type of the action extending off of {@link ModelAction}
   * @param args - arguments to be passed during action creation
   * @returns {ModelAction} - instance of the given model action type
   */
  notifyForModelActionCreate(type, ...args) {
    if (type && this.onModelActionCreateRequest) {
      return this.onModelActionCreateRequest({type, args});
    }
  }

  /**
   * Notifies for actions execution. An array of actions is required to be passed as
   * first argument to this method, while in return the method returns as result an
   * array of results each of each corresponds to the respective actions being executed.
   *
   * Additionally the validation mapping processor is executed. For each action mapped for
   * validation a validation
   *
   * @param actions - the array of actions to be executed
   * @returns {array} - array of results corresponding to each action in the array.
   */
  notifyForModelActionExecute(actions) {
    if (actions && this.onModelActionExecuteRequest) {
      let results = this.onModelActionExecuteRequest({actions});
      actions = this.getValidationsActionsPolicies(actions, results);
      results = results.concat(this.onModelActionExecuteRequest({actions}));
      return results.filter(r => !_.isUndefined(r) && !_.isNull(r));
    }
  }

  /**
   * Notifies for actions reversion. An array of actions is required to be passed as
   * first argument to this method, while in return the method returns as result an
   * array of results each of each corresponds to the respective actions being reverted.
   *
   * @param actions - the array of actions to be reverted
   * @returns {array} - array of results corresponding to each action in the array.
   */
  notifyForModelActionRevert(actions) {
    if (actions && this.onModelActionRevertRequest) {
      let results = this.onModelActionRevertRequest({actions});
      actions = this.getValidationsActionsPolicies(actions, results);
      results = results.concat(this.onModelActionRevertRequest({actions}));
      return results.filter(r => !_.isUndefined(r) && !_.isNull(r));
    }
  }

  computeModelAttributeModify(attribute, context) {
    // extract existing action of the same type for the same context and target attribute
    let existing = this.getModelAction(context, ModelChangeAttributeAction, attribute);
    let action = this.notifyForModelActionCreate(ModelChangeAttributeAction, attribute, context);
    attribute = this.notifyForModelActionExecute([action])[0];

    // avoid add consecutive change actions
    if (!existing || action.isInherited()) {
      this.insertAction(context, action);
    }
    return attribute;
  }

  computeModelAttributeRestore(attribute, context) {
    let actions = this.extractActions(context);

    if (!actions.length) {
      return attribute;
    }
    let generator = a => this.getModelActionId(context, a, a.getModel());
    let id = this.getModelActionId(context, ModelChangeAttributeAction, attribute);

    let result = [], action = null;
    for (let i = actions.length - 1; i >= 0; --i) {
      let current = actions[i];
      if (generator(current) !== id) {
        result.push(current);
      } else {
        action = current;
        if (current.isInherited()) {
          result = actions.slice(0, i).concat(result);
          break;
        }
      }
    }
    this.assignActions(context, result);
    if (action) {
      attribute = this.notifyForModelActionRevert([action])[0];
    }
    return attribute;
  }

  isAttributeDirty(attribute, context) {
    let action = this.getModelAction(context, ModelChangeAttributeAction, attribute);
    return action && action.isInherited() ? attribute.isDirtyComparedToReference() : attribute.isDirty();
  }

  isSectionSaving() {
    return this.modelSaving;
  }

  insertValidation(action, type) {
    if (!this.validationMapping) {
      this.validationMapping = {};
    }
    this.validationMapping[action] = type;
    return type;
  }

  insertAction(context, action) {
    if (!this.getModelState(context).actions) {
      this.getModelState(context).actions = [];
    }
    this.extractActions(context).push(action);
    this.notifyForModelStateChange(context);
    this.notifyForSectionStateChange();
    return action;
  }

  removeAction(context, type, model) {
    if (!this.getModelState(context).actions) {
      return;
    }
    let actions = this.extractActions(context);
    let action = this.getModelAction(context, type, model);
    action && actions.splice(actions.indexOf(action), 1);
    this.notifyForModelStateChange(context);
    this.notifyForSectionStateChange();
    return action;
  }

  processActions(models, clear, revert) {
    models.forEach(model => {
      revert && this.revertActions(model);
      clear && this.clearActions(model);
    });
    this.notifyForSectionStateChange();
  }

  clearActions(context) {
    this.assignActions(context, []);
    this.notifyForModelStateChange(context);
  }

  revertActions(context) {
    let toRevert = this.extractActions(context).reverse();
    this.notifyForModelActionRevert(toRevert);
  }

  assignActions(context, actions) {
    context ? this.getModelState(context).actions = actions : null;
  }

  extractActions(context) {
    return context ? this.getModelState(context).actions || [] : [];
  }

  filterActions(actions, type) {
    return type ? actions.filter(a => a.getType() === type.getType()) : actions;
  }

  getModelAction(context, type, model, actions) {
    actions = actions || this.extractActions(context);
    let id = this.getModelActionId(context, type, model);
    let generator = a => this.getModelActionId(context, a, a.getModel());
    let index = _.findLastIndex(actions, a => generator(a) === id);
    return index >= 0 ? actions[index] : null;
  }

  getModelActionId(context, type, model) {
    let cond = m => ModelManagementUtility.isOwningType(m.getParent());
    let suffix = ModelManagementUtility.getUniqueIdentifier(model, cond);
    return `${type.getType()}/${context.getId()}/${suffix}`;
  }

  getModelState(model) {
    if (!this.hasModelState(model)) {
      this.modelStates[model.getId()] = {};
    }
    return this.modelStates[model.getId()];
  }

  getValidationsActionsPolicies(actions, results) {
    let final = [];
    if (this.validationMapping) {
      for (let i = 0; i < actions.length; ++i) {
        let action = actions[i], result = results[i];
        let type = this.validationMapping[action.getType()];
        if (result && type) {
          let args = [type, result, action.getContext()];
          final.push(this.notifyForModelActionCreate(...args));
        }
      }
    }
    return final;
  }

  hasModelState(model) {
    return !!this.modelStates[model.getId()];
  }

  getActualModel(model) {
    // extract the actual base Class or Definition model
    return ModelManagementUtility.getOwningModel(model);
  }

  getPreparedModels() {
    let models = this.getSectionModels();
    // make sure models is delivered as an array
    return Array.isArray(models) ? models : [models];
  }

  enterSavingState() {
    this.modelSaving = true;
    return this;
  }

  exitSavingState() {
    this.modelSaving = false;
    return this;
  }

  onSavePerform(actions) {
    return this.onModelSave({actions});
  }

  onSaveResolve(actions) {
    // reset the dirty state of all modified attributes saved as actions
    let filtered = this.filterActions(actions, ModelChangeAttributeAction);
    filtered.forEach(action => action.getModel().setDirty(false));
  }

  onSaveReject(actions) {
    // marker method left empty for now
  }

  ensureFunctionOverride(functionName) {
    if (typeof this[functionName] !== 'function') {
      throw new Error(`Must override ${functionName} function!`);
    }
  }
}