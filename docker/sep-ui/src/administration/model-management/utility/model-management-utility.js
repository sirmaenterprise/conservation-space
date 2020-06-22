import {ModelClass} from 'administration/model-management/model/model-class';
import {ModelDefinition} from 'administration/model-management/model/model-definition';
import {ModelField} from 'administration/model-management/model/model-field';
import {ModelControl} from 'administration/model-management/model/model-control';
import {ModelControlParam} from 'administration/model-management/model/model-control-param';
import {ModelRegion} from 'administration/model-management/model/model-region';
import {ModelAction} from 'administration/model-management/model/model-action';
import {ModelActionGroup} from 'administration/model-management/model/model-action-group';
import {ModelActionExecution} from 'administration/model-management/model/model-action-execution';
import {ModelAttribute} from 'administration/model-management/model/attributes/model-attribute';
import {ModelProperty} from 'administration/model-management/model/model-property';
import {ModelHeader} from 'administration/model-management/model/model-header';

import {getClassName} from 'app/app';
import _ from 'lodash';

/**
 * Provides a common entry point utility methods for the models API
 *
 * @author Svetlozar Iliev
 */
export class ModelManagementUtility {

  /**
   * Appends a given model to another model based on the type of the model to add. By default
   * the method uses a default appender which resolves the types to be added based on the model
   * API, in case a custom appending is required then a custom appender should be provided.
   *
   * @param model - the destination model for which to append
   * @param toAdd - model to add to the destination model
   * @param appender - map which resolves how to append different types of models.
   */
  static addToModel(model, toAdd, appender = ModelManagementUtility.DEFAULT_APPENDER) {
    appender[ModelManagementUtility.getModelType(toAdd)](model, toAdd);
  }

  /**
   * Retrieves the class name of an instance
   *
   * @param obj - instance for which to retrieve the class name
   */
  static getClassName(obj) {
    return getClassName(obj.constructor);
  }

  /**
   * Retrieves the super (parent) class name of an instance
   *
   * @param obj - instance for which to retrieve the parent class name
   */
  static getSuperClassName(obj) {
    return ModelManagementUtility.getClassName(Object.getPrototypeOf(Object.getPrototypeOf(obj)));
  }

  /**
   * Retrieves the type of a model instance. The different types are defined in {@link ModelManagementUtility.TYPES}
   * These types define all basic supported model types throughout the model management API.
   *
   * @param model - instance for which to retrieve the class name
   */
  static getModelType(model) {
    let concreteType = ModelManagementUtility.getClassName(model);
    let superType = ModelManagementUtility.getSuperClassName(model);
    return ModelManagementUtility.TYPE_MAPPER[concreteType] || ModelManagementUtility.TYPE_MAPPER[superType];
  }

  /**
   * Retrieves a unique identifier for a given model. By default this method works by traversing the owning
   * branch of the provided model until a base model type is met. A custom condition can be provided
   * which can be used to precisely stop the traversal at a given node in the owning branch
   *
   * @param model - model for which to compute the unique identifier
   * @param condition - custom condition which when resolved with true would stop traversal
   * @returns {string} - the unique identifier for that model
   */
  static getUniqueIdentifier(model, condition) {
    if (!model) {
      return;
    }
    let current = model.getId();
    // recurse in depth until the condition to stop the recursion is met
    let shouldStop = condition ? condition(model) : this.isBaseType(model);

    if (!shouldStop) {
      let next = this.getUniqueIdentifier(model.getParent(), condition);
      return next ? `${next}/` + current : current;
    }
    return current;
  }

  /**
   * Retrieves the owning model of a given model. By default this method works by traversing the owning
   * branch of the provided model until an owning model type is met. A custom condition can be provided
   * which can be used to precisely stop the traversal at a given node in the owning branch
   *
   * @param model - model for which to compute the owning model
   * @param condition - custom condition which when resolved with true would stop traversal
   * @returns {@link ModelBase} - the owning model
   */
  static getOwningModel(model, condition) {
    if (!model) {
      return;
    }

    // recurse in depth until the condition to stop the recursion is met
    let shouldStop = condition ? condition(model) : this.isOwningType(model);
    return !shouldStop ? this.getOwningModel(model.getParent(), condition) : model;
  }

  /**
   * Extracts the actual string representing the name of a given model from it's description.
   *
   * @param model - the model for which to extract the name
   * @returns {string} - the name of the given model
   */
  static getModelName(model) {
    return this.getModelValue(model.getDescription());
  }

  /**
   * Extracts the actual string representing the identifier of a given model from it's contents.
   *
   * @param model - the model for which to extract the id
   * @returns {string} - the id of the given model
   */
  static getModelIdentifier(model) {
    return this.getModelValue(model.getId());
  }

  /**
   * Resolves the type of a given action which can be either an instance of {@link ModelAction}
   * or a plain type extending off of {@link ModelAction}
   *
   * @param action - instance of {@link ModelAction} or a plain type
   * @returns {string} - the type of action as a string representation
   */
  static getActionType(action) {
    return _.isFunction(action) ? this.getClassName(action.prototype) : this.getClassName(action);
  }

  /**
   * Resolves the id of the passed model.
   * @param model the model to extract the id from
   * @returns {String} the model id
   * @throws TypeError when the model does not have getter method for the id or it is not a string
   */
  static getModelId(model) {
    if (_.isFunction(model.getId)) {
      return model.getId();
    } else if (_.isString(model)) {
      return model;
    }
    throw new TypeError('Invalid model format passed');
  }

  static getModelValue(model) {
    // handle proper model value case or do a fallback
    return model.getValue ? model.getValue() : model;
  }

  static isInherited(model, owner) {
    return model.getParent() !== owner;
  }

  static isBaseType(model) {
    // check if a given model is owning type or a semantic property
    return this.isOwningType(model) || this.isModelProperty(model);
  }

  static isOwningType(model) {
    // owning types are such that are topmost owners of any model
    return this.isModelClass(model) || this.isModelDefinition(model);
  }

  static isModelClass(model) {
    return model instanceof ModelClass;
  }

  static isModelDefinition(model) {
    return model instanceof ModelDefinition;
  }

  static isModelProperty(model) {
    return model instanceof ModelProperty;
  }

  static isModelAttribute(model) {
    return model instanceof ModelAttribute;
  }

  static walk(path, model, walker = ModelManagementUtility.DEFAULT_WALKER) {
    return path && path.walk(model, walker);
  }

  static isAttributeEmpty(attribute) {
    return !attribute || attribute.isEmpty();
  }

  static isModelsEqualById(left, right) {
    return this.getModelType(left) === this.getModelType(right) && left.getId() === right.getId();
  }
}

ModelManagementUtility.TYPES = {
  CLASS: 'class',
  DEFINITION: 'definition',
  FIELD: 'field',
  CONTROL: 'control',
  CONTROL_PARAM: 'controlParam',
  REGION: 'region',
  PROPERTY: 'property',
  HEADER: 'header',
  ATTRIBUTE: 'attribute',
  ACTION: 'action',
  ACTION_GROUP: 'actionGroup',
  ACTION_EXECUTION: 'actionExecution'
};

ModelManagementUtility.TYPE_MAPPER = {};
ModelManagementUtility.TYPE_MAPPER[getClassName(ModelClass)] = ModelManagementUtility.TYPES.CLASS;
ModelManagementUtility.TYPE_MAPPER[getClassName(ModelDefinition)] = ModelManagementUtility.TYPES.DEFINITION;

ModelManagementUtility.TYPE_MAPPER[getClassName(ModelField)] = ModelManagementUtility.TYPES.FIELD;
ModelManagementUtility.TYPE_MAPPER[getClassName(ModelControl)] = ModelManagementUtility.TYPES.CONTROL;
ModelManagementUtility.TYPE_MAPPER[getClassName(ModelControlParam)] = ModelManagementUtility.TYPES.CONTROL_PARAM;
ModelManagementUtility.TYPE_MAPPER[getClassName(ModelRegion)] = ModelManagementUtility.TYPES.REGION;
ModelManagementUtility.TYPE_MAPPER[getClassName(ModelProperty)] = ModelManagementUtility.TYPES.PROPERTY;
ModelManagementUtility.TYPE_MAPPER[getClassName(ModelHeader)] = ModelManagementUtility.TYPES.HEADER;
ModelManagementUtility.TYPE_MAPPER[getClassName(ModelAttribute)] = ModelManagementUtility.TYPES.ATTRIBUTE;
ModelManagementUtility.TYPE_MAPPER[getClassName(ModelAction)] = ModelManagementUtility.TYPES.ACTION;
ModelManagementUtility.TYPE_MAPPER[getClassName(ModelActionGroup)] = ModelManagementUtility.TYPES.ACTION_GROUP;
ModelManagementUtility.TYPE_MAPPER[getClassName(ModelActionExecution)] = ModelManagementUtility.TYPES.ACTION_EXECUTION;

ModelManagementUtility.DEFAULT_WALKER = {};
ModelManagementUtility.DEFAULT_WALKER[ModelManagementUtility.TYPES.CLASS] = (model, id) => model.getClass(id);
ModelManagementUtility.DEFAULT_WALKER[ModelManagementUtility.TYPES.DEFINITION] = (model, id) => model.getDefinition(id);

ModelManagementUtility.DEFAULT_WALKER[ModelManagementUtility.TYPES.FIELD] = (model, id) => model.getField(id);
ModelManagementUtility.DEFAULT_WALKER[ModelManagementUtility.TYPES.CONTROL] = (model, id) => model.getControl(id);
ModelManagementUtility.DEFAULT_WALKER[ModelManagementUtility.TYPES.CONTROL_PARAM] = (model, id) => model.getControlParam(id);
ModelManagementUtility.DEFAULT_WALKER[ModelManagementUtility.TYPES.REGION] = (model, id) => model.getRegion(id);
ModelManagementUtility.DEFAULT_WALKER[ModelManagementUtility.TYPES.PROPERTY] = (model, id) => model.getProperty(id);
ModelManagementUtility.DEFAULT_WALKER[ModelManagementUtility.TYPES.HEADER] = (model, id) => model.getHeader(id);
ModelManagementUtility.DEFAULT_WALKER[ModelManagementUtility.TYPES.ATTRIBUTE] = (model, id) => model.getAttribute(id);
ModelManagementUtility.DEFAULT_WALKER[ModelManagementUtility.TYPES.ACTION] = (model, id) => model.getAction(id);
ModelManagementUtility.DEFAULT_WALKER[ModelManagementUtility.TYPES.ACTION_GROUP] = (model, id) => model.getActionGroup(id);
ModelManagementUtility.DEFAULT_WALKER[ModelManagementUtility.TYPES.ACTION_EXECUTION] = (model, id) => model.getActionExecution(id);

ModelManagementUtility.DEFAULT_APPENDER = {};
ModelManagementUtility.DEFAULT_APPENDER[ModelManagementUtility.TYPES.CLASS] = (model, toAdd) => model.addClass(toAdd);
ModelManagementUtility.DEFAULT_APPENDER[ModelManagementUtility.TYPES.DEFINITION] = (model, toAdd) => model.addDefinition(toAdd);

ModelManagementUtility.DEFAULT_APPENDER[ModelManagementUtility.TYPES.FIELD] = (model, toAdd) => model.addField(toAdd);
ModelManagementUtility.DEFAULT_APPENDER[ModelManagementUtility.TYPES.CONTROL] = (model, toAdd) => model.addControl(toAdd);
ModelManagementUtility.DEFAULT_APPENDER[ModelManagementUtility.TYPES.CONTROL_PARAM] = (model, toAdd) => model.addControlParam(toAdd);
ModelManagementUtility.DEFAULT_APPENDER[ModelManagementUtility.TYPES.REGION] = (model, toAdd) => model.addRegion(toAdd);
ModelManagementUtility.DEFAULT_APPENDER[ModelManagementUtility.TYPES.PROPERTY] = (model, toAdd) => model.addProperty(toAdd);
ModelManagementUtility.DEFAULT_APPENDER[ModelManagementUtility.TYPES.HEADER] = (model, toAdd) => model.addHeader(toAdd);
ModelManagementUtility.DEFAULT_APPENDER[ModelManagementUtility.TYPES.ATTRIBUTE] = (model, toAdd) => model.addAttribute(toAdd);
ModelManagementUtility.DEFAULT_APPENDER[ModelManagementUtility.TYPES.ACTION] = (model, toAdd) => model.addAction(toAdd);
ModelManagementUtility.DEFAULT_APPENDER[ModelManagementUtility.TYPES.ACTION_GROUP] = (model, toAdd) => model.addActionGroup(toAdd);
