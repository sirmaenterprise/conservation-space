import {ModelDescription} from 'administration/model-management/model/model-value';

/**
 * Base class for working with descriptions in multiple languages.
 *
 * Descriptions for this base model must be provided as an instance of the {@link ModelDescription} class.
 *
 * For specifying the description that is in the user's current language, there is a <code>description</code> field.
 *
 * @author Mihail Radkov
 * @see ModelDescription
 */
export class Described {

  constructor() {
    this.descriptions = {};
    this.description = null;
  }

  /**
   * Returns the map of all descriptions.
   */
  getDescriptions() {
    return this.descriptions;
  }

  /**
   * Replaces the map of descriptions with the provided one.
   */
  setDescriptions(descriptions) {
    this.descriptions = descriptions;
    return this;
  }

  /**
   * Returns a description for the given language/locale.
   */
  getDescriptionByLanguage(locale) {
    return this.descriptions[locale];
  }

  /**
   * Adds the provided description to the map of descriptions.
   */
  addDescription(description) {
    if (description instanceof ModelDescription) {
      let lang = description.getLanguage();
      this.descriptions[lang] = description;
    }
    return this;
  }

  /**
   * Returns the description for the user's current language.
   */
  getDescription() {
    return this.description;
  }

  /**
   * Sets the given description that is in the user's current language.
   */
  setDescription(description) {
    this.description = description;
    return this;
  }

}