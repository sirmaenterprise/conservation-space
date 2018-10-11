import {View, Component, Inject, NgScope, NgElement, NgTimeout} from 'app/app';
import {FormControl} from 'form-builder/form-control';
import {CodelistRestService} from 'services/rest/codelist-service';
import {CodelistFilterProvider} from 'form-builder/validation/related-codelist-filter/codelist-filter-provider';
import 'filters/array-to-csv';
import template from './codelist-list.html!text';

const CHECKBOX = 'checkbox';
const RADIO = 'radio';

@Component({
  selector: 'seip-codelist-list',
  properties: {
    'formWrapper': 'form-wrapper',
    'identifier': 'identifier'
  }
})
@View({
  template
})
@Inject(NgScope, NgElement, NgTimeout, CodelistRestService, CodelistFilterProvider)
export class CodelistList extends FormControl {

  constructor($scope, $element, $timeout, codelistRestService, codelistFilterProvider) {
    super();
    this.$scope = $scope;
    this.$element = $element;
    this.$timeout = $timeout;
    this.codelistRestService = codelistRestService;
    this.codelistFilterProvider = codelistFilterProvider;

    // specify control visualization
    this.inputType = this.fieldViewModel.multivalue ? CHECKBOX : RADIO;

    this.initDataLoader();
    this.setSelectedValues();
    this.loadData();
  }

  ngOnInit() {
    this.initElement();
    this.fieldViewModelSubscription = this.fieldViewModel.subscribe('propertyChanged', (propertyChanged)=> {
      this.executeCommonPropertyChangedHandler(propertyChanged);
    });
    this.validationModelSubscription = this.validationModel[this.fieldViewModel.identifier].subscribe('propertyChanged', (propertyChanged)=> {
      let changedProperty = Object.keys(propertyChanged)[0];
      if (changedProperty === 'value') {
        this.$scope.$evalAsync(() => {
          this.setSelectedValues();
        });
        this.validateForm();
      } else {
        this.executeCommonPropertyChangedHandler(propertyChanged);
      }
    });
  }

  /**
   * Handle checkbox selection. The property may be single or multi valued.
   */
  setSelectedValues() {
    this.selectedValues = {};
    let currentValue = this.validationModel[this.fieldViewModel.identifier].value;
    if (currentValue) {
      // handle multi selection
      if (currentValue instanceof Array) {
        currentValue.forEach((value) => {
          this.selectedValues[value] = true;
        });
      } else {
        // handle single selection
        this.selectedValues[currentValue] = true;
      }
    }
  }

  initDataLoader() {
    if (!this.validationModel[this.fieldViewModel.identifier].fieldConfig) {
      this.validationModel[this.fieldViewModel.identifier].fieldConfig = {
        dataLoader: () => {
          return this.loadData();
        }
      };
    }
  }

  selectValue(newValue) {
    // check for value selection
    if (this.selectedValues[newValue]) {
      if (!this.validationModel[this.fieldViewModel.identifier].value) {
        this.validationModel[this.fieldViewModel.identifier].value = [];
      }
      this.validationModel[this.fieldViewModel.identifier].value.push(newValue);
    } else {
      let newValueIndex = this.validationModel[this.fieldViewModel.identifier].value.indexOf(newValue);
      // the value is not selected but need to be removed from the model
      if (newValueIndex !== -1) {
        this.validationModel[this.fieldViewModel.identifier].value.splice(newValueIndex, 1);
      }
    }
  }

  loadData() {
    let opts = {
      codelistNumber: this.fieldViewModel.codelist,
      customFilters: this.fieldViewModel.filters
    };
    // in case we have dependencies between codelist fields
    const codelistFilters = this.codelistFilterProvider.getFilterConfig(this.objectId || this.definitionId, this.fieldViewModel.identifier);
    if (codelistFilters) {
      opts.filterBy = codelistFilters.filterBy;
      opts.inclusive = codelistFilters.inclusive;
      opts.filterSource = codelistFilters.filterSource;
    }
    return this.codelistRestService.getCodelist(opts).then((codelistData) => {
      this.validationModel[this.fieldViewModel.identifier].sharedCodelistData = {};
      codelistData.data.map((element) => {
        this.validationModel[this.fieldViewModel.identifier].sharedCodelistData[element.value] = element.label;
      });

      // reinitialize after view is reloaded
      this.$timeout(() => {
        this.initElement();
      });

      return codelistData;
    });
  }

  notifyWhenReady() {
    return true;
  }
}