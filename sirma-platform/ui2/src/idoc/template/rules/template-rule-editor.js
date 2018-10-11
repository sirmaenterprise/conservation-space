import {Component, View, Inject} from 'app/app';
import {Configurable} from 'components/configurable';
import {DefinitionService} from 'services/rest/definition-service';
import {InstanceObject} from 'models/instance-object';
import {Logger} from 'services/logging/logger';
import {ValidationService} from 'form-builder/validation/validation-service';
import {TemplateRuleUtils} from 'idoc/template/rules/template-rule-utils';
import {TemplateService} from 'services/rest/template-service';
import {TranslateService} from 'services/i18n/translate-service';
import _ from 'lodash';
import {FormWrapper} from 'form-builder/form-wrapper';
import {ModelUtils} from 'models/model-utils';
import './template-rule-editor.css!';
import template from './template-rule-editor.html!text';

/**
 * Provides a form for editing template rules
 *
 * Config properties:
 * type - type for which a rule is created.
 * template - an object representing the template being edited..
 *
 * When the rule is changed, the rule config property gets updated and the wrapping component should use
 * the same property to get the value of the new rule.
 */
@Component({
  selector: 'seip-template-rule-editor',
  properties: {
    'config': 'config'
  }
})
@View({
  template
})
@Inject(DefinitionService, TemplateService, TranslateService, Logger)
export class TemplateRuleEditor extends Configurable {

  constructor(definitionService, templateService, translateService, logger) {
    super({});
    this.definitionService = definitionService;
    this.templateService = templateService;
    this.translateService = translateService;
    this.subscriptions = [];
    this.logger = logger;
  }

  ngOnInit() {
    this.template = this.config.template;

    this.loadForm(this.template).then(() => {
      this.config.formAvailable = !this.errorMessage;
    });
  }

  loadForm(template) {
    if (template.primary) {
      // the order of calls matter because the message produced by the buildModel() method is more specific
      return this.checkIfTemplateIsPrimaryWithExistingSecondaryTemplates(template).then(() => {
        return this.buildForm(template.forObjectType);
      });
    }

    return this.buildForm(template.forObjectType);
  }

  checkIfTemplateIsPrimaryWithExistingSecondaryTemplates(template) {
    let rules;
    if (template.rules) {
      rules = this.parseRuleExpression(template.rules);
    }

    return this.templateService.loadTemplates(template.forObjectType, template.purpose, rules).then(respnse => {
      let secondaryTemplate = _.find(respnse.data, currentTemplate => {
        return !currentTemplate.primary && currentTemplate.correspondingInstance;
      });

      if (secondaryTemplate) {
        this.errorMessage = this.translateService.translateInstant('idoc.template.rules.existing_secondary_of_type');
      }
    });
  }

  buildForm(typeUnderModeling) {
    return this.definitionService.getDefinitions(typeUnderModeling).then(result => {
      let models = result.data[typeUnderModeling];
      this.filterNonCodelistAndNonBooleanFields(models);

      this.processModel(models);

      // used for testing assertions
      this.models = {
        validationModel: models.validationModel,
        viewModel: models.viewModel
      };

      // use InstanceObject because it converts "model as data" to "model as objects" suitable for the form builder
      this.formModels = new InstanceObject(null, models).getModels();

      this.initModelValuesFromRule(this.formModels.validationModel, this.formModels.viewModel, this.template.rules);

      this.form = {
        config: {
          formViewMode: FormWrapper.FORM_VIEW_MODE_EDIT
        },
        formConfig: {
          models: this.formModels
        }
      };

      let eligibleFields = this.getEligibleFields();

      if (eligibleFields.length === 0) {
        this.errorMessage = this.translateService.translateInstant('idoc.template.rules.not_applicable_definition');
      }

      eligibleFields.forEach(viewModelField => {
        let validationField = this.formModels.validationModel[viewModelField.identifier];
        if (validationField) {
          let subscription = validationField.subscribe('propertyChanged', () => {
            this.buildRuleFromModel();
          });

          this.subscriptions.push(subscription);
        }
      });
    });
  }

  getEligibleFields() {
    let result = [];

    ModelUtils.walkModelTree(this.formModels.viewModel.fields, {}, viewModelField => {
      if (!ModelUtils.isRegion(viewModelField) && viewModelField.displayType === ValidationService.DISPLAY_TYPE_EDITABLE) {
        result.push(viewModelField);
      }
    });

    return result;
  }

  filterNonCodelistAndNonBooleanFields(models) {
    ModelUtils.filterFields(models.viewModel.fields, models.validationModel, viewModelField => {
      return TemplateRuleUtils.isEligible(viewModelField);
    });
  }

  /**
   * Performs prerendering logic on the form models.
   * - all fields are made optional because the user can set a subset of properties
   * - single-value codelists fields are made multivalue so the user can construct OR clauses
   *
   * @param models
   */
  processModel(models) {
    ModelUtils.walkModelTree(models.viewModel.fields, models.validationModel, viewModelField => {
      viewModelField.isMandatory = false;

      viewModelField.validators = _.filter(viewModelField.validators, function (validator) {
        return validator.id === 'regex';
      });

      if (viewModelField.codelist && !viewModelField.multivalue) {
        viewModelField.multivalue = true;
      }
    });
  }

  buildRuleFromModel() {
    let rule = '';

    this.getEligibleFields().forEach(viewModelField => {
      let validationField = this.formModels.validationModel[viewModelField.identifier];

      let fieldName = viewModelField.identifier;
      let value = validationField.value;
      if (value) {
        if (_.isBoolean(value)) {
          rule = this.addBooleanValueToRule(rule, fieldName, value);
        } else if (_.isArray(value)) {
          rule = this.addArrayValuesToRule(rule, fieldName, value);
        } else if (_.isString(value)) {
          rule = this.addStringValueToRule(rule, fieldName, value);
        }
      }
    });

    this.template.rules = rule;
  }

  addBooleanValueToRule(rule, name, value) {
    rule = this.appendAndClause(rule);
    rule += name + ' == ' + value;
    return rule;
  }

  addStringValueToRule(rule, name, value) {
    rule = this.appendAndClause(rule);
    rule += name + ' == "' + value + '"';
    return rule;
  }

  addArrayValuesToRule(rule, name, value) {
    if (value.length > 0) {
      rule = this.appendAndClause(rule);

      if (value.length > 1) {
        rule = rule + '(';
      }

      value.forEach(function (element, index) {
        if (index > 0) {
          rule = rule + ' || ';
        }
        rule = rule + name + ' == "' + element + '"';
      });

      if (value.length > 1) {
        rule = rule + ')';
      }
    }

    return rule;
  }

  appendAndClause(rule) {
    if (rule && rule.length > 0) {
      rule = rule + ' && ';
    }
    return rule;
  }

  initModelValuesFromRule(instanceModel, definitionModel, rule) {
    if (rule) {
      let ruleCriteria = this.parseRuleExpression(rule);

      _.forEach(ruleCriteria, (value, key) => {
        let modelProperty = instanceModel[key];

        if (!modelProperty) {
          this.logger.warn(`Property ${key} not found in the model. This can occur when there is an existing rule with fields that are later removed from the model`, false);
          return;
        }

        let definitionProperty = _.find(definitionModel.flatModelMap, function (field) {
          return field.identifier === key;
        });

        if (definitionProperty.dataType === 'boolean') {
          value = (value === 'true');
        }

        if (definitionProperty.multivalue && !_.isArray(value)) {
          value = [value];
        }

        modelProperty.value = value;
      });
    }
  }

  /**
   * Parses a rule and converts it to a key-value object.
   */
  parseRuleExpression(rule) {
    let result = {};

    let expressionEntries = rule.split('&&');

    expressionEntries.forEach(entry => {
      entry = entry.trim();

      //remove wrapping brackets
      if (entry.charAt(0) === '(' && entry.charAt(entry.length - 1) === ')') {
        entry = entry.substring(1, entry.length - 1);
      }

      let orStatements = entry.split('||');

      orStatements.forEach(statement => {
        let keyValue = statement.split('==');
        let name = keyValue[0].trim();
        let value = keyValue[1].trim();

        // also trim the surrounding string quotes
        if (value.charAt(0) === '"') {
          value = value.substring(1, value.length - 1);
        }

        this.setValue(result, name, value);
      });
    });

    return result;
  }

  setValue(valueHolder, name, value) {
    let oldValue = valueHolder[name];

    if (oldValue) {
      if (!_.isArray(oldValue)) {
        valueHolder[name] = [oldValue];
      }

      valueHolder[name].push(value);
    } else {
      valueHolder[name] = value;
    }
  }

  ngOnDestroy() {
    this.subscriptions.forEach(function (subscription) {
      subscription.unsubscribe();
    });
  }

}