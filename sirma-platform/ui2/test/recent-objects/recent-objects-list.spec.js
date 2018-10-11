import {RecentObjectsList} from 'recent-objects/recent-objects-list';
import {PromiseStub} from 'test/promise-stub';
import {Eventbus} from 'services/eventbus/eventbus';
import {RecentObjectsService} from 'services/recent/recent-objects-service';
import {InstanceRestService} from 'services/rest/instance-service';
import {RecentObjectAddedEvent, RecentObjectUpdatedEvent, RecentObjectRemovedEvent} from 'recent-objects/events';

import {stub} from 'test/test-utils';

describe('RecentObjectsList', () => {

  const INSTANCE_1_ID = '1';
  const INSTANCE_2_ID = '2';

  let list;

  beforeEach(() => {
    list = new RecentObjectsList(stubEventBus(), stubRecentObjectsService([INSTANCE_1_ID, INSTANCE_2_ID]), stubInstanceRestService());
    list.config.filterByWritePermissions = true;
    list.ngOnInit();
  });

  it('should have default component configuration', () => {
    expect(list.config.selectableItems).to.be.false;
    expect(list.config.singleSelection).to.be.false;
    expect(list.config.emptyListMessage).to.exist;
  });

  it('should unsubscribe from event when component is destroyed', () => {
    list.ngOnDestroy();
    list.events.forEach((event) => {
      expect(event.unsubscribe.calledOnce).to.be.true;
    });
  });

  describe('ngOnInit()', () => {
    it('should subscribe to recent objects events', () => {
      expect(list.events).to.exist;
      expect(list.eventbus.subscribe.calledWith(RecentObjectAddedEvent)).to.be.true;
      expect(list.eventbus.subscribe.calledWith(RecentObjectUpdatedEvent)).to.be.true;
      expect(list.eventbus.subscribe.calledWith(RecentObjectRemovedEvent)).to.be.true;
    });
  });

  describe('updateList(instance)', () => {
    it('should call the identifiers filter if present and not insert to list whe instance is not correctly filtered', () => {
      list.insertToList = sinon.spy();
      list.config.identifiersFilter = sinon.spy(() => {
        return PromiseStub.resolve([{id: 1}, {id: 2}]);
      });

      list.updateList({id: 1});
      expect(list.insertToList.calledOnce).to.be.false;
      expect(list.config.identifiersFilter.calledOnce).to.be.true;
    });

    it('should call the identifiers filter if present and insert to list when instance is correctly filtered', () => {
      list.insertToList = sinon.spy();
      list.config.identifiersFilter = sinon.spy(() => {
        return PromiseStub.resolve([{id: 1}]);
      });

      list.updateList({id: 1});
      expect(list.insertToList.calledOnce).to.be.true;
      expect(list.insertToList.calledWith({id: 1})).to.be.true;
      expect(list.config.identifiersFilter.calledOnce).to.be.true;
    });

    it('should not call the identifiers filter if not present and insider to list the provided instance', () => {
      list.insertToList = sinon.spy();

      list.updateList({id: 1});
      expect(list.insertToList.calledOnce).to.be.true;
      expect(list.insertToList.calledWith({id: 1})).to.be.true;
    });
  });

  describe('insertToList(instance)', () => {
    it('should load the instance and add it to the list of instances', () => {
      list.insertToList({id: 1, headers: {},writeAllowed: true});
      expect(list.instanceRestService.load.calledOnce).to.be.true;
      expect(list.recentObjectsService.insertRecentObject.calledOnce).to.be.true;
      expect(list.recentObjectsService.insertRecentObject.calledWith([{'id':1,'headers':{'HEADER_DEFAULT':'test'},writeAllowed: true}], {
        id: 1,
        headers: {HEADER_DEFAULT: 'test'},writeAllowed: true
      })).to.be.true;
    });

    it('should not load the instance and add it to the list of instances', () => {
      list.insertToList({id: 1, headers: {HEADER_DEFAULT: 'test'},writeAllowed: true});
      expect(list.instanceRestService.load.calledOnce).to.be.false;
      expect(list.recentObjectsService.insertRecentObject.calledOnce).to.be.true;
      expect(list.recentObjectsService.insertRecentObject.calledWith([{'id':1,'headers':{'HEADER_DEFAULT':'test'},writeAllowed: true}], {
        id: 1,
        headers: {HEADER_DEFAULT: 'test'},writeAllowed: true
      })).to.be.true;
    });
  });

  describe('removeFromList(instance)', () => {
    it('should remove instance from the list if it is available in the list', () => {
      list.removeFromList({id: 1,writeAllowed: true});
      expect(list.recentObjectsService.removeRecentObjectFromList.calledOnce).to.be.true;
      expect(list.recentObjectsService.removeRecentObjectFromList.calledWith([{'id':1,'headers':{'HEADER_DEFAULT':'test'},writeAllowed: true}], {id: 1,writeAllowed: true})).to.be.true;
    });
  });

  describe('filterRecentObjects()', () => {
    it('should filter recent objects and assign directly to instances', () => {
      list.config.identifiersFilter = sinon.spy(() => {
        return PromiseStub.resolve([{id: 1,writeAllowed: true}, {id: 2,writeAllowed: false}]);
      });

      list.filterRecentObjects();
      expect(list.instances).to.deep.eq([{id: 1,writeAllowed: true}]);
      expect(list.config.identifiersFilter.calledOnce).to.be.true;
      expect(list.config.identifiersFilter.calledWith([INSTANCE_1_ID, INSTANCE_2_ID])).to.be.true;
    });

    it('should not filter recent objects and load instance', () => {
      list.filterRecentObjects();
      expect(list.instances).to.deep.eq([{
        id: 1,
        headers: {
          HEADER_DEFAULT: 'test'
        },
        writeAllowed: true
      }]);
    });

    it('should not load recent objects instances if there are no recent objects', () => {
      list = new RecentObjectsList(stubEventBus(), stubRecentObjectsService([]), stubInstanceRestService());
      list.ngOnInit();
      expect(list.instanceRestService.loadBatch.called).to.be.false;
      expect(list.instances).to.deep.equal([]);
    });
  });

  function stubEventBus() {
    let eventbusStub = stub(Eventbus);
    eventbusStub.subscribe = sinon.spy(() => {
      return {unsubscribe: sinon.spy()};
    });
    return eventbusStub;
  }

  function stubRecentObjectsService(identifiers = []) {
    let serviceStub = stub(RecentObjectsService);
    serviceStub.getRecentObjects.returns(identifiers);
    return serviceStub;
  }

  function stubInstanceRestService() {
    let instanceRestService = stub(InstanceRestService);
    instanceRestService.loadBatch.returns(PromiseStub.resolve({
      data: [
        {
          id: 1,
          headers: {
            HEADER_DEFAULT: 'test'
          },
          writeAllowed: true
        },{
          id: 2,
          headers: {
            HEADER_DEFAULT: 'HEADER_DEFAULT 2'
          },
          writeAllowed: false
        }
      ]
    }));
    instanceRestService.load.returns(PromiseStub.resolve({
      data: {
        id: 1,
        headers: {
          HEADER_DEFAULT: 'test'
        }
      }
    }));
    return instanceRestService;
  }
});
