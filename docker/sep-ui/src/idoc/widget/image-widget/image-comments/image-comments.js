import {View, Component, Inject, NgTimeout, NgScope, NgElement,NgCompile} from 'app/app';
import {Eventbus} from 'services/eventbus/eventbus';
import {CommentsRestService} from 'services/rest/comments-service';
import {ImageCommentsDataProvider,EDIT_FROM_MIRADOR_KEY} from 'idoc/widget/image-widget/image-comments/image-comments-data-provider';
import {CommentContentDialog} from 'idoc/comments/comment-content-dialog/comment-content-dialog';
import {DialogService} from 'components/dialog/dialog-service';
import {AnnotationShapeCreatedEvent, MiradorLoadedEvent, RemoveAnnotationsDialogEvent, MiradorChangeViewTypeEvent,AnnotationEditModeEvent} from 'idoc/widget/image-widget/mirador-integration/mirador-events';
import {CommentInstance} from 'idoc/comments/comment-instance';
import './image-comments-section';
import {CommentContentDialogClosedEvent} from 'idoc/comments/events/comment-content-dialog-closed-event';
import { ResourceRestService } from 'services/rest/resources-service';
import {IconsService} from 'services/icons/icons-service';
import imageCommentsTemplate from './image-comments.html!text';
import './image-comments.css!css';

@Component({
  selector: 'image-comments',
  properties: {
    'context': 'context',
    'control': 'control',
    'config': 'config'
  }
})
@View({
  template: imageCommentsTemplate
})
@Inject(Eventbus, CommentsRestService, NgTimeout, DialogService, NgScope, NgElement, NgCompile, ResourceRestService, IconsService)
export class ImageComments {

  constructor(eventbus, commentsRestService, $timeout, dialogService, $scope, element, $compile, resourceRestService, iconsService) {
    this.dataProvider = new ImageCommentsDataProvider(commentsRestService);
    this.dataProvider.setEventsAdapter(this.config.commentConfig);
    this.element = element;
    this.eventbus = eventbus;
    this.$timeout = $timeout;
    this.dialogService = dialogService;
    this.$scope = $scope;
    this.$compile = $compile;
    this.widgetId = this.control.getId();
    this.resourceRestService = resourceRestService;
    this.iconsService = iconsService;

    this.commentComponentConfig = {
      dataProvider: this.dataProvider,
      widgetId: this.widgetId,
      filterConfig: this.config.filterConfig
    };

    this.subscribeToEvents();
  }

  subscribeToEvents() {
    this.events = [];
    this.events.push(this.eventbus.subscribe(MiradorLoadedEvent, (event)=> {
      if (event[0] === this.widgetId) {
        this.dataProvider.setEventsAdapter(event[1]);
      }
    }));

    this.events.push(this.eventbus.subscribe(MiradorChangeViewTypeEvent, this.appendCommentsSection.bind(this)));
    this.events.push(this.eventbus.subscribe(AnnotationShapeCreatedEvent, this.handleAnnotationShapeCreatedEvent.bind(this)));
    this.events.push(this.eventbus.subscribe(AnnotationEditModeEvent, this.handleEditAnnotationEvent.bind(this)));
    this.events.push(this.eventbus.subscribe(RemoveAnnotationsDialogEvent, this.handleRemoveAnnotationsEvent.bind(this)));
    this.events.push(this.eventbus.subscribe(CommentContentDialogClosedEvent, this.handleContentDialogClosedEvent.bind(this)));

  }

  appendCommentsSection(event) {
    if (event[0] === this.control.getId()) {
      if (this.innerScope) {
        return;
      }
      let imageCommentsComponent = $('<image-comments-section context="::imageComments.context" control="::imageComments.control" config="imageComments.commentComponentConfig"></image-comments-section>');
      this.element.append(imageCommentsComponent);
      this.innerScope = this.$scope.$new();
      this.$compile(imageCommentsComponent)(this.innerScope);
    }
  }

  handleAnnotationShapeCreatedEvent(event) {
    if (event[0] === this.widgetId) {
      this.openCreateDialog(event[1]);
    }
  }

  handleRemoveAnnotationsEvent(event) {
    if (event[0] === this.widgetId && this.commentDialog) {
      this.commentDialog.dialogService.closeExistingDialogs();
    }
  }

  handleContentDialogClosedEvent(event) {
    if (event[0] === this.widgetId && this.commentDialog) {
      this.dataProvider.dialogClosed();
      delete this.commentDialog;
    }
  }

  handleEditAnnotationEvent(event) {
    if (event[0] === this.widgetId) {
      let comment = new CommentInstance(event[1]);
      comment.addData(EDIT_FROM_MIRADOR_KEY, true);
      var config = {
        comment,
        dialogConfig: {
          dismissOnSave: false
        }
      };
      this.openEditDialog(config);
    }
  }

  openCreateDialog(options) {
    if (!this.commentDialog) {
      this.context.getCurrentObject().then((currentObject)=> {
        this.commentDialog = new CommentContentDialog(this.dialogService, this.eventbus, this.dataProvider, this.resourceRestService, this.iconsService);
        let config = {
          currentObject,
          options,
          widgetId: this.widgetId
        };
        this.commentDialog.createDialog(config);
      });
    }
  }

  openEditDialog(options) {
    if (!this.commentDialog) {
      this.context.getCurrentObject().then((currentObject)=> {
        this.commentDialog = new CommentContentDialog(this.dialogService, this.eventbus, this.dataProvider, this.resourceRestService, this.iconsService);
        let config = {
          currentObject,
          options,
          widgetId: this.widgetId,
          comment: options.comment
        };
        this.commentDialog.createDialog(config);
      });
    }
  }

  ngOnDestroy() {
    this.innerScope.$destroy();
    for (let event of this.events) {
      event.unsubscribe();
    }
  }
}
