import {ModelRestoreInheritedRegionActionProcessor} from 'administration/model-management/actions/restore/model-restore-inherited-region-action-processor';
import {ModelRestoreInheritedRegionAction} from 'administration/model-management/actions/restore/model-restore-inherited-region-action';

import {ModelChangeSetBuilder} from 'administration/model-management/services/builders/model-changeset-builder';
import {ModelField} from 'administration/model-management/model/model-field';
import {ModelRegion} from 'administration/model-management/model/model-region';
import {ModelDefinition} from 'administration/model-management/model/model-definition';

import {stub} from 'test/test-utils';

describe('ModelRestoreInheritedRegionActionProcessor', () => {

  let modelRestoreInheritedRegionActionProcessor;
  let modelChangeSetBuilderStub;

  beforeEach(() => {
    modelChangeSetBuilderStub = stub(ModelChangeSetBuilder);
    modelRestoreInheritedRegionActionProcessor = new ModelRestoreInheritedRegionActionProcessor(modelChangeSetBuilderStub);
  });

  it('should completely restore inherited region from the parent definition', () => {
    let action = new ModelRestoreInheritedRegionAction();

    let parentRegion = new ModelRegion('region');
    let parentDefinition = new ModelDefinition('definition');

    let concreteRegion = new ModelRegion('region');
    let concreteDefinition = new ModelDefinition('definition');

    let containedField = new ModelField('field');

    parentRegion.setParent(parentDefinition);
    concreteRegion.setParent(concreteDefinition);

    parentDefinition.addRegion(parentRegion);
    concreteDefinition.addRegion(concreteRegion);

    concreteRegion.setReference(parentRegion);
    concreteDefinition.setParent(parentDefinition);

    action.setModel(concreteRegion);
    action.setContext(concreteDefinition);
    action.setContainedFields([containedField]);

    modelRestoreInheritedRegionActionProcessor.execute(action);

    expect(containedField.getRegionId()).to.eq(null);
    expect(concreteDefinition.getRegion('region')).to.eq(parentRegion);
  });

  it('should revert the restore action for region by restoring the initial region', () => {
    let action = new ModelRestoreInheritedRegionAction();

    let parentRegion = new ModelRegion('region');
    let parentDefinition = new ModelDefinition('definition');

    let concreteRegion = new ModelRegion('region');
    let concreteDefinition = new ModelDefinition('definition');

    let containedField = new ModelField('field');

    parentRegion.setParent(parentDefinition);
    concreteRegion.setParent(concreteDefinition);

    parentDefinition.addRegion(parentRegion);
    concreteDefinition.addRegion(parentRegion);

    concreteRegion.setReference(parentRegion);
    concreteDefinition.setParent(parentDefinition);

    action.setModel(concreteRegion);
    action.setContext(concreteDefinition);
    action.setContainedFields([containedField]);

    modelRestoreInheritedRegionActionProcessor.restore(action);

    expect(containedField.getRegionId()).to.eq('region');
    expect(concreteDefinition.getRegion('region')).to.eq(concreteRegion);
  });

  it('should be able to build change set for the given action', () => {
    let target = new ModelRegion('region');
    modelRestoreInheritedRegionActionProcessor.changeset(new ModelRestoreInheritedRegionAction().setModel(target));

    expect(modelChangeSetBuilderStub.buildChangeSet.calledOnce);
    expect(modelChangeSetBuilderStub.buildChangeSet.calledWith(target)).to.be.true;
  });
});