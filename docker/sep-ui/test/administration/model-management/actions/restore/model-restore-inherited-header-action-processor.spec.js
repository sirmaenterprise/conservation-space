import {ModelRestoreInheritedHeaderActionProcessor} from 'administration/model-management/actions/restore/model-restore-inherited-header-action-processor';
import {ModelRestoreInheritedHeaderAction} from 'administration/model-management/actions/restore/model-restore-inherited-header-action';

import {ModelChangeSetBuilder} from 'administration/model-management/services/builders/model-changeset-builder';
import {ModelHeader} from 'administration/model-management/model/model-header';
import {ModelDefinition} from 'administration/model-management/model/model-definition';

import {stub} from 'test/test-utils';

describe('ModelRestoreInheritedHeaderActionProcessor', () => {

  let modelRestoreInheritedHeaderActionProcessor;
  let modelChangeSetBuilderStub;

  beforeEach(() => {
    modelChangeSetBuilderStub = stub(ModelChangeSetBuilder);
    modelRestoreInheritedHeaderActionProcessor = new ModelRestoreInheritedHeaderActionProcessor(modelChangeSetBuilderStub);
  });

  it('should completely restore inherited header from the parent definition', () => {
    let action = new ModelRestoreInheritedHeaderAction();

    let parentField = new ModelHeader('header');
    let parentDefinition = new ModelDefinition('definition');

    let concreteField = new ModelHeader('header');
    let concreteDefinition = new ModelDefinition('definition');

    parentField.setParent(parentDefinition);
    concreteField.setParent(concreteDefinition);

    parentDefinition.addHeader(parentField);
    concreteDefinition.addHeader(concreteField);

    concreteField.setReference(parentField);
    concreteDefinition.setParent(parentDefinition);

    action.setModel(concreteField);
    action.setContext(concreteDefinition);

    modelRestoreInheritedHeaderActionProcessor.execute(action);
    expect(concreteDefinition.getHeader('header')).to.eq(parentField);
  });

  it('should revert the restore action for header by restoring the initial field', () => {
    let action = new ModelRestoreInheritedHeaderAction();

    let parentField = new ModelHeader('header');
    let parentDefinition = new ModelDefinition('definition');

    let concreteField = new ModelHeader('header');
    let concreteDefinition = new ModelDefinition('definition');

    parentField.setParent(parentDefinition);
    concreteField.setParent(concreteDefinition);

    parentDefinition.addHeader(parentField);
    concreteDefinition.addHeader(parentField);

    concreteField.setReference(parentField);
    concreteDefinition.setParent(parentDefinition);

    action.setModel(concreteField);
    action.setContext(concreteDefinition);

    modelRestoreInheritedHeaderActionProcessor.restore(action);
    expect(concreteDefinition.getHeader('header')).to.eq(concreteField);
  });

  it('should be able to build change set for the given action', () => {
    let target = new ModelHeader('header');
    modelRestoreInheritedHeaderActionProcessor.changeset(new ModelRestoreInheritedHeaderAction().setModel(target));

    expect(modelChangeSetBuilderStub.buildChangeSet.calledOnce);
    expect(modelChangeSetBuilderStub.buildChangeSet.calledWith(target)).to.be.true;
  });
});