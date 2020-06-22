import {View, Component, Inject} from 'app/app';
import {ModelValue} from 'administration/model-management/model/model-value';
import {ModelManagementLanguageService} from 'administration/model-management/services/utility/model-management-language-service';

import 'administration/model-management/components/validation/model-validation-messages';

import './model-values-view.css!css';
import template from './model-values-view.html!text';

/**
 * Component responsible for rendering all values of a multi
 * valued attribute. Values model is provided through a basic
 * component property and should be of type {@link ModelValue}.
 *
 * @author Svetlozar Iliev
 */
@Component({
  selector: 'model-values-view',
  properties: {
    'attribute': 'attribute',
    'editable': 'editable',
    'onChange': 'on-change'
  }
})
@View({
  template
})
@Inject(ModelManagementLanguageService)
export class ModelValuesView {

  constructor(modelManagementLanguageService) {
    this.modelManagementLanguageService = modelManagementLanguageService;
  }

  ngOnInit() {
    this.modelManagementLanguageService.getLanguages().then(languages => this.appendAdditionalLanguages(languages));
  }

  getValues() {
    return this.attribute.getValues();
  }

  appendAdditionalLanguages(languages) {
    this.languages = ModelManagementLanguageService.transformLanguages(languages.concat(this.attribute.getLanguages()));

    // add missing values for languages
    languages.forEach(language => {
      let hasLanguage = this.attribute.getValueByLanguage(language.value);
      !hasLanguage && this.attribute.addValue(new ModelValue(language.value, ''));
    });

    // sort alphabetically all of the values after all language keys have been inserted
    this.getValues().sort((left, right) => left.getLanguage().localeCompare(right.getLanguage()));
  }

  onModelChange(source, oldValue) {
    let attribute = this.onChange && this.onChange();

    if (this.attribute !== attribute) {
      source.setValue(oldValue);
      this.attribute = attribute;
    }
    return attribute;
  }

  getLanguageLabel(code) {
    return this.languages[code];
  }

  getValidation(value) {
    return this.attribute.getValidationForValue(value);
  }

  isInvalid(value) {
    return this.getValidation(value).isInvalid();
  }

  isDirty(value) {
    return value.isDirty();
  }
}