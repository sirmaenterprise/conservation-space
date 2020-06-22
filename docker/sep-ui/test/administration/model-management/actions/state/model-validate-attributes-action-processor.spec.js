import {ModelValidateAttributesActionProcessor} from 'administration/model-management/actions/state/model-validate-attributes-action-processor';
import {ModelValidateAttributesAction} from 'administration/model-management/actions/state/model-validate-attributes-action';
import {ModelManagementValidationService} from 'administration/model-management/services/model-managament-validation-service';

import {ModelDefinition} from 'administration/model-management/model/model-definition';
import {ModelSingleAttribute} from 'administration/model-management/model/attributes/model-single-attribute';

import {stub} from 'test/test-utils';

describe('ModelValidateAttributesActionProcessor', () => {

  let modelValidateAttributesActionProcessor;
  let modelManagementValidationServiceStub;

  beforeEach(() => {
    modelManagementValidationServiceStub = stub(ModelManagementValidationService);
    modelValidateAttributesActionProcessor = new ModelValidateAttributesActionProcessor(modelManagementValidationServiceStub);
  });

  it('should properly validate all attributes of the provided model', () => {
    let model = new ModelDefinition();
    model.addAttribute(new ModelSingleAttribute('1').setParent(model));
    model.addAttribute(new ModelSingleAttribute('2').setParent(model));
    let action = new ModelValidateAttributesAction().setModel(model);

    modelValidateAttributesActionProcessor.execute(action);
    expect(modelManagementValidationServiceStub.validateAttribute.calledTwice).to.be.true;
  });
});
