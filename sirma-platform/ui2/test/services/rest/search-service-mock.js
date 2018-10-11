import {SearchService} from 'services/rest/search-service';
import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';

export class SearchServiceMock {

  search() {
    return {
      promise: PromiseStub.resolve(),
      timeout: {
        resolve: () => {
        }
      }
    };
  }
}

export function stubSearchService(values = [], total = 0) {
  let stubbedSearchService = stub(SearchService);
  stubbedSearchService.search.returns({
    timeout: PromiseStub.resolve(),
    promise: PromiseStub.resolve({
      data: {total, values}
    })
  });
  return stubbedSearchService;
}