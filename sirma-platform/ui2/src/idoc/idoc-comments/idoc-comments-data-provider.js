import {CommentBuilder} from 'idoc/comments/comment-builder';

export class IdocCommentsDataProvider {
  constructor(commentsRestService) {
    this.commentsRestService = commentsRestService;
  }

  loadComments(instanceId, tabId) {
    return this.commentsRestService.loadComments(instanceId, tabId);
  }

  createComment(config) {
    return this.commentsRestService.createComment(CommentBuilder.constructComment(config.instanceId, config.tabId, config.content, config.replyTo, config.mentionedUsers));
  }

  updateComment(config) {
    return this.commentsRestService.updateComment(config.instanceId, config.content, config.mentionedUsers);
  }

  deleteComment(config) {
    return this.commentsRestService.deleteComment(config.commentId);
  }

  loadCommentsAndReplies(instanceId) {
    return this.commentsRestService.loadAllComments(instanceId);
  }

}