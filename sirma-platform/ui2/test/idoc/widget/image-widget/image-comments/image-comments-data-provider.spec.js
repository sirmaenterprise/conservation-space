import {ImageCommentsDataProvider} from 'idoc/widget/image-widget/image-comments/image-comments-data-provider';
import {CommentParser} from 'idoc/comments/comment-parser';

describe('ImageCommentsDataProvider', () => {
  let imageCommentsDataProvider;
  beforeEach(function () {
    let commentsRestService = {
      loadComments: sinon.spy(),
      createComment: sinon.spy(),
      updateComment: sinon.spy(),
      deleteComment: sinon.spy(),
      loadAllComments: sinon.spy()
    };
    imageCommentsDataProvider = new ImageCommentsDataProvider(commentsRestService);
    let config = {
      eventsAdapter: {
        getCurrentImageId() {
          return 'imageId';
        },
        publishMiradorEvent: sinon.spy(),
        publishEventbusEvent: sinon.spy(),
        getCurrentSlotId(){
          return '1'
        },
        switchCommentIds: sinon.spy(function (comment) {
          return comment;
        }),
        isImageView(){
          return true;
        }
      }
    };
    imageCommentsDataProvider.setEventsAdapter(config);
  });

  it('should load all comments', () => {
    imageCommentsDataProvider.loadAllComments();
    expect(imageCommentsDataProvider.commentsRestService.loadAllComments.callCount).to.equal(1);
    expect(imageCommentsDataProvider.commentsRestService.loadAllComments.firstCall.args[0]).to.equal('imageId');
  });

  it('should create comment', (done) => {
    let config = {
      content: 'content',
      options: {
        getSVGString(){

        },
        shape: {}
      }
    };
    return imageCommentsDataProvider.createComment(config).then(function () {
      expect(imageCommentsDataProvider.eventsAdapter.publishMiradorEvent.callCount).to.equal(1);
      done()
    }).catch(done);
  });

  it('should update comment edited from mirador with fullId provided', (done) => {
    let config = {
      comment: {
        isReply() {
          return false;
        },
        getData(){
          return true;
        },
        getComment(){
          return 'commentAsString';
        }
      },
      instanceId: 'commentId',
      content: 'content'
    };
    imageCommentsDataProvider.updateComment(config).then(function () {
      expect(imageCommentsDataProvider.eventsAdapter.publishMiradorEvent.callCount).to.equal(1);
      expect(imageCommentsDataProvider.eventsAdapter.publishMiradorEvent.args[0][1]).to.deep.equal(['commentAsString']);
      done();
    }).catch(done);
  });

  it('should update comment with fullId provided', (done) => {
    let config = {
      comment: {
        isReply() {
          return false;
        },
        getData(){
          return false;
        },
        getComment(){
          return 'commentAsString';
        }
      },
      instanceId: 'commentId',
      content: 'content'
    };
    imageCommentsDataProvider.updateComment(config).then(function () {
      expect(imageCommentsDataProvider.eventsAdapter.publishMiradorEvent.callCount).to.equal(1);
      expect(imageCommentsDataProvider.eventsAdapter.publishMiradorEvent.args[0][1]).to.deep.equal(['commentAsString']);
      done();
    }).catch(done);
  });

  it('should update comment without fullId provided (will need call the mapping function)', (done) => {
    let config = {
      comment: {
        isReply() {
          return false;
        },
        getData(){
          return true;
        },
        getComment(){
          return 'commentAsString';
        }
      },
      instanceId: 'commentId',
      content: 'content'
    };
    imageCommentsDataProvider.updateComment(config).then(function () {
      expect(imageCommentsDataProvider.eventsAdapter.publishMiradorEvent.callCount).to.equal(1);
      expect(imageCommentsDataProvider.eventsAdapter.publishMiradorEvent.args[0][1]).to.deep.equal(['commentAsString']);
      done();
    }).catch(done);
  });

  it('should delete comment with full id provided', (done) => {
    let config = {
      comment: {
        isReply(){
          return false;
        },
        getId(){
          return '1';
        }
      },
      commentId: 'commentId'
    };
    imageCommentsDataProvider.deleteComment(config).then(function () {
      expect(imageCommentsDataProvider.eventsAdapter.publishMiradorEvent.callCount).to.equal(1);
      expect(imageCommentsDataProvider.eventsAdapter.publishMiradorEvent.args[0][1]).to.deep.equal(['1']);
      done();
    }).catch(done);

  });

  it('should delete comment with no full id provided', (done) => {
    let config = {
      comment: {
        isReply(){
          return false;
        },
        getId(){
          return '1';
        }
      },
      commentId: 'commentId'
    };
    imageCommentsDataProvider.deleteComment(config).then(function () {
      expect(imageCommentsDataProvider.eventsAdapter.publishMiradorEvent.callCount).to.equal(1);
      expect(imageCommentsDataProvider.eventsAdapter.publishMiradorEvent.args[0][1]).to.deep.equal(['1']);
      done();
    }).catch(done);

  });

  it('should create reply', () => {
    let config = {
      replyTo: 'commentId',
      content: 'content'
    };
    imageCommentsDataProvider.createComment(config);
    expect(imageCommentsDataProvider.commentsRestService.createComment.callCount).to.equal(1);
  });


  it('should update reply', () => {
    let config = {
      comment: {
        isReply() {
          return true;
        }
      },
      instanceId: 'commentId',
      content: 'content'
    };
    imageCommentsDataProvider.updateComment(config);
    expect(imageCommentsDataProvider.commentsRestService.updateComment.callCount).to.equal(1);
    expect(imageCommentsDataProvider.commentsRestService.updateComment.firstCall.args[0]).to.equal('commentId');
    expect(imageCommentsDataProvider.commentsRestService.updateComment.firstCall.args[1]).to.equal('content');
  });

  it('should delete reply', () => {
    let config = {
      comment: {
        isReply: ()=> {
          return true;
        }
      },
      commentId: 'commentId'
    };
    imageCommentsDataProvider.deleteComment(config);
    expect(imageCommentsDataProvider.commentsRestService.deleteComment.callCount).to.equal(1);
    expect(imageCommentsDataProvider.commentsRestService.deleteComment.firstCall.args[0]).to.equal('commentId');
  });

  it('should fire event to mirador when closing comment dialog', ()=> {
    imageCommentsDataProvider.dialogClosed();
    expect(imageCommentsDataProvider.eventsAdapter.publishMiradorEvent.callCount).to.equal(1);
  });

});