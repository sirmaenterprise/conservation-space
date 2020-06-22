import {Injectable} from 'app/app';
import _ from 'lodash';

export const MAX_CL_LENGTH = 40;
export const MAX_CV_LENGTH = 100;
export const MAX_NAME_LENGTH = 256;
const COMPARE_OPTIONS = {sensitivity: 'base'};
const IDENTIFIER_REGEX = new RegExp('^[a-zA-Z0-9_/\\-\\.\\+]+$');

/**
 * Performs validation upon controlled vocabulary and its values.
 *
 * To validate values, they should either be marked as new with isNew property or as dirty with isModified.
 *
 * Each validated list or value will be assigned with validationModel property.
 *
 * @author Mihail Radkov
 */
@Injectable()
export class CodelistValidationService {

  validate(codeLists, codeList) {
    // Reset validationModel & validate
    this.assignValidationModel(codeList);
    this.validateCode(codeList, codeLists, MAX_CL_LENGTH);

    codeList.values.filter(value => {
      // Validate only new or dirty values to avoid performance issues
      return value.isNew || value.isModified;
    }).forEach(value => {
      this.assignValidationModel(value);
      let valid = this.validateCode(value, codeList.values, MAX_CV_LENGTH);
      if (!valid) {
        codeList.validationModel.valid = false;
      }
    });
  }

  assignValidationModel(code) {
    this.assignIdentifierValidationModel(code);

    if (code.descriptions) {
      // each description should require only the name validation model
      _.forEach(code.descriptions, d => this.assignNameValidationModel(d));

      // prepare description ref validation model
      this.assignDescriptionReferenceModel(code);
    }
  }

  assignBaseValidationModel(code) {
    // base validation control
    code.validationModel = {
      valid: true
    };
  }

  assignIdentifierValidationModel(code) {
    // base model is required to exist
    this.assignBaseValidationModel(code);

    // attach identifier validation model
    code.validationModel['id'] = {
      valid: true
    };
  }

  assignNameValidationModel(code) {
    // base model is required to exist
    this.assignBaseValidationModel(code);

    // attach name validation model
    code.validationModel['name'] = {
      valid: true
    };
  }

  assignDescriptionReferenceModel(code) {
    let baseDescription = code.description;
    let referenceDescription = code.descriptions[baseDescription.language];

    if (!baseDescription || !referenceDescription) {
      return;
    }
    // set simply a reference to the validation model to the base description
    baseDescription.validationModel = referenceDescription.validationModel;
  }

  validateCode(code, codes, maxIdSize) {
    let validDescriptions = this.validateDescriptions(code, codes);
    let validId = this.validateIdentifier(code, codes, maxIdSize);

    code.validationModel.valid = validId && validDescriptions;
    return code.validationModel.valid;
  }

  validateIdentifier(code, codes, maxIdSize) {
    let isIdValid = this.validateField(code, code.id, 'id', maxIdSize) && this.validateIdentifierCharacters(code) && this.validateUniqueness(code, codes, 'id');
    code.validationModel['id'].valid = isIdValid;
    return isIdValid;
  }

  validateIdentifierCharacters(code) {
    if (IDENTIFIER_REGEX.test(code.id)) {
      return true;
    }
    code.validationModel['id'].valid = false;
    code.validationModel['id'].invalidCharacters = true;
    return false;
  }

  validateDescriptions(code, codes) {
    let languages = Object.keys(code.descriptions);

    let areAllNamesValid = _.filter(languages, language => {
      let description = code.descriptions[language];
      let isBaseDescription = language === code.description.language;

      // skip non mandatory empty description when not the base
      if (!description.name && !isBaseDescription) {
        return true;
      }

      // perform only unique validation for each language & name, mandatory only for base lang
      let comparator = (left, right) => this.equals(left.name, right.descriptions[language].name);
      let isNameValid = this.validateField(description, description.name, 'name', MAX_NAME_LENGTH)
        && this.validateUniqueness(description, codes, 'name', comparator);

      if (!isNameValid && description.validationModel.valid) {
        // invalidate the validation model only if it's necessary
        description.validationModel.valid = false;
      }

      // set final result of the validation to the name model
      description.validationModel['name'].valid = isNameValid;

      // filter condition
      return isNameValid;
    }).length === languages.length;

    return areAllNamesValid;
  }

  validateField(code, field, fieldName, maxSize) {
    if (!field) {
      code.validationModel[fieldName].empty = true;
      return false;
    } else if (field.length > maxSize) {
      code.validationModel[fieldName].exceedsSize = true;
      code.validationModel[fieldName].maxSize = maxSize;
      return false;
    }
    return true;
  }

  /**
   * Validates the uniqueness of a given code for a given collection of codes.
   * The code to be validated must be present inside the collection of codes.
   *
   * @param code - the code to be checked for uniqueness
   * @param codes - the collection of codes, must contain the code to be validated
   * @param model - the validation model property at which uniqueness result would be assigned
   * @param comparator - either a callback comparison method or an existing property by which to compare
   * @returns {boolean} - true if unique, false otherwise
   */
  validateUniqueness(code, codes, model, comparator) {
    let isUnique = this.checkUniqueness(code, codes, comparator || model);
    if (!isUnique) {
      code.validationModel[model].exists = true;
    }
    return isUnique;
  }

  /**
   * Validates uniqueness by comparing a given code with a collection of codes.
   * Comparison can be implemented by an explicit callback comparator or an
   * existing property which belongs to the object being validated
   *
   * @param code - the code to be validated
   * @param codes - the collection of codes against which to validate uniqueness
   * @param comparator - either a callback comparison method or an existing property by which to compare
   * @returns {boolean} - true if unique, false otherwise
   */
  checkUniqueness(code, codes, comparator) {
    if (!_.isFunction(comparator)) {
      let field = String(comparator);
      comparator = (left, right) => this.equals(this.getField(left, field), this.getField(right, field));
    }
    return codes.filter(c => comparator(code, c)).length === 1;
  }

  /**
   * Extracts a field represented as a string from a given object.
   * This method supports a deep property access for objects
   * For example field = level1.level2.level3.levelN
   *
   * @param code the object from which to extract the field
   * @param field the field represented as a string
   * @returns the value of the property for the given object
   */
  getField(code, field) {
    return _.get(code, field);
  }

  /**
   * Checks if two strings are equal ignoring case sensitivity
   * @param left - the first string
   * @param right - the second string
   * @returns {boolean} - true if equal, false otherwise
   */
  equals(left, right) {
    return left.localeCompare(right, [], COMPARE_OPTIONS) === 0;
  }
}
