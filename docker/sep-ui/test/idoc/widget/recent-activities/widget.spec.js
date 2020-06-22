import {RecentActivities} from 'idoc/widget/recent-activities/widget';
import {WidgetReadyEvent} from 'idoc/widget/widget-ready-event';

import {PromiseStub} from 'test/promise-stub';

describe('RecentActivities', () => {

  let widget;
  beforeEach(() => {
    RecentActivities.prototype.config = {};
    RecentActivities.prototype.control = {
      getId: () => 1,
      subscribe: sinon.spy()
    };
    RecentActivities.prototype.context = {
      isModeling: () => false,
      getCurrentObject: function () {
        return PromiseStub.resolve({
          isVersion: () => false,
          models: {validationModel: {modifiedOn: {defaultValue: moment().valueOf()}}}
        });
      }
    };
    widget = getWidgetInstance();
  });

  afterEach(() => {
    RecentActivities.prototype.config = undefined;
  });

  function mockObjectSelectorHelper(data, reject = false) {
    return {
      getSelectedObjects: sinon.spy(() => {
        if (reject) {
          return PromiseStub.reject(data);
        }
        return PromiseStub.resolve(data);
      })
    };
  }

  function mockInstanceRestService(response) {
    return {
      loadAuditDataForInstances: sinon.spy(() => {
        return PromiseStub.resolve(response);
      })
    };
  }

  function getWidgetInstance() {
    let locationAdapterMock = {
      url: sinon.spy()
    };
    let eventbus = {
      publish: sinon.spy()
    };
    let instanceRestService = mockInstanceRestService({data: []});
    let objectSelectorHelper = mockObjectSelectorHelper({results: []});
    return new RecentActivities(objectSelectorHelper, eventbus, instanceRestService, locationAdapterMock);
  }

  it('should always show first/last buttons in pagination', () => {
    expect(widget.paginationConfig.showFirstLastButtons).to.be.true;
  });

  describe('resultsTotal', () => {

    it('should return 0 if there is no search request yet', () => {
      expect(widget.resultsTotal).to.eq(0);
    });

    it('should calculate total from search request', () => {
      widget.searchRequest = {
        offset: 5,
        limit: 5
      };

      expect(widget.resultsTotal).to.eq(10);
    });
  });

  describe('ngOnInit()', () => {

    it('should initiate data loading', () => {
      widget.load = sinon.spy();
      widget.ngOnInit();

      expect(widget.load.calledOnce).to.be.true;
    });
  });

  describe('loadNextPage(data)', () => {

    it('should update current page and call load', () => {
      let num = new Date().getTime();
      widget.load = sinon.spy();
      widget.loadNextPage({pageNumber: num});

      expect(widget.currentPage).to.eq(num);
      expect(widget.load.calledOnce).to.be.true;
    });
  });

  describe('onConfigConfirmed(config)', () => {

    it('should remove unnecessary properties from selected items', () => {
      widget.load = () => {
      };
      let config = {
        selectedItems: [{id: 1, test: 2}]
      };

      widget.onConfigConfirmed(config);
      expect(config.selectedItems).to.deep.eq([{id: 1}]);
    });

    it('should pageSize in pagination config and call load', () => {
      let time = new Date().getTime();
      widget.load = sinon.spy();
      let config = {
        pageSize: time
      };

      widget.onConfigConfirmed(config);

      expect(widget.paginationConfig.pageSize).to.eq(time);

      expect(widget.load.calledOnce).to.be.true;
      expect(widget.load.getCall(0).args[0]).to.deep.eq(config);
    });

    it('should reset current page number', () => {
      widget.currentPage = 101;
      widget.load = () => {
      };
      let config = {
        selectedItems: [{id: 1, test: 2}]
      };

      widget.onConfigConfirmed(config);
      expect(widget.currentPage).to.equal(1);
    });
  });

  describe('load(config)', () => {

    beforeEach(() => {
      widget.instanceRestService = mockInstanceRestService({
        data: [{}]
      });
    });

    it('should display an error message if there is a problem during activities loading', () => {
      widget.objectSelectorHelper = mockObjectSelectorHelper({reason: 'Error!'}, true);
      widget.load({});
      expect(widget.errorMessage).to.equal('Error!');
    });

    it('should resolve the provided array of instance identifiers', () => {
      widget.objectSelectorHelper = mockObjectSelectorHelper({results: ['123']});
      widget.load({});

      let loadSpy = widget.instanceRestService.loadAuditDataForInstances;
      expect(loadSpy.calledOnce).to.be.true;
      expect(loadSpy.getCall(0).args[0]).to.deep.eq(['123']);
    });

    it('should resolve the provided array of instance objects', () => {
      widget.objectSelectorHelper = mockObjectSelectorHelper({results: [{id: '123'}]});
      widget.load({});

      let loadSpy = widget.instanceRestService.loadAuditDataForInstances;
      expect(loadSpy.calledOnce).to.be.true;
      expect(loadSpy.getCall(0).args[0]).to.deep.eq(['123']);
    });

    it('should call load data with selected objects, limit and offset', () => {
      widget.currentPage = 5;
      widget.objectSelectorHelper = mockObjectSelectorHelper({results: [{id: '123'}]});
      widget.load({pageSize: 5});

      let loadSpy = widget.instanceRestService.loadAuditDataForInstances;
      expect(loadSpy.calledOnce).to.be.true;
      expect(loadSpy.getCall(0).args[0]).to.deep.eq(['123']);
      expect(loadSpy.getCall(0).args[1]).to.eq(5);
      expect(loadSpy.getCall(0).args[2]).to.eq(20);
    });

    it('should send 0 as offset and -1 as limit if pageSize is "all"', () => {
      widget.objectSelectorHelper = mockObjectSelectorHelper({results: [{id: '1'}]});
      widget.load({pageSize: 'all'});

      let loadSpy = widget.instanceRestService.loadAuditDataForInstances;
      expect(loadSpy.calledOnce).to.be.true;
      expect(loadSpy.getCall(0).args[0]).to.deep.eq(['1']);
      expect(loadSpy.getCall(0).args[1]).to.eq(-1);
      expect(loadSpy.getCall(0).args[2]).to.eq(0);
    });

    it('should remove any error message on successful activities load', () => {
      widget.errorMessage = 'huston, we have a problem!';
      widget.objectSelectorHelper = mockObjectSelectorHelper({results: [{id: '123'}]});
      widget.load({pageSize: 5});
      expect(widget.errorMessage).to.not.exist;
    });

    it('should enable the pagination after loading activities', () => {
      widget.paginationConfig.disabled = true;
      widget.objectSelectorHelper = mockObjectSelectorHelper({results: [{id: '123'}]});
      widget.load({});
      expect(widget.paginationConfig.disabled).to.be.false;
    });

    it('should enable the pagination even if there is an error during activities loading', () => {
      widget.paginationConfig.disabled = true;
      widget.objectSelectorHelper = mockObjectSelectorHelper('Error!', true);
      widget.load({});
      expect(widget.paginationConfig.disabled).to.be.false;
    });

    it('should subscribe for userActivityRendered event to check if the widget is ready', () => {
      widget.objectSelectorHelper = mockObjectSelectorHelper({results: [{id: '123'}]});
      widget.load({pageSize: 5});
      expect(widget.control.subscribe.calledOnce).to.be.true;
    });

    it('should fire an event to notify that the widget is ready if there is an error', () => {
      widget.objectSelectorHelper = mockObjectSelectorHelper('Error!', true);
      widget.load({pageSize: 5});
      expect(widget.eventbus.publish.calledOnce).to.be.true;
    });


    it('should remove version part of the id', () => {
      let ids = widget.getIdentifiers([{id: '123-v1.1'}]);
      expect(ids[0]).to.eq('123');
    });
  });

  describe('loadAuditData(objects, limit, offset)', () => {

    it('should call the service and update the response', () => {
      let response = {
        data: ['test']
      };
      widget.instanceRestService = mockInstanceRestService(response);

      widget.loadAuditData([{id: '1'}]);
      expect(widget.instanceRestService.loadAuditDataForInstances.calledOnce).to.be.true;
      expect(widget.instanceRestService.loadAuditDataForInstances.getCall(0).args[0]).to.deep.equal(['1']);
      expect(widget.searchRequest).to.deep.equal(response);
    });

    it('should display error message if no activities are returned', () => {
      widget.instanceRestService = mockInstanceRestService({});

      widget.loadAuditData([{id: '1'}]);
      expect(widget.instanceRestService.loadAuditDataForInstances.calledOnce).to.be.true;
      expect(widget.searchRequest).to.not.exist;

      widget.instanceRestService = mockInstanceRestService({data: []});
      widget.instanceRestService.loadAuditDataForInstances.reset();

      widget.loadAuditData([{id: '1'}]);
      expect(widget.instanceRestService.loadAuditDataForInstances.calledOnce).to.be.true;
      expect(widget.searchRequest).to.not.exist;
    });

    it('should hide any previous activities if none are returned', () => {
      widget.instanceRestService = mockInstanceRestService({data: []});
      widget.instanceRestService.searchRequest = {};

      widget.loadAuditData([{id: '1'}]);
      expect(widget.searchRequest).to.not.exist;
    });
  });

  describe('fireWidgetReadyEvent()', () => {

    it('should publish a WidgetReadyEvent', () => {
      widget.fireWidgetReadyEvent();

      expect(widget.eventbus.publish.calledOnce).to.be.true;

      let event = widget.eventbus.publish.getCall(0).args[0];
      expect(event instanceof WidgetReadyEvent).to.be.true;
      expect(event.getData()[0]).to.deep.eq({widgetId: 1});
    });
  });

  it('should set pageSize to all if mode is print', () => {
    let locationAdapterMock = {
      url: () => {
        return 'testURL?mode=print';
      }
    };
    widget = new RecentActivities(undefined, undefined, undefined, locationAdapterMock);
    expect(widget.config.pageSize).to.equals('all');
  });
});
