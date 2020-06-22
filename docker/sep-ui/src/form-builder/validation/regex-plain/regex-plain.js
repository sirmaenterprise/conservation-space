import {Injectable} from 'app/app';
import {FieldValidator} from 'form-builder/validation/field-validator';

/**
 * Regex validator that validates model values based on regex patterns without trimming or any preprocessing.
 */
@Injectable()
export class RegexPlain extends FieldValidator {

  constructor() {
    super();
    this.regexCache = {};
  }

  /**
   * Field values are not trimmed before validation. Fields that has no value are considered to be
   * invalid. Missing regex pattern is considered an error.
   */
  validate(fieldName, validatorDef, validationModel) {
    let isValid = false;
    let viewValue = this.getViewValueAsString(fieldName, validationModel);
    let pattern = validatorDef.context.pattern;
    if (!pattern) {
      throw new Error(`Not provided field "${fieldName}" validation pattern!`);
    }
    let regex = this.regexCache[pattern];
    try {
      if (!regex) {
        regex = new RegExp('^' + pattern + '$');
        this.regexCache[pattern] = regex;
      }
      isValid = regex.test(viewValue);
    } catch (err) {
      throw new Error(`Wrong regex pattern for field "${fieldName}": ${pattern}`);
    }
    this.setWasInvalid(isValid, validationModel[fieldName]);
    return isValid;
  }

  getRegexCache() {
    return this.regexCache;
  }

}