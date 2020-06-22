import {View, Component, Inject, NgScope} from 'app/app';
import {TranslateService} from 'services/i18n/translate-service';
import {ModelAttribute} from 'administration/model-management/model/attributes/model-attribute';
import {ModelManagementCodelistService} from 'administration/model-management/services/utility/model-management-codelist-service';
import {ModelGenericControl} from 'administration/model-management/components/field/control/model-generic-control';

import 'administration/model-management/components/attributes/bool/model-boolean-attribute';
import 'administration/model-management/components/attributes/select/model-select-attribute';
import 'components/hint/label-hint';

import _ from 'lodash';

import './model-related-fields-control.css!css';
import template from './model-related-fields-control.html!text';

const PARAM_FIELDS_TO_RERENDER = 'fieldsToRerender';
const PARAM_FILTER_SOURCE = 'filterSource';
const PARAM_FILTER_INCLUSIVE = 'filterInclusive';

/**
 * Component responsible for rendering specific view for the RELATED_FIELDS control.
 * Control model is provided through a component property and should be of type {@link ModelControl}.
 */
@Component({
  selector: 'model-related-fields-control',
  properties: {
    'control': 'control',
    'context': 'context',
    'editable': 'editable'
  },
  events: ['onChange']
})
@View({
  template
})
@Inject(TranslateService, ModelManagementCodelistService, NgScope)
export class ModelRelatedFieldsControl extends ModelGenericControl {

  constructor(translateService, modelManagementCodelistService, $scope) {
    super();
    this.translateService = translateService;
    this.modelManagementCodelistService = modelManagementCodelistService;
    this.$scope = $scope;
  }

  ngOnInit() {
    this.initControlParamValues();
    this.initFilterSourceConfig();
    this.initFilterSourceData();
    this.initRerenderConfig();
    this.initCustomFilterConfig();
    this.initWatchers();
  }

  initWatchers() {
    this.$scope.$watch(() => this.getRerenderValue(), (oldValue, newValue) => {
      if(oldValue !== newValue) {
        this.initFilterSourceData();
      }
    });

    this.$scope.$watch(() => this.getFieldCodelistValue(this.context), (newValue, oldValue) => {
      oldValue !== newValue && this.getCutomFilterOptions(newValue);
    });
  }

  initControlParamValues() {
    this.rerender = this.getParamValue(PARAM_FIELDS_TO_RERENDER);
    this.filterSource = this.getParamValue(PARAM_FILTER_SOURCE).getValue().getValue();
  }

  initFilterSourceConfig() {
    this.filterSourceConfig = {
      data: [],
      reloadOnDataChange: true,
      defaultValue: this.filterSource
    };
  }

  initFilterSourceData() {
    // when current field is selected to be re-rendered then custom filter could be provided
    if (this.isCurrentFieldSelected()) {
      this.customFilter = ModelRelatedFieldsControl.CUSTOM;
      this.filterSourceConfig.multiple = true;
      this.filterSourceConfig.commaSeparated = true;
      this.getCutomFilterOptions();
    } else {
      this.customFilter = false;
      this.filterSourceConfig.multiple = false;
      this.filterSourceConfig.commaSeparated = false;
      this.filterSourceConfig.data = this.getExtraOptions();
    }
  }

  initRerenderConfig() {
    // Extract all fields which have code list
    let filteredFields = this.getFields().filter(field => {
      return !!this.getFieldCodelistValue(field);
    }).map(field => {
      return this.buildEntry(field.getId(), field.getDescription().getValue());
    });

    this.rerenderConfig = {
      data: filteredFields
    };
  }

  initCustomFilterConfig() {
    this.customFilterConfig = {
      data: [this.buildEntry(ModelRelatedFieldsControl.CUSTOM, this.getCustomEntryLabel())]
    };
  }

  getExtraOptions() {
    if (!this.extraOptions) {
      this.extraOptions = [
        this.buildEntry('extra1', 'Extra 1'),
        this.buildEntry('extra2', 'Extra 2'),
        this.buildEntry('extra3', 'Extra 3')
      ];
    }
    return this.extraOptions;
  }

  getCutomFilterOptions(codeListNumber) {
    let relatedField = this.getRelatedField(this.getRerenderValue());
    let code = codeListNumber || this.getFieldCodelistValue(relatedField);

    code && this.modelManagementCodelistService.getCodeList(code).then(list => {
      this.filterSourceConfig.data = list.values.map(item => this.buildEntry(item.value, item.label));
    });
  }

  getFilterSourceOption() {
    if (!this.customFilterOption) {
      this.customFilterOption = [
        this.buildEntry(ModelRelatedFieldsControl.CUSTOM, this.getCustomEntryLabel())
      ];
    }
    return this.customFilterOption;
  }

  onRerenderChange() {
    this.initFilterSourceData();
    return this.onChange({attribute: this.getParamValue(PARAM_FIELDS_TO_RERENDER)});
  }

  onFilterSourceChange() {
    return this.onChange({attribute: this.getParamValue(PARAM_FILTER_SOURCE)});
  }

  normalizeString(string) {
    return string.replace(/\s/g, '').split(',').sort().join();
  }

  getRelatedField(id) {
    return _.find(this.getFields(), field => field.getId() === id);
  }

  getFieldCodelistValue(field) {
    return field.getAttribute(ModelAttribute.CODELIST_ATTRIBUTE).getValue().getValue();
  }

  getRerenderValueAttribute() {
    return this.getParamValue(PARAM_FIELDS_TO_RERENDER);
  }

  getFilterSourceValueAttribute() {
    return this.getParamValue(PARAM_FILTER_SOURCE);
  }

  getInclusiveValueAttribute() {
    return this.getParamValue(PARAM_FILTER_INCLUSIVE);
  }

  onInclusiveChange() {
    return this.onChange({attribute: this.getParamValue(PARAM_FILTER_INCLUSIVE)});
  }

  getFilterSourceValue() {
    return this.getFilterSourceValueAttribute().getValue().getValue();
  }

  getRerenderValue() {
    return this.getRerenderValueAttribute().getValue().getValue();
  }

  getFields() {
    return this.control.getParent().getParent().getFields();
  }

  getCustomLabel() {
    return this.translateService.translateInstant('administration.models.management.field.controls.related_fields.custom.label');
  }

  getCustomEntryLabel() {
    return this.translateService.translateInstant('administration.models.management.field.controls.related_fields.custom.label.entry');
  }

  isCustomFilter() {
    return this.customFilter === ModelRelatedFieldsControl.CUSTOM;
  }

  isExtraFilter() {
    return !this.isCustomFilter();
  }

  isCurrentFieldSelected() {
    return this.getRerenderValue() === this.control.getParent().getId();
  }

  buildEntry(value, label) {
    return {id: value, text: label};
  }
}

ModelRelatedFieldsControl.CUSTOM = 'values';