import {CommentsWidget} from 'idoc/widget/comments-widget/comments-widget';
import {IdocMocks} from '../../idoc-mocks';
import {PromiseStub} from 'test/promise-stub';

describe('CommentsWidget', () => {
  let commentsWidget;

  let commentsService = {
    loadRecentComments: () => {
      return PromiseStub.resolve({data: {}});
    },
    loadCommentsCount: () => {
      return PromiseStub.resolve({data: {count: 10}});
    }
  };

  let $scope = {
    $watchCollection: (callback) => {
      callback();
    }
  };

  let event = {
    unsubscribe: sinon.spy()
  };

  let eventbus = IdocMocks.mockEventBus();
  eventbus.subscribe = () => {
    return event
  };

  let searchResolver = {
    resolve: sinon.stub().returns(PromiseStub.resolve([{}, {}]))
  };

  let $element = {
    find: () => {
      let array = [{scrollHeight: 0}];
      array.css = sinon.spy();
      array.scroll = (callback) => {
        callback();
      };
      array.scrollTop = () => {
        return 50;
      };
      array.innerHeight = () => {
        return 100;
      };
      return array;
    }
  };

  let $timeout = ((callback) => {
    callback();
  });

  let dateRangeResolver = {
    resolveRule: () => {
      return [];
    }
  };

  CommentsWidget.prototype.control = {
    getId: () => {
      return 'widgetId';
    }
  };

  CommentsWidget.prototype.config = {
    offset: 0,
    limit: 10,
    selectedObjects: ['emf:1', 'emf:2'],
    selectCurrentObject: true,
    criteria: {search: true},
    selectObjectMode: 'automatically'
  };

  CommentsWidget.prototype.context = {
    currentObjectId: "emf:1234",
    getCurrentObjectId: function() {
      return this.currentObjectId;
    },
    isPrintMode: sinon.spy(),
    isModeling: () => false
  };

  beforeEach(() => {
    commentsWidget = new CommentsWidget(commentsService, $scope, $element, $timeout, dateRangeResolver, searchResolver, eventbus);
    commentsWidget.config.limit = 10;
  });

  it('should create comments loader config with automatically selected config', (done) => {
    commentsWidget.createCommentsLoaderConfig().then((config) => {
      expect(searchResolver.resolve.calledOnce).to.be.true;
      expect(config.manuallySelectedObjects[0]).to.equal(commentsWidget.context.getCurrentObjectId());
      expect(config.filters.limit).to.equal(10);
      done();
    });
  });

  it('should craete comments loader config with manually selected config', (done) => {
    commentsWidget.config.selectObjectMode = 'manually';
    commentsWidget.createCommentsLoaderConfig().then((config) => {
      expect(config.manuallySelectedObjects.length).to.equal(3);
      expect(config.criteria).to.be.empty;
      expect(config.filters.limit).to.equal(10);
      done();
    });
  });

  it('should craete comments loader config with list all comments in preview mode', (done) => {
    commentsWidget.config.selectObjectMode = 'manually';
    commentsWidget.context.isPrintMode = function () {
      return true;
    }

    commentsWidget.createCommentsLoaderConfig().then((config) => {
      expect(config.filters.limit).to.equal(0);
      done();
    });
  });

  it('should hanlde comments reload event', () => {
    commentsWidget.obtainComments = sinon.spy();
    commentsWidget.handleReloadComments([commentsWidget.control.getId()]);
    expect(commentsWidget.obtainComments.called).to.be.true;
  });

  it('should unsubscribe from the events when destroyed', () => {
    commentsWidget.ngOnDestroy();
    expect(event.unsubscribe.called).to.be.true;
  });

  it('should not load new comments when the scroll is active', () => {
    commentsWidget.loadingActive = true;
    let scroll = commentsWidget.registerInfiniteScroll();
    expect(scroll).to.be.undefined;
  });

  it('should load the comments when the scroll is to the bottom of the comments', () => {
    commentsWidget.loadingActive = false;
    commentsWidget.ngAfterViewInit();
    expect(commentsWidget.loadingElement.css.callCount).to.equal(1);
  });
});
