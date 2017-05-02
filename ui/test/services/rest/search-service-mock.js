import {PromiseStub} from 'test/promise-stub';

export class SearchServiceMock {

  search(searchRequest) {
    return {
      promise: PromiseStub.resolve(),
      timeout: {
        resolve: () => {}
      }
    }
  }

}