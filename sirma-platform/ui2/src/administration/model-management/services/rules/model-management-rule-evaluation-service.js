import {Injectable, Inject} from 'app/app';
import {CommandChain} from 'common/command-chain/command-chain';
import {PluginsService} from 'services/plugin/plugins-service';

/**
 * Evaluates a provided instance of {@link ModelRule}
 *
 * Rule can have multiple expressions and two conditions ("AND"/"OR"). Evaluation iterates over all expressions and
 * calculate if rule is fulfilled or not.
 * For AND condition - if at least one expression is false then the rule is not fulfilled
 * For OR condition - if at least one expression is true then the rule is fulfilled
 *
 * @author Stela Djulgerova
 */
@Injectable()
@Inject(PluginsService)
export class ModelManagementRuleEvaluationService {

  constructor(pluginsService) {
    pluginsService.loadPluginServiceModules('model-management-rule-command').then(modules => {
      this.command = new CommandChain(modules);
    });
  }

  evaluateRule(rule, field, attributeValue) {
    let ruleIsFulfilled = false;
    // If attribute value is not in the range for which the rule should be applied than rule is not applicable
    if(rule.getValues() && rule.getValues().indexOf(attributeValue) === -1) {
      return ruleIsFulfilled;
    }
    let conditionValue = rule.getCondition() === 'OR';
    ruleIsFulfilled = !conditionValue;
    rule.getExpressions().some(expression => {
      if (this.command.execute(this.getData(expression, field)) === conditionValue) {
        ruleIsFulfilled = conditionValue;
        return true;
      }
    });
    return ruleIsFulfilled;
  }

  getData(expression, field) {
    return {
      operation: expression.getOperation(),
      values: expression.getValues(),
      value: field.getAttribute(expression.getField()).getValue().getValue()
    };
  }
}