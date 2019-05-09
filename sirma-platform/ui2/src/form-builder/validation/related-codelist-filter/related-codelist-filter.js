import {Injectable, Inject} from 'app/app';
import {FieldValidator} from 'form-builder/validation/field-validator';
import {CodelistRestService} from 'services/rest/codelist-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {CodelistFilterProvider} from 'form-builder/validation/related-codelist-filter/codelist-filter-provider';
import _ from 'lodash';

@Injectable()
@Inject(CodelistFilterProvider, CodelistRestService, PromiseAdapter)
export class RelatedCodelistFilter extends FieldValidator {

  constructor(codelistFilterProvider, codelistRestService, promiseAdapter) {
    super();
    this.promiseAdapter = promiseAdapter;
    this.codelistRestService = codelistRestService;
    this.codelistFilterProvider = codelistFilterProvider;
  }

  validate(fieldName, validatorDef, validationModel, flatModel, formControl, definitionId, instanceId) {
    // customize select config to force filtration of values in related field
    let isRelatedFieldChanged = !!(fieldName && flatModel[fieldName]);
    if (isRelatedFieldChanged) {
      this.codelistFilterProvider.setFilterConfig(instanceId || definitionId, fieldName, {
        filterBy: validationModel[validatorDef.rerender].value,
        inclusive: validatorDef.inclusive,
        filterSource: validatorDef.filter_source
      });
    }

    // Keep previous selected value if new set contains such option or clear selection
    let isRelatedFieldAvailable = formControl && validatorDef.rerender === formControl.fieldViewModel.identifier &&
      validationModel[fieldName] && validationModel[fieldName].fieldConfig && validationModel[fieldName].fieldConfig.dataLoader;

    if (isRelatedFieldAvailable) {
      let firstSearchTerm = {data: {q: ''}};
      validationModel[fieldName].fieldConfig.dataLoader(firstSearchTerm).then((response) => {
        // If current related field value doesn't match the filter, then it should be cleared.
        let reloadDropdown = !!_.find(response.data, (item) => {
          if (validationModel[fieldName].value instanceof Array) {
            return validationModel[fieldName].value.indexOf(item.value) > -1;
          }
          return item.value === validationModel[fieldName].value;
        });
        if (!reloadDropdown) {
          validationModel[fieldName].value = validationModel[fieldName].value instanceof Array ? [] : null;
        }
      });
    } else {
      // otherwise validate current field value according to existing dynamic filter and provide feedback if the value
      // is invalid
      let opts = {};
      const codelistFilters = this.codelistFilterProvider.getFilterConfig(instanceId, fieldName);
      if (codelistFilters) {
        opts.filterBy = validationModel[validatorDef.rerender].value;
        opts.inclusive = codelistFilters.inclusive;
        opts.filterSource = codelistFilters.filterSource;
        opts.codelistNumber = flatModel[fieldName].codelist;

        return this.codelistRestService.getCodelist(opts).then(({data: codelists}) => {
          let currentValue = validationModel[fieldName].value;
          // assume missing value as valid
          return !currentValue || this.valueIsPresent(currentValue, codelists);
        });
      }
      return this.promiseAdapter.resolve(true);
    }
    return this.promiseAdapter.resolve(true);
  }

  valueIsPresent(currentValue, codelists = []) {
    let toCheck = currentValue instanceof Array ? currentValue : [currentValue];
    return toCheck.every(value => {
      return codelists.some(cl => {
        return cl.value === value;
      });
    });
  }
}