import {ValidationService} from 'form-builder/validation/validation-service';

export class TemplateRuleUtils {

  /**
   * Eligible fields for template rules are boolean fields or single value codelist fields, both editable and mandatory.
   * Multivalue codelist fields are currently not supported.
   * @param viewModelField to be checked
   * @returns {boolean}
   */
  static isEligible(viewModelField) {
    var editable = viewModelField.displayType === ValidationService.DISPLAY_TYPE_EDITABLE;
    var mandatory = viewModelField.isMandatory;
    var booleanField = viewModelField.dataType === 'boolean';
    var singleValueCodelistField = viewModelField.codelist && !viewModelField.multivalue;
    return (booleanField || singleValueCodelistField) && editable && mandatory;
  }
}
