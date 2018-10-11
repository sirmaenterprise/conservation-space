import {ResultsWithActions} from 'external-search/components/results-with-actions';
import {ExternalObjectService} from 'services/rest/external-object-service';
import {SearchResults} from 'search/components/common/search-results';
import {NO_SELECTION, SINGLE_SELECTION, MULTIPLE_SELECTION} from 'search/search-selection-modes';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';
import {PromiseStub} from 'test/promise-stub';
import {ReloadSearchEvent} from 'external-search/actions/reload-search-event';

describe('ResultsWithActions', () => {

  let eventbus;
  let scope;
  let element;
  let service;
  let results;
  let config;
  let searchMediator;
  let getSpy = sinon.spy();

  beforeEach(() => {
    scope = mock$scope();
    config = {};
    config.searchMediator = {};
    eventbus = mockEventBus();
    service = importObjectsMock();
    searchMediator = searchMediatorMock();
    results = new ResultsWithActions(scope);
    results.eventbus = eventbus;
    results.notification = mockNotificationService();
  });

  it('Check if the result is selectable', () => {
    results.config.selection = SINGLE_SELECTION;
    expect(results.isSelectable()).to.be.true;
  });

  it('Should return select type - single selection', () => {
    results.config.selection = SINGLE_SELECTION;
    expect('radio').to.equal(results.selectionType());
  });

  it('Should return select type - multiple selection', () => {
    results.config.selection = MULTIPLE_SELECTION;
    expect('checkbox').to.equal(results.selectionType());
  });

  it('should properly handle selection', () => {
    results.selectedItems = ['1'];
    results.handleSelection('2');
    expect(results.selectedItems.length).to.equal(2);
  });

  it('should properly deselect multiple items', () => {
    results.selectedItems = ['1', '2'];
    results.handleSelection('3');
    results.handleSelection('3');
    expect(results.selectedItems.length).to.equal(2);
  });


  it('should properly deselect all items', () => {
    results.selectedItems = ['1', '2'];
    results.deselectAll();
    expect(results.selectedItems.length).to.equal(0);
  });

  it('should properly select all items', () => {
    results.results = [];
    results.results.data = [{
      data: {
        selectable: true
      }
    }, {
      data: {
        selectable: true
      }
    }];
    results.selectAll();
    expect(results.selectedItems.length).to.equal(2);
  });

  it('should return tabs config', () => {
    expect({
      activeTab: 'results'
    }).to.deep.equal(results.getTabsConfig());
  });

  it('Import instance ', () => {
    results.service = service;
    results.searchMediator = searchMediator;
    results.executeAction([]);
    expect(service.importObjects.calledOnce).to.be.true;
    expect(eventbus.publish.calledOnce).to.be.true;
  });

  it('should clear selected items after multiple import', () => {
    results.service = service;
    results.selectedItems = ['1', '2'];
    results.searchMediator = searchMediator;
    results.executeAction(results.selectedItems);
    expect(results.selectedItems.length).to.equal(0);
  });

  it('should warn user for status 403', () => {
    results.selectedItems = ['1', '2'];
    results.translateService = mockTranslateService();
    results.notification = mockNotificationService();
    let response = {
      status: 403
    };
    results.searchMediator = searchMediator;
    results.executeRefresh(response);
    expect(results.notification.warning.calledOnce).to.be.true;
  });

  function importObjectsMock(objects) {
    let response = {};
    response.data = {
      cause: {
        message: 'Hello World'
      }
    };
    return {
      importObjects: sinon.spy((objects) => {
        return PromiseStub.resolve(response);
      })
    }
  }

  function searchMediatorMock() {
    return {
      lastSearch: sinon.spy(() => {
        return true;
      })
    }
  }

  function mockEventBus() {
    return {
      publish: sinon.spy(() => {
        return true;
      })
    };
  }

  function mockNotificationService() {
    return {
      error: sinon.spy((message) => {

      }),
      success: sinon.spy((message) => {

      }),
      warning : sinon.spy((message) => {

      })
    };
  }

  function mockTranslateService() {
    return {
      translateInstant: (string) => {
        return string;
      }
    };
  }

});