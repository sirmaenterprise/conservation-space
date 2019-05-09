import {Injectable} from 'app/app';
import {ModelAttribute} from 'administration/model-management/model/attributes/model-attribute';
import {ModelAttributeTypes} from 'administration/model-management/model/attributes/model-attribute-types';
import {ModelValue} from 'administration/model-management/model/model-value';
import {ModelManagementUtility} from 'administration/model-management/utility/model-management-utility';
import {ModelActionGroup} from 'administration/model-management/model/model-action-group';

const ACTION_TREE_ID_PREFIX = 'action_';
const ACTION_GROUP_TREE_ID_PREFIX = 'actionGroup_';

/**
 * Represents a class that actually does the work on building the action tree hierarchy.
 * Because of their semantic similarity, the action and the groups can be treated in a similar way,
 * with very minimal differences. Therefore, the processing methods constituting the tree hierarchy are reused,
 * with minimum checks being carried out on the type of the processed element.
 * The finished hierarchy object is submitted back for visualization {@see ModelActionsTree}.
 *
 * @author T. Dossev
 */
@Injectable()
export class ModelActionsTreeService {

  buildHierarchy(model, rootId, rootText) {
    let hierarchy = [];
    if (model.actions) {
      let actionGroupNodes = this.convertToActionGroupNodes(model.getActionGroups(), model.getId());
      let actionNodes = this.convertToActionNodes(model.getActions(), model.getId());
      let actionGroupsMapping = this.buildActionGroupsMapping(actionGroupNodes);
      this.populateParentHierarchy(hierarchy, actionGroupsMapping, actionGroupNodes);
      this.populateParentHierarchy(hierarchy, actionGroupsMapping, actionNodes);
    }
    if (rootId) {
      let text = rootText || rootId;
      let root = ModelActionsTreeService.createRootNode(rootId, text);
      hierarchy.forEach(child => root.insertChild(child));
      return root;
    }
    return hierarchy;
  }

  convertToActionNodes(actions, modelContextId) {
    return actions.map((action) => {
      let parentId = action.getAttribute(ModelAttribute.GROUP_ATTRIBUTE).getValue().getValue();
      return new ModelActionHierarchy(action, 'action-link', undefined, modelContextId).setParentId(parentId);
    });
  }

  convertToActionGroupNodes(actionGroups, modelContextId) {
    return actionGroups.map((actionGroup) => {
      let parentId = actionGroup.getAttribute(ModelAttribute.PARENT_ATTRIBUTE).getValue().getValue();
      return new ModelActionGroupHierarchy(actionGroup, 'action-group-link', true, modelContextId).setParentId(parentId);
    });
  }

  buildActionGroupsMapping(actionGroupNodes) {
    let actonGroupsMap = new Map();
    actionGroupNodes.forEach(actionGroupNode => {
      actonGroupsMap.set(actionGroupNode.getId(), actionGroupNode);
    });
    return actonGroupsMap;
  }

  populateParentHierarchy(hierarchy, actionGroupsMapping, actionOrGroupNodes) {
    actionOrGroupNodes.forEach((elementNode) => {
      let parentId = elementNode.getParentId();
      let parentNode = actionGroupsMapping.get(parentId);
      if (parentNode) {
        parentNode.insertChild(elementNode);
      } else {
        hierarchy.push(elementNode);
      }
    });
  }

  static createRootNode(id, text) {
    let actionGroupModel = new ModelActionGroup(id);
    let actionsRootOrderValue = new ModelValue('en', 0);
    let actionsRootAttribute = new ModelAttribute(ModelAttribute.ORDER_ATTRIBUTE, ModelAttributeTypes.MODEL_INTEGER_TYPE, actionsRootOrderValue);
    actionGroupModel.addAttribute(actionsRootAttribute);

    let actionsRoot = new ActionAndGroupHierarchy(actionGroupModel, 'action-group-link');
    actionsRoot.setText(text);
    return actionsRoot;
  }

  static getDescription(model) {
    let label = model.getAttributeByType(ModelAttributeTypes.MULTI_VALUE.MODEL_LABEL_TYPE);
    let value = label && label.getValue();
    // provide the value of the label as description when it's not empty or is made dirty
    return value && !value.isEmpty() ? value : model.description;
  }

  static getText(model, context, isNodeDirty) {
    let parent = model.getParent();
    let modelDescription = ModelActionsTreeService.getDescription(model) && ModelActionsTreeService.getDescription(model).getValue() || '';
    let parentDescription = parent && parent.getDescription().getValue();

    let contextId = context && ModelManagementUtility.getModelId(context);
    let isInParentContext = parent && parent.getId() === contextId;
    let isInheritedDirty = model.isDirty() && ModelManagementUtility.isInherited(model, context) && !isNodeDirty;
    if (!isInParentContext && (!model.isDirty() && !isNodeDirty || isInheritedDirty)) {
      return `${modelDescription} <i class="action-parent">(${parentDescription})</i>`;
    }
    return modelDescription;
  }

  static getActionTreeId(modelId) {
    return ACTION_TREE_ID_PREFIX + modelId;
  }

  static getActionGroupTreeId(modelId) {
    return ACTION_GROUP_TREE_ID_PREFIX + modelId;
  }
}

export class ActionAndGroupHierarchy {

  constructor(model, cssClass, opened, modelContextId) {
    this.id = model.getId();
    this.text = ModelActionsTreeService.getText(model, modelContextId);
    this.children = [];
    let originalText = model.getOriginalDescription() && model.getOriginalDescription().getValue();
    this.data = {order: this.getOrder(model), parentId: model.parentId, id: model.getId(), originalText };

    if (opened) {
      this.state = {opened};
    }

    this.a_attr = this.a_attr || {};
    this.a_attr.id = this.getId();

    if (cssClass) {
      this.a_attr = {class: cssClass};
    }

    if (model.isDirty()) {
      this.type = 'dirty';
    }
  }

  getId() {
    return this.id;
  }

  getModelId() {
    return this.data.id;
  }

  setInherited(inherited) {
    this.inherited = inherited;
  }

  setIcon(icon) {
    this.icon = icon;
  }

  insertChild(child) {
    if (child instanceof ActionAndGroupHierarchy) {
      return this.children.push(child);
    }
  }

  getParentId() {
    return this.data.parentId;
  }

  setParentId(parentId) {
    if (parentId) {
      this.data.parentId = 'actionGroup_' + parentId;
    }
    return this;
  }

  getOrder(model) {
    return model.getAttribute(ModelAttribute.ORDER_ATTRIBUTE).getValue().getValue();
  }

  setText(text) {
    this.text = text;
  }

  setStyle(style) {
    this.a_attr = this.a_attr || {};
    this.a_attr.style = style;
    this.a_attr.id = this.getId();
  }

  hasParent(model) {
    return model.getView().showParent();
  }
}

export class ModelActionGroupHierarchy extends ActionAndGroupHierarchy {

  constructor(model, cssClass, opened, modelContextId) {
    super(model, cssClass, opened, modelContextId);
    // Actions and actionGroups are different models and there is scenario when an action and an actionGroup can have same id.
    // Such case will broke jsTree library, so to prevent it, we will use prefix "actionGroup_" when constructing
    // the id of a jsTree action group node.
    this.id = ModelActionsTreeService.getActionGroupTreeId(model.getId());
    this.data.isAction = false;
  }
}

export class ModelActionHierarchy extends ActionAndGroupHierarchy {

  constructor(model, cssClass, opened, modelContextId) {
    super(model, cssClass, opened, modelContextId);
    // Actions and actionGroups are different models and there is scenario when an action and an actionGroup can have same id.
    // Such case will broke jsTree library, so to prevent it, we will use prefix "action_" when constructing
    // the id of a jsTree action node.
    this.id = ModelActionsTreeService.getActionTreeId(model.getId());
    this.data.isAction = true;
  }

  insertChild(child) {
    // override default insertChildren. Actions can not have children
  }
}