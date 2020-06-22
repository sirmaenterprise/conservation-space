import {Inject, Component, View} from 'app/app';
import {SearchResults} from 'search/components/common/search-results';
import 'font-awesome';
import config from 'sandbox/search/components/common/search-results/search-result-data.json!';
import searchResultsTemplateStub from 'search-results-template!text';

@Component({
  selector: 'seip-search-results-stub'
})
@View({
  template: searchResultsTemplateStub
})
export class SearchResultsStub {

  constructor() {
    this.config = config;
  }
}