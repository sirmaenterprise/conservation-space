import {AnnotationEndpoint} from 'idoc/widget/image-widget/mirador-integration/annotation-endpoint';
import {CommentsRestServiceMock} from 'test/idoc/widget/image-widget/mirador-integration/comments-service.mock'
import {EMPTY_FILTERS} from 'idoc/comments/comments-filter/comments-filter-const';

describe('Tests for mirador annotation endpoint', function () {

  var annotationEndpoint;
  var commentsRestService;
  var imageWidget;

  beforeEach(function () {
    commentsRestService = CommentsRestServiceMock.mock();
    imageWidget = {
      filterConfig: {
        filters: {}
      },
      commentsFilterService: {
        filter: sinon.stub().returns([{
          comment: {}
        }])
      }
    };

    annotationEndpoint = new AnnotationEndpoint(commentsRestService, imageWidget);
  });

  it('should handle properly if there are not comments', function () {
    var miradorEndpointContext = {
      dfd: {
        resolve: sinon.spy()
      }
    };
    var options = {
      uri: 'none'
    };

    annotationEndpoint.search(miradorEndpointContext, options);

    expect(annotationEndpoint.commentRestService.loadAllComments.callCount).to.equal(1);
    expect(annotationEndpoint.commentRestService.loadAllComments.getCall(0).args[0]).to.equal(options.uri);
    expect(miradorEndpointContext.annotationsList.length).to.equal(0);
  });

  it('should call the comments rest service when calling search', function () {
    var miradorEndpointContext = {
      dfd: {
        resolve: sinon.spy()
      }
    };
    var options = {
      uri: 'all'
    };

    annotationEndpoint.search(miradorEndpointContext, options);

    expect(commentsRestService.loadAllComments.callCount).to.equal(1);
    expect(commentsRestService.loadAllComments.getCall(0).args[0]).to.equal(options.uri);
    expect(miradorEndpointContext.dfd.resolve.callCount).to.equal(1);
    expect(miradorEndpointContext.annotationsList[0].endpoint).to.equal(miradorEndpointContext);
  });

  it('should filter the comments if they are already laoded', function () {
    var miradorEndpointContext = {
      dfd: {
        resolve: sinon.spy()
      }
    };
    var options = {
      uri: 'all'
    };
    annotationEndpoint.widget.allComments = [{}];
    annotationEndpoint.search(miradorEndpointContext, options);

    expect(miradorEndpointContext.dfd.resolve.callCount).to.equal(1);
    expect(miradorEndpointContext.annotationsList[0].endpoint).to.equal(miradorEndpointContext);
  });

  it('should filter the comments with the saved filters when loading', function () {
    var miradorEndpointContext = {
      dfd: {
        resolve: sinon.spy()
      }
    };
    var options = {
      uri: 'all'
    };

    annotationEndpoint.widget.filterConfig.filters = EMPTY_FILTERS;
    annotationEndpoint.search(miradorEndpointContext, options);

    expect(commentsRestService.loadComments.callCount).to.equal(1);
    expect(commentsRestService.loadComments.getCall(0).args[0]).to.equal(options.uri);
    expect(miradorEndpointContext.dfd.resolve.callCount).to.equal(1);
    expect(miradorEndpointContext.annotationsList[0].endpoint).to.equal(miradorEndpointContext);

  });


  it('should call the comments rest service when calling deleteAnnotation', function () {
    var miradorEndpointContext = {};
    var successCallback = sinon.spy();

    annotationEndpoint.deleteAnnotation(miradorEndpointContext, 'imageId', successCallback);

    expect(commentsRestService.deleteComment.callCount).to.equal(1);
    expect(commentsRestService.deleteComment.getCall(0).args[0]).to.equal('imageId');
    expect(successCallback.callCount).to.equal(1);

  });

  it('should call the comments rest service when calling update', function () {
    var miradorEndpointContext = {};
    var successCallback = sinon.spy();
    var errorCallback = sinon.spy();
    var oaAnnotation = {
      '@id': 'imageId'
    };

    annotationEndpoint.update(miradorEndpointContext, oaAnnotation, successCallback, errorCallback);

    expect(commentsRestService.updateComment.callCount).to.equal(1);
    expect(oaAnnotation.endpoint).to.equal(miradorEndpointContext);
  });

  it('should return true when widget is in preview mode and it is not locked', function () {
    var miradorEndpointContext = {};
    var action = '';
    var annotation = {
      'emf:status': 'OPEN'
    };

    annotationEndpoint.widget = {
      context: {
        isPreviewMode: sinon.stub().returns(true)
      },
      config: {
        lockWidget: false
      }
    };

    expect(annotationEndpoint.userAuthorize(miradorEndpointContext, action, annotation)).to.be.true;
  });

  it('should return false when widget is in preview mode and it is locked ', function () {
    var miradorEndpointContext = {};
    var action = '';
    var annotation = '';

    annotationEndpoint.widget = {
      context: {
        isPreviewMode: sinon.stub().returns(true)
      },
      config: {
        lockWidget: true
      }
    };

    expect(annotationEndpoint.userAuthorize(miradorEndpointContext, action, annotation)).to.be.false;
  });

  it('should return true when widget is in not in preview mode and it is locked ', function () {
    var miradorEndpointContext = {};
    var action = '';
    var annotation = {
      'emf:status': 'OPEN'
    };

    annotationEndpoint.widget = {
      context: {
        isPreviewMode: sinon.stub().returns(false)
      },
      config: {
        lockWidget: true
      }
    };

    expect(annotationEndpoint.userAuthorize(miradorEndpointContext, action, annotation)).to.be.true;
  });

  it('should call createComment when calling create', function () {
    var miradorEndpointContext = {};
    var successCallback = sinon.spy();
    var errorCallback = sinon.spy();
    var oaAnnotation = {};

    annotationEndpoint.create(miradorEndpointContext, oaAnnotation, successCallback, errorCallback);

    expect(successCallback.callCount).to.equal(1);
    expect(successCallback.getCall(0).args[0].endpoint).to.equal(miradorEndpointContext);
  });


});