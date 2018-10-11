import {ANNOTATION_UPDATED} from 'idoc/widget/image-widget/mirador-integration/mirador-events-adapter';
import {CommentBuilder} from 'idoc/comments/comment-builder';

const COMMENT_CREATED_EVENT = 'onAnnotationCreated';
const COMMENT_UPDATED_INTERNAL_EVENT = 'annotationEditSave';
const COMMENT_DELETED_EVENT = 'onAnnotationDeleted';

const COMMENT_DIALOG_CLOSED = 'onAnnotationCreatedCanceled';


export const EDIT_FROM_MIRADOR_KEY = 'EDIT_FROM_MIRADOR_KEY';

export class ImageCommentsDataProvider {
  constructor(commentsRestService) {
    this.commentsRestService = commentsRestService;
  }

  setEventsAdapter(eventsAdapter) {
    this.eventsAdapter = eventsAdapter.eventsAdapter;
  }

  createComment(config) {
    if (config.replyTo) {
      let comment = CommentBuilder.constructComment(this.eventsAdapter.getCurrentImageId(), null, config.content, config.replyTo, config.mentionedUsers);
      return this.commentsRestService.createComment(comment);
    } else {
      let comment = CommentBuilder.constructComment(config.instanceId, null, config.content, false, config.mentionedUsers);
      this.eventsAdapter.publishMiradorEvent(`${COMMENT_CREATED_EVENT}.${this.eventsAdapter.getCurrentSlotId()}`, [comment]);
      return Promise.resolve();
    }
  }

  dialogClosed() {
    this.eventsAdapter.publishMiradorEvent(`${COMMENT_DIALOG_CLOSED}.${this.eventsAdapter.getCurrentSlotId()}`, [null, true]);
  }

  updateComment(config) {
    let comment = config.comment;
    if (comment.isReply()) {
      return this.commentsRestService.updateComment(config.instanceId, config.content);
    } else {
      if (!comment.getData(EDIT_FROM_MIRADOR_KEY)) {
        this.eventsAdapter.publishMiradorEvent(`${ANNOTATION_UPDATED[0]}.${this.eventsAdapter.getCurrentSlotId()}`, [comment.getComment()]);
      } else {
        this.eventsAdapter.publishMiradorEvent(`${COMMENT_UPDATED_INTERNAL_EVENT}.${this.eventsAdapter.getCurrentSlotId()}`, [comment.getComment()]);
      }

      return Promise.resolve();
    }
  }

  loadAllComments() {
    return this.commentsRestService.loadAllComments(this.eventsAdapter.getCurrentImageId());
  }

  deleteComment(config) {
    let comment = config.comment;
    if (comment.isReply()) {
      return this.commentsRestService.deleteComment(config.commentId);
    } else {
      this.eventsAdapter.publishMiradorEvent(`${COMMENT_DELETED_EVENT}.${this.eventsAdapter.getCurrentSlotId()}`, [comment.getId()]);
      return Promise.resolve();
    }
  }

}