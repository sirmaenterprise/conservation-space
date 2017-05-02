import {Injectable} from 'app/app';
import {FieldValidator} from 'form-builder/validation/field-validator';
import _ from 'lodash';

@Injectable()
export class RelatedCodelistFilter extends FieldValidator {

  validate(fieldName, validatorDef, validationModel, flatModel, formControl) {
    let isRelatedFieldChanged = validatorDef.rerender && validationModel[fieldName].value;
    if (isRelatedFieldChanged && flatModel[validatorDef.rerender]) {
      flatModel[validatorDef.rerender].codelistFilters = {
        filterBy: validationModel[fieldName].value,
        inclusive: validatorDef.inclusive,
        filterSource: validatorDef.filter_source
      };
      if (formControl && fieldName === formControl.fieldViewModel.identifier) {
        let firstSearchTerm = {data: {q: ''}};
        formControl.form[validatorDef.rerender].fieldConfig.dataLoader(firstSearchTerm).then((response) => {
          let reloadDropdown = _.find(response.data, (item) => {
            if (validationModel[validatorDef.rerender].value instanceof Array) {
              return validationModel[validatorDef.rerender].value.indexOf(item.value) > -1;
            }
            return item.value === validationModel[validatorDef.rerender].value;
          });
          if (!reloadDropdown) {
            validationModel[validatorDef.rerender].value = validationModel[validatorDef.rerender].value instanceof Array ? [] : null;
          }
        });
      }
    }
    return true;
  }
}
