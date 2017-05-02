import {Injectable} from 'app/app';
import {FieldValidator} from 'form-builder/validation/field-validator'

/**
 * Regex validator that validates model values based on regex patterns.
 *
 * @author svelikov
 */
@Injectable()
export class Regex extends FieldValidator {

  constructor() {
    super();
    this.regexCache = {};
  }

  /**
   * Field values are trimmed before validation. Fields that has no value are not validated and are considered to be
   * valid. Missing regex pattern is considered an error.
   *
   * @returns {boolean}
   */
  validate(fieldName, validatorDef, validationModel, flatModel) {
    let isValid = true;
    let viewValue = this.getViewValueAsString(fieldName, validationModel);
    viewValue = viewValue ? viewValue.trim() : viewValue;
    if (!viewValue) {
      return isValid;
    }
    var pattern = validatorDef.context.pattern;
    if (!pattern) {
      throw new Error(`Not provided field "${fieldName}" validation pattern!`);
    }
    var regex = this.regexCache[pattern];
    try {
      if (!regex) {
        this.regexCache[pattern] = regex = new RegExp('^' + pattern + '$');
      }
      isValid = regex.test(viewValue);
      // If the field contains invalid data, then mark it as visible to allow users to fix it.
      if (!isValid) {
        flatModel[fieldName].rendered = true;
      }
      return isValid;
    } catch (err) {
      throw new Error(`Wrong regex pattern for field "${fieldName}": ${pattern}`);
    }
  }

  getRegexCache() {
    return this.regexCache;
  }

}