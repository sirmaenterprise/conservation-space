import {ModelRestoreInheritedRegionActionFactory} from 'administration/model-management/actions/restore/model-restore-inherited-region-action-factory';
import {ModelRestoreInheritedRegionAction} from 'administration/model-management/actions/restore/model-restore-inherited-region-action';

import {ModelField} from 'administration/model-management/model/model-field';
import {ModelRegion} from 'administration/model-management/model/model-region';
import {ModelDefinition} from 'administration/model-management/model/model-definition';

describe('ModelRestoreInheritedRegionActionFactory', () => {

  let modelRestoreInheritedRegionActionFactory;

  beforeEach(() => {
    modelRestoreInheritedRegionActionFactory = new ModelRestoreInheritedRegionActionFactory();
  });

  it('should properly create restore region action', () => {
    let expected = new ModelRestoreInheritedRegionAction();
    expect(modelRestoreInheritedRegionActionFactory.create()).to.deep.eq(expected);
  });

  it('should properly evaluate restore region action', () => {
    let action = new ModelRestoreInheritedRegionAction();

    let definition = new ModelDefinition('definition');
    let region = new ModelRegion('region').setParent(definition);

    let fieldInsideRegionOne = new ModelField('field1').setRegionId('region');
    let fieldInsideRegionTwo = new ModelField('field2').setRegionId('region');

    fieldInsideRegionTwo.setParent(null);
    fieldInsideRegionOne.setParent(definition);

    definition.addField(fieldInsideRegionOne);
    definition.addField(fieldInsideRegionTwo);

    action.setModel(region);
    action.setContext(definition);

    modelRestoreInheritedRegionActionFactory.evaluate(action);
    expect(action.getContainedFields()).to.deep.eq([fieldInsideRegionOne]);
  });
});
