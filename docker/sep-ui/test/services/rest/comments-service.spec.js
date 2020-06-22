import {CommentsRestService} from 'services/rest/comments-service';
import {CommentBuilder} from 'idoc/comments/comment-builder';

import {PromiseStub} from 'test/promise-stub';

describe('CommentsRestService', () => {
  let restClient = {};
  let authorityService = {};
  let commentRestService;

  beforeEach(() => {
    restClient = {
      get: sinon.spy(),
      post: sinon.spy(),
      delete: sinon.spy()
    };
    authorityService.getToken = sinon.stub();
    authorityService.getToken.onFirstCall().returns(PromiseStub.resolve('userToken'));
    commentRestService = new CommentsRestService(restClient, authorityService);
  });

  describe('#loadComments()', () => {
    it('should load all the comments', ()=> {
      commentRestService.loadAllComments();
      expect(restClient.get.calledOnce).to.be.true;
      expect(restClient.get.getCall(0).args[0]).to.contains('annotations/search/all');
    });

    it('should call the correct url', () => {
      commentRestService.loadComments();
      expect(restClient.get.called).to.be.true;
      expect(restClient.get.getCall(0).args[0]).to.contains('annotations/search');
    });

    it('should pass correct parameters', () => {
      let data = {
        params: {
          media: 'image',
          limit: '1000',
          id: 'emf:123456',
          tabId: '147',
          APIKey: 'userToken'
        }
      };
      commentRestService.loadComments('emf:123456', '147');
      expect(restClient.get.calledOnce).to.be.true;
      expect(restClient.get.getCall(0).args[1]).to.deep.equal(data);
    });
  });

  describe('#loadReplies()', () => {
    it('should call the correct url', () => {
      commentRestService.loadReplies('id');
      expect(restClient.get.calledOnce).to.be.true;
      expect(restClient.get.getCall(0).args[0]).to.contains('annotations/id');
    });

    it('should pass correct parameters', () => {
      let data = {
        params: {
          media: 'image',
          limit: '1000',
          APIKey: 'userToken'
        }
      };
      commentRestService.loadReplies('emf:123456');
      expect(restClient.get.calledOnce).to.be.true;
      expect(restClient.get.getCall(0).args[1]).to.deep.equal(data);
    });
  });

  describe('#createComment()', () => {
    it('should call the correct url', () => {
      commentRestService.createComment();
      expect(restClient.post.calledOnce).to.be.true;
      expect(restClient.post.getCall(0).args[0]).to.contains('annotations/create');
    });

    it('should pass correct parameters when creating comment', () => {
      // commentRestService.createComment('emf:123456', null, 'commentDescription');
      // expect(restClient.post.calledOnce).to.be.true;
      // expect(restClient.post.getCall(0).args[1].on).to.have.deep.property('full', 'emf:123456');
      // expect(restClient.post.getCall(0).args[1].resource[0]).to.have.deep.property('chars', 'commentDescription');
      // expect(restClient.post.getCall(0).args[1]['emf:commentsOn']).to.be.undefined;
      // commentRestService.createComment('emf:123456', 'tab1', 'commentDescription');
      // expect(restClient.post.getCall(1).args[1]).to.have.deep.property('emf:commentsOn', 'tab1');
    });

    it('should pass correct parameters when creating reply', () => {

      var comment = CommentBuilder.constructComment('emf:123456', null, 'commentDescription', 'emf:topicComment');

      commentRestService.createComment(comment);
      expect(restClient.post.calledOnce).to.be.true;
      expect(restClient.post.getCall(0).args[1].on).to.have.deep.property('full', 'emf:123456');
      expect(restClient.post.getCall(0).args[1].resource[0]).to.have.deep.property('chars', 'commentDescription');
      expect(restClient.post.getCall(0).args[1]['emf:commentsOn']).to.be.undefined;
      expect(restClient.post.getCall(0).args[1]['emf:replyTo']).to.equal('emf:topicComment');
    });

    it('should pass correct parameters when creating reply with tab id', () => {
      var comment = CommentBuilder.constructComment('emf:123456', 'tab1', 'commentDescription');
      commentRestService.createComment(comment);
      expect(restClient.post.calledOnce).to.be.true;
      expect(restClient.post.getCall(0).args[1]).to.have.deep.property('emf:commentsOn', 'tab1');
    });
  });

  describe('#updateComment()', () => {
    it('should call the correct url', () => {
      commentRestService.updateComment(null, {});
      expect(restClient.post.calledOnce).to.be.true;
      expect(restClient.post.getCall(0).args[0]).to.contains('annotations/update');
    });

    it('should pass correct parameters', () => {
      let comment = {
        'resource': [{
          'chars': 'description'
        }],
        'on': {
          'full': 'emf:123456'
        }
      };

      let config = {
        params: {
          APIKey: 'userToken'
        }
      };
      commentRestService.updateComment('emf:123456', comment);
      expect(restClient.post.calledOnce).to.be.true;
      expect(restClient.post.getCall(0).args[0]).to.contains('emf%3A123456');
      expect(restClient.post.getCall(0).args[1]).to.deep.equal(comment);
      expect(restClient.post.getCall(0).args[2]).to.deep.equal(config);

    });
  });

  describe('#deleteComment()', () => {
    it('should call the correct url', () => {
      commentRestService.deleteComment();
      expect(restClient.delete.calledOnce).to.be.true;
      expect(restClient.delete.getCall(0).args[0]).to.contains('annotations/destroy');
    });
    it('should pass correct parameters', () => {
      commentRestService.deleteComment('emf:123456');
      expect(restClient.delete.calledOnce).to.be.true;
      expect(restClient.delete.getCall(0).args[0]).to.contains('APIKey=userToken&id=emf%3A123456');
    });
  });

  describe('#loadRecentComments()', () => {
    it('should call the correct url', () => {
      commentRestService.loadRecentComments({});
      expect(restClient.post.calledOnce).to.be.true;
    });
    it('should pass correct parameters', () => {
      commentRestService.loadRecentComments({});
      expect(restClient.post.calledOnce).to.be.true;
    });
  });

  describe('#loadCommentsCount()', () => {
    it('should call the correct url', () => {
      commentRestService.loadCommentsCount({});
      expect(restClient.post.calledOnce).to.be.true;
    });
    it('should pass correct parameters', () => {
      commentRestService.loadCommentsCount({});
      expect(restClient.post.calledOnce).to.be.true;
      expect(restClient.post.getCall(0).args[0]).to.contains('count');
    });
  });
});