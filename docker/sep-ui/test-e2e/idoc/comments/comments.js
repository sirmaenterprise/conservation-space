'use strict';

var Dialog = require('../../components/dialog/dialog');
var ConfirmationPopup = require('../../components/dialog/confirmation-popup');

class Comments {

  constructor(element, commentsToolbar) {
    this.element = element.$('.comments') || $('.comments');
    this.commentsToolbar = commentsToolbar;
  }

  waitUntilLoaded() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  getComments() {
    return this.element.$$('.comment:not(.comment-reply)');
  }

  getCommentByIndex(index) {
    return new Comment(this.getComments().get(index));
  }

  postComment(text) {
    let commentDialog = this.openCommentDialog();
    return commentDialog.type(text).then(()=> {
      commentDialog.ok();
      return this.getCommentByIndex(0);
    });
  }

  openCommentDialog() {
    this.commentsToolbar.clickCommentButton();
    return new CreateCommentDialog($('.modal-dialog'));
  }
}

class CreateCommentDialog extends Dialog {
  constructor(element) {
    super(element);
    this.waitUntilOpened();
  }

  type(text) {
    this.focusEditor();
    return this.getEditorElement().sendKeys(text);
  }

  ok() {
    super.ok();
    this.waitUntilClosed();
  }

  focusEditor() {
    this.getEditorElement().click();
  }

  getEditorElement() {
    browser.wait(EC.visibilityOf(this.element.$('.modal-body')), DEFAULT_TIMEOUT);
    browser.wait(EC.visibilityOf(this.element.$('.create-comment-dialog')), DEFAULT_TIMEOUT);
    let editor = this.element.$('#cke_comment-editor').$('.cke_wysiwyg_div');
    browser.wait(EC.elementToBeClickable(editor), DEFAULT_TIMEOUT);
    return editor;
  }

  getEditorContent() {
    return this.getEditorElement().getText();
  }

  getTextcomplete() {
    var selectElement = $('.textcomplete-item');
    browser.wait(EC.visibilityOf(selectElement), DEFAULT_TIMEOUT);
    return selectElement;
  }
}

class Comment {
  constructor(element) {
    this.element = element;
  }

  expandComment() {
    browser.wait(EC.elementToBeClickable(this.element), DEFAULT_TIMEOUT);
    this.element.click();
  }

  getDescription(getSpanElement) {
    if (getSpanElement) {
      return this.element.$('.comment-description span');
    } else {
      return this.element.$('.comment-description');
    }
  }

  getDescriptionAsText(getSpanElement) {
    return this.getDescription(getSpanElement).getText();
  }

  getDescriptionAsHtml() {
    return this.getDescription().getAttribute('innerHTML');
  }

  getHeader() {
    return new CommentHeader(this.element.$('.comment-header'));
  }

  getId() {
    return this.element.getAttribute('id');
  }

  getCommentActions() {
    let commentActions = this.element.$$('.comment-actions-menu').first();
    browser.wait(EC.elementToBeClickable(commentActions), DEFAULT_TIMEOUT);
    commentActions.click();
    return new CommentActions(commentActions, this.element);
  }

  getReplies() {
    let replies = this.element.$$('.comment-replies').first().$$('.comment');
    return replies.then(function (elements) {
      return elements.map(function (element) {
        return new Comment(element);
      });
    });
  }

  getReplyButton() {
    return this.element.$('.comment-reply-btn');
  }

  clickReplyButton() {
    let replyBtn = this.getReplyButton();
    browser.wait(EC.elementToBeClickable(replyBtn), DEFAULT_TIMEOUT);
    replyBtn.click();
    return new CreateCommentDialog($('.modal-dialog'));
  }
}

class CommentActions {
  constructor(element, comment) {
    this.element = element;
    this.comment = comment;
  }

  edit() {
    let editComment = this.getEditAction();
    editComment.click();
    let dialog = $('.modal-dialog');
    browser.wait(EC.visibilityOf(dialog), DEFAULT_TIMEOUT);
    return new CreateCommentDialog(dialog);
  }

  delete(confirmDelete) {

    let deleteComment = this.getDeleteAction();
    deleteComment.click();
    let confirm = new ConfirmationPopup();
    let confirmPopupElement = confirm.getConfirmationPopup();
    browser.wait(EC.visibilityOf(confirmPopupElement), DEFAULT_TIMEOUT);

    return new Comment(this.comment).getId().then(function (id) {
      let comment = element(by.id(id));
      if (confirmDelete) {
        browser.wait(EC.visibilityOf(confirmPopupElement), DEFAULT_TIMEOUT);
        confirm.clickConfirmButton();
        browser.wait(EC.stalenessOf(confirmPopupElement), DEFAULT_TIMEOUT);
        browser.wait(EC.stalenessOf(comment), DEFAULT_TIMEOUT);
      } else {
        browser.wait(EC.visibilityOf(confirmPopupElement), DEFAULT_TIMEOUT);
        confirm.clickCancelButton();
        browser.wait(EC.stalenessOf(confirmPopupElement), DEFAULT_TIMEOUT);
      }
    });

  }

  suspend() {
    let suspendComment = this.getSuspendAction();
    return suspendComment.click();
  }

  getSuspendAction() {
    let suspendComment = this.element.$('.suspendComment');
    browser.wait(EC.elementToBeClickable(suspendComment), DEFAULT_TIMEOUT);
    return suspendComment;
  }

  isRestartActionPresent() {
    this.getRestartAction();
  }

  restart() {
    let restartComment = this.getRestartAction();
    restartComment.click();
  }

  getRestartAction() {
    let restartComment = this.element.$('.restartComment');
    browser.wait(EC.elementToBeClickable(restartComment), DEFAULT_TIMEOUT);
    return restartComment;
  }

  getEditAction() {
    let editComment = this.element.$('.editComment');
    browser.wait(EC.elementToBeClickable(editComment), DEFAULT_TIMEOUT);
    return editComment;
  }

  getDeleteAction() {
    let deleteComment = this.element.$('.deleteComment');
    browser.wait(EC.elementToBeClickable(deleteComment), DEFAULT_TIMEOUT);
    return deleteComment;
  }

  getReplyAction() {
    let reply = this.element.$('.replyComment');
    browser.wait(EC.elementToBeClickable(reply), DEFAULT_TIMEOUT);
    return reply;
  }

  reply() {
    let reply = this.getReplyAction();
    reply.click();
    return new CreateCommentDialog($('.modal-dialog'));
  }

}

class CommentHeader {
  constructor(element) {
    this.element = element;
  }

  getIconSource() {
    return this.element.$('icon').getAttribute('src');
  }

  getAuthorAsText() {
    return this.element.$('author').getText();
  }

  getDateAsText() {
    return this.element.$('date').getText();
  }
}

class CommentsPanel {

  waitForCommentsPanel() {
    browser.wait(EC.presenceOf($('.idoc-comments-wrapper')), DEFAULT_TIMEOUT);
  }

}

module.exports = Comments;
module.exports.CommentsPanel = CommentsPanel;
