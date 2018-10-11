import {Injectable} from 'app/app';
import {FieldValidator} from 'form-builder/validation/field-validator';

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
    if (viewValue) {
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
        // If the field contains invalid data, then mark it as visible to allow users to fix it.
        if (!isValid) {
          flatModel[fieldName].rendered = true;
        }
      } catch (err) {
        throw new Error(`Wrong regex pattern for field "${fieldName}": ${pattern}`);
      }
    }
    this.setWasInvalid(isValid, validationModel[fieldName]);
    return isValid;
  }

  getRegexCache() {
    return this.regexCache;
  }

}