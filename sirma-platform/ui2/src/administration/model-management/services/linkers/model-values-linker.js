import {Inject, Injectable} from 'app/app';
import {ModelAttributeTypes} from 'administration/model-management/model/attributes/model-attribute-types';
import {ModelValue} from 'administration/model-management/model/model-value';
import {ModelManagementLanguageService} from 'administration/model-management/services/utility/model-management-language-service';
import _ from 'lodash';

/**
 * Service which builds and links values to a given model. The values can either be an object for
 * which keys are language identifiers and values associated with them, or a simple primitive
 * object such as string, boolean, number etc.
 *
 * @author Svetlozar Iliev
 */
@Injectable()
@Inject(ModelManagementLanguageService)
export class ModelValuesLinker {

  constructor(modelManagementLanguageService) {
    this.modelManagementLanguageService = modelManagementLanguageService;
  }

  /**
   * Insert values for a provided model. Values can be treated as either multi or single valued.
   * Multi valued values are such that are consisting of language - value pairs. Single valued
   * values are a simple primitive value. If the provided values are not properly defined, by
   * default the value is considered single valued.
   *
   * @param model - the model for which to insert descriptions
   * @param values - can be either a map of values consisting of language - value pairs, or a primitive value
   */
  insertValues(model, values) {
    let systemLanguage = this.modelManagementLanguageService.getSystemLanguage();

    if (!ModelAttributeTypes.isMultiValued(model.getType())) {
      // just add the value when a single valued attribute
      model.setValue(new ModelValue(systemLanguage, values));
    } else {
      // for each language add a value related to that language to the multi valued attribute model
      this.getLanguages(values).forEach(key => model.addValue(this.getModelValue(key, values[key])));

      // make sure that a value for the base language is present
      if (!model.getValueByLanguage(systemLanguage)) {
        model.addValue(this.getModelValue(systemLanguage));
      }

      // set the base value of the attribute based on sys lang
      model.setValue(model.getValueByLanguage(systemLanguage));
    }
  }

  getLanguages(values) {
    return _.isObject(values) ? Object.keys(values) : [];
  }

  getModelValue(language, value) {
    return new ModelValue(ModelManagementLanguageService.transformLanguage(language), value || '');
  }
}