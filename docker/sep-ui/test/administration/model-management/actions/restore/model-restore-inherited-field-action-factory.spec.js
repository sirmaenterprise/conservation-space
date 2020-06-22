import {ModelRestoreInheritedFieldActionFactory} from 'administration/model-management/actions/restore/model-restore-inherited-field-action-factory';
import {ModelRestoreInheritedFieldAction} from 'administration/model-management/actions/restore/model-restore-inherited-field-action';

describe('ModelRestoreInheritedFieldActionFactory', () => {

  let modelRestoreInheritedFieldActionFactory;

  beforeEach(() => {
    modelRestoreInheritedFieldActionFactory = new ModelRestoreInheritedFieldActionFactory();
  });

  it('should properly create restore field action', () => {
    let expected = new ModelRestoreInheritedFieldAction();
    expect(modelRestoreInheritedFieldActionFactory.create()).to.deep.eq(expected);
  });
});
