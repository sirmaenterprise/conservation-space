import {ModelRemoveControlActionFactory} from 'administration/model-management/actions/remove/model-remove-control-action-factory';
import {ModelRemoveControlAction} from 'administration/model-management/actions/remove/model-remove-control-action';

describe('ModelRemoveControlActionFactory', () => {

  let modelCreateControlActionFactory;

  beforeEach(() => {
    modelCreateControlActionFactory = new ModelRemoveControlActionFactory();
  });

  it('should properly create control remove action', () => {
    let expected = new ModelRemoveControlAction();
    expect(modelCreateControlActionFactory.create()).to.deep.eq(expected);
  });

});
