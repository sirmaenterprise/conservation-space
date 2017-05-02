import {Component, View} from 'app/app';
import 'idoc/comments/comments-filter/comments-filter';
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
      filters: {
        keyword: '',
        author: '',
        commentStatus: '',
        fromDate: '',
        toDate: ''
      },
      comments: ()=> {
        return Promise.resolve([]);
      },
      tabId: 'tab1'
    };
  }

  loadFilter() {
    this.config.filters = {
      keyword: ''
    };
  }
}