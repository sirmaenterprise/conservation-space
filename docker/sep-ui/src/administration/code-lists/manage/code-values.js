import {Component, Inject, View, NgScope} from 'app/app';
import {Configuration} from 'common/application-config';
import {CodelistManagementService} from 'administration/code-lists/services/codelist-management-service';
import {PREVIEW} from 'administration/code-lists/manage/code-manage-modes';
import 'administration/code-lists/manage/code-descriptions-button';
import 'administration/code-lists/manage/code-validation-messages';
import 'search/components/common/pagination';
import 'filters/paginate';

import './code-values.css!css';
import template from './code-values.html!text';

/**
 * Component for rendering & interacting with code values.
 *
 * Code values are read from be provided <code>codeList</code> component property, the supported structure is:
 * [{
 *   id: 'APPROVED'
 *   descriptions: { // A map with language abbreviation for key
 *     'EN': {
 *        name: 'Approved'
 *     }
 *   },
 *   description: { // The description for the current users/system language which will be rendered
 *      name: 'Approved'
 *   },
 *   extras: { // A map with extras
 *      '1': 'Extra 1'
 *   }
 * }]
 *
 * @author Mihail Radkov
 */
@Component({
  selector: 'code-values',
  properties: {
    'codeList': 'code-list',
    'mode': 'mode'
  },
  events: ['onChange']
})
@View({
  template
})
@Inject(NgScope, Configuration, CodelistManagementService)
export class CodeValues {

  constructor($scope, configuration, codelistManagementService) {
    this.$scope = $scope;
    this.configuration = configuration;
    this.codelistManagementService = codelistManagementService;
  }

  ngOnInit() {
    this.paginationConfig = {
      pageSize: this.configuration.get(Configuration.SEARCH_PAGE_SIZE),
      total: this.codeList.values.length,
      page: 1,
      showFirstLastButtons: true
    };
    this.registerPaginationWatcher();
  }

  registerPaginationWatcher() {
    this.$scope.$watch(() => this.codeList.values.length, () => this.updatePagination());
  }

  updatePagination() {
    this.paginationConfig.total = this.codeList.values.length;
    this.paginationConfig.page = 1;
  }

  addCodeValue() {
    let newCodeValueModel = this.codelistManagementService.createCodeValue(this.codeList);
    newCodeValueModel.isNew = true;
    this.codeList.values.unshift(newCodeValueModel);

    this.onChange();
  }

  removeNewValue(value) {
    let index = this.codeList.values.indexOf(value);
    this.codeList.values.splice(index, 1);

    this.onChange();
  }

  onModelChange(value) {
    value.isModified = true;
    this.onChange();
  }

  isPreviewMode() {
    return this.mode === PREVIEW;
  }

  isIdInvalid(value) {
    return this.isValueFieldInvalid(value, 'id');
  }

  isNameInvalid(value) {
    return this.isValueFieldInvalid(value, 'name', 'description');
  }

  isValueFieldInvalid(value, field, property) {
    let validationModel = (value[property] || value).validationModel;
    return !!validationModel && validationModel[field] && !validationModel[field].valid;
  }
}
