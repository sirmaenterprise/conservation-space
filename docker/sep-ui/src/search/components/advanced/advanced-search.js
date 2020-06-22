import {Component, View, Inject, NgScope, NgTimeout} from 'app/app';
import {SearchCriteriaComponent} from 'search/components/common/search-criteria-component';
import {PropertiesRestService} from 'services/rest/properties-service';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import 'search/components/advanced/advanced-search-section';
import 'search/components/saved/save-search';

import template from './advanced-search.html!text';

/**
 * Component combining the advanced search section and groups and criteria rows.
 *
 * Every configuration or context given to this component is passed down to every section, criteria group and
 * criteria row. Example configuration:
 *  {
 *    searchMediator: {...},
 *    disabled: false
 *  }
 *
 * The search mediator is a mandatory configuration and should be provided from outside! Otherwise the advanced
 * search will not work.
 *
 * If no criteria tree is present in the mediator's query builder, a default one will be assigned with default
 * sections.
 *
 * @author Mihail Radkov
 */
@Component({
  selector: 'seip-advanced-search',
  properties: {
    'config': 'config',
    'context': 'context'
  }
})
@View({template})
@Inject(NgScope, NgTimeout, PropertiesRestService)
export class AdvancedSearch extends SearchCriteriaComponent {

  constructor($scope, $timeout, propertiesRestService) {
    super({});
    this.$scope = $scope;
    this.$timeout = $timeout;
    this.propertiesRestService = propertiesRestService;
  }

  ngOnInit() {
    this.assignLoaders(this.propertiesRestService);
    this.assignCriteria();
    this.assignModelWatchers();
  }

  /**
   * Constructs an object with loader functions needed by the advanced search section component.
   */
  assignLoaders(propertiesRestService) {
    this.loaders = {
      properties: (types) => propertiesRestService.getSearchableProperties(types)
    };
  }

  assignModelWatchers() {
    // Watches if the criteria tree is replaced externally and updates the current one.
    this.$scope.$watch(() => {
      return this.config.searchMediator.queryBuilder.tree;
    }, () => {
      this.assignCriteria();
    });
  }

  assignCriteria() {
    delete this.criteria;
    // Forcing recreation of the section in another digest cycle
    this.$timeout(() => {
      this.assignDefaultCriteria();
      this.criteria = this.config.searchMediator.queryBuilder.tree;
    });
  }

  assignDefaultCriteria() {
    let tree = this.config.searchMediator.queryBuilder.tree;
    if (!tree.rules || tree.rules.length < 1) {
      let defaultCriteria = SearchCriteriaUtils.getDefaultAdvancedSearchCriteria();
      SearchCriteriaUtils.assignRestrictions(defaultCriteria, this.config.restrictions);
      this.config.searchMediator.queryBuilder.init(defaultCriteria);
    }
  }

  clear() {
    this.criteria = {};
    this.config.searchMediator.queryBuilder.init({});
    this.clearResults();
  }
}
