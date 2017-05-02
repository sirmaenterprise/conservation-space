import {IdocCommentsToolbar} from 'idoc/idoc-comments/idoc-comments-toolbar';
import {PromiseStub} from 'test/promise-stub';
import {EMPTY_FILTERS} from 'idoc/comments/comments-filter/comments-filter-const';

describe('Idoc comments toolbar test', function () {

  let idocCommentsToolbar, eventbus, element, mockElement;
  eventbus = {
    subscribe: sinon.spy()
  };

  element = {
    addClass: sinon.spy(),
    removeClass: sinon.spy()
  };
  mockElement = {
    remove: sinon.spy(),
    find: sinon.stub().returns(element)
  };

  beforeEach(function () {
    IdocCommentsToolbar.prototype.commentsComponent = {config: {}, tabId: 'tabId'};
    IdocCommentsToolbar.prototype.context = {};
    IdocCommentsToolbar.prototype.context.getCurrentObject = () => {
      return PromiseStub.resolve({
        isPersisted: () => {
        }
      });
    };
    idocCommentsToolbar = new IdocCommentsToolbar(eventbus, mockElement);
  });

  it('should indicate that a filter applied', function () {
    idocCommentsToolbar.commentsComponent.config.filtersConfig = {
      filters: {}
    };

    idocCommentsToolbar.applyFilter(['tabId']);
    expect(element.addClass.called).to.be.true;
  });

  it('should not indicate when a filter in not applied', function () {
    idocCommentsToolbar.commentsComponent.config.filtersConfig = {
      filters: EMPTY_FILTERS
    };

    idocCommentsToolbar.applyFilter(['tabId']);
    expect(element.removeClass.called).to.be.true;
  });
});
