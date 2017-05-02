import _ from 'lodash';
import {Injectable, Inject} from 'app/app';
import {Logger} from 'services/logging/logger';
import {ValidationService} from 'form-builder/validation/validation-service';
import {Configuration} from 'common/application-config';

const OUTCOME = {
  'HIDDEN': 'HIDDEN',
  'VISIBLE': 'VISIBLE',
  'READONLY': 'READONLY',
  'ENABLED': 'ENABLED',
  'MANDATORY': 'MANDATORY',
  'OPTIONAL': 'OPTIONAL'
};

const MANDATORY = 'mandatory';

@Injectable()
@Inject(Logger, Configuration)
export class ConditionEvaluator {

  constructor(logger, configuration) {
    this.logger = logger;
    this.debugMode = configuration.get(Configuration.RNC_DEBUG_ENABLED);
    this.tokenizedExpressionCache = {};
  }

  /**
   * Executes all enabled rules for given condition. The algorithm is as follows:
   * - Get the applicable conditions for current field
   * - Execute conditions
   * - Modify view model according to result
   * - If the condition is of type MANDATORY then trigger validation again in order to be applied the new conditions
   * - Return
   *
   *
   * @param fieldName
   * @param validatorDef
   * @param validationModel
   * @param flatModel
   * @returns {boolean} Returns true only in case no MANDATORY|OPTIONAL conditions are found for given field.
   */
  evaluate(fieldName, validatorDef, validationModel, flatModel) {
    // there might be more than one condition defined for the same field
    let rules = validatorDef.rules;
    for (let i = 0, length = rules.length; i < length; i++) {
      let rule = rules[i];
      if (rule.disabled) {
        continue;
      }
      let tokenizedExpression = this.parseExpression(rule.expression);
      let booleanExpression = ConditionEvaluator.convertToBooleanExpression(tokenizedExpression, validationModel);
      // If expression is evaluated to be true means that the outcome of the condition should be applied. For example if
      // a field has HIDDEN condition which is evaluated as true, then the field should become visible.
      let fulfilledCondition = eval(booleanExpression);

      if (this.debugMode) {
        this.logger.debug(`${fieldName} \n    rule=${rule.id} \n  renderAs=${rule.renderAs} \n   condition=${rule.expression} \n   converted=${booleanExpression} \n   fulfilled=${fulfilledCondition}`);
      }

      this.applyCondition(flatModel[fieldName], rule.renderAs, fulfilledCondition, validationModel);
    }
  }

  /**
   * Conditions are applied according to the validation result and the desired outcome. That means if a field has
   * condition with renderAs=HIDDEN and the condition rule is evaluated to be true, then the field is marked as hidden.
   *
   * @param fieldViewModel
   * @param renderAs
   * @param conditionResult
   * @param validationModel
   * @returns {boolean}
   */
  applyCondition(fieldViewModel, renderAs, conditionResult, validationModel) {
    // VISIBLE conditions must not be applied to system fields
    // In order to streamline the processing flow view models of the regions and the fields out and inside of regions
    // are stored inside an array and are processed sequentially.
    // This is due the fact that if a condition is applied to a region we should process its fields also when the condition
    // is applied.
    let viewModels = [fieldViewModel];
    if (ConditionEvaluator.isRegion(fieldViewModel)) {
      viewModels = viewModels.concat(fieldViewModel.fields);
    }

    // for regions apply hidden and readonly to the region and all of the region fields then reset region fields values
    viewModels.forEach((viewModel) => {
      // Usually there are cases where some fields are populated by the system and they are not editable and in some
      // cases not visible too. In these cases the value of such fields shouldn't be cleared when HIDDEN condition is
      // applied.
      if (renderAs === OUTCOME.HIDDEN) {
        // Hidden condition is true and field was currently visible. It will be forced to become hidden.
        // Hidden condition is true and field was currently hidden. It will remain hidden.
        // Hidden condition is false and field was currently visible. It will remain visible.
        // Hidden condition is false and field was currently hidden. It will remain hidden.

        // Backup the original displayType in order to be able to restore the original value if the condition gets a
        // negative evaluation.
        if (!viewModel._displayType) {
          viewModel._displayType = viewModel.displayType;
        }
        // When a HIDDEN condition is evaluated to be true, then the field should be hidden. Otherwise, restore its
        // original displayType. The actual field visibility is calculated in the form builder when all additional form
        // configurations are considered.
        viewModel.displayType = conditionResult ? ValidationService.DISPLAY_TYPE_HIDDEN : viewModel._displayType;
        // Readonly fields with HIDDEN condition should not get their values cleared when the condition is applied!
        let shouldClearValue = conditionResult && viewModel.displayType === ValidationService.DISPLAY_TYPE_HIDDEN
          && viewModel._displayType !== ValidationService.DISPLAY_TYPE_READ_ONLY;
        ConditionEvaluator.clearFieldValue(shouldClearValue, validationModel[viewModel.identifier]);

      } else if (renderAs === OUTCOME.VISIBLE) {
        // Visible condition is true and field was currently hidden. It will be forced to become visible.
        // Visible condition is true and field was currently visible. It will remain visible.
        // Visible condition is false and field was currently hidden. It will remain hidden.
        // Visible condition is false and field was currently visible. It will remain visible.
        if (!viewModel._displayType) {
          viewModel._displayType = viewModel.displayType;
        }
        if (!conditionResult) {
          viewModel.displayType = viewModel._displayType;
        } else {
          if (ConditionEvaluator.isHidden(viewModel)) {
            viewModel.displayType = ValidationService.DISPLAY_TYPE_READ_ONLY;
          }
        }
        // Readonly fields with HIDDEN condition should not get their values cleared when the condition is applied!
        let shouldClearValue = !conditionResult && viewModel.displayType === ValidationService.DISPLAY_TYPE_HIDDEN
          && viewModel._displayType !== ValidationService.DISPLAY_TYPE_READ_ONLY;
        ConditionEvaluator.clearFieldValue(shouldClearValue, validationModel[viewModel.identifier]);

      } else if (renderAs === OUTCOME.READONLY) {
        // Readonly condition is true and field was currently editable. It will be forced to become readonly.
        // Readonly condition is true and field was currently readonly. It will remain readonly.
        // Readonly condition is false and field was currently editable. It will remain editable.
        // Readonly condition is false and field was currently readonly. It will remain readonly.
        if (viewModel._preview === undefined) {
          viewModel._preview = viewModel.preview;
        }
        viewModel.preview = conditionResult ? true : viewModel._preview;
        ConditionEvaluator.resetFieldValue(conditionResult, validationModel[viewModel.identifier]);

      } else if (renderAs === OUTCOME.ENABLED) {
        // Editable condition is true and field was currently readonly. It will be forced to become editable.
        // Editable condition is true and field was currently editable. It will remain editable.
        // Editable condition is false and field was currently readonly. It will remain readonly.
        // Editable condition is false and field was currently editable. It will remain editable.
        if (viewModel._preview === undefined) {
          viewModel._preview = viewModel.preview;
        }
        viewModel.preview = conditionResult ? false : viewModel._preview;
        ConditionEvaluator.resetFieldValue(!conditionResult, validationModel[viewModel.identifier]);

      } else if (renderAs === OUTCOME.MANDATORY) {
        // Mandatory condition is true and field was currently optional. It will be forced to become mandatory.
        // Mandatory condition is true and field was currently mandatory. It will remain mandatory.
        // Mandatory condition is false and field was currently mandatory. It will remain mandatory.
        // Mandatory condition is false and field was currently optional. It will restore to original isMandatory.
        if (viewModel._isMandatory === undefined) {
          viewModel._isMandatory = viewModel.isMandatory;
        }
        viewModel.isMandatory = conditionResult ? true : viewModel._isMandatory;

      } else if (renderAs === OUTCOME.OPTIONAL) {
        // Apply inverted because this is for OPTIONAL conditions that are the opposite from MANDATORY
        // Optional condition is true and field was currently mandatory. It will be forced to become optional.
        // Optional condition is true and field was currently optional. It will remain optional.
        // Optional condition is false and field was currently optional. It will be forced to become mandatory.
        // Optional condition is false and field was currently mandatory. It will remain mandatory.
        if (viewModel._isMandatory === undefined) {
          viewModel._isMandatory = viewModel.isMandatory;
        }
        viewModel.isMandatory = conditionResult ? false : viewModel._isMandatory;

      }
    });
  }

  static isHidden(fieldViewModel) {
    return fieldViewModel.displayType === ValidationService.DISPLAY_TYPE_HIDDEN;
  }

  static isRegion(fieldViewModel) {
    return fieldViewModel.fields !== undefined;
  }

  /**
   * Once a condition is applied, then according to its result the field value could be changed to its default if the
   * field has a defaultValue or null otherwise. Resetting the field's value happens when the field is made hidden or
   * readonly. There are cases where the value should not be reset:
   * - If field is displayType=editable, there is condition
   * Resets the field value to its default or null if there is no default value found.
   *
   * @param reset
   * @param fieldValidationModel
   */
  static resetFieldValue(reset, fieldValidationModel) {
    // If value has been cleared before from another condition (HIDDEN or VISIBLE), then skip the reset because there is
    // nothing to reset anyway. The additional flag is needed in order to avoid infinite loop caused by conditions that
    // trigger each other - one that clears the value and another that resets it.
    if (fieldValidationModel && reset && !fieldValidationModel._cleared) {
      var cloneDeep = _.cloneDeep(fieldValidationModel.defaultValue);
      fieldValidationModel.value = cloneDeep || null;
      if (fieldValidationModel.valueLabel) {
        fieldValidationModel.valueLabel = _.cloneDeep(fieldValidationModel.defaultValueLabel) || null;
      }
    }
  }

  /**
   * Field value is cleared when the field becomes inapplicable (hidden). A flag validationModel._cleared is set to
   * prevent resetting the value from another condition once it was cleared.
   *
   * @param clear If the field's value should be cleared or not.
   * @param fieldValidationModel
   */
  static clearFieldValue(clear, fieldValidationModel) {
    if (fieldValidationModel) {
      if (clear) {
        fieldValidationModel.value = null;
        fieldValidationModel._cleared = true;
        if (fieldValidationModel.valueLabel) {
          fieldValidationModel.valueLabel = null;
        }
      } else {
        fieldValidationModel._cleared = false;
      }
    }
  }

  /**
   * Parse the expression given as string like '+[field1] AND [field2] IN ("opt1")'. Each valid token that is found is
   * put in array and the resulting array is returned.
   *
   * @param expression
   * @returns {*}
   */
  parseExpression(expression) {
    if (!this.tokenizedExpressionCache[expression]) {
      let normalized = expression.replace(/[\s\r\n]+/g, '');
      let tokenizer = new ExpressionTokenizer(normalized);
      let tokenizedExpression = [];
      while (tokenizer.hasNext()) {
        let token = tokenizer.next();
        if (!token) {
          throw Error(`Invalid token "${token}" in "${expression}" expression was found!`);
        }
        tokenizedExpression.push(token);
        if (token === 'IN' || token === 'NOTIN') {
          token = tokenizer.nextCollection();
          tokenizedExpression.push(token);
        }
      }
      this.tokenizedExpressionCache[expression] = tokenizedExpression;
    }
    return this.tokenizedExpressionCache[expression];
  }

  /**
   * Converts an expression that comes from a condition validator in the field's model to a boolean expression that might
   * be evaluated to a boolean value true or false.
   * The conversion is done by iterating the expression tokens array and matching each one to predefined regex patterns.
   * On every match is executed specific logic for extracting field value from validation model and converting the value
   * to boolean value. For example if we have the token +[field1] and in the validation model for the field1 there is a
   * value '123', then this expression is evaluated to be true because the + sign infront implies that it expects a value.
   *
   * @param tokens
   * @param model
   * @returns {string}
   */
  static convertToBooleanExpression(tokens, model) {
    let choise = /^(?:\[\w+\])$/;
    let empty = /^(?:-\[\w+\])$/;
    let notEmpty = /^(?:\+\[\w+\])$/;
    let operations = {
      AND: '&&',
      OR: '||'
    };
    let evaluatedTokens = [];
    for (let i = 0; i < tokens.length; i++) {
      let token = tokens[i];
      let value;
      if (empty.test(token)) {
        value = ConditionEvaluator.getFieldValue(token, model);
        value = (value === null) || (value === undefined) || (value === '') || (value === 'false') || (value instanceof Array && value.length === 0);

      } else if (notEmpty.test(token)) {
        value = ConditionEvaluator.getFieldValue(token, model);
        value = !((value === null) || (value === undefined) || (value === '') || (value === 'false') || (value instanceof Array && value.length === 0));

      } else if (choise.test(token)) {
        // Matched token like '[somefield]' which means that this one and the next two tokens are part of collection condition
        // like '[somefield] IN ("opt1","opt2")
        value = ConditionEvaluator.convertCollectionExpression(token, tokens, i, model);
        // Increment the counter here and not in the convertCollectionExpression method because it is a primitive type
        // and thus it is passed by value.
        i += 2;
      } else if (token in operations) {
        value = operations[token];

      } else {
        // All not processed tokens fall through here and are returned as is. For example all braces: '(', ')'
        value = token;

      }

      evaluatedTokens.push(value);
    }
    return evaluatedTokens.join('');
  }

  static convertCollectionExpression(currentToken, tokens, loopCounter, model) {
    let value = ConditionEvaluator.getFieldValue(currentToken, model);
    // Get the next token which has to be one of the IN or NOTIN.
    // The exclusion is NOTIN.
    let inclusive = tokens[loopCounter + 1] === 'IN';
    // Get the collection token
    let collection = tokens[loopCounter + 2];
    // Check if the value of the field exists in the collection and convert to boolean.
    let matched = collection.match(new RegExp(`('|")${value}\\1`, 'i'));
    value = inclusive ? ((matched !== null) && matched[0] !== '') : ((matched === null) || matched[0] === '');
    return value;
  }

  static getFieldValue(token, model) {
    let fieldId = ConditionEvaluator.stripBrackets(token);
    return model[fieldId] && model[fieldId].value;
  }

  static stripBrackets(token) {
    return token.replace(/(\+|\-)?(\d)?\[|\]/g, '');
  }
}

/**
 * The purpose of this class is to parse the provided condition expression string and to return meaningful tokens from it.
 * The parsing is done by iterating the expression character by character and building tokens from them and matching
 * against those tokens against a regex in order to find if the token is recognized as a valid or not. On initialization
 * should be provided the expression string and every call to #next will produce a valid token if found any.
 */
class ExpressionTokenizer {
  constructor(expression) {
    this.expression = expression;
    this.expressionLength = expression.length;
    // This should match one of the following:
    // +[field]
    // -[field]
    // [field]
    // IN
    // NOTIN
    // ("opt1,"opt2",...)
    // AND
    // OR
    // (
    // )
    this.pattern = /^(?:[\+|-]?(\d)?\[.*\]|AND|OR|IN|NOTIN|\(|\)|\s)$/;
    this.collectionPattern = /\(.+?\)/;
    this.pointer = 0;
    this.sub = '';
  }

  next() {
    for (this.pointer; this.pointer <= this.expressionLength; this.pointer++) {
      this.sub += this.expression.substring(this.pointer, this.pointer + 1);
      let match = this.pattern.test(this.sub);
      if (match) {
        let token = this.sub;
        this.sub = '';
        this.pointer = (this.pointer < this.expressionLength) ? this.pointer + 1 : this.expressionLength;
        return token;
      }
    }
  }

  hasNext() {
    return this.pointer < this.expressionLength;
  }

  nextCollection() {
    let subExpr = this.expression.substring(this.pointer);
    let result = this.collectionPattern.exec(subExpr);
    if (result) {
      this.pointer += result[0].length;
      return result[0];
    }
    throw Error(`Can't match regex ${this.collectionPattern} on string ${subExpr}. Please check the definition!`);
  }
}