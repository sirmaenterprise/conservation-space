import {Inject, Injectable} from 'app/app';
import {ModelDescription} from 'administration/model-management/model/model-value';
import {ModelManagementLanguageService} from 'administration/model-management/services/utility/model-management-language-service';
import _ from 'lodash';

/**
 * Service, which builds and links descriptions and tooltips to a given model. The descriptions as well as the
 * tooltips must be objects for which keys are language identifiers and values associated with them
 *
 * @author Svetlozar Iliev
 */
@Injectable()
@Inject(ModelManagementLanguageService)
export class ModelDescriptionLinker {

  constructor(modelManagementLanguageService) {
    this.modelManagementLanguageService = modelManagementLanguageService;
    this.defaultLanguage = this.modelManagementLanguageService.getDefaultLanguage();
  }

  /**
   * Insert labels as descriptions for a provided model. If the provided labels are not properly
   * defined a default description based on the model identifier is manually inserted.
   *
   * @param model - the model for which to insert descriptions
   * @param labels - can be either a map of labels consisting of language - value pairs, or a primitive value
   */
  insertDescriptions(model, labels) {
    // for each of the languages create & add a model description extracting the value for lang
    this.getLanguages(labels).forEach(key => model.addDescription(this.getModelDescription(key, labels[key])));

    // make sure that a value for default language is always present
    if (!this.getDescriptionByLanguage(model, this.defaultLanguage)) {
      model.addDescription(this.getModelDescription(this.defaultLanguage, model.getId()));
    }

    // resolve the description based on present languages
    model.setDescription(this.getBaseDescription(model));
  }

  /**
   * Insert tooltips for a provided model. The tooltip data is more detailed description of a model,
   * which is addition to the label data.
   *
   * @param model - the model for which to insert tooltip data
   * @param tooltips - can be either a map of tooltips consisting of language - value pairs, or a primitive value
   */
  insertTooltips(model, tooltips) {
    // for each of the languages create & add a model full description extracting the value for lang
    this.getLanguages(tooltips).forEach(key => model.addTooltip(this.getModelDescription(key, tooltips[key])));

    // resolve the full description based on present languages
    model.setTooltip(this.getBaseTooltip(model));
  }

  getLanguages(values) {
    return _.isObject(values) ? Object.keys(values) : [];
  }

  getModelDescription(language, value) {
    return new ModelDescription(ModelManagementLanguageService.transformLanguage(language), value || '');
  }

  getBaseDescription(model) {
    return this.modelManagementLanguageService.getApplicableDescription((locale) => this.getDescriptionByLanguage(model, locale));
  }

  getDescriptionByLanguage(model, locale) {
    let description = model.getDescriptionByLanguage(locale);
    return this.getValueIfValid(description);
  }

  getBaseTooltip(model) {
    return this.modelManagementLanguageService.getApplicableDescription((locale) => this.getTooltipByLanguage(model, locale));
  }

  getTooltipByLanguage(model, locale) {
    let tooltip = model.getTooltipByLanguage(locale);
    return this.getValueIfValid(tooltip);
  }

  getValueIfValid(value) {
    // a value is valid, when it exists and is not empty
    return value && !value.isEmpty() && value;
  }
}