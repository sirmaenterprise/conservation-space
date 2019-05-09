import {View, Component, Inject, NgElement} from 'app/app';
import {FormControl} from 'form-builder/form-control';
import {DEFAULT_VALUE_PATTERN} from 'form-builder/validation/calculation/calculation';
import 'filters/array-to-csv';
import './codelist-select/codelist-select';
import {CodelistRestService} from 'services/rest/codelist-service';
import {CodelistFilterProvider} from 'form-builder/validation/related-codelist-filter/codelist-filter-provider';
import {TranslateService} from 'services/i18n/translate-service';
import template from './codelist.html!text';

@Component({
  selector: 'seip-codelist',
  properties: {
    'formWrapper': 'form-wrapper',
    'identifier': 'identifier'
  }
})
@View({
  template
})
@Inject(CodelistRestService, TranslateService, NgElement, CodelistFilterProvider)
export class Codelist extends FormControl {

  constructor(codelistRestService, translateService, $element, codelistFilterProvider) {
    super();
    this.translateService = translateService;
    this.codelistRestService = codelistRestService;
    this.codelistFilterProvider = codelistFilterProvider;
    this.$element = $element;
  }

  ngOnInit() {
    this.fieldConfig = this.getFieldConfig();
    this.validationModel[this.fieldViewModel.identifier].sharedCodelistData =
      this.validationModel[this.fieldViewModel.identifier].sharedCodelistData || {};
    //the fieldConfig is needed only in the form used in the related codelist filter.
    this.validationModel[this.fieldViewModel.identifier].fieldConfig = this.fieldConfig;

    this.initElement();

    this.fieldViewModelSubscription = this.fieldViewModel.subscribe('propertyChanged', (propertyChanged) => {
      this.executeCommonPropertyChangedHandler(propertyChanged);
    });
    this.validationModelSubscription = this.validationModel[this.fieldViewModel.identifier].subscribe('propertyChanged', (propertyChanged) => {
      let changedProperty = Object.keys(propertyChanged)[0];
      if (changedProperty === 'value') {
        this.validateAndUpdateModel(this.fieldViewModel.identifier);
      } else {
        this.executeCommonPropertyChangedHandler(propertyChanged);
      }
    });
  }

  getFieldConfig() {
    let opts = {
      codelistNumber: this.fieldViewModel.codelist,
      customFilters: this.fieldViewModel.filters
    };
    let setEditedByUserCallback;
    if (this.isControl(DEFAULT_VALUE_PATTERN)) {
      setEditedByUserCallback = () => {
        this.fieldViewModel.editedByUser = true;
      };
    }
    return {
      placeholder: this.translateService.translateInstant('select.value.placeholder'),
      dataLoader: (term) => {
        opts.q = term !== null ? term.data.q : undefined;
        const codelistFilters = this.codelistFilterProvider.getFilterConfig(this.objectId || this.definitionId, this.fieldViewModel.identifier);
        if (codelistFilters) {
          opts.filterBy = codelistFilters.filterBy;
          opts.inclusive = codelistFilters.inclusive;
          opts.filterSource = codelistFilters.filterSource;
        }
        return this.codelistRestService.getCodelist(opts);
      },
      dataConverter: (data) => {
        return this.convertData(data);
      },
      onSelectCallback: setEditedByUserCallback,
      defaultToSingleValue: true,
      disabled: this.fieldViewModel.disabled,
      multiple: this.fieldViewModel.multivalue,
      allowClear: !this.fieldViewModel.multivalue
    };
  }

  convertData(data) {
    //shared data must be reset on each dropdown opening
    this.validationModel[this.fieldViewModel.identifier].sharedCodelistData = {};
    return data.data.map((element) => {
      let select2Data = {id: element.value, text: element.label};
      this.validationModel[this.fieldViewModel.identifier].sharedCodelistData[select2Data.id] = select2Data.text;
      return select2Data;
    });
  }

  validateAndUpdateModel(identifier) {
    this.validateForm();
    let code = this.validationModel[identifier].value;
    if (!(code instanceof Array)) {
      code = [code];
    }
    this.validationModel[identifier]['valueLabel'] = code.map((codeValue) => {
      return this.validationModel[this.fieldViewModel.identifier].sharedCodelistData[codeValue];
    }).join(', ');
  }

  notifyWhenReady() {
    return true;
  }
}
