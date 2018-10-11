import {Injectable, Inject} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import data from 'sandbox/idoc/widget/comments-widget/services/comments-service.data.json!text';

@Injectable()
@Inject(PromiseAdapter)
export class CommentsRestService {
  constructor(promiseAdapter) {
    this.promiseAdapter = promiseAdapter;
  }

  loadRecentComments() {
    return this.promiseAdapter.resolve({data: JSON.parse(data)});
  }

  loadCommentsCount() {
    return this.promiseAdapter.resolve({data: JSON.parse(data).annotations.length});
  }

}