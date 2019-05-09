import {ModelRestoreInheritedHeaderActionFactory} from 'administration/model-management/actions/restore/model-restore-inherited-header-action-factory';
import {ModelRestoreInheritedHeaderAction} from 'administration/model-management/actions/restore/model-restore-inherited-header-action';

describe('ModelRestoreInheritedHeaderActionFactory', () => {

  let modelRestoreInheritedHeaderActionFactory;

  beforeEach(() => {
    modelRestoreInheritedHeaderActionFactory = new ModelRestoreInheritedHeaderActionFactory();
  });

  it('should properly create restore header action', () => {
    let expected = new ModelRestoreInheritedHeaderAction();
    expect(modelRestoreInheritedHeaderActionFactory.create()).to.deep.eq(expected);
  });
});
