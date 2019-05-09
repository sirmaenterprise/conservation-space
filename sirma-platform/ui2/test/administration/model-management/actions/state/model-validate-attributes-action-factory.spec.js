import {ModelValidateAttributesActionFactory} from 'administration/model-management/actions/state/model-validate-attributes-action-factory';
import {ModelValidateAttributesAction} from 'administration/model-management/actions/state/model-validate-attributes-action';

describe('ModelValidateAttributesActionFactory', () => {

  let modelValidateAttributesActionFactory;

  beforeEach(() => {
    modelValidateAttributesActionFactory = new ModelValidateAttributesActionFactory();
  });

  it('should properly create validate attributes action', () => {
    let expected = new ModelValidateAttributesAction();
    expect(modelValidateAttributesActionFactory.create()).to.deep.eq(expected);
  });
});
