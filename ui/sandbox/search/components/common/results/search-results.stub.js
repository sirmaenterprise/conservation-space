import {Inject, Component, View} from 'app/app';
import {SearchResults} from 'search/components/common/search-results';
import searchResultsTemplateStub from 'search-results-template!text';
import config from 'sandbox/search/components/common/results/search-result-data.json!';
import 'font-awesome';

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