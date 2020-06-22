import {ModelManagementRuleEvaluationService} from 'administration/model-management/services/rules/model-management-rule-evaluation-service';
import {ModelField} from 'administration/model-management/model/model-field';
import {ModelAttribute} from 'administration/model-management/model/attributes/model-attribute';
import {ModelSingleAttribute} from 'administration/model-management/model/attributes/model-single-attribute';
import {ModelRule} from 'administration/model-management/model/validation/model-rule';
import {ModelRuleExpression} from 'administration/model-management/model/validation/model-rule-expression';
import {ModelValue} from 'administration/model-management/model/model-value';
import {InCommand} from 'administration/model-management/services/rules/in-command';
import {NotMatchCommand} from 'administration/model-management/services/rules/not-match-command';
import {PluginsService} from 'services/plugin/plugins-service';
import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';

const TYPE_ATTRIBUTE = ModelAttribute.TYPE_ATTRIBUTE;
const ORDER_ATTRIBUTE = ModelAttribute.ORDER_ATTRIBUTE;
const DISPLAY_ATTRIBUTE = ModelAttribute.DISPLAY_ATTRIBUTE;

describe('ModelManagementRuleEvaluationService', () => {

  let evaluationService;
  let pluginsServiceStub = stub(PluginsService);
  pluginsServiceStub.loadPluginServiceModules.returns(PromiseStub.resolve([new InCommand(), new NotMatchCommand()]));

  beforeEach(() => {
    evaluationService = new ModelManagementRuleEvaluationService(pluginsServiceStub);
  });

  describe('evaluateRule()', () => {

    it('should evaluate model validation rule correct', () => {
      let firstExpression =  {
        field: 'displayType',
        operation: 'in',
        values: ['READ_ONLY', 'HIDDEN', 'SYSTEM']
      };
      let secondExpression = {
        field: 'type',
        operation: 'in',
        values: ['boolean']
      };
      let modelRule = createModelRule([true], 'OR', [firstExpression, secondExpression], 'error');
      let readOnlyTextField = createField('testField1', 'an..180', 'READ_ONLY');
      expect(evaluationService.evaluateRule(modelRule, readOnlyTextField, true)).to.be.true;
      expect(evaluationService.evaluateRule(modelRule, readOnlyTextField, false)).to.be.false;

      let editableTextField = createField('testField2', 'an..180', 'EDITABLE');
      expect(evaluationService.evaluateRule(modelRule, editableTextField, true)).to.be.false;
      expect(evaluationService.evaluateRule(modelRule, editableTextField, false)).to.be.false;

      let editableBoleenField = createField('testField2', 'boolean', 'EDITABLE');
      expect(evaluationService.evaluateRule(modelRule, editableBoleenField, true)).to.be.true;
      expect(evaluationService.evaluateRule(modelRule, editableBoleenField, false)).to.be.false;

      // If no attribute value is specified rule should be applicable for all possible values
      modelRule.setValues(undefined);
      expect(evaluationService.evaluateRule(modelRule, readOnlyTextField, true)).to.be.true;
      expect(evaluationService.evaluateRule(modelRule, readOnlyTextField, false)).to.be.true;
    });

    it('should evaluate regex operation correct', () => {
      let firstExpression =  {
        field: 'order',
        operation: 'not_match',
        values: ['^([1-9][0-9]{0,3}|10000|)$']
      };
      let modelRule = createModelRule(undefined, 'AND', [firstExpression], 'error');
      // If not allowed order value is entered rule should be fulfilled
      let textField = createField('testField', 'an..180', 'EDITABLE', 'test');
      expect(evaluationService.evaluateRule(modelRule, textField, 'test')).to.be.true;

      // if correct value is entered rule should not be fulfilled
      textField = createField('testField', 'an..180', 'EDITABLE', '12');
      expect(evaluationService.evaluateRule(modelRule, textField, '12')).to.be.false;

      textField = createField('testField', 'an..180', 'EDITABLE', '0');
      expect(evaluationService.evaluateRule(modelRule, textField, '0')).to.be.true;

      textField = createField('testField', 'an..180', 'EDITABLE', '10001');
      expect(evaluationService.evaluateRule(modelRule, textField, '0')).to.be.true;
    });
  });

  function createModelRule(values, condition, expressions, label) {
    let modelExpressions = [];
    expressions.forEach(expression => {
      modelExpressions.push(new ModelRuleExpression(expression.field, expression.operation, expression.values));
    });
    return new ModelRule().setValues(values).setCondition(condition).setExpressions(modelExpressions).setErrorLabel(label);
  }

  function createField(id, type, displayType, order) {
    let field = new ModelField(id);

    let fieldType = new ModelSingleAttribute(TYPE_ATTRIBUTE);
    fieldType.setValue(new ModelValue('en', type));
    field.addAttribute(fieldType);

    let ordering = new ModelSingleAttribute(ORDER_ATTRIBUTE);
    ordering.setValue(new ModelValue('en', order));
    field.addAttribute(ordering);

    let display = new ModelSingleAttribute(DISPLAY_ATTRIBUTE);
    display.setValue(new ModelValue('en', displayType));
    field.addAttribute(display);

    return field;
  }

});