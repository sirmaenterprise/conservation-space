import {ModelChangeAttributeActionFactory} from 'administration/model-management/actions/change/model-change-attribute-action-factory';
import {ModelChangeAttributeAction} from 'administration/model-management/actions/change/model-change-attribute-action';

describe('ModelChangeAttributeActionFactory', () => {

  let modelChangeAttributeActionFactory;

  beforeEach(() => {
    modelChangeAttributeActionFactory = new ModelChangeAttributeActionFactory();
  });

  it('should properly create change attribute action', () => {
    let expected = new ModelChangeAttributeAction();
    expect(modelChangeAttributeActionFactory.create()).to.deep.eq(expected);
  });
});
