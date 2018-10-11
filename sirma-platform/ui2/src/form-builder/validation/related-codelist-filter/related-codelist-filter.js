import {Injectable, Inject} from 'app/app';
import {FieldValidator} from 'form-builder/validation/field-validator';
import {CodelistFilterProvider} from 'form-builder/validation/related-codelist-filter/codelist-filter-provider';
import _ from 'lodash';

@Injectable()
@Inject(CodelistFilterProvider)
export class RelatedCodelistFilter extends FieldValidator {

  constructor(codelistFilterProvider) {
    super();
    this.codelistFilterProvider = codelistFilterProvider;
  }

  validate(fieldName, validatorDef, validationModel, flatModel, formControl, definitionId, instanceId) {
    // customize select config to force filtration of values in related field
    let isRelatedFieldChanged = !!(validatorDef.rerender && flatModel[validatorDef.rerender]);
    if (isRelatedFieldChanged) {
      this.codelistFilterProvider.setFilterConfig(instanceId || definitionId, validatorDef.rerender, {
        filterBy: validationModel[fieldName].value,
        inclusive: validatorDef.inclusive,
        filterSource: validatorDef.filter_source
      });
    }

    // Keep previous selected value if new set contains such option or clear selection
    let isRelatedFieldAvailable = formControl && fieldName === formControl.fieldViewModel.identifier &&
      validationModel[validatorDef.rerender] && validationModel[validatorDef.rerender].fieldConfig && validationModel[validatorDef.rerender].fieldConfig.dataLoader;

    if (isRelatedFieldAvailable) {
      let firstSearchTerm = {data: {q: ''}};
      validationModel[validatorDef.rerender].fieldConfig.dataLoader(firstSearchTerm).then((response) => {
        // If current related field value doesn't match the filter, then it should be cleared.
        let reloadDropdown = !!_.find(response.data, (item) => {
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
    return true;
  }
}
