import {Component, View} from 'app/app';
import 'idoc/comments/comments-filter/comments-filter';
import {EMPTY_FILTERS} from 'idoc/comments/comments-filter/comments-filter-const';
import commentsFilterTemplate from 'comments-filter-stub-template!text';

@Component({
  selector: 'comments-filter-stub'
})
@View({
  template: commentsFilterTemplate
})
class CommentsFilterStub {

  constructor() {

    this.config = {
      filters: EMPTY_FILTERS,
      comments: () => {
        return Promise.resolve([]);
      },
      tabId: 'tab1'
    };
  }

  loadFilter() {
    this.config.filters = {
      author: null,
      commentStatus: "OPEN",
      fromDate: "",
      keyword: "test",
      toDate: "",
    };
  }
}