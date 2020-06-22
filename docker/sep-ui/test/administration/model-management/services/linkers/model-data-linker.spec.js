import {ModelDataLinker} from 'administration/model-management/services/linkers/model-data-linker';
import {ModelPropertyLinker} from 'administration/model-management/services/linkers/model-property-linker';
import {ModelManagementLanguageService} from 'administration/model-management/services/utility/model-management-language-service';

import {ModelDefinition} from 'administration/model-management/model/model-definition';
import {ModelValue} from 'administration/model-management/model/model-value';
import {ModelField} from 'administration/model-management/model/model-field';
import {ModelRegion} from 'administration/model-management/model/model-region';
import {ModelList} from 'administration/model-management/model/model-list';
import {ModelClass} from 'administration/model-management/model/model-class';
import {ModelHeader} from 'administration/model-management/model/model-header';
import {ModelProperty} from 'administration/model-management/model/model-property';
import {ModelSingleAttribute} from 'administration/model-management/model/attributes/model-single-attribute';
import {ModelMultiAttribute} from 'administration/model-management/model/attributes/model-multi-attribute';
import {ModelAttributeTypes} from 'administration/model-management/model/attributes/model-attribute-types';
import {ModelAction} from 'administration/model-management/model/model-action';
import {ModelActionGroup} from 'administration/model-management/model/model-action-group';

import {stub} from 'test/test-utils';
import {ModelActionExecutionTypes, ModelActionExecution} from 'administration/model-management/model/model-action-execution';

const DEFAULT_LANGUAGE = 'en';
const URI = ModelAttributeTypes.SINGLE_VALUE.MODEL_URI_TYPE;
const LABEL = ModelAttributeTypes.MULTI_VALUE.MODEL_LABEL_TYPE;
const OPTION = ModelAttributeTypes.SINGLE_VALUE.MODEL_OPTION_TYPE;
const IDENTIFIER = ModelAttributeTypes.SINGLE_VALUE.MODEL_IDENTIFIER_TYPE;
const STRING = ModelAttributeTypes.SINGLE_VALUE.MODEL_STRING_TYPE;

describe('ModelDataLinker', () => {

  let modelCoreLinker;
  let modelPropertyLinkerStub;
  let modelManagementLanguageServiceStub;

  beforeEach(() => {
    modelPropertyLinkerStub = stub(ModelPropertyLinker);
    modelManagementLanguageServiceStub = stub(ModelManagementLanguageService);
    modelManagementLanguageServiceStub.getDefaultLanguage.returns(DEFAULT_LANGUAGE);

    modelCoreLinker = new ModelDataLinker(modelPropertyLinkerStub, modelManagementLanguageServiceStub);
  });

  it('should properly link the provided class model', () => {
    let clazz = new ModelClass('some-class');
    clazz.addAttribute(createAttribute(LABEL, LABEL, {'en': 'Class', 'bg': 'Клас'}));

    modelCoreLinker.linkInheritanceModel(clazz, getProperties());
    expect(modelPropertyLinkerStub.linkPropertiesToClassModel.called).to.be.true;
  });

  it('should properly link the provided definition model', () => {
    let pr01 = createBaseProject().setParent(null);
    let pr02 = createEmailProject().setParent(pr01);
    modelCoreLinker.linkInheritanceModel(pr02, getProperties());

    // should link the base label attribute with the actual model description
    expect(pr02.getDescription()).to.equal(pr02.getAttribute(LABEL).getValue());

    // should not inherit base level attributes from parent definition
    expect(pr02.getAttribute(LABEL)).to.not.equal(pr01.getAttribute(LABEL));
    expect(pr02.getAttribute(IDENTIFIER)).to.not.equal(pr01.getAttribute(IDENTIFIER));

    // should inherit the base header from the parent definition
    expect(pr02.getHeader('base-header')).to.equal(pr01.getHeader('base-header'));

    // should inherit the entire region from the parent definition
    expect(pr02.getRegion('details')).to.equal(pr01.getRegion('details'));

    // should not directly inherit the entire field from the parent
    expect(pr02.getField('type')).to.not.equal(pr01.getField('type'));

    // should directly inherit the entire field from the parent
    expect(pr02.getField('watcher')).to.equal(pr01.getField('watcher'));

    // should inherit only attributes which are not present or overridden in current definition field
    expect(pr02.getField('type').getAttribute(URI)).to.equal(pr01.getField('type').getAttribute(URI));
    expect(pr02.getField('type').getAttribute(OPTION)).to.equal(pr01.getField('type').getAttribute(OPTION));

    // should inherit attributes from another field with the same id but located and defined in a different model level
    expect(pr02.getField('overridden').getAttribute(URI)).to.equal(pr01.getField('overridden').getAttribute(URI));
    expect(pr02.getField('overridden').getAttribute(LABEL)).to.equal(pr01.getField('overridden').getAttribute(LABEL));
    expect(pr02.getField('overridden').getAttribute(OPTION)).to.equal(pr01.getField('overridden').getAttribute(OPTION));

    // should link fields and properties with the associated semantic types
    expect(modelPropertyLinkerStub.linkPropertiesToFieldModel.called).to.be.true;
  });

  it('should properly link the actions in provided definition model', () => {
    let pr01 = createBaseProject().setParent(null);
    let pr02 = createEmailProject().setParent(pr01);
    modelCoreLinker.linkInheritanceModel(pr02, getProperties());

    // parent project should not contains action from the sub project.
    expect(pr01.getAction('email-project-action') === undefined).to.be.true;
    // should inherit action from parent project
    expect(pr02.getAction('base-project-action')).to.equal(pr01.getAction('base-project-action'));
    // should inherit action attribute order from parent action.
    expect(pr02.getAction('base-project-action-one').getAttribute('order')).to.equal(pr01.getAction('base-project-action-one').getAttribute('order'));
    // should not inherit action attribute order from parent action.
    expect(pr02.getAction('base-project-action-two').getAttribute('order').getValue().getValue()).to.equal(55);
  });

  it('should properly link the action groups in provided definition model', () => {
    let pr01 = createBaseProject().setParent(null);
    let pr02 = createEmailProject().setParent(pr01);
    modelCoreLinker.linkInheritanceModel(pr02, getProperties());

    // parent project should not contains action group from the sub project.
    expect(pr01.getActionGroup('email-project-action-group') === undefined).to.be.true;
    // should inherit action group from parent project action group.
    expect(pr02.getActionGroup('base-project-action-group')).to.equal(pr01.getActionGroup('base-project-action-group'));
    // should inherit action group attribute order from parent action group.
    expect(pr02.getActionGroup('base-project-action-group-one').getAttribute('order')).to.equal(pr01.getActionGroup('base-project-action-group-one').getAttribute('order'));
    // should not inherit action attribute order from parent action group.
    expect(pr02.getActionGroup('base-project-action-group-two').getAttribute('order').getValue().getValue()).to.equal(44);
  });

  it('should properly link the action executions in provided definition model', () => {
    let pr01 = createBaseProject().setParent(null);
    let pr02 = createEmailProject().setParent(pr01);
    modelCoreLinker.linkInheritanceModel(pr02, getProperties());

    // action "action-with-executions" should have tree action executions
    let actionWihExecutions = pr02.getAction('action-with-executions');
    expect(actionWihExecutions.getActionExecutions().length).to.equal(3);
    // the action have to contains createRelation action execution from email project.
    expect(actionWihExecutions.getActionExecution('createRelationActionExecution').getAttribute('value').getValue().getValue()).to.equal('email project create relation action execution');
    // the action have to contains executeScript action execution from email project.
    expect(actionWihExecutions.getActionExecution('executeScriptActionExecutionEmailProject') !== undefined).to.be.true;
    // the action have to contains executeScript action execution from email project.
    expect(actionWihExecutions.getActionExecution('executeScriptActionExecution') !== undefined).to.be.true;
  });

  function createEmailProject() {
    let type = new ModelClass('semantic-type');
    type.addAttribute(createAttribute(LABEL, LABEL, {'en': 'emf:Activity', 'bg': 'emf:Activity'}).setParent(type));
    let emailProject = new ModelDefinition('PR2').setType(type);

    // add attributes to the base definition model
    emailProject.addAttribute(createAttribute(IDENTIFIER, 'string', 'PR2').setParent(emailProject));
    emailProject.addAttribute(createAttribute(LABEL, LABEL, {'en': 'E-mail', 'bg': 'Е-мейл'}).setParent(emailProject));

    // add fields to the base definition model but skip some attributes
    emailProject.addField(createField(emailProject, 'type', ['Type', 'Tип'], null, null));
    emailProject.addField(createField(emailProject, 'missing', []));

    // add regions to the base definition model
    emailProject.addRegion(createRegion(emailProject, 'other', ['Other', 'Други'], 'EDITABLE', [
      createField(emailProject, 'overridden')
    ]));

    // add a action which will not be present in parent project.
    emailProject.addAction(createAction(emailProject, 'email-project-action', 2));
    // add a action which order attribute have to be fetched from parent action.
    emailProject.addAction(createAction(emailProject, 'base-project-action-one'));
    // add a action which order attribute will override parent action order attribute.
    emailProject.addAction(createAction(emailProject, 'base-project-action-two', 55));

    // add a action croup which will not be present in parent project.
    emailProject.addActionGroup(createActionGroup(emailProject, 'email-project-action-group', 2));
    // add a action group which order attribute have to be fetched from parent action group.
    emailProject.addActionGroup(createActionGroup(emailProject, 'base-project-action-group-one'));
    // add a action group which order attribute will override parent action group order attribute.
    emailProject.addActionGroup(createActionGroup(emailProject, 'base-project-action-group-two', 44));

    // add a action which have a action execution of type "createRelation" and one of "executeScript".
    // the createRelation action execution have to override parent one
    // the action will have two executeScript action execution the parent one and current one.
    let actionWithConfiguration = createAction(emailProject, 'action-with-executions');
    emailProject.addAction(actionWithConfiguration);
    actionWithConfiguration.addActionExecution(createActionExecution(actionWithConfiguration, 'createRelationActionExecution', ModelActionExecutionTypes.MODEL_ACTION_EXECUTION_TYPE_CREATE_RELATION, 'email project create relation action execution'));
    actionWithConfiguration.addActionExecution(createActionExecution(actionWithConfiguration, 'executeScriptActionExecutionEmailProject', ModelActionExecutionTypes.MODEL_ACTION_EXECUTION_TYPE_EXECUTE_SCRIPT, 'email project execute script action execution'));
    return emailProject;
  }

  function createBaseProject() {
    let baseProject = new ModelDefinition('PR1');

    // add attributes to the base definition model
    baseProject.addAttribute(createAttribute(IDENTIFIER, 'string', 'PR1').setParent(baseProject));
    baseProject.addAttribute(createAttribute(LABEL, LABEL, {'en': 'Project', 'bg': 'Проект'}).setParent(baseProject));

    // add headers to the base definition model
    baseProject.addHeader(createHeader('base-header', {'en': 'base', 'bg': 'основен'}).setParent(baseProject));

    // add regions to the base definition model
    baseProject.addRegion(createRegion(baseProject, 'details', ['Details', 'Детайли'], 'EDITABLE', [
      createField(baseProject, 'hours', ['Hours', 'Часове'], 'rdf:type', 'HIDDEN'),
      createField(baseProject, 'status', ['Status', 'Статус'], 'emf:status', 'EDITABLE'),
      createField(baseProject, 'ownedBy', ['Owned By', 'Притежаван'], 'emf:ownedBy', 'SYSTEM')
    ]));

    // add fields to the base definition model
    baseProject.addField(createField(baseProject, 'type', ['Type', 'Tип'], 'rdf:type', 'READONLY'));
    baseProject.addField(createField(baseProject, 'watcher', ['Watcher', 'Наблюдател'], 'rdf:watcher', 'EDITABLE'));
    baseProject.addField(createField(baseProject, 'overridden', ['Overridden', 'Презаписан'], 'rdf:watcher', 'EDITABLE'));

    // add a action which have to be inherited form the sub project.
    baseProject.addAction(createAction(baseProject, 'base-project-action', 2));
    // add a action which order attribute have to be inherited from the sub project action.
    baseProject.addAction(createAction(baseProject, 'base-project-action-one', 3));
    // add a action which order attribute will be overridden from sub project action.
    baseProject.addAction(createAction(baseProject, 'base-project-action-two', 5));

    // add a action croup which have to be inherited from sub project.
    baseProject.addActionGroup(createActionGroup(baseProject, 'base-project-action-group', 2));
    // add a action group which order attribute have to be inherited from sub project action group.
    baseProject.addActionGroup(createActionGroup(baseProject, 'base-project-action-group-one', 3));
    // add a action group which order attribute will be overridden from sub project action group.
    baseProject.addActionGroup(createActionGroup(baseProject, 'base-project-action-group-two', 4));

    // add a action which have a action execution of type "createRelation" and one of "executeScript".
    let actionWithConfiguration = createAction(baseProject, 'action-with-executions');
    baseProject.addAction(actionWithConfiguration);
    actionWithConfiguration.addActionExecution(createActionExecution(actionWithConfiguration, 'createRelationActionExecution', ModelActionExecutionTypes.MODEL_ACTION_EXECUTION_TYPE_CREATE_RELATION, 'base project create relation action execution'));
    actionWithConfiguration.addActionExecution(createActionExecution(actionWithConfiguration, 'executeScriptActionExecution', ModelActionExecutionTypes.MODEL_ACTION_EXECUTION_TYPE_EXECUTE_SCRIPT, 'base project execute script action execution'));
    return baseProject;
  }

  function createAction(parent, name, order) {
    let action = new ModelAction(name).setParent(parent);
    order && action.addAttribute(createAttribute('order', STRING, order).setParent(action));
    return action;
  }

  function createActionExecution(parent, name, type, value) {
    let actionExecution = new ModelActionExecution(name).setParent(parent);
    type && actionExecution.addAttribute(createAttribute('type', OPTION, type).setParent(actionExecution));
    value && actionExecution.addAttribute(createAttribute('value', STRING, value).setParent(actionExecution));
    return actionExecution;
  }

  function createActionGroup(parent, name, order) {
    let actionGroup = new ModelActionGroup(name).setParent(parent);
    order && actionGroup.addAttribute(createAttribute('order', STRING, order).setParent(actionGroup));
    return actionGroup;
  }

  function createRegion(parent, name, labels, state, fields) {
    let region = new ModelRegion(name).setParent(parent);
    state && region.addAttribute(createAttribute(OPTION, OPTION, state).setParent(region));
    labels && region.addAttribute(createAttribute(LABEL, LABEL, {'en': labels[0], 'bg': labels[1]}).setParent(region));
    fields.forEach(field => field.setRegionId(region.getId()) && parent.addField(field));
    return region;
  }

  function createField(parent, name, labels, uri, state) {
    let field = new ModelField(name).setParent(parent);
    uri && field.addAttribute(createAttribute(URI, URI, uri).setParent(field));
    state && field.addAttribute(createAttribute(OPTION, OPTION, state).setParent(field));
    labels && field.addAttribute(createAttribute(LABEL, LABEL, {'en': labels[0], 'bg': labels[1]}).setParent(field));
    return field;
  }

  function createAttribute(name, type, values) {
    if (ModelAttributeTypes.isMultiValued(type)) {
      let multiAttribute = new ModelMultiAttribute(name).setType(type);
      Object.keys(values).forEach(locale => multiAttribute.addValue(new ModelValue(locale, values[locale])));
      multiAttribute.setValue(multiAttribute.getValueByLanguage(DEFAULT_LANGUAGE));
      return multiAttribute;
    }
    return new ModelSingleAttribute(name).setType(type).setValue(new ModelValue(DEFAULT_LANGUAGE, values));
  }

  function createHeader(name, values) {
    let header = new ModelHeader(name);

    let labelAttribute = new ModelMultiAttribute(ModelAttributeTypes.MULTI_VALUE.MODEL_LABEL_TYPE);
    Object.keys(values).forEach(locale => labelAttribute.addValue(new ModelValue(locale, values[locale])));
    labelAttribute.setValue(labelAttribute.getValueByLanguage(DEFAULT_LANGUAGE));
    header.addAttribute(labelAttribute);

    // header type is not required here

    return header;
  }

  function getProperties() {
    let properties = new ModelList();
    properties.insert(getProperty('rdf:type', 'Type', 'Тип'));
    properties.insert(getProperty('emf:status', 'Status', 'Статус'));
    properties.insert(getProperty('emf:watcher', 'Watcher', 'Наблюдател'));
    return properties;
  }

  function getProperty(uri, enLabel, bgLabel) {
    let property = new ModelProperty(uri);
    property.addAttribute(createAttribute(LABEL, LABEL, {'en': enLabel, 'bg': bgLabel}));
    return property;
  }
});