import {Component, View, Inject, NgTimeout} from 'app/app';
import {SearchMediator} from 'search/search-mediator';
import {QueryBuilder} from 'search/utils/query-builder';
import {SearchService} from 'services/rest/search-service';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {MixedSearchCriteria} from 'search/components/common/mixed-search-criteria';
import complexCriteria from 'sandbox/search/components/advanced/advanced-search-criteria.data.json!';

import template from './mixed-search-criteria.stub.html!text';

@Component({
  selector: 'mixed-search-criteria-stub'
})
@View({
  template: template
})
@Inject(NgTimeout, SearchService)
export class MixedSearchCriteriaStub {

  constructor($timeout, searchService) {
    this.$timeout = $timeout;
    this.searchService = searchService;
  }

  ngOnInit() {
    this.configureCriteria({});
  }

  configureCriteria(tree) {
    this.config = {
      searchMediator: new SearchMediator(this.searchService, new QueryBuilder(tree))
    };
    this.renderCriteria = true;
  }

  simpleCriteria() {
    let types = ['http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document', 'MS210001'];
    let tree = SearchCriteriaUtils.getSearchTree({objectType: types, freeText: 'abc 123', context: ['1']});
    this.setCriteriaTree(tree);
  }

  complexCriteria() {
    this.setCriteriaTree(complexCriteria['predefined']);
  }

  setCriteriaTree(tree) {
    this.renderCriteria = false;
    this.$timeout(() => {
      this.configureCriteria(tree);
    });
  }
}