import {Inject, Component, View} from 'app/app';
import {QueryBuilder} from 'search/utils/query-builder';
import {SearchService} from 'services/rest/search-service';
import {SearchMediator, EVENT_SEARCH} from 'search/search-mediator';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import 'search/components/common/results-toolbar';
import resultsToolbarTemplateStub from 'results-toolbar-template!text';

@Component({
  selector: 'seip-results-toolbar-stub'
})
@View({
  template: resultsToolbarTemplateStub
})
@Inject(SearchService)
export class ResultsToolbarStub {

  constructor(service) {
    let tree = this.createTree();

    this.currentPageSize = 50;
    this.currentPageNumber = 1;
    this.currentResultSize = 500;

    this.service = service;
    this.query = new QueryBuilder(tree);
    this.mediator = new SearchMediator(this.service, this.query);

    this.resultsToolbarConfig = {
      searchMediator: this.mediator
    }
  }

  createTree() {
    // construct the default template for the tree
    let tree = SearchCriteriaUtils.getDefaultAdvancedSearchCriteria();
    tree.rules[0].rules.push(SearchCriteriaUtils.getDefaultObjectTypeRule(this.getPredefinedTypes()));
    tree.rules[0].rules.push(SearchCriteriaUtils.buildCondition());
    // attach fts & relation rules to the constructed tree
    tree.rules[0].rules[1].rules.push(SearchCriteriaUtils.buildRule('freeText', 'fts', 'contains', 'free text search'));
    tree.rules[0].rules[1].rules.push(SearchCriteriaUtils.buildRule('anyRelation', 'object', 'set_to', ['emf:123456']));

    return tree;
  }

  search() {
    this.mediator.trigger(EVENT_SEARCH, this.getSearchResponse());
  }

  getSearchResponse() {
    return SearchMediator.buildSearchResponse(this.query, this.getSearchResults(), this.getSearchArguments());
  }

  getSearchArguments() {
    return {
      pageSize: this.currentPageSize,
      pageNumber: this.currentPageNumber
    };
  }

  getSearchResults() {
    return {
      data: {
        values: [{}],
        resultSize: this.currentResultSize
      }
    };
  }

  getPredefinedTypes() {
    return ['http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document'];
  }

  getTotalNumberOfPages() {
    return Math.ceil(this.currentResultSize / this.currentPageSize);
  }

  nextPage() {
    let total = this.getTotalNumberOfPages();
    this.currentPageNumber = Math.min(total, this.currentPageNumber + 1);
    this.search();
  }

  prevPage() {
    this.currentPageNumber = Math.max(1, this.currentPageNumber - 1);
    this.search();
  }

  toggleType() {
    this.resultsToolbarConfig.message.renderType = !this.resultsToolbarConfig.message.renderType;
  }

  toggleContext() {
    this.resultsToolbarConfig.message.renderContext = !this.resultsToolbarConfig.message.renderContext;
  }

  toggleFts() {
    this.resultsToolbarConfig.message.renderFts = !this.resultsToolbarConfig.message.renderFts;
  }

  toggleResults() {
    this.resultsToolbarConfig.message.renderResults = !this.resultsToolbarConfig.message.renderResults;
  }
}