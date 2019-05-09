import {ModelRestoreInheritedFieldActionProcessor} from 'administration/model-management/actions/restore/model-restore-inherited-field-action-processor';
import {ModelRestoreInheritedFieldAction} from 'administration/model-management/actions/restore/model-restore-inherited-field-action';

import {ModelChangeSetBuilder} from 'administration/model-management/services/builders/model-changeset-builder';
import {ModelField} from 'administration/model-management/model/model-field';
import {ModelDefinition} from 'administration/model-management/model/model-definition';

import {stub} from 'test/test-utils';

describe('ModelRestoreInheritedFieldActionProcessor', () => {

  let modelRestoreInheritedHeaderActionProcessor;
  let modelChangeSetBuilderStub;

  beforeEach(() => {
    modelChangeSetBuilderStub = stub(ModelChangeSetBuilder);
    modelRestoreInheritedHeaderActionProcessor = new ModelRestoreInheritedFieldActionProcessor(modelChangeSetBuilderStub);
  });

  it('should completely restore inherited field from the parent definition', () => {
    let action = new ModelRestoreInheritedFieldAction();

    let parentField = new ModelField('field');
    let parentDefinition = new ModelDefinition('definition');

    let concreteField = new ModelField('field');
    let concreteDefinition = new ModelDefinition('definition');

    parentField.setParent(parentDefinition);
    concreteField.setParent(concreteDefinition);

    parentDefinition.addField(parentField);
    concreteDefinition.addField(concreteField);

    concreteField.setReference(parentField);
    concreteDefinition.setParent(parentDefinition);

    action.setModel(concreteField);
    action.setContext(concreteDefinition);

    modelRestoreInheritedHeaderActionProcessor.execute(action);
    expect(concreteDefinition.getField('field')).to.eq(parentField);
  });

  it('should revert the restore action for field by restoring the initial field', () => {
    let action = new ModelRestoreInheritedFieldAction();

    let parentField = new ModelField('field');
    let parentDefinition = new ModelDefinition('definition');

    let concreteField = new ModelField('field');
    let concreteDefinition = new ModelDefinition('definition');

    parentField.setParent(parentDefinition);
    concreteField.setParent(concreteDefinition);

    parentDefinition.addField(parentField);
    concreteDefinition.addField(parentField);

    concreteField.setReference(parentField);
    concreteDefinition.setParent(parentDefinition);

    action.setModel(concreteField);
    action.setContext(concreteDefinition);

    modelRestoreInheritedHeaderActionProcessor.restore(action);
    expect(concreteDefinition.getField('field')).to.eq(concreteField);
  });

  it('should be able to build change set for the given action', () => {
    let target = new ModelField('field');
    modelRestoreInheritedHeaderActionProcessor.changeset(new ModelRestoreInheritedFieldAction().setModel(target));

    expect(modelChangeSetBuilderStub.buildChangeSet.calledOnce);
    expect(modelChangeSetBuilderStub.buildChangeSet.calledWith(target)).to.be.true;
  });
});