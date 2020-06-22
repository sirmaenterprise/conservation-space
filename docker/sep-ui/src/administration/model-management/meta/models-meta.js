import {ModelMetaData} from 'administration/model-management/meta/model-meta';
import {ModelActionExecutionMetaData} from 'administration/model-management/meta/model-action-execution-meta';

import {ModelList} from 'administration/model-management/model/model-list';

/**
 * Provides a description of a meta data which is used as a reference point.
 * Meta data is not associated with any given model it currently supports a
 * collection of meta data for semantic and definition attributes. Models
 * Meta data also contains a collection of meta data for semantic properties
 *
 * @author Svetlozar Iliev
 */
export class ModelsMetaData {

  constructor() {
    this.fields = new ModelList();
    this.regions = new ModelList();
    this.controls = new ModelList();
    this.controlParams = new ModelList();
    this.headers = new ModelList();
    this.semantics = new ModelList();
    this.properties = new ModelList();
    this.definitions = new ModelList();
    this.actionGroups = new ModelList();
    this.actions = new ModelList();
    this.actionExecutions = new ModelList();
  }

  addSemantic(semanticMetaData) {
    if (semanticMetaData instanceof ModelMetaData) {
      this.semantics.insert(semanticMetaData);
    }
    return this;
  }

  addDefinition(definitionMetaData) {
    if (definitionMetaData instanceof ModelMetaData) {
      this.definitions.insert(definitionMetaData);
    }
    return this;
  }

  addProperty(propertyMetaData) {
    if (propertyMetaData instanceof ModelMetaData) {
      this.properties.insert(propertyMetaData);
    }
    return this;
  }

  addField(fieldMetaData) {
    if (fieldMetaData instanceof ModelMetaData) {
      this.fields.insert(fieldMetaData);
    }
    return this;
  }

  addRegion(regionMetaData) {
    if (regionMetaData instanceof ModelMetaData) {
      this.regions.insert(regionMetaData);
    }
    return this;
  }

  addControl(controlMetaData) {
    if (controlMetaData instanceof ModelMetaData) {
      this.controls.insert(controlMetaData);
    }
    return this;
  }

  addControlParam(controlParamMetaData) {
    if (controlParamMetaData instanceof ModelMetaData) {
      this.controlParams.insert(controlParamMetaData);
    }
    return this;
  }

  addActionGroup(actionGroupMetaData) {
    if (actionGroupMetaData instanceof ModelMetaData) {
      this.actionGroups.insert(actionGroupMetaData);
    }
    return this;
  }

  addAction(actionMetaData) {
    if (actionMetaData instanceof ModelMetaData) {
      this.actions.insert(actionMetaData);
    }
    return this;
  }

  addActionExecution(actionExecutionMetaData) {
    if (actionExecutionMetaData instanceof ModelActionExecutionMetaData) {
      this.actionExecutions.insert(actionExecutionMetaData);
    }
    return this;
  }

  addHeader(headerMetaData) {
    if (headerMetaData instanceof ModelMetaData) {
      this.headers.insert(headerMetaData);
    }
    return this;
  }

  getSemantics() {
    return this.semantics;
  }

  getDefinitions() {
    return this.definitions;
  }

  getProperties() {
    return this.properties;
  }

  getFields() {
    return this.fields;
  }

  getRegions() {
    return this.regions;
  }

  getControls() {
    return this.controls;
  }

  getControlParams() {
    return this.controlParams;
  }

  getActionGroups() {
    return this.actionGroups;
  }

  getActions() {
    return this.actions;
  }

  getActionExecutions() {
    return this.actionExecutions;
  }

  getHeaders() {
    return this.headers;
  }

  seal() {
    this.fields.seal();
    this.regions.seal();
    this.controls.seal();
    this.controlParams.seal();
    this.headers.seal();
    this.semantics.seal();
    this.properties.seal();
    this.definitions.seal();
    this.actionGroups.seal();
    this.actions.seal();
    this.actionExecutions.seal();

    // Using Object.freeze() to avoid reassigning existing fields which Object.seal() won't prevent.
    Object.freeze(this);
  }
}