var ImageWidgetSandboxPage = require('./image-widget').ImageWidgetSandboxPage;
var Comments = require('../../comments/comments');

describe('ImageWidget', function () {

  var page = new ImageWidgetSandboxPage();

  beforeEach(() => {
    // Given I have opened the sandbox page
    page.open();
  });

  describe('comments', function () {

    it('it should have inserted image in image widget with 1 comment', function () {
      var imageWidget = page.getWidget();
      var commentSection = imageWidget.getCommentsSection();
      commentSection.waitUntilLoaded();
      var imageComments = new Comments(commentSection.element);
      var comments = imageComments.getComments();
      expect(comments.count()).to.eventually.equal(1);
    });

    it('should have loaded the comment section on widget load', function () {
      var imageWidget = page.getWidget();
      var commentSection = imageWidget.getCommentsSection();
      commentSection.waitUntilLoaded();
      expect(commentSection.element.isDisplayed()).to.eventually.equal(true);
    });

    it('it should reply to existing comment after canceling the first reply', function () {
      var imageWidget = page.getWidget();
      var commentSection = imageWidget.getCommentsSection();
      commentSection.waitUntilLoaded();
      var imageComments = new Comments(commentSection.element);
      var comment = imageComments.getCommentByIndex(0);
      var replyDialog = comment.getCommentActions().reply();
      replyDialog.type('TestReply').then(function () {
        replyDialog.cancel();
        replyDialog = comment.getCommentActions().reply();
        expect(replyDialog.element.isPresent()).to.eventually.be.true;
      });
    });

    it('should not delete existing comment', function () {
      var imageWidget = page.getWidget();
      var commentSection = imageWidget.getCommentsSection();
      commentSection.waitUntilLoaded();
      var imageComments = new Comments(commentSection.element);
      imageComments.getCommentByIndex(0).getCommentActions().delete(false).then(function () {
        expect(imageComments.getComments().count()).to.eventually.equal(1);
      });
    });

    it('should delete existing comment', function () {
      var imageWidget = page.getWidget();
      imageWidget.switchToIframe().then(function (mirador) {
        imageWidget.switchToMainFrame().then(function () {
          var commentSection = imageWidget.getCommentsSection();
          commentSection.waitUntilLoaded();
          var imageComments = new Comments(commentSection.element);
          imageComments.waitUntilLoaded();
          imageComments.getCommentByIndex(0).getCommentActions().delete(true).then(function () {
            expect(imageComments.element.isPresent()).to.eventually.be.false;
          });
        });
      });
    });

    it('should not show comments in comments section when in gallery mode', function () {
      var imageWidget = page.getWidget();
      var commentSection = imageWidget.getCommentsSection();
      commentSection.waitUntilLoaded();
      var imageComments = new Comments(commentSection.element);
      imageWidget.switchToIframe().then(function (mirador) {
        mirador.changeViewModeToGallery().then(function () {
          imageWidget.switchToMainFrame().then(function () {
            commentSection = imageWidget.getCommentsSection();
            commentSection.waitUntilLoaded();
            expect(commentSection.hasComments()).to.eventually.equal(false);
          });
        });
      });
    });

  });
});