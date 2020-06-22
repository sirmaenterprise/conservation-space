import {ModelRestoreInheritedControlActionFactory} from 'administration/model-management/actions/restore/model-restore-inherited-control-action-factory';
import {ModelRestoreInheritedControlAction} from 'administration/model-management/actions/restore/model-restore-inherited-control-action';

import {ModelControl} from 'administration/model-management/model/model-control';

describe('ModelRestoreInheritedControlActionFactory', () => {

  let modelRestoreInheritedControlActionFactory;

  beforeEach(() => {
    modelRestoreInheritedControlActionFactory = new ModelRestoreInheritedControlActionFactory();
  });

  it('should properly create restore control action', () => {
    let toRestore = new ModelControl();
    let expected = new ModelRestoreInheritedControlAction().setControlsToRestore([toRestore]);
    expect(modelRestoreInheritedControlActionFactory.create(toRestore)).to.eql(expected);
  });

});
