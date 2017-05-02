import {Component, View, Inject} from 'app/app';
import {MixedSearchCriteria} from 'search/components/common/mixed-search-criteria';
import {SearchMediator} from 'search/search-mediator';
import {QueryBuilder} from 'search/utils/query-builder'

import template from 'mixed-search-criteria-stub-template!text';

@Component({
  selector: 'seip-mixed-search-criteria-stub'
})
@View({
  template: template
})
export class MixedSearchCriteriaStub {
  constructor() {
    this.config = {
      searchMediator: new SearchMediator({}, new QueryBuilder({}))
    };
  }
}