import {ModelValidateAttributeActionFactory} from 'administration/model-management/actions/state/model-validate-attribute-action-factory';
import {ModelValidateAttributeAction} from 'administration/model-management/actions/state/model-validate-attribute-action';

describe('ModelValidateAttributeActionFactory', () => {

  let modelValidateAttributeActionFactory;

  beforeEach(() => {
    modelValidateAttributeActionFactory = new ModelValidateAttributeActionFactory();
  });

  it('should properly create validate attribute action', () => {
    let expected = new ModelValidateAttributeAction();
    expect(modelValidateAttributeActionFactory.create()).to.deep.eq(expected);
  });
});
