import {View, Component, Inject, NgTimeout, NgScope, NgElement} from 'app/app';
import {FormControl} from 'form-builder/form-control';
import template from './codelist.html!text';
import 'filters/array-to-csv';
import {CodelistSelect} from './codelist-select/codelist-select';
import {CodelistRestService} from 'services/rest/codelist-service';
import {TranslateService} from 'services/i18n/translate-service';

@Component({
  selector: 'seip-codelist',
  properties: {
    'fieldViewModel': 'field-view-model',
    'validationModel': 'validation-model',
    'flatFormViewModel': 'flat-form-view-model',
    'form': 'form',
    'validationService': 'validation-service',
    'widgetConfig': 'widget-config',
    'objectId': 'object-id'
  }
})
@View({
  template: template
})
@Inject(CodelistRestService, TranslateService, NgTimeout, NgScope, NgElement)
export class Codelist extends FormControl {

  constructor(codelistRestService, translateService, $timeout, $scope, $element) {
    super();
    this.translateService = translateService;
    this.codelistRestService = codelistRestService;
    this.fieldConfig = this.getFieldConfig();
    this.$timeout = $timeout;
    this.$scope = $scope;
    this.$element = $element;
    this.validationModel[this.fieldViewModel.identifier].sharedCodelistData =
      this.validationModel[this.fieldViewModel.identifier].sharedCodelistData || {};
    //the fieldConfig is needed only in the form used in the related codelist filter.
    this.form[this.fieldViewModel.identifier] = {fieldConfig: this.fieldConfig};
  }

  ngOnInit() {
    this.initElement();

    this.fieldViewModelSubscription = this.fieldViewModel.subscribe('propertyChanged', (propertyChanged)=> {
      this.executeCommonPropertyChangedHandler(propertyChanged);
    });
    this.validationModelSubscription = this.validationModel[this.fieldViewModel.identifier].subscribe('propertyChanged', (propertyChanged)=> {
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
    return {
      placeholder: this.translateService.translateInstant('select.codelist.placeholder'),
      dataLoader: (term)=> {
        opts.q = term !== null ? term.data.q : undefined;
        if (this.fieldViewModel.codelistFilters) {
          opts.filterBy = this.fieldViewModel.codelistFilters.filterBy;
          opts.inclusive = this.fieldViewModel.codelistFilters.inclusive;
          opts.filterSource = this.fieldViewModel.codelistFilters.filterSource;
        }
        return this.codelistRestService.getCodelist(opts);
      },
      dataConverter: (data) => {
        return this.convertData(data);
      },
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
}
