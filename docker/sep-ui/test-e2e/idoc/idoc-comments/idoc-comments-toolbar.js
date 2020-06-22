'use strict';
var CommentsFilter = require('../comments/comments-filter/comments-filter').CommentsFilter;


class IdocCommentsToolbar {
  constructor(element) {
    this.element = element;
  }

  clickCommentButton() {
    let commentButton = this.getCommentButton();
    browser.wait(EC.elementToBeClickable(commentButton), DEFAULT_TIMEOUT);
    commentButton.click();
  }

  getCommentButton() {
    return this.element.$('.seip-btn-add-comment');
  }

  openFilter() {
    let commentsFilter = new CommentsFilter(this.element);
    return commentsFilter.openFilterPanel();
  }

}

module.exports = IdocCommentsToolbar;