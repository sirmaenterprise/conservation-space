'use strict';

let IdocPage = require('../idoc-page').IdocPage;
let Comments = require('../comments/comments');
let IdocCommentsToolbar = require('./idoc-comments-toolbar');

const IDOC_ID = 'emf:123456';

describe('IdocComments', function () {
  let idocPage;
  beforeEach(function () {
    idocPage = new IdocPage();
  });

  it('should have comment button if idoc is persisted', function () {
    idocPage.open(false, IDOC_ID);
    let tab = idocPage.getIdocTabs().getTabByIndex(0);
    let commentSection = tab.getContent().getCommentsSection();
    let commentsToolbar = new IdocCommentsToolbar(commentSection);
    browser.wait(EC.presenceOf(commentsToolbar.getCommentButton()), DEFAULT_TIMEOUT);
  });

  it('should open the create-comment-dialog when posting new comment', function () {
    idocPage.open(false, IDOC_ID);

    let tab = idocPage.getIdocTabs().getTabByIndex(0);
    let commentSection = tab.getContent().getCommentsSection();
    let comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    let dialog = comments.openCommentDialog();
    browser.wait(EC.presenceOf(dialog.element), DEFAULT_TIMEOUT);
  });

  it('should not create comment with empty comment description', function () {
    idocPage.open(false, IDOC_ID);

    let tab = idocPage.getIdocTabs().getTabByIndex(0);
    let commentSection = tab.getContent().getCommentsSection();
    let comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    let dialog = comments.openCommentDialog();
    browser.wait(EC.presenceOf(dialog.element), DEFAULT_TIMEOUT);
    expect(dialog.getOkButton().isEnabled()).to.eventually.be.false;
  });

  it('should not create comment with new lines', function () {
    idocPage.open(false, IDOC_ID);

    let tab = idocPage.getIdocTabs().getTabByIndex(0);
    let commentSection = tab.getContent().getCommentsSection();
    let comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    let dialog = comments.openCommentDialog();
    // here we test HTML tag strip and remove new line as html entities
    dialog.type(protractor.Key.ENTER);
    expect(dialog.getOkButton().isEnabled()).to.eventually.be.false;
  });

  it('should load previously posted comments', function () {
    idocPage.open(false, IDOC_ID);
    let tab = idocPage.getIdocTabs().getTabByIndex(0);
    let commentSection = tab.getContent().getCommentsSection();
    let comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    expect(comments.getComments().count()).to.eventually.equal(1);
  });

  it('should add comment with given text as description and then delete it', function () {
    idocPage.open(false, IDOC_ID);

    let tab = idocPage.getIdocTabs().getTabByIndex(0);
    let commentSection = tab.getContent().getCommentsSection();
    let comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    let commentDesc = 'Test Comment';
    comments.postComment(commentDesc).then(function (comment) {
      expect(comments.getComments().count()).to.eventually.equal(2);
      comment.expandComment();
      comment.getCommentActions().delete(true).then(function () {
        expect(comments.getComments().count()).to.eventually.equal(1);
      });
    });
  });

  it('should delete existing comment', function () {
    idocPage.open(false, IDOC_ID);
    let tab = idocPage.getIdocTabs().getTabByIndex(0);
    let commentSection = tab.getContent().getCommentsSection();
    let comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    comments.waitUntilLoaded();
    let comment = comments.getCommentByIndex(0);
    comment.getCommentActions().delete(true).then(function () {
      expect(comments.getComments().count()).to.eventually.equal(0);
    });
  });

  it('should not delete existing comment', function () {
    idocPage.open(false, IDOC_ID);
    let tab = idocPage.getIdocTabs().getTabByIndex(0);
    let commentSection = tab.getContent().getCommentsSection();
    let comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    comments.getCommentByIndex(0).getCommentActions().delete(false).then(function () {
      expect(comments.getComments().count()).to.eventually.equal(1);
    });
  });

  it('should open dialog when edit action is executed on comment', function () {
    idocPage.open(false, IDOC_ID);
    let tab = idocPage.getIdocTabs().getTabByIndex(0);
    let commentSection = tab.getContent().getCommentsSection();
    let comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    let commentDialog = comments.getCommentByIndex(0).getCommentActions().edit();
    browser.wait(EC.presenceOf(commentDialog), DEFAULT_TIMEOUT);
  });

  it('should open dialog with the edited comment content', function () {
    idocPage.open(false, IDOC_ID);
    let tab = idocPage.getIdocTabs().getTabByIndex(0);
    let commentSection = tab.getContent().getCommentsSection();
    let comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    let commentDesc = 'Test Comment';
    comments.postComment(commentDesc).then(function (comment) {
      let commentDialog = comment.getCommentActions().edit();
      browser.wait(EC.textToBePresentInElement(commentDialog.element, commentDesc), DEFAULT_TIMEOUT);
    });
  });

  it('should change the content of a comment when edit is performed', function () {
    idocPage.open(false, IDOC_ID);
    let tab = idocPage.getIdocTabs().getTabByIndex(0);
    let commentSection = tab.getContent().getCommentsSection();
    let comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    let comment = comments.getCommentByIndex(0);
    let commentDialog = comment.getCommentActions().edit();
    let additionalDesc = '2';
    commentDialog.type(additionalDesc).then(function () {
      commentDialog.ok();
      browser.wait(EC.textToBePresentInElement(comment.element, 'default comment' + additionalDesc), DEFAULT_TIMEOUT);
    });
  });

  it('should have reply action in the action menu', function () {
    idocPage.open(false, IDOC_ID);
    let tab = idocPage.getIdocTabs().getTabByIndex(0);
    let commentSection = tab.getContent().getCommentsSection();
    let comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    let replyAction = comments.getCommentByIndex(0).getCommentActions().getReplyAction();
    browser.wait(EC.presenceOf(replyAction), DEFAULT_TIMEOUT);
  });

  it('should open dialog when reply action is selected', function () {
    idocPage.open(false, IDOC_ID);
    let tab = idocPage.getIdocTabs().getTabByIndex(0);
    let commentSection = tab.getContent().getCommentsSection();
    let comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    let replyDialog = comments.getCommentByIndex(0).getCommentActions().reply();
    browser.wait(EC.presenceOf(replyDialog.element), DEFAULT_TIMEOUT);
  });

  it('should create reply', function () {
    idocPage.open(false, IDOC_ID);
    let tab = idocPage.getIdocTabs().getTabByIndex(0);
    let commentSection = tab.getContent().getCommentsSection();
    let comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    let comment = comments.getCommentByIndex(0);
    let replyDialog = comment.getCommentActions().reply();

    replyDialog.type('TestReply').then(function () {
      replyDialog.ok();
      comment.getReplies().then(function (replies) {
        browser.wait(EC.textToBePresentInElement(replies[1].element, 'TestReply'), DEFAULT_TIMEOUT);
      });
    });
  });

  it('should delete reply when confirmed', function () {
    idocPage.open(false, IDOC_ID);
    let tab = idocPage.getIdocTabs().getTabByIndex(0);
    let commentSection = tab.getContent().getCommentsSection();
    let comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    let comment = comments.getCommentByIndex(0);
    comment.expandComment();
    comment.getReplies().then(function (replies) {
      replies[0].getCommentActions().delete(true).then(function () {
        comment.expandComment();
        expect(comment.getReplies()).to.be.eventually.empty;
      });

    });
  });

  it('should not delete reply when dialog canceled', function () {
    idocPage.open(false, IDOC_ID);
    let tab = idocPage.getIdocTabs().getTabByIndex(0);
    let commentSection = tab.getContent().getCommentsSection();
    let comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    let comment = comments.getCommentByIndex(0);
    comment.expandComment();
    comment.getReplies().then(function (replies) {
      replies[0].getCommentActions().delete(false).then(function () {
        comment.expandComment();
        expect(comment.getReplies()).to.eventually.not.empty;
      });

    });
  });

  it('should open dialog with the edited reply content', function () {
    idocPage.open(false, IDOC_ID);
    let tab = idocPage.getIdocTabs().getTabByIndex(0);
    let commentSection = tab.getContent().getCommentsSection();
    let comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    let comment = comments.getCommentByIndex(0);
    comment.expandComment();
    comment.getReplies().then(function (replies) {
      let editReplyDialog = replies[0].getCommentActions().edit();
      browser.wait(EC.textToBePresentInElement(editReplyDialog.element, 'default reply'), DEFAULT_TIMEOUT);
    });
  });

  it('should open dialog with the edited reply content and append content', function () {
    idocPage.open(false, IDOC_ID);
    let tab = idocPage.getIdocTabs().getTabByIndex(0);
    let commentSection = tab.getContent().getCommentsSection();
    let comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    let comment = comments.getCommentByIndex(0);
    comment.expandComment();
    comment.getReplies().then(function (replies) {
      let editReplyDialog = replies[0].getCommentActions().edit();
      let additionalDesc = '2';
      editReplyDialog.type(additionalDesc).then(function () {
        editReplyDialog.ok();
        comment.expandComment();
        comment.getReplies().then(function (replies) {
          browser.wait(EC.textToBePresentInElement(replies[0].element, 'default reply' + additionalDesc), DEFAULT_TIMEOUT);
        });
      });
    });
  });

  it('should open add reply dialog when clicking the reply toolbar', function () {
    idocPage.open(false, IDOC_ID);
    let tab = idocPage.getIdocTabs().getTabByIndex(0);
    let commentSection = tab.getContent().getCommentsSection();
    let comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    let comment = comments.getCommentByIndex(0);
    comment.expandComment();
    let replyDialog = comment.clickReplyButton();
    browser.wait(EC.presenceOf(replyDialog.element), DEFAULT_TIMEOUT);
  });

  it('comments should have reply button when expanded', function () {
    idocPage.open(false, IDOC_ID);
    let tab = idocPage.getIdocTabs().getTabByIndex(0);
    let commentSection = tab.getContent().getCommentsSection();
    let comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    let comment = comments.getCommentByIndex(0);
    comment.expandComment();
    let replyBtn = comment.getReplyButton();
    browser.wait(EC.presenceOf(replyBtn), DEFAULT_TIMEOUT);
  });

  it('comments should be collapsed by default', function () {
    idocPage.open(false, IDOC_ID);
    let tab = idocPage.getIdocTabs().getTabByIndex(0);
    let commentSection = tab.getContent().getCommentsSection();
    let comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    let comment = comments.getCommentByIndex(0);
    let replyBtn = comment.getReplyButton();
    expect(replyBtn.isDisplayed()).to.eventually.be.false;
  });

  it('replies should not have reply button', function () {
    idocPage.open(false, IDOC_ID);
    let tab = idocPage.getIdocTabs().getTabByIndex(0);
    let commentSection = tab.getContent().getCommentsSection();
    let comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    let comment = comments.getCommentByIndex(0);
    comment.expandComment();
    comment.getReplies().then(function (replies) {
      browser.wait(EC.not(EC.presenceOf(replies[0].getReplyButton())), DEFAULT_TIMEOUT);
    });
  });

  it('should expand and then collapse the comment', function () {
    idocPage.open(false, IDOC_ID);
    let tab = idocPage.getIdocTabs().getTabByIndex(0);
    let commentSection = tab.getContent().getCommentsSection();
    let comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    let commentDesc = 'Test Comment';
    comments.postComment(commentDesc).then(function () {
      let newComment = comments.getCommentByIndex(0);
      newComment.expandComment();
      browser.wait(EC.presenceOf(newComment.getReplyButton()), DEFAULT_TIMEOUT);
      comments.getCommentByIndex(1).expandComment();
      browser.wait(EC.not(EC.visibilityOf(newComment.getReplyButton())), DEFAULT_TIMEOUT)
      browser.wait(EC.textToBePresentInElement(newComment.element, commentDesc), DEFAULT_TIMEOUT);
    });
  });

  it('should suspend comment', function () {
    idocPage.open(false, IDOC_ID);
    let tab = idocPage.getIdocTabs().getTabByIndex(0);
    let commentSection = tab.getContent().getCommentsSection();
    let comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    comments.waitUntilLoaded();
    let comment = comments.getCommentByIndex(0);
    comment.getCommentActions().suspend().then(() => {
      let actions = comment.getCommentActions();
      actions.isRestartActionPresent();
    });
  });

  it('should suspend comment and restart it', function () {
    idocPage.open(false, IDOC_ID);
    let tab = idocPage.getIdocTabs().getTabByIndex(0);
    let commentSection = tab.getContent().getCommentsSection();
    let comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    comments.waitUntilLoaded();
    let comment = comments.getCommentByIndex(0);
    comment.getCommentActions().suspend();
    comment.getCommentActions().restart();

    browser.wait(EC.visibilityOf(comment.getCommentActions().getSuspendAction()), DEFAULT_TIMEOUT)
  });

  function writeMentionOrEmoji(dialog, content, selectElement) {
    let saveButton = element(by.css(('.modal-dialog .seip-btn-save-comment')));
    expect(saveButton.isEnabled(), 'Save button before insert should be disabled').to.eventually.be.false;

    dialog.type(content);

    if (selectElement) {
      let textCompleteDialog = dialog.getTextcomplete();
      textCompleteDialog.click();
    }
    expect(saveButton.isEnabled(), 'Save button after insert should be enabled').to.eventually.be.true;
    dialog.ok();
  }

  it('should have users with label in dropdown menu only', function () {
    idocPage.open(false, IDOC_ID);
    let tab = idocPage.getIdocTabs().getTabByIndex(0);
    let commentSection = tab.getContent().getCommentsSection();
    let comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    let dialog = comments.openCommentDialog();
    dialog.type('@');
    browser.wait(EC.presenceOf(element(by.css('#textcomplete-dropdown-1 > li.textcomplete-item'))), DEFAULT_TIMEOUT);
    browser.driver.findElements(by.css('#textcomplete-dropdown-1 > li.textcomplete-item')).then(function (elems) {
      expect(elems.length).to.equal(2);
    });
  });

  it('should mention an user', function () {
    idocPage.open(false, IDOC_ID);
    let tab = idocPage.getIdocTabs().getTabByIndex(0);
    let commentSection = tab.getContent().getCommentsSection();
    let comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    let dialog = comments.openCommentDialog();
    writeMentionOrEmoji(dialog, '@Ja', true);
    browser.wait(EC.textToBePresentInElement(comments.getCommentByIndex(0).element, 'Jane Doe'), DEFAULT_TIMEOUT);
  });

  it('should build link when mentioning an user', function () {
    idocPage.open(false, IDOC_ID);
    let tab = idocPage.getIdocTabs().getTabByIndex(0);
    let commentSection = tab.getContent().getCommentsSection();
    let comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    let dialog = comments.openCommentDialog();
    writeMentionOrEmoji(dialog, '@Ja', true);
    let comment = comments.getCommentByIndex(0);
    expect(comment.getDescriptionAsHtml()).to.eventually.contains('Jane Doe');
    expect(comment.getDescriptionAsHtml()).to.eventually.contains('data-mention-id="janedoe@doeandco.com"');
    expect(comment.getDescriptionAsHtml()).to.eventually.contains('href="#/idoc/janedoe@doeandco.com"');
    expect(comment.getDescriptionAsHtml()).to.eventually.contains('target="_blank"');
    expect(comment.getDescriptionAsHtml()).to.eventually.contains('class="instance-link"');
  });

  it('should not build link when user is not selected from dropdown', function () {
    idocPage.open(false, IDOC_ID);
    let tab = idocPage.getIdocTabs().getTabByIndex(0);
    let commentSection = tab.getContent().getCommentsSection();
    let comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    let dialog = comments.openCommentDialog();
    writeMentionOrEmoji(dialog, '@Jane Doe', false);
    browser.wait(EC.textToBePresentInElement(comments.getCommentByIndex(0).element, '@Jane Doe'), DEFAULT_TIMEOUT);
  });

  it('should type emoji', function () {
    idocPage.open(false, IDOC_ID);
    let tab = idocPage.getIdocTabs().getTabByIndex(0);
    let commentSection = tab.getContent().getCommentsSection();
    let comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    let dialog = comments.openCommentDialog();
    writeMentionOrEmoji(dialog, ':smil', true);
    browser.wait(EC.textToBePresentInElement(comments.getCommentByIndex(0).element, '😄'), DEFAULT_TIMEOUT);
  });

  it('should not type emoji when emoji is not selected from dropdown', function () {
    idocPage.open(false, IDOC_ID);
    let tab = idocPage.getIdocTabs().getTabByIndex(0);
    let commentSection = tab.getContent().getCommentsSection();
    let comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    let dialog = comments.openCommentDialog();
    writeMentionOrEmoji(dialog, ':smile', false);
    browser.wait(EC.textToBePresentInElement(comments.getCommentByIndex(0).element, ':smile'), DEFAULT_TIMEOUT);
  });

  it('should apply comments filtering', function () {
    idocPage.open(false, IDOC_ID);
    let tab = idocPage.getIdocTabs().getTabByIndex(0);
    let commentSection = tab.getContent().getCommentsSection();
    let commentsToolbar = new IdocCommentsToolbar(commentSection);
    let panel = commentsToolbar.openFilter();
    let comments = new Comments(commentSection, commentsToolbar);

    panel.keywordField().type('testKeyword').then(function () {
      panel.filter();
      comments.getComments().then(function (commentsEl) {
        expect(commentsEl.length).to.equal(0);
      });
    });
  });
});
