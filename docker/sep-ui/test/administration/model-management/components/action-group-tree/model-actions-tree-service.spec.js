import {ModelActionsTreeService} from 'administration/model-management/components/action-group-tree/model-actions-tree-service';
import {ModelAction} from 'administration/model-management/model/model-action';
import {ModelActionGroup} from 'administration/model-management/model/model-action-group';
import {ModelDefinition} from 'administration/model-management/model/model-definition';
import {ModelAttribute} from 'administration/model-management/model/attributes/model-attribute';
import {ModelSingleAttribute} from 'administration/model-management/model/attributes/model-single-attribute';
import {ModelValue} from 'administration/model-management/model/model-value';


describe('ModelActionsTreeService', () => {

  let service;

  beforeEach(() => {
    service = new ModelActionsTreeService();
  });

  it('should create a tree without root group', () => {
    // GIVEN
    // there is a definition.
    let model = new ModelDefinition();
    // AND it has an action without parent.
    let actionWithoutParentGroup = createAction('actionWithoutParentGroup');
    model.addAction(actionWithoutParentGroup);
    // AND it has an action with parent.
    let actionWithParentGroup = createAction('actionWithParentGroup', 'groupId');
    model.addAction(actionWithParentGroup);
    // AND it has a group.
    let group = createActionGroup('groupId');
    model.addActionGroup(group);

    // WHEN:
    // builds an action/actionGroup tree without pass root group id.
    let buildHierarchy = service.buildHierarchy(model);

    // THEN:
    // build tree have not contains a root group.
    expect(buildHierarchy.length).to.equals(2);
  });

  it('should create a tree with root group', () => {
    // GIVEN
    // there is a definition.
    let model = new ModelDefinition();
    // AND it has an action without parent.
    let actionWithoutParentGroup = createAction('actionWithoutParentGroup');
    model.addAction(actionWithoutParentGroup);
    // AND it has an action with parent.
    let actionWithParentGroup = createAction('actionWithParentGroup', 'groupId');
    model.addAction(actionWithParentGroup);
    // AND it has a group.
    let group = createActionGroup('groupId');
    model.addActionGroup(group);

    // WHEN:
    // builds an action/actionGroup tree with root group id.
    let buildHierarchy = service.buildHierarchy(model, 'rootGroupId', 'rootGroupText');

    // THEN:
    // build tree have to contains a root group.
    expect(buildHierarchy.getId()).to.equals('rootGroupId');
    expect(buildHierarchy.text).to.equals('rootGroupText');
    expect(buildHierarchy.children.length).to.equals(2);
  });

  it('should create a tree', () => {
    // GIVEN
    // there is a definition.
    let model = new ModelDefinition();
    // AND it has an action without parent.
    let actionWithoutParentGroup = createAction('actionWithoutParentGroup');
    model.addAction(actionWithoutParentGroup);
    // AND it has an action with parent.
    let actionWithParentGroup = createAction('actionWithParentGroup', 'groupId');
    model.addAction(actionWithParentGroup);
    // AND it has a group.
    let group = createActionGroup('groupId');
    model.addActionGroup(group);

    // WHEN:
    // builds an action/actionGroup tree.
    let buildHierarchy = service.buildHierarchy(model, 'rootGroupId', 'rootGroupText');

    // THEN:
    // expect two child on first level
    expect(buildHierarchy.children.length).to.equals(2);
    // expect the action "actionGroup_groupId" to be in first level.
    expect(buildHierarchy.children[0].getId()).to.equals('actionGroup_groupId');
    // expect the action "actionWithoutParentGroup" to be in first level
    expect(buildHierarchy.children[1].getId()).to.equals('action_actionWithoutParentGroup');
    // expect "actionWithParentGroup" to be a child of the "group".
    expect(buildHierarchy.children[0].children[0].getId()).to.equals('action_actionWithParentGroup');
  });

  function createAction(id, parentActionGroupId = '', order = Number.MAX_VALUE) {
    let action = new ModelAction(id);
    let parentActionGroupValue = new ModelValue('en', parentActionGroupId);
    let parentActionGroupAttribute = new ModelSingleAttribute(ModelAttribute.GROUP_ATTRIBUTE, 'string', parentActionGroupValue);
    action.addAttribute(parentActionGroupAttribute);

    let actionOrderValue = new ModelValue('en', order);
    let actionOrderAttribute = new ModelSingleAttribute(ModelAttribute.ORDER_ATTRIBUTE, 'integer', actionOrderValue);
    action.addAttribute(actionOrderAttribute);
    return action;
  }

  function createActionGroup(id, parentActionGroupId = '', order = Number.MAX_VALUE) {
    let actionGroup = new ModelActionGroup(id);
    let parentActionGroupValue = new ModelValue('en', parentActionGroupId);
    let parentActionGroupAttribute = new ModelAttribute(ModelAttribute.PARENT_ATTRIBUTE, 'string', parentActionGroupValue);
    actionGroup.addAttribute(parentActionGroupAttribute);

    let actionGroupOrderValue = new ModelValue('en', order);
    let actionGroupOrderAttribute = new ModelSingleAttribute(ModelAttribute.ORDER_ATTRIBUTE, 'integer', actionGroupOrderValue);
    actionGroup.addAttribute(actionGroupOrderAttribute);
    return actionGroup;
  }
});