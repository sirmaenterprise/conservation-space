import {ModelRestoreInheritedControlActionProcessor} from 'administration/model-management/actions/restore/model-restore-inherited-control-action-processor';
import {ModelRestoreInheritedControlAction} from 'administration/model-management/actions/restore/model-restore-inherited-control-action';

import {ModelChangeSetBuilder} from 'administration/model-management/services/builders/model-changeset-builder';
import {ModelField} from 'administration/model-management/model/model-field';
import {ModelControl} from 'administration/model-management/model/model-control';
import {ModelDefinition} from 'administration/model-management/model/model-definition';

import {stub} from 'test/test-utils';

describe('ModelRestoreInheritedControlActionProcessor', () => {

  let modelRestoreInheritedControlActionProcessor;
  let modelChangeSetBuilderStub;

  beforeEach(() => {
    modelChangeSetBuilderStub = stub(ModelChangeSetBuilder);
    modelRestoreInheritedControlActionProcessor = new ModelRestoreInheritedControlActionProcessor(modelChangeSetBuilderStub);
  });

  it('should restore inherited control from the parent definition', () => {
    let action = new ModelRestoreInheritedControlAction();

    let parentControl = new ModelControl('picker');
    let parentField = new ModelField('field');
    let parentDefinition = new ModelDefinition('definition');
    parentControl.setParent(parentField);
    parentField.setParent(parentDefinition);
    parentField.addControl(parentControl);

    let concreteControl = new ModelControl('picker');
    let concreteField = new ModelField('field');
    let concreteDefinition = new ModelDefinition('definition');
    concreteControl.setParent(concreteField);
    concreteField.setParent(parentDefinition);
    concreteField.addControl(concreteControl);
    concreteField.setReference(parentField);

    action.setModel(concreteField);
    action.setContext(concreteDefinition);
    action.setControlsToRestore([concreteControl]);

    modelRestoreInheritedControlActionProcessor.execute(action);

    expect(concreteField.getControl('picker')).to.equal(parentControl);
  });

  it('should revert the restore action for control', () => {
    let action = new ModelRestoreInheritedControlAction();

    let parentControl = new ModelControl('picker');
    let parentField = new ModelField('field');
    let parentDefinition = new ModelDefinition('definition');
    parentControl.setParent(parentField);
    parentField.setParent(parentDefinition);
    parentField.addControl(parentControl);

    let concreteControl = new ModelControl('picker');
    let concreteField = new ModelField('field');
    let concreteDefinition = new ModelDefinition('definition');
    concreteControl.setParent(concreteField);
    concreteField.setParent(parentDefinition);
    concreteField.addControl(parentControl);
    concreteField.setReference(parentField);

    action.setModel(concreteField);
    action.setContext(concreteDefinition);
    action.setControlsToRestore([concreteControl]);

    modelRestoreInheritedControlActionProcessor.restore(action);

    expect(concreteField.getControl('picker')).to.equal(concreteControl);
  });

  it('should be able to build change set for the given action', () => {
    let target = new ModelControl('picker');
    modelRestoreInheritedControlActionProcessor.changeset(new ModelRestoreInheritedControlAction().setControlsToRestore([target]));

    expect(modelChangeSetBuilderStub.buildChangeSets.calledOnce);
    expect(modelChangeSetBuilderStub.buildChangeSets.calledWith([target])).to.be.true;
  });
});