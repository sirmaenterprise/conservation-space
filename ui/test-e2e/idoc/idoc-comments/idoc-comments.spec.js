var IdocPage = require('../idoc-page');
var Comments = require('../comments/comments');
var IdocCommentsToolbar = require('./idoc-comments-toolbar');

const IDOC_ID = 'emf:123456';

describe('IdocComments', function () {
  var idocPage;
  beforeEach(function () {
    idocPage = new IdocPage();
  });

  it('should not have comment button if idoc is not persisted', function () {
    idocPage.open(true);
    var tab = idocPage.getIdocTabs().getTabByIndex(0);
    var commentSection = tab.getContent().getCommentsSection();
    var commentsToolbar = new IdocCommentsToolbar(commentSection);

    expect(commentsToolbar.getCommentButton().isPresent()).to.eventually.be.false;
  });

  it('should have comment button if idoc is persisted', function () {
    idocPage.open(false, IDOC_ID);
    var tab = idocPage.getIdocTabs().getTabByIndex(0);
    var commentSection = tab.getContent().getCommentsSection();
    var commentsToolbar = new IdocCommentsToolbar(commentSection);
    expect(commentsToolbar.getCommentButton().isPresent()).to.eventually.be.true;
  });

  it('should open the create-comment-dialog when posting new comment', function () {
    idocPage.open(false, IDOC_ID);

    var tab = idocPage.getIdocTabs().getTabByIndex(0);
    var commentSection = tab.getContent().getCommentsSection();
    var comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    var dialog = comments.openCommentDialog();
    expect(dialog.element.isPresent()).to.eventually.be.true;
  });

  it('should not create comment with empty comment description', function () {
    idocPage.open(false, IDOC_ID);

    var tab = idocPage.getIdocTabs().getTabByIndex(0);
    var commentSection = tab.getContent().getCommentsSection();
    var comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    var dialog = comments.openCommentDialog();
    expect(dialog.element.isPresent()).to.eventually.be.true;
    expect(dialog.getOkButton().isEnabled()).to.eventually.be.false;
  });

  it('should not create comment with new lines', function () {
    idocPage.open(false, IDOC_ID);

    var tab = idocPage.getIdocTabs().getTabByIndex(0);
    var commentSection = tab.getContent().getCommentsSection();
    var comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    var dialog = comments.openCommentDialog();
    // here we test HTML tag strip and remove new line as html entities
    dialog.type(protractor.Key.ENTER);
    expect(dialog.getOkButton().isEnabled()).to.eventually.be.false;
  });

  it('should load previously posted comments', function () {
    idocPage.open(false, IDOC_ID);
    var tab = idocPage.getIdocTabs().getTabByIndex(0);
    var commentSection = tab.getContent().getCommentsSection();
    var comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    expect(comments.getComments().count()).to.eventually.equal(1);
  });

  it('should add comment with given text as description and then delete it', function () {
    idocPage.open(false, IDOC_ID);

    var tab = idocPage.getIdocTabs().getTabByIndex(0);
    var commentSection = tab.getContent().getCommentsSection();
    var comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    var commentDesc = 'Test Comment';
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
    var tab = idocPage.getIdocTabs().getTabByIndex(0);
    var commentSection = tab.getContent().getCommentsSection();
    var comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    comments.waitUntilLoaded();
    var comment = comments.getCommentByIndex(0);
    comment.getCommentActions().delete(true).then(function () {
      expect(comments.getComments().count()).to.eventually.equal(0);
    });
  });

  it('should not delete existing comment', function () {
    idocPage.open(false, IDOC_ID);
    var tab = idocPage.getIdocTabs().getTabByIndex(0);
    var commentSection = tab.getContent().getCommentsSection();
    var comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    comments.getCommentByIndex(0).getCommentActions().delete(false).then(function () {
      expect(comments.getComments().count()).to.eventually.equal(1);
    });
  });

  it('should open dialog when edit action is executed on comment', function () {
    idocPage.open(false, IDOC_ID);
    var tab = idocPage.getIdocTabs().getTabByIndex(0);
    var commentSection = tab.getContent().getCommentsSection();
    var comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    var commentDialog = comments.getCommentByIndex(0).getCommentActions().edit();
    expect(commentDialog.isPresent()).to.eventually.to.be.true;
  });

  it('should open dialog with the edited comment content', function () {
    idocPage.open(false, IDOC_ID);
    var tab = idocPage.getIdocTabs().getTabByIndex(0);
    var commentSection = tab.getContent().getCommentsSection();
    var comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    var commentDesc = 'Test Comment';
    comments.postComment(commentDesc).then(function (comment) {
      var commentDialog = comment.getCommentActions().edit();
      expect(commentDialog.getEditorContent()).to.eventually.equal(commentDesc);
    });
  });

  it('should change the content of a comment when edit is performed', function () {
    idocPage.open(false, IDOC_ID);
    var tab = idocPage.getIdocTabs().getTabByIndex(0);
    var commentSection = tab.getContent().getCommentsSection();
    var comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    var comment = comments.getCommentByIndex(0);
    var commentDialog = comment.getCommentActions().edit();
    var additionalDesc = '2';
    commentDialog.type(additionalDesc).then(function () {
      commentDialog.ok();
      expect(comment.getDescriptionAsText()).to.eventually.equal('default comment' + additionalDesc);
    });
  });

  it('should have reply action in the action menu', function () {
    idocPage.open(false, IDOC_ID);
    var tab = idocPage.getIdocTabs().getTabByIndex(0);
    var commentSection = tab.getContent().getCommentsSection();
    var comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    var replyAction = comments.getCommentByIndex(0).getCommentActions().getReplyAction();
    expect(replyAction.isPresent()).to.eventually.be.true;
  });

  it('should open dialog when reply action is selected', function () {
    idocPage.open(false, IDOC_ID);
    var tab = idocPage.getIdocTabs().getTabByIndex(0);
    var commentSection = tab.getContent().getCommentsSection();
    var comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    var replyDialog = comments.getCommentByIndex(0).getCommentActions().reply();
    expect(replyDialog.element.isPresent()).to.eventually.be.true;
  });

  it('should create reply', function () {
    idocPage.open(false, IDOC_ID);
    var tab = idocPage.getIdocTabs().getTabByIndex(0);
    var commentSection = tab.getContent().getCommentsSection();
    var comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    var comment = comments.getCommentByIndex(0);
    var replyDialog = comment.getCommentActions().reply();

    replyDialog.type('TestReply').then(function () {
      replyDialog.ok();
      comment.getReplies().then(function (replies) {
        expect(replies[1].getDescriptionAsText()).to.eventually.equal('TestReply');
      });
    })
  });

  it('should delete reply when confirmed', function () {
    idocPage.open(false, IDOC_ID);
    var tab = idocPage.getIdocTabs().getTabByIndex(0);
    var commentSection = tab.getContent().getCommentsSection();
    var comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    var comment = comments.getCommentByIndex(0);
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
    var tab = idocPage.getIdocTabs().getTabByIndex(0);
    var commentSection = tab.getContent().getCommentsSection();
    var comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    var comment = comments.getCommentByIndex(0);
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
    var tab = idocPage.getIdocTabs().getTabByIndex(0);
    var commentSection = tab.getContent().getCommentsSection();
    var comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    var comment = comments.getCommentByIndex(0);
    comment.expandComment();
    var replyDesc = 'default reply';
    comment.getReplies().then(function (replies) {
      var editReplyDialog = replies[0].getCommentActions().edit();
      expect(editReplyDialog.getEditorContent()).to.eventually.equal(replyDesc);
    });
  });

  it('should open dialog with the edited reply content and append content', function () {
    idocPage.open(false, IDOC_ID);
    var tab = idocPage.getIdocTabs().getTabByIndex(0);
    var commentSection = tab.getContent().getCommentsSection();
    var comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    var comment = comments.getCommentByIndex(0);
    comment.expandComment();
    comment.getReplies().then(function (replies) {
      var editReplyDialog = replies[0].getCommentActions().edit();
      var additionalDesc = '2';
      editReplyDialog.type(additionalDesc).then(function () {
        editReplyDialog.ok();
        comment.expandComment();
        comment.getReplies().then(function (replies) {
          expect(replies[0].getDescriptionAsText()).to.eventually.equal('default reply' + additionalDesc);
        });

      });
    });
  });

  it('should open add reply dialog when clicking the reply toolbar', function () {
    idocPage.open(false, IDOC_ID);
    var tab = idocPage.getIdocTabs().getTabByIndex(0);
    var commentSection = tab.getContent().getCommentsSection();
    var comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    var comment = comments.getCommentByIndex(0);
    comment.expandComment();
    var replyDialog = comment.clickReplyButton();

    expect(replyDialog.element.isPresent()).to.eventually.be.true;
  });

  it('comments should have reply button when expanded', function () {
    idocPage.open(false, IDOC_ID);
    var tab = idocPage.getIdocTabs().getTabByIndex(0);
    var commentSection = tab.getContent().getCommentsSection();
    var comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    var comment = comments.getCommentByIndex(0);
    comment.expandComment();
    var replyBtn = comment.getReplyButton();

    expect(replyBtn.isPresent()).to.eventually.be.true;
  });

  it('comments should be collapsed by default', function () {
    idocPage.open(false, IDOC_ID);
    var tab = idocPage.getIdocTabs().getTabByIndex(0);
    var commentSection = tab.getContent().getCommentsSection();
    var comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    var comment = comments.getCommentByIndex(0);
    var replyBtn = comment.getReplyButton();
    expect(replyBtn.isDisplayed()).to.eventually.be.false;
  });

  it('replies should not have reply button', function () {
    idocPage.open(false, IDOC_ID);
    var tab = idocPage.getIdocTabs().getTabByIndex(0);
    var commentSection = tab.getContent().getCommentsSection();
    var comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    var comment = comments.getCommentByIndex(0);
    comment.expandComment();
    comment.getReplies().then(function (replies) {
      expect(replies[0].getReplyButton().isPresent()).to.eventually.be.false;
    });
  });

  it('should expand and then collapse the comment', function () {
    idocPage.open(false, IDOC_ID);
    var tab = idocPage.getIdocTabs().getTabByIndex(0);
    var commentSection = tab.getContent().getCommentsSection();
    var comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    var commentDesc = 'Test Comment';
    comments.postComment(commentDesc).then(function (comment) {
      var newComment = comments.getCommentByIndex(0);
      newComment.expandComment();
      expect(newComment.getReplyButton().isPresent()).to.eventually.be.true;
      comments.getCommentByIndex(1).expandComment();
      expect(newComment.getReplyButton().isDisplayed()).to.eventually.be.false;
      expect(newComment.getDescriptionAsText()).to.eventually.equal(commentDesc);
    });
  });

  it('should suspend comment', function () {
    idocPage.open(false, IDOC_ID);
    var tab = idocPage.getIdocTabs().getTabByIndex(0);
    var commentSection = tab.getContent().getCommentsSection();
    var comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    comments.waitUntilLoaded();
    var comment = comments.getCommentByIndex(0);
    comment.getCommentActions().suspend();
    expect(comment.getCommentActions().getRestartAction().isDisplayed()).to.eventually.be.true;
  });

  it('should suspend comment and restart it', function () {
    idocPage.open(false, IDOC_ID);
    var tab = idocPage.getIdocTabs().getTabByIndex(0);
    var commentSection = tab.getContent().getCommentsSection();
    var comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    comments.waitUntilLoaded();
    var comment = comments.getCommentByIndex(0);
    comment.getCommentActions().suspend();
    comment.getCommentActions().restart();

    expect(comment.getCommentActions().getSuspendAction().isDisplayed()).to.eventually.be.true;
  });

  function writeMentionOrEmoji(dialog, content, selectElement) {
    dialog.type(content);

    if (selectElement) {
      var textCompleteDialog = dialog.getTextcomplete();
      textCompleteDialog.click();
    }
    dialog.ok();
  }

  it('should have users with label in dropdown menu only', function () {
    idocPage.open(false, IDOC_ID);
    var tab = idocPage.getIdocTabs().getTabByIndex(0);
    var commentSection = tab.getContent().getCommentsSection();
    var comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    var dialog = comments.openCommentDialog();
    dialog.type('@');
    browser.wait(EC.presenceOf(element(by.css("#textcomplete-dropdown-1 > li.textcomplete-item"))), DEFAULT_TIMEOUT);
    browser.driver.findElements(by.css('#textcomplete-dropdown-1 > li.textcomplete-item')).then(function (elems) {
        expect(elems.length).to.equal(2);
      }
    );
  });

  it('should mention an user', function () {
    idocPage.open(false, IDOC_ID);
    var tab = idocPage.getIdocTabs().getTabByIndex(0);
    var commentSection = tab.getContent().getCommentsSection();
    var comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    var dialog = comments.openCommentDialog();
    writeMentionOrEmoji(dialog, '@Ja', true);
    var comment = comments.getCommentByIndex(0);
    expect(comment.getDescriptionAsText()).to.eventually.equal('Jane Doe');
  });

  it('should build link when mentioning an user', function () {
    idocPage.open(false, IDOC_ID);
    var tab = idocPage.getIdocTabs().getTabByIndex(0);
    var commentSection = tab.getContent().getCommentsSection();
    var comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    var dialog = comments.openCommentDialog();
    writeMentionOrEmoji(dialog, '@Ja', true);
    var comment = comments.getCommentByIndex(0);
    expect(comment.getDescriptionAsHtml()).to.eventually.contains('Jane Doe');
    expect(comment.getDescriptionAsHtml()).to.eventually.contains('data-mention-id="janedoe@doeandco.com"');
    expect(comment.getDescriptionAsHtml()).to.eventually.contains('href="#/idoc/janedoe@doeandco.com"');
    expect(comment.getDescriptionAsHtml()).to.eventually.contains('target="_blank"');
    expect(comment.getDescriptionAsHtml()).to.eventually.contains('class="instance-link"');
  });

  it('should not build link when user is not selected from dropdown', function () {
    idocPage.open(false, IDOC_ID);
    var tab = idocPage.getIdocTabs().getTabByIndex(0);
    var commentSection = tab.getContent().getCommentsSection();
    var comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    var dialog = comments.openCommentDialog();
    writeMentionOrEmoji(dialog, '@Jane Doe', false);
    var comment = comments.getCommentByIndex(0);
    expect(comment.getDescriptionAsText()).to.eventually.equal('@Jane Doe');
  });

  it('should type emoji', function () {
    idocPage.open(false, IDOC_ID);
    var tab = idocPage.getIdocTabs().getTabByIndex(0);
    var commentSection = tab.getContent().getCommentsSection();
    var comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    var dialog = comments.openCommentDialog();
    writeMentionOrEmoji(dialog, ':smil', true);
    var comment = comments.getCommentByIndex(0);
    expect(comment.getDescriptionAsText()).to.eventually.equal('😄');
  });

  it('should not type emoji when emoji is not selected from dropdown', function () {
    idocPage.open(false, IDOC_ID);
    var tab = idocPage.getIdocTabs().getTabByIndex(0);
    var commentSection = tab.getContent().getCommentsSection();
    var comments = new Comments(commentSection, new IdocCommentsToolbar(commentSection));
    var dialog = comments.openCommentDialog();
    writeMentionOrEmoji(dialog, ':smile', false);
    var comment = comments.getCommentByIndex(0);
    expect(comment.getDescriptionAsText()).to.eventually.equal(':smile');
  });

  it('should apply comments filtering', function () {
    idocPage.open(false, IDOC_ID);
    var tab = idocPage.getIdocTabs().getTabByIndex(0);
    var commentSection = tab.getContent().getCommentsSection();
    var commentsToolbar = new IdocCommentsToolbar(commentSection);
    var panel = commentsToolbar.openFilter();
    var comments = new Comments(commentSection, commentsToolbar);

    panel.keywordField().type('testKeyword').then(function () {
      panel.filter();
      comments.getComments().then(function (commentsEl) {
        expect(commentsEl.length).to.equal(0);
      });

    });

  });

});
