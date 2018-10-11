import {SearchContextMenu} from 'search/components/search-bar/search-context-menu';
import {Eventbus} from 'services/eventbus/eventbus';
import {RecentObjectsService} from 'services/recent/recent-objects-service';
import {RecentObjectAddedEvent, RecentObjectUpdatedEvent, RecentObjectRemovedEvent} from 'recent-objects/events';
import {InstanceRestService} from 'services/rest/instance-service';
import {CURRENT_OBJECT} from 'services/context/contextual-objects-factory';
import {HEADER_BREADCRUMB} from 'instance-header/header-constants';

import {stub} from 'test/test-utils';
import {PromiseStub} from 'test/promise-stub';
import {stubContextualFactory} from 'test/services/context/contextual-objects-factory.stub';

describe('SearchContextMenu', () => {

  let contextMenu;
  beforeEach(() => {
    contextMenu = new SearchContextMenu(stubEventbus(), stubRecentObjectsService(), stubInstanceRestService(), PromiseStub, stubContextualFactory());
    contextMenu.ngOnInit();
  });

  describe('upon initialization', () => {
    it('should enable current object by default', () => {
      expect(contextMenu.config.enableCurrentObject).to.be.true;
    });

    it('should construct a configuration for the underlying headers', () => {
      expect(contextMenu.headerConfig.preventLinkRedirect).to.be.true;
    });

    it('should register in the eventbus for recent objects events', () => {
      expect(contextMenu.eventbus.subscribe.calledWith(RecentObjectAddedEvent)).to.be.true;
      expect(contextMenu.eventbus.subscribe.calledWith(RecentObjectUpdatedEvent)).to.be.true;
      expect(contextMenu.eventbus.subscribe.calledWith(RecentObjectRemovedEvent)).to.be.true;
    });

    it('should load the recently used objects with their identifiers from the recent objects service', () => {
      let instances = [{id: 1}, {id: 2}];
      let objects = [1, 2];
      contextMenu = new SearchContextMenu(stubEventbus(), stubRecentObjectsService(objects), stubInstanceRestService(instances), PromiseStub, stubContextualFactory());
      contextMenu.ngOnInit();
      expect(contextMenu.instanceRestService.loadBatch.calledWith(objects)).to.be.true;
      expect(contextMenu.recentObjects).to.deep.equal(instances);
      expect(contextMenu.initialized).to.be.true;

      let requestConfig = contextMenu.instanceRestService.loadBatch.getCall(0).args[1];
      expect(requestConfig.params.properties).to.contains(HEADER_BREADCRUMB);
    });

    it('should not load recently used objects if there are none', () => {
      expect(contextMenu.instanceRestService.loadBatch.called).to.be.false;
      expect(contextMenu.recentObjects).to.deep.equal([]);
      expect(contextMenu.initialized).to.be.true;
    });
  });

  describe('selectCurrentObject()', () => {
    it('should invoke onContextChange with the current object', () => {
      contextMenu.onContextChange = sinon.spy();
      contextMenu.selectCurrentObject();
      expect(contextMenu.onContextChange.calledOnce).to.be.true;
      expect(contextMenu.onContextChange.getCall(0).args[0].instance.id).to.equal(CURRENT_OBJECT);
    });
  });

  describe('selectContext(instance)', () => {
    it('should invoke onContextChange with the selected instance', () => {
      contextMenu.onContextChange = sinon.spy();
      let instance = {id: 1};
      contextMenu.selectContext(instance);
      expect(contextMenu.onContextChange.calledOnce).to.be.true;
      expect(contextMenu.onContextChange.getCall(0).args[0].instance.id).to.equal(1);
    });
  });

  describe('updateList(instance)', () => {
    it('should update the list directly with the provided instance when breadcrumb header is present', () => {
      let toInsert = {id: 2, headers: {}};
      toInsert.headers[HEADER_BREADCRUMB] = 'header';
      contextMenu.recentObjects = [{id: 1}];

      contextMenu.updateList(toInsert);
      expect(contextMenu.instanceRestService.load.calledOnce).to.be.false;
      expect(contextMenu.recentObjectsService.insertRecentObject.calledWith(contextMenu.recentObjects, toInsert)).to.be.true;
    });

    it('should update the list by fetching the instance when breadcrumb header is not present', () => {
      contextMenu.instanceRestService.load.returns(PromiseStub.resolve({data: {}}));
      let toInsert = {id: 2, headers: {}};
      contextMenu.recentObjects = [{id: 1}];

      contextMenu.updateList(toInsert);
      expect(contextMenu.instanceRestService.load.calledOnce).to.be.true;
      expect(contextMenu.recentObjectsService.insertRecentObject.calledWith(contextMenu.recentObjects, {})).to.be.true;
    });

    it('should not do anything if the component is not yet initialized', () => {
      contextMenu.recentObjects = undefined;
      contextMenu.updateList({id: 2});
      expect(contextMenu.recentObjectsService.insertRecentObject.called).to.be.false;
    });
  });

  describe('removeFromList(instance)', () => {
    it('should update the list to be without the removed instance', () => {
      contextMenu.recentObjects = [{id: 1}];
      contextMenu.removeFromList({id: 2});
      expect(contextMenu.recentObjectsService.removeRecentObjectFromList.calledWith(contextMenu.recentObjects, {id: 2})).to.be.true;
    });

    it('should not do anything if the component is not yet initialized', () => {
      contextMenu.recentObjects = undefined;
      contextMenu.removeFromList({id: 2});
      expect(contextMenu.recentObjectsService.insertRecentObject.called).to.be.false;
    });
  });

  describe('ngOnDestroy()', () => {
    it('should unsubscribe from all registered events', () => {
      contextMenu.ngOnInit();
      contextMenu.ngOnDestroy();
      contextMenu.events.forEach((event) => {
        expect(event.unsubscribe.calledOnce).to.be.true;
      });
    });
  });

  function stubEventbus() {
    let eventbusStub = stub(Eventbus);
    eventbusStub.subscribe = sinon.spy(() => {
      return {unsubscribe: sinon.spy()};
    });
    return eventbusStub;
  }

  function stubRecentObjectsService(objects = []) {
    let recentServiceStub = stub(RecentObjectsService);
    recentServiceStub.getRecentObjects.returns(objects);
    return recentServiceStub;
  }

  function stubInstanceRestService(objects = []) {
    let instanceServiceStub = stub(InstanceRestService);
    instanceServiceStub.loadBatch.returns(PromiseStub.resolve({
      data: objects
    }));
    return instanceServiceStub;
  }

});