import {CommentsWidget} from 'idoc/widget/comments-widget/comments-widget';
import {CommentsRestService} from 'services/rest/comments-service';
import {SearchResolverService} from 'services/resolver/search-resolver-service';
import {DateRangeResolver} from 'idoc/widget/comments-widget/date-range-resolver';
import {IdocContext} from 'idoc/idoc-context';

import {IdocMocks} from '../../idoc-mocks';
import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';

describe('CommentsWidget', () => {
  let commentsWidget;

  let context = stub(IdocContext);
  context.getCurrentObject.returns(PromiseStub.resolve({isVersion: () => PromiseStub.resolve(false)}));
  context.isModeling.returns(false);


  let commentsService = stub(CommentsRestService);
  commentsService.loadRecentComments.returns(PromiseStub.resolve({data: {annotations: []}}));
  commentsService.loadCommentsCount.returns(PromiseStub.resolve({data: {count: 10}}));

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

  let searchResolver = stub(SearchResolverService);
  searchResolver.resolve.returns(PromiseStub.resolve([{}, {}]));

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

  let dateRangeResolver = stub(DateRangeResolver);
  dateRangeResolver.resolveRule.returns([]);

  CommentsWidget.prototype.control = {
    getId: () => {
      return 'widgetId';
    }
  };

  CommentsWidget.prototype.config = {};

  beforeEach(() => {
    commentsWidget = new CommentsWidget(commentsService, $scope, $element, $timeout, dateRangeResolver, searchResolver, eventbus, PromiseStub);
    searchResolver.resolve.reset();
    event.unsubscribe.reset();
    $element.find().css.reset();
    context.isPrintMode.reset();

    context.isPrintMode.returns(false);
    commentsWidget.context = context;
    commentsWidget.config = {
      offset: 0,
      limit: 10,
      selectedObjects: ['emf:1', 'emf:2'],
      selectCurrentObject: true,
      criteria: {search: true},
      selectObjectMode: 'automatically'
    };
  });

  describe('#createCommentsLoaderConfig', () => {
    it('should create comments loader config with automatically selected config', () => {
      commentsWidget.createCommentsLoaderConfig().then(config => {
        expect(searchResolver.resolve.calledOnce).to.be.true;
        expect(config.manuallySelectedObjects[0]).to.equal(commentsWidget.context.getCurrentObjectId());
        expect(config.filters.limit).to.equal(10);
      });
    });

    it('should filter out own id from selected objects and move to manually selected objects', () => {
      commentsWidget.config.selectedObjects = ['emf:test1', 'emf:test2', commentsWidget.context.getCurrentObjectId(), 'emf:test3'];
      commentsWidget.config.selectCurrentObject = true;
      commentsWidget.createCommentsLoaderConfig().then(config => expect(config.manuallySelectedObjects[0]).to.equal(commentsWidget.context.getCurrentObjectId()));
    });

    it('should craete comments loader config with manually selected config', () => {
      commentsWidget.config.selectObjectMode = 'manually';
      commentsWidget.createCommentsLoaderConfig().then(config => {

        expect(config.manuallySelectedObjects.length).to.equal(3);
        expect(config.criteria).to.be.empty;
        expect(config.filters.limit).to.equal(10);
      });
    });

    it('should create comments loader config with list all comments in preview mode', () => {
      commentsWidget.config.selectObjectMode = 'manually';
      commentsWidget.context.isPrintMode.returns(true);

      commentsWidget.createCommentsLoaderConfig().then((config) => {
        expect(config.filters.limit).to.equal(0);
      });
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
    expect(commentsWidget.loadingElement.css.called).to.be.true;
  });
});
