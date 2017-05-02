import {Component, View, Inject} from 'app/app';
import {Search} from 'search/components/search';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';

@Component({
  selector: 'search-stub'
})
@View({
  template: '<div><seip-search config="searchStub.config"></seip-search></div>'
})
export class SearchStub {

  constructor() {
    this.config = {
      criteriaType: SearchCriteriaUtils.MIXED_MODE
    }
  }
}