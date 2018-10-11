import {Component,View,Inject,NgScope,NgCompile,NgElement} from 'app/app';
import 'idoc/comments/comment';
import 'idoc/widget/comments-widget/comment-header';
import commentContainerTemplate from 'idoc/comments/comment-container.html!text';

@Component({
  selector: 'comment-container',
  properties: {
    'config': 'config',
    'comment': 'comment'
  }
})
@View({
  template: commentContainerTemplate
})
@Inject(NgScope, NgCompile, NgElement)
export class CommentContainer {

  constructor($scope, $compile, element) {
    let beforeCommentEl = element.find('.before-comment');
    if (this.config.additionalComponent) {
      let insertComponent = $(`<${this.config.additionalComponent} comment="::commentContainer.comment" >`);
      beforeCommentEl.replaceWith(insertComponent);
      $compile(insertComponent)($scope);
    }
  }

}
