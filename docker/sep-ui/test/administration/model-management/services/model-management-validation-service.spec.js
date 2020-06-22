import {ModelManagementValidationService} from 'administration/model-management/services/model-managament-validation-service';
import {ModelClass} from 'administration/model-management/model/model-class';
import {ModelValue} from 'administration/model-management/model/model-value';
import {ModelSingleAttribute} from 'administration/model-management/model/attributes/model-single-attribute';
import {ModelMultiAttribute} from 'administration/model-management/model/attributes/model-multi-attribute';
import {ModelAttributeMetaData} from 'administration/model-management/meta/model-attribute-meta';
import {ModelRule} from 'administration/model-management/model/validation/model-rule';
import {ModelRuleExpression} from 'administration/model-management/model/validation/model-rule-expression';
import {ModelManagementRuleEvaluationService} from 'administration/model-management/services/rules/model-management-rule-evaluation-service';
import {InCommand} from 'administration/model-management/services/rules/in-command';
import {NotMatchCommand} from 'administration/model-management/services/rules/not-match-command';
import {PluginsService} from 'services/plugin/plugins-service';
import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';
import {ModelRuleOutcome} from 'administration/model-management/model/validation/model-rule-outcome';

describe('ModelManagementValidationService', () => {

  let nonMandatoryMeta = new ModelAttributeMetaData();
  nonMandatoryMeta.getValidationModel().getRestrictions().setMandatory(false);
  nonMandatoryMeta.getValidationModel().getRestrictions().setUpdateable(true);
  nonMandatoryMeta.getValidationModel().getRestrictions().setVisible(true);

  let mandatoryMeta = new ModelAttributeMetaData();
  mandatoryMeta.getValidationModel().getRestrictions().setMandatory(true);
  mandatoryMeta.getValidationModel().getRestrictions().setUpdateable(true);
  mandatoryMeta.getValidationModel().getRestrictions().setVisible(true);

  let pluginsServiceStub = stub(PluginsService);
  pluginsServiceStub.loadPluginServiceModules.returns(PromiseStub.resolve([new InCommand(), new NotMatchCommand()]));
  let validationService;
  beforeEach(() => {
    validationService = new ModelManagementValidationService(new ModelManagementRuleEvaluationService(pluginsServiceStub));
  });

  describe('validateAttribute()', () => {

    it('should validate valid model attribute', () => {
      let clazz = new ModelClass();
      let validAttr = getAttribute('attr1', mandatoryMeta, 'new value', 'old value');
      insertAttributes(clazz, validAttr);

      validationService.validateActualAttribute(validAttr, clazz);
      expect(validAttr.getValidation().isValid()).to.be.true;
    });

    it('should validate non mandatory model attribute with empty value', () => {
      let clazz = new ModelClass();
      // Non mandatory but with empty value -> should be valid
      let nonMandatory = getAttribute('attr1', nonMandatoryMeta, '', 'old value');
      insertAttributes(clazz, nonMandatory);

      validationService.validateActualAttribute(nonMandatory, clazz);
      expect(nonMandatory.getValidation().isValid()).to.be.true;
    });

    it('should validate mandatory model attribute with empty value', () => {
      let clazz = new ModelClass();
      let mandatoryAndInvalid = getAttribute('attr1', mandatoryMeta, '', 'old value');
      insertAttributes(clazz, mandatoryAndInvalid);

      validationService.validateActualAttribute(mandatoryAndInvalid, clazz);
      expect(mandatoryAndInvalid.getValidation().isValid()).to.be.false;
      expect(mandatoryAndInvalid.getValidation().hasError()).to.be.true;
    });

    it('should validate non mandatory model multi attribute with valid value and invalid', () => {
      let clazz = new ModelClass();
      let nonMandatory = getMultiAttribute('attr1', nonMandatoryMeta, getModelValue('value1', 'value1', 'en'), getModelValue('value2', 'value2', 'bg'));
      let nonMandatoryAndEmpty = getMultiAttribute('attr2', nonMandatoryMeta, getModelValue('', 'oldValue1', 'en'), getModelValue('', 'oldValue2', 'bg'));
      insertAttributes(clazz, nonMandatory, nonMandatoryAndEmpty);

      validationService.validateActualAttribute(nonMandatory, clazz);
      expect(nonMandatory.getValidation().isValid()).to.be.true;

      validationService.validateActualAttribute(nonMandatoryAndEmpty, clazz);
      expect(nonMandatoryAndEmpty.getValidation().isValid()).to.be.true;
    });

    it('should validate mandatory model multi attribute base value', () => {
      let clazz = new ModelClass();
      let baseValueValid = getMultiAttribute('attr4', mandatoryMeta, getModelValue('value1', 'value1', 'en'), getModelValue('', 'oldValue2', 'bg'));
      let baseValueInvalid = getMultiAttribute('attr5', mandatoryMeta, getModelValue('', 'oldValue1', 'en'), getModelValue('value2', 'value2', 'bg'));
      insertAttributes(clazz, baseValueValid, baseValueInvalid);

      validationService.validateActualAttribute(baseValueValid, clazz);
      expect(baseValueValid.getValidation().isValid()).to.be.true;

      // It should become invalid
      validationService.validateActualAttribute(baseValueInvalid, clazz);
      expect(baseValueInvalid.getValidation().isValid()).to.be.false;
      expect(baseValueInvalid.getValidation().hasError()).to.be.true;
    });

    it('should validate an invalid model with new valid attributes', () => {
      let clazz = new ModelClass();

      let previouslyInvalid = getAttribute('attr2', mandatoryMeta, 'new value', 'old value');
      previouslyInvalid.getValidation().addError('some-error-code');
      insertAttributes(clazz, previouslyInvalid);

      validationService.validateActualAttribute(previouslyInvalid, clazz);
      expect(previouslyInvalid.getValidation().isValid()).to.be.true;
    });

    it('should validate model with invalid and valid attributes', () => {
      let clazz = new ModelClass();
      let valid = getAttribute('attr1', mandatoryMeta, 'new value', 'old value');
      let invalid = getAttribute('attr2', mandatoryMeta, '', 'old value');
      insertAttributes(clazz, valid, invalid);

      validationService.validateActualAttribute(invalid, clazz);
      expect(invalid.getValidation().isValid()).to.be.false;
      // Default state is to be true and should not be modified
      expect(valid.getValidation().isValid()).to.be.true;
    });

    it('should validate model with validation rules', () => {
      let clazz = new ModelClass();
      let modelRuleExpression = new ModelRuleExpression('displayType', 'in', ['READ_ONLY', 'HIDDEN', 'SYSTEM']);
      let outcome = new ModelRuleOutcome().setUpdateable(true);
      let modelRule = new ModelRule().setValues([true]).setExpressions([modelRuleExpression]).setErrorLabel('error').setOutcome(outcome);
      nonMandatoryMeta.getValidationModel().getValidationRules().setRules([modelRule]);
      let mandatory = getAttribute('mandatory', nonMandatoryMeta, true, false);
      let displayType = getAttribute('displayType', nonMandatoryMeta, 'READ_ONLY', 'READ_ONLY');
      insertAttributes(clazz, mandatory, displayType);

      validationService.validateActualAttribute(mandatory, clazz);
      expect(mandatory.getValidation().isInvalid()).to.be.true;

      displayType = getAttribute('displayType', nonMandatoryMeta, 'EDITABLE', 'READ_ONLY');
      insertAttributes(clazz, mandatory, displayType);
      validationService.validateActualAttribute(mandatory, clazz);
      expect(mandatory.getValidation().isInvalid()).to.be.false;
    });

    it('should update attribute mandatory restriction', () => {
      let clazz = new ModelClass();
      let modelRuleExpression = new ModelRuleExpression('displayType', 'in', ['READ_ONLY', 'HIDDEN', 'SYSTEM']);
      let outcome = new ModelRuleOutcome().setMandatory(true);
      let modelRule = new ModelRule().setValues([true]).setExpressions([modelRuleExpression]).setErrorLabel('error').setOutcome(outcome);
      nonMandatoryMeta.getValidationModel().getValidationRules().setRules([modelRule]);

      let mandatory = getAttribute('mandatory', nonMandatoryMeta, true, false);
      let displayType = getAttribute('displayType', nonMandatoryMeta, 'READ_ONLY', 'READ_ONLY');

      insertAttributes(clazz, mandatory, displayType);

      // initially attribute is not mandatory
      expect(mandatory.getRestrictions().isMandatory()).to.be.false;

      // After validation if rule is fulfilled mandatory flag is changed to true
      validationService.validateActualAttribute(mandatory, clazz);
      expect(mandatory.getRestrictions().isMandatory()).to.be.true;
    });

    it('should update attribute updateable restriction', () => {
      let clazz = new ModelClass();
      let modelRuleExpression = new ModelRuleExpression('displayType', 'in', ['READ_ONLY', 'HIDDEN', 'SYSTEM']);
      let outcome = new ModelRuleOutcome().setUpdateable(false);
      let modelRule = new ModelRule().setValues([true]).setExpressions([modelRuleExpression]).setErrorLabel('error').setOutcome(outcome);
      nonMandatoryMeta.getValidationModel().getValidationRules().setRules([modelRule]);
      let mandatory = getAttribute('mandatory', nonMandatoryMeta, true, false);
      let displayType = getAttribute('displayType', nonMandatoryMeta, 'READ_ONLY', 'READ_ONLY');
      insertAttributes(clazz, mandatory, displayType);

      // initially attribute is updateable
      expect(mandatory.getRestrictions().isUpdateable()).to.be.true;
      // Afetr validation if rule is fulfilled updateable flag is changed to false
      validationService.validateActualAttribute(mandatory, clazz);
      expect(mandatory.getRestrictions().isUpdateable()).to.be.false;
    });

    it('should update attribute visibility restriction', () => {
      let clazz = new ModelClass();
      let modelRuleExpression = new ModelRuleExpression('displayType', 'in', ['READ_ONLY', 'HIDDEN', 'SYSTEM']);
      let outcome = new ModelRuleOutcome().setVisible(false);
      let modelRule = new ModelRule().setValues([true]).setExpressions([modelRuleExpression]).setErrorLabel('error').setOutcome(outcome);
      nonMandatoryMeta.getValidationModel().getValidationRules().setRules([modelRule]);
      let mandatory = getAttribute('mandatory', nonMandatoryMeta, true, false);
      let displayType = getAttribute('displayType', nonMandatoryMeta, 'READ_ONLY', 'READ_ONLY');
      insertAttributes(clazz, mandatory, displayType);

      // initially attribute is visible
      expect(mandatory.getRestrictions().isVisible()).to.be.true;
      // After validation if rule is fulfilled visibility flag is changed to false
      validationService.validateActualAttribute(mandatory, clazz);
      expect(mandatory.getRestrictions().isVisible()).to.be.false;
    });

    it('should validate related attributes', () => {
      let clazz = new ModelClass();
      nonMandatoryMeta.getValidationModel().setAffected(['displayType']);
      let mandatory = getAttribute('mandatory', nonMandatoryMeta, true, false);

      let modelRuleExpression = new ModelRuleExpression('mandatory', 'in', [true]);
      let modelRule = new ModelRule().setValues(['READ_ONLY']).setCondition('OR')
        .setExpressions([modelRuleExpression]).setErrorLabel('error').setOutcome(new ModelRuleOutcome());
      nonMandatoryMeta = new ModelAttributeMetaData();
      nonMandatoryMeta.getValidationModel().getValidationRules().setRules([modelRule]);
      let displayType = getAttribute('displayType', nonMandatoryMeta, 'READ_ONLY', 'READ_ONLY');

      insertAttributes(clazz, mandatory, displayType);

      validationService.validateRelatedAttributes(mandatory, clazz);
      expect(displayType.getValidation().isInvalid()).to.be.true;
    });
  });

  function getAttribute(name, meta, value, oldValue) {
    let modelValue = getModelValue(value, oldValue);
    let attribute = new ModelSingleAttribute(name);
    attribute.setValue(modelValue);
    attribute.setMetaData(meta);
    attribute.getRestrictions().copyFrom(attribute.getMetaData().getValidationModel().getRestrictions());
    return attribute;
  }

  function getModelValue(value, oldValue = value, language = 'en') {
    let modelValue = new ModelValue(language, value);
    if (oldValue !== undefined) {
      modelValue.setOldValue(oldValue);
    }
    return modelValue;
  }

  function getMultiAttribute(name, meta, ...values) {
    let multiAttribute = new ModelMultiAttribute(name);
    multiAttribute.setMetaData(meta);
    multiAttribute.getRestrictions().copyFrom(multiAttribute.getMetaData().getValidationModel().getRestrictions());
    if (values) {
      values.forEach(value => multiAttribute.addValue(value));
      multiAttribute.setValue(multiAttribute.getValueByLanguage('en'));
    }
    return multiAttribute;
  }

  function insertAttributes(model, ...attributes) {
    attributes.forEach(attr => {
      model.addAttribute(attr);
      attr.setParent(model);
    });
  }
});