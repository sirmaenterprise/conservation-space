import {Component,View,Inject,NgScope,NgCompile,NgElement} from 'app/app';
import 'idoc/comments/comment';
import {CommentInstance} from 'idoc/comments/comment-instance';
import {CommentContentDialog} from 'idoc/comments/comment-content-dialog/comment-content-dialog';
import {DialogService} from 'components/dialog/dialog-service';
import {Eventbus} from 'services/eventbus/eventbus';
import { ResourceRestService } from 'services/rest/resources-service';
import 'idoc/comments/comment-container';
import {IconsService} from 'services/icons/icons-service';
import commentsTemplate from 'idoc/comments/comments.html!text';

@Component({
  selector: 'seip-comments',
  properties: {
    'config': 'config',
    'context': 'context',
    'comments': 'comments'
  }
})
@View({
  template: commentsTemplate
})
@Inject(DialogService, Eventbus, NgScope, NgCompile, NgElement, ResourceRestService, IconsService)
export class Comments {

  constructor(dialogService, eventbus, $scope, $compile, $element, resourceRestService, iconsService) {
    this.tabId = this.config.tabId;
    this.dataProvider = this.config.dataProvider;
    this.commentContentDialog = new CommentContentDialog(dialogService, eventbus, this.dataProvider, resourceRestService, iconsService);
    this.commentConfig = {
      dataProvider: this.dataProvider,
      tabId: this.tabId,
      commentContentDialog: this.commentContentDialog,
      eventEmitter: this.config.eventEmitter,
      context: this.context,
      widgetId: this.config.widgetId,
      additionalComponent: this.config.additionalComponent
    };

    let commentsToolbarElement = $element.find('.comments-toolbar');

    if (this.config.toolbarComponent) {
      let commentsToolbarComponent = $(`<${this.config.toolbarComponent} comments-component="comments" context="::comments.context">`);
      commentsToolbarElement.replaceWith(commentsToolbarComponent);
      $compile(commentsToolbarComponent)($scope);
    }
    else {
      commentsToolbarElement.remove();
    }
  }

  openPostDialog() {
    this.context.getCurrentObject().then((currentObject)=> {
      let config = {
        currentObject: currentObject,
        tabId: this.tabId
      };
      this.commentContentDialog.createDialog(config);
    });
  }
}
