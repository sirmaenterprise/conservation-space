import {Inject, Injectable} from 'app/app';
import {ModelPropertyLinker} from './model-property-linker';
import {ModelAttribute} from 'administration/model-management/model/attributes/model-attribute';

import {ModelManagementLanguageService} from 'administration/model-management/services/utility/model-management-language-service';
import {ModelActionExecutionTypes} from 'administration/model-management/model/model-action-execution';
import {ModelManagementUtility} from 'administration/model-management/utility/model-management-utility';

import _ from 'lodash';

/**
 * Service which performs any base or core linking between models. Currently this service links the inherited
 * models such as attributes, fields, regions for the hierarchy branch formed by the current model. The core
 * linker also takes care of linking model properties with their respective fields inside model definitions.
 * In case a model inside the hierarchy is changed it might be required to perform re-linking so changes take
 * effect in the entire hierarchy and affected models inside that branch.
 *
 * @author Svetlozar Iliev
 */
@Injectable()
@Inject(ModelPropertyLinker, ModelManagementLanguageService)
export class ModelDataLinker {

  constructor(modelPropertyLinker, modelManagementLanguageService) {
    this.modelPropertyLinker = modelPropertyLinker;
    this.modelManagementLanguageService = modelManagementLanguageService;
    this.defaultLanguage = this.modelManagementLanguageService.getDefaultLanguage();
  }

  /**
   * Perform linking of the provided model based on the type of the provided model.
   *
   * When a {@link ModelDefinition} model is provided all sub-models which are not present in the
   * currently passed model are inherited and propagated from the parents of the model.
   *
   * When a {@link ModelClass} model is provided semantic properties related to this model are
   * link and attached to the provided model. These properties are those that belong to the domain
   * of the provided model.
   *
   * @param model - the model to be linked and from which linking is performed upwards the hierarchy
   * @param properties - map of semantic properties used to link definition fields and other models.
   */
  linkInheritanceModel(model, properties) {
    let models = [model, ...model.getParents()].reverse();
    models.forEach(model => {
      if (ModelManagementUtility.isModelDefinition(model)) {
        let parent = model.getParent();

        // link inherited regions from the parent model to the current model
        parent && this.linkInheritedRegions(model, parent.getRegions());

        // link inherited fields from the parent model to the current model
        parent && this.linkInheritedFields(model, parent.getFields());

        // link inherited attributes from the parent model to the current model
        parent && this.linkInheritedAttributes(model, parent.getAttributes());

        // link inherited headers from the parent model to the current model
        parent && this.linkInheritedHeaders(model, parent.getHeaders());

        // link inherited actions and its attributes from the parent model to the current model
        parent && this.linkInheritedActions(model, parent.getActions());

        // link inherited action group and its attributes from the parent model to the current model
        parent && this.linkInheritedActionGroups(model, parent.getActionGroups());

        // link related definition models to semantic properties
        this.linkDefinitionWithProperties(model, properties);
      }
      else if (ModelManagementUtility.isModelClass(model)) {
        // link all related to this class semantic properties
        this.linkClassWithProperties(model, properties);
      }
    });

    // linked model
    return model;
  }

  linkDefinitionWithProperties(model, properties) {
    if (model && properties) {
      model.getFields().forEach(field => !field.getProperty() && this.linkPropertiesToField(field, properties));
    }
  }

  linkClassWithProperties(model, properties) {
    if (model && properties) {
      this.linkPropertiesToClass(model, properties);
      model.getParent() && model.getParent().getProperties().forEach(property => model.addProperty(property));
    }
  }

  linkPropertiesToField(field, properties) {
    this.modelPropertyLinker.linkPropertiesToFieldModel(field, properties);
  }

  linkPropertiesToClass(clazz, properties) {
    this.modelPropertyLinker.linkPropertiesToClassModel(clazz, properties);
  }

  linkInheritedFields(model, inheritedFields) {
    inheritedFields.forEach(inheritedField => {
      let field = this.addFieldIfNotPresentAndGet(model, inheritedField);
      this.linkInheritedAttributes(field, inheritedField.getAttributes());
      this.linkInheritedControls(field, inheritedField.getControls());
      this.linkModelReference(field, inheritedField);
    });
  }

  linkInheritedControls(model, inheritedControls) {
    // if any control is defined in child, do not inherit from parent!
    if (model.getControls().length === 0) {
      inheritedControls.forEach((inheritedControl) => {
        let control = this.addControlIfNotPresentAndGet(model, inheritedControl);
        this.linkModelReference(control, inheritedControl);
      });
    }
  }

  linkInheritedRegions(model, inheritedRegions) {
    inheritedRegions.forEach(inheritedRegion => {
      let region = this.addRegionIfNotPresentAndGet(model, inheritedRegion);
      this.linkInheritedAttributes(region, inheritedRegion.getAttributes());
      this.linkModelReference(region, inheritedRegion);
    });
  }

  linkInheritedAttributes(model, inheritedAttributes) {
    inheritedAttributes.forEach(inheritedAttribute => {
      let attribute = this.addAttributeIfNotPresentAndGet(model, inheritedAttribute);
      this.linkModelReference(attribute, inheritedAttribute);
    });
  }

  linkInheritedHeaders(model, inheritedHeaders) {
    inheritedHeaders.forEach(inheritedHeader => {
      let header = this.addHeaderIfNotPresentAndGet(model, inheritedHeader);
      this.linkModelReference(header, inheritedHeader);
    });
  }

  linkInheritedActions(model, inheritedActions) {
    inheritedActions.forEach((inheritedAction) => {
      let action = this.addActionIfNotPresentAndGet(model, inheritedAction);
      this.linkInheritedAttributes(action, inheritedAction.getAttributes());
      this.linkInheritedActionExecution(action, inheritedAction.getActionExecutions());
      this.linkModelReference(action, inheritedAction);
    });
  }

  linkInheritedActionExecution(model, inherited) {
    inherited.forEach((inheritedActionExecution) => {
      let actionExecution = this.addActionExecutionIfNotPresentAndGet(model, inheritedActionExecution);
      this.linkInheritedAttributes(actionExecution, inheritedActionExecution.getAttributes());
      this.linkModelReference(actionExecution, inheritedActionExecution);
    });
  }

  linkInheritedActionGroups(model, inherited) {
    inherited.forEach((inheritedActionGroup) => {
      let actionGroup = this.addActionGroupIfNotPresentAndGet(model, inheritedActionGroup);
      this.linkInheritedAttributes(actionGroup, inheritedActionGroup.getAttributes());
      this.linkModelReference(actionGroup, inheritedActionGroup);
    });
  }

  linkModelReference(concrete, parent) {
    // skip parent model not applicable
    if (!concrete || concrete === parent) {
      return;
    }
    // attach parent reference model
    concrete.setReference(parent);
  }

  addHeaderIfNotPresentAndGet(model, inheritedHeader) {
    let currentHeader = this.getHeader(model, inheritedHeader);

    if (this.isHeaderEmpty(currentHeader, model, inheritedHeader)) {
      model.addHeader(inheritedHeader);
      currentHeader = this.getHeader(model, inheritedHeader);
    }
    return currentHeader;
  }

  isHeaderEmpty(currentHeader, currentModel, inheritedHeader) {
    // Consider empty values as missing headers and inherit from parent.
    return !currentHeader || currentHeader.getParent() !== currentModel
      || (ModelManagementUtility.isAttributeEmpty(currentHeader.getLabelAttribute())
      && !ModelManagementUtility.isAttributeEmpty(inheritedHeader.getLabelAttribute()));
  }

  addRegionIfNotPresentAndGet(model, region) {
    let currentRegion = this.getRegion(model, region);

    if (!currentRegion || currentRegion.getParent() !== model) {
      model.addRegion(region);
      currentRegion = model.getRegion(region.getId());
    }
    return currentRegion;
  }

  addFieldIfNotPresentAndGet(model, field) {
    let currentField = this.getField(model, field);
    // fields are special case where owner is the model
    // inside which field is stored in this case a region
    let owner = ModelManagementUtility.getOwningModel(model);

    if (!currentField || currentField.getParent() !== owner) {
      model.addField(field);
      currentField = model.getField(field.getId());
    }
    return currentField;
  }

  addAttributeIfNotPresentAndGet(model, attribute) {
    let currentAttr = this.getAttribute(model, attribute);

    // meta data attributes or special attributes are unconditionally inherited
    if (!currentAttr || currentAttr.getParent() !== model ||
      this.isAlwaysInherited(model, currentAttr, attribute))
    {
      model.addAttribute(attribute);
      currentAttr = model.getAttribute(attribute.getId());
    }
    return currentAttr;
  }

  addControlIfNotPresentAndGet(model, control) {
    let currentControl = this.getControl(model, control);
    if (!currentControl || currentControl.getParent() !== model) {
      model.addControl(control);
      currentControl = model.getControl(control.getId());
    }
    return currentControl;
  }

  addActionIfNotPresentAndGet(model, action) {
    let currentAction = this.getAction(model, action);

    if (!currentAction || currentAction.getParent() !== model) {
      model.addAction(action);
      currentAction = model.getAction(action.getId());
    }
    return currentAction;
  }

  addActionExecutionIfNotPresentAndGet(model, actionExecution) {
    let currentActionExecution = this.getActionExecution(model, actionExecution);
    let type = actionExecution.getAttribute(ModelAttribute.TYPE_ATTRIBUTE).getValue().getValue();

    // An action can have 0 or 1 action execution of type createRelation so we check if such execution is already added.
    if (ModelActionExecutionTypes.MODEL_ACTION_EXECUTION_TYPE_CREATE_RELATION === type && this.hasCreateRelationActionExecution(model)) {
      return actionExecution;
    }

    if (!currentActionExecution || currentActionExecution.getParent() !== model) {
      model.addActionExecution(actionExecution);
      currentActionExecution = model.getActionExecution(actionExecution.getId());
    }
    return currentActionExecution;
  }

  hasCreateRelationActionExecution(model) {
    return _.find(model.getActionExecutions(), (actionExecution) => {
      return actionExecution.getAttribute(ModelAttribute.TYPE_ATTRIBUTE).getValue().getValue() === ModelActionExecutionTypes.MODEL_ACTION_EXECUTION_TYPE_CREATE_RELATION;
    });
  }

  addActionGroupIfNotPresentAndGet(model, actionGroup) {
    let currentActionGroup = this.getActionGroup(model, actionGroup);

    if (!currentActionGroup || currentActionGroup.getParent() !== model) {
      model.addActionGroup(actionGroup);
      currentActionGroup = model.getActionGroup(actionGroup.getId());
    }
    return currentActionGroup;
  }

  getHeader(model, header) {
    return model.getHeader(header.getId());
  }

  getAttribute(model, attribute) {
    return model.getAttribute(attribute.getId());
  }

  getRegion(model, region) {
    return model.getRegion(region.getId());
  }

  getField(model, field) {
    return model.getField(field.getId());
  }

  getControl(model, control) {
    return model.getControl(control.getId());
  }

  getAction(model, action) {
    return model.getAction(action.getId());
  }

  getActionExecution(model, actionExecution) {
    return model.getActionExecution(actionExecution.getId());
  }

  getActionGroup(model, actionGroup) {
    return model.getActionGroup(actionGroup.getId());
  }

  isAlwaysInherited(model, current, parent) {
    let type = model && ModelManagementUtility.getModelType(model);
    let targets = ModelDataLinker.INHERIT_ATTRIBUTES[type] || [];

    let source = current && current.getSource();
    let isMetaSource = source === ModelAttribute.SOURCE.META_DATA;
    let isTargetAttribute = targets.indexOf(parent.getId()) > -1;

    return isMetaSource || isTargetAttribute;
  }
}

ModelDataLinker.INHERIT_ATTRIBUTES = {
  // both name and uri attributes of the field should be the same for a field defined by the same id
  // therefore it's better to directly inherit both of them from the topmost possible parent this way.
  [ModelManagementUtility.TYPES.FIELD]: [ModelAttribute.NAME_ATTRIBUTE, ModelAttribute.URI_ATTRIBUTE]
};