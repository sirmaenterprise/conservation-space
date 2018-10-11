import {View, Inject, NgScope,NgCompile,NgElement } from 'app/app';
import {WidgetConfig} from 'idoc/widget/widget';
import {ObjectSelector, SELECT_OBJECT_CURRENT, SELECT_OBJECT_MANUALLY, SELECT_OBJECT_AUTOMATICALLY} from 'idoc/widget/object-selector/object-selector';
import {SearchMediator} from 'search/search-mediator';
import {QueryBuilder} from 'search/utils/query-builder';
import {MULTIPLE_SELECTION} from 'search/search-selection-modes';
import {ObjectSelectorHelper} from 'idoc/widget/object-selector/object-selector-helper';
import {TranslateService} from 'services/i18n/translate-service';
import 'search/components/advanced/advanced-search-criteria-row';
import 'components/select/resource/resource-select';
import _ from 'lodash';
import commentsWidgetConfigTemplate from './comments-widget-config.html!text';
import './comments-widget-config.css!css';

export const DEFAULT_SIZE = 10;
export const EMPTY_CRITERIA = {
  field: 'emf:createdOn',
  operator: 'after'
};
@WidgetConfig
@View({
  template: commentsWidgetConfigTemplate
})
@Inject(ObjectSelectorHelper, TranslateService, NgScope, NgCompile, NgElement)
export class CommentsWidgetConfig {

  constructor(objectSelectorHelper, translateService, $scope, $compile, $element) {
    this.$scope = $scope;
    this.$element = $element;
    this.$compile = $compile;
    this.objectSelectorHelper = objectSelectorHelper;
    this.translateService = translateService;
    this.config.selectObjectMode = this.config.selectObjectMode || SELECT_OBJECT_MANUALLY;
    // Trigger search if there is initial criteria and initial criteria is not an empty object
    let triggerSearch = !!this.config.criteria && !(Object.keys(this.config.criteria).length === 0);

    this.config.criteria = this.config.criteria || {};
    this.config.size = this.config.size || DEFAULT_SIZE;
    this.createFilterConfig();

    this.limitMenuConfig = {
      data: [
        {id: 0, text: translateService.translateInstant('comments.widget.select.all')},
        {id: 5, text: 5},
        {id: 10, text: 10},
        {id: 20, text: 20},
        {id: 50, text: 50},
        {id: 100, text: 100}
      ]
    };

    this.usersSelectConfig = {
      multiple: true
    };

    this.objectSelectorConfig = {
      selection: MULTIPLE_SELECTION,
      criteria: this.config.criteria,
      selectObjectMode: this.config.selectObjectMode,
      triggerSearch: triggerSearch,
      excludeOptions: [SELECT_OBJECT_CURRENT],
      onObjectSelectorChanged: (onSelectorChangedPayload) => {
        var selectObjectMode = onSelectorChangedPayload.selectObjectMode;
        this.config.selectObjectMode = selectObjectMode;
        this.config.criteria = onSelectorChangedPayload.searchCriteria;
        this.config.searchMode = onSelectorChangedPayload.searchMode;
        delete this.config.selectedObjects;
        if (selectObjectMode === SELECT_OBJECT_MANUALLY) {
          this.setSelectedObjects(onSelectorChangedPayload.selectedItems);
        } else if (selectObjectMode === SELECT_OBJECT_AUTOMATICALLY) {
          this.setSelectedObjects(onSelectorChangedPayload.searchResults);
        }
      },
      searchMode: this.config.searchMode
    };
    this.objectSelectorConfig.selectedItems = [];
    this.objectSelectorHelper.getSelectedItems(this.config, this.objectSelectorConfig.selectedItems);
    this.dateRangeComponent = this.$element.find('.date-range-component');
    this.appendDateRangeComponent();
  }

  ngAfterViewInit() {
    this.appendSelectCurrentObject(this.$element.find('.object-selector>div.inline-group'));
  }

  appendSelectCurrentObject(objectSelectorComponent) {
    let insertComponent = $('<label class="checkbox show-first-page-only"> <input type="checkbox" ng-model="commentsWidgetConfig.config.selectCurrentObject">{{::"comments.widget.select.current" | translate}} <i></i> </label>');
    objectSelectorComponent.append(insertComponent);
    this.$compile(insertComponent)(this.$scope);
  }

  setSelectedObjects(objectsArray) {
    if (objectsArray) {
      this.config.selectedObjects = objectsArray.map((value)=> {
        return value.id;
      });
    }
  }

  //Creates the config for the date filter. It uses a criteria row from the advanced search.
  createFilterConfig() {
    this.config.filterConfig = {
      disabled: false,
      // Fake mediator
      searchMediator: new SearchMediator(undefined, new QueryBuilder({}))
    };
    this.config.filterCriteria = this.config.filterCriteria || _.cloneDeep(EMPTY_CRITERIA);
    //Only the created on property is provided
    let createdOn = {
      id: 'emf:createdOn',
      text: this.translateService.translateInstant('objects.properties.createdOn'),
      type: 'dateTime'
    };
    this.config.filterProperties = [createdOn];
  }

  clearFilters() {
    this.config.selectedUsers = [];
    this.config.filterCriteria = _.cloneDeep(EMPTY_CRITERIA);
    this.removeDateRangeComponent();
    this.appendDateRangeComponent();
  }

  removeDateRangeComponent() {
    this.dateRangeScope.$destroy();
    this.dateRangeComponent.empty();
  }

  appendDateRangeComponent() {
    let insertComponent = $(`<seip-advanced-search-criteria-row config="::commentsWidgetConfig.config.filterConfig"
                                               context="::commentsWidgetConfig.context"
                                               criteria="commentsWidgetConfig.config.filterCriteria"
                                               properties="::commentsWidgetConfig.config.filterProperties"></seip-advanced-search-criteria-row>`);
    this.dateRangeComponent.append(insertComponent);
    this.dateRangeScope = this.$scope.$new();
    this.$compile(insertComponent)(this.dateRangeScope);
  }

}