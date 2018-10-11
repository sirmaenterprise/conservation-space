import {Component,View,Inject} from 'app/app';
import {HEADER_COMPACT} from 'instance-header/header-constants';
import 'instance-header/static-instance-header/static-instance-header';
import commentHeaderTemplate from 'idoc/widget/comments-widget/comment-header.html!text';

@Component({
  selector: 'comment-header',
  properties: {
    'comment': 'comment'
  }
})
@View({
  template: commentHeaderTemplate
})
@Inject()
export class CommentHeader {

  constructor() {
    this.headerConfig = {
      disabled: false
    };
    this.headerType = HEADER_COMPACT;
  }
}