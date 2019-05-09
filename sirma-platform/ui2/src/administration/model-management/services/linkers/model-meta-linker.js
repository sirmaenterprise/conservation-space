import {Inject, Injectable} from 'app/app';
import {ModelDescriptionLinker} from './model-description-linker';
import {ModelRule} from 'administration/model-management/model/validation/model-rule';
import {ModelRuleExpression} from 'administration/model-management/model/validation/model-rule-expression';
import {ModelRuleOutcome} from 'administration/model-management/model/validation/model-rule-outcome';
import _ from 'lodash';

/**
 * Service which builds and links provided model of type {@link ModelMetaData} or any type extending off of it
 * with a meta data provided as a restful response.
 *
 * @author Svetlozar Iliev
 */
@Injectable()
@Inject(ModelDescriptionLinker)
export class ModelMetaDataLinker {

  constructor(modelDescriptionLinker) {
    this.modelDescriptionLinker = modelDescriptionLinker;
  }

  /**
   * Links the provided data with a meta model of type {@link ModelMetaData}
   *
   * @param model - the model meta object {@link ModelMetaData}
   * @param data - response provided by a restful service.
   * @returns {*} - linked model meta data
   */
  linkMetaData(model, data) {
    this.setCoreModel(model, data).setValidationModel(model, data);
    this.modelDescriptionLinker.insertDescriptions(model, data.labels);
    this.modelDescriptionLinker.insertTooltips(model, data.descriptions);
    return model;
  }

  /**
   * Sets the base properties for a given {@link ModelMeta} model
   *
   * @param model - a given {@link ModelMeta} model
   * @param data - raw external data from which to build base properties
   * @returns {ModelMetaDataLinker} - reference to present service instance
   */
  setCoreModel(model, data) {
    model.setType(data.type);
    model.setOrder(data.order);
    model.setOptions(data.options);
    model.setDefaultValue(data.defaultValue);
    return this;
  }

  /**
   * Sets the validation model for a given {@link ModelMeta} model
   *
   * @param model - a given {@link ModelMeta} model
   * @param data - raw external data from which to build validation model
   * @returns {ModelMetaDataLinker} - reference to present service instance
   */
  setValidationModel(model, data) {
    let validationModel = model.getValidationModel();
    validationModel.setAffected(data.validationModel.affected || []);
    validationModel.getValidationRules().setRules(this.createModelRules(data));

    let restrictions = validationModel.getRestrictions();
    restrictions.setUpdateable(data.validationModel.updateable);
    restrictions.setMandatory(data.validationModel.mandatory);
    restrictions.setVisible(data.visible);

    return this;
  }

  createModelRules(data) {
    let rules = data.validationModel.rules || [];
    let modelRules = [];
    rules.forEach(rule => {
      let modelRule = new ModelRule();
      let outcome = rule.outcome || {};
      let expressions = rule.expressions || [];
      modelRule.setValues(rule.values)
        .setCondition(rule.condition)
        .setExpressions(this.createModelExpressions(expressions))
        .setErrorLabel(rule.errorLabel)
        .setOutcome(this.createModelOutcome(outcome, data));
      modelRules.push(modelRule);
    });
    return modelRules;
  }

  createModelExpressions(expressions) {
    let modelExpressions = [];
    expressions.forEach(expression => {
      modelExpressions.push(new ModelRuleExpression(expression.field, expression.operation, expression.values));
    });
    return modelExpressions;
  }

  createModelOutcome(outcome, data) {
    let ruleOutcome = new ModelRuleOutcome();
    ruleOutcome.setUpdateable(this.getOrDefault(outcome.updateable, data.validationModel.updateable));
    ruleOutcome.setMandatory(this.getOrDefault(outcome.mandatory, data.validationModel.mandatory));
    ruleOutcome.setVisible(this.getOrDefault(outcome.visible, data.visible));
    return ruleOutcome;
  }

  getOrDefault(initialValue, defaultValue) {
    return !_.isNull(initialValue) && !_.isUndefined(initialValue) ? initialValue : defaultValue;
  }
}