import { SearchCriteriaComponent } from 'search/components/common/search-criteria-component';
import { QueryBuilder } from 'search/utils/query-builder';
import { SearchMediator, EVENT_SEARCH } from 'search/search-mediator';
import {SearchServiceMock} from 'test/services/rest/search-service-mock';

describe('SearchCriteriaComponent', () => {

  var searchService;
  var mediator;
  var component;

  beforeEach(() => {
    searchService = new SearchServiceMock();
    sinon.spy(searchService, 'search');
    var builder = new QueryBuilder();
    mediator = new SearchMediator(searchService, builder);
    component = new SearchCriteriaComponent();
    component.config = {
      searchMediator: mediator
    };
  });

  it('should invoke search through the mediator', () => {
    component.search();
    expect(searchService.search.calledOnce).to.be.true;
  });

  it('should reset pagination before search', () => {
    mediator.arguments.pageNumber = 3;
    component.search();
    expect(mediator.arguments.pageNumber).to.equal(1);
  });

  it('should clear any results', () => {
    var searchResponse;
    mediator.registerListener(EVENT_SEARCH, (response) => {
      searchResponse = response;
    });

    component.clearResults();
    expect(searchResponse).to.exist;
    expect(searchResponse.response.data).to.exist;
    expect(searchResponse.response.data.values).to.deep.equal([]);
    expect(searchResponse.response.data.resultSize).to.equal(0);
  });

});