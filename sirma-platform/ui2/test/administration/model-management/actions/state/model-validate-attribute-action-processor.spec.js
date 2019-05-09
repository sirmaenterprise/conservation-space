import {ModelValidateAttributeActionProcessor} from 'administration/model-management/actions/state/model-validate-attribute-action-processor';
import {ModelValidateAttributeAction} from 'administration/model-management/actions/state/model-validate-attribute-action';
import {ModelManagementValidationService} from 'administration/model-management/services/model-managament-validation-service';
import {ModelSingleAttribute} from 'administration/model-management/model/attributes/model-single-attribute';

import {stub} from 'test/test-utils';

describe('ModelValidateAttributeActionProcessor', () => {

  let modelValidateAttributeActionProcessor;
  let modelManagementValidationServiceStub;

  beforeEach(() => {
    modelManagementValidationServiceStub = stub(ModelManagementValidationService);
    modelValidateAttributeActionProcessor = new ModelValidateAttributeActionProcessor(modelManagementValidationServiceStub);
  });

  it('should properly validate the attribute of the provided model', () => {
    let action = new ModelValidateAttributeAction().setModel(new ModelSingleAttribute());

    modelValidateAttributeActionProcessor.execute(action);
    expect(modelManagementValidationServiceStub.validateAttribute.calledOnce).to.be.true;
  });
});
