import {RecentObjectsService} from 'services/recent/recent-objects-service';
import {Configuration} from 'common/application-config';
import {Eventbus} from 'services/eventbus/eventbus';
import {UserService} from 'services/identity/user-service';
import {LocalStorageService} from 'services/storage/local-storage-service';
import {RecentObjectAddedEvent, RecentObjectUpdatedEvent, RecentObjectRemovedEvent} from 'recent-objects/events';

import {stub} from 'test/test-utils';
import {PromiseStub} from 'test/promise-stub';

describe('RecentObjectsService', () => {

  let recentObjectsService;
  beforeEach(() => {
    let configuration = stubConfiguration();
    let userService = stubUserService();
    let localStorageService = stubLocalStorageService();
    recentObjectsService = new RecentObjectsService(stub(Eventbus), configuration, userService, localStorageService);
  });

  describe('initialization', ()=> {
    it('should reduce the size of recent objects for user if config is less than objects length', () => {
      let objectsPerUser = getObjectsPerUserJson();
      let localStorageService = stubLocalStorageService(objectsPerUser);
      let configuration = stubConfiguration(5);
      recentObjectsService = new RecentObjectsService(stub(Eventbus), configuration, stubUserService(), localStorageService);
      expect(localStorageService.set.calledOnce).to.be.true;
      expect(localStorageService.set.getCall(0).args[1]['user-id']).to.deep.equal([{id: 1}, {id: 2}, {id: 3}, {id: 4}, {id: 5}]);
      expect(localStorageService.set.getCall(0).args[1]['user-id2'].length).to.equal(10);
    });

    it('should not reduce the size of recent objects for user if config is greater than objects length', () => {
      let objectsPerUser = getObjectsPerUserJson();
      let localStorageService = stubLocalStorageService(objectsPerUser);
      recentObjectsService = new RecentObjectsService(stub(Eventbus), stubConfiguration(), stubUserService(), localStorageService);
      expect(localStorageService.set.called).to.be.false;
    });

    function getObjectsPerUserJson() {
      return {
        'user-id': [{id: 1}, {id: 2}, {id: 3}, {id: 4}, {id: 5}, {id: 6}, {id: 7}, {id: 8}, {id: 9}, {id: 10}],
        'user-id2': [{id: 11}, {id: 12}, {id: 13}, {id: 14}, {id: 15}, {id: 16}, {id: 17}, {id: 18}, {id: 19}, {id: 20}]
      };
    }
  });

  describe('addRecentObject(instance)', () => {

    beforeEach(() => {
      recentObjectsService.localStorageService.set.reset();
    });

    it('should not set objects if the instance is a version', () => {
      let instance = {
        id: 'instance-id',
        isVersion: () => {
          return true;
        }
      };
      recentObjectsService.addRecentObject(instance);
      expect(recentObjectsService.localStorageService.set.called).to.be.false;
    });

    it('should not set objects if id equals current user id', () => {
      recentObjectsService.addRecentObject({id: 'user-id'});
      expect(recentObjectsService.localStorageService.set.called).to.be.false;
    });

    it('should set objects if id is already contained in recent objects', () => {
      recentObjectsService.localStorageService = stubLocalStorageService({'user-id': [1]});
      recentObjectsService.addRecentObject({id: 1});
      expect(recentObjectsService.localStorageService.set.called).to.be.true;
    });

    it('should add to the beginning if id is already contained in recent objects', () => {
      recentObjectsService.localStorageService = stubLocalStorageService({'user-id': [1, 2, 3, 4, 5]});
      recentObjectsService.addRecentObject({id: 5});
      expect(recentObjectsService.localStorageService.set.called).to.be.true;
      expect(recentObjectsService.localStorageService.set.getCall(0).args[1]).to.deep.eq({'user-id': [5, 1, 2, 3, 4]});
    });

    it('should fire RecentObjectUpdatedEvent if the instance has already been in the list', () => {
      recentObjectsService.localStorageService = stubLocalStorageService({'user-id': [1, 2, 3, 4, 5]});
      recentObjectsService.addRecentObject({id: 5});
      expect(recentObjectsService.eventbus.publish.calledOnce).to.be.true;
      let payload = recentObjectsService.eventbus.publish.getCall(0).args[0];
      expect(payload instanceof RecentObjectUpdatedEvent).to.be.true;
      expect(payload.getData()).to.deep.equal({id: 5});
    });

    it('should add to the beginning of the list', () => {
      recentObjectsService.localStorageService = stubLocalStorageService({'user-id': [1]});

      recentObjectsService.addRecentObject({id: 2});
      expect(recentObjectsService.localStorageService.set.calledOnce).to.be.true;
      expect(recentObjectsService.localStorageService.set.getCall(0).args[1]).to.deep.eq({'user-id': [2, 1]});
    });

    it('should remove last element if size exceeds the configured', () => {
      recentObjectsService.configuration = stubConfiguration(2);
      recentObjectsService.localStorageService = stubLocalStorageService({'user-id': [2, 1]});
      recentObjectsService.addRecentObject({id: 3});
      expect(recentObjectsService.localStorageService.set.calledOnce).to.be.true;
      expect(recentObjectsService.localStorageService.set.getCall(0).args[1]).to.deep.eq({'user-id': [3, 2]});
    });

    it('should fire RecentObjectAddedEvent', () => {
      recentObjectsService.localStorageService = stubLocalStorageService({'user-id': []});
      recentObjectsService.addRecentObject({id: 1});

      expect(recentObjectsService.eventbus.publish.calledOnce).to.be.true;

      let payload = recentObjectsService.eventbus.publish.getCall(0).args[0];
      expect(payload instanceof RecentObjectAddedEvent).to.be.true;
      expect(payload.getData()).to.deep.equal({id: 1});
    });

    it('should remove all objects over the configured size', () => {
      recentObjectsService.configuration = stubConfiguration(5);
      recentObjectsService.localStorageService = stubLocalStorageService({'user-id': [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]});
      recentObjectsService.addRecentObject({id: 11});
      expect(recentObjectsService.localStorageService.set.calledOnce).to.be.true;
      expect(recentObjectsService.localStorageService.set.getCall(0).args[1]['user-id']).to.deep.eq([11, 1, 2, 3, 4]);
    });
  });

  describe('removeRecentObject', () => {
    it('should remove the provided instance from the recent objects', () => {
      recentObjectsService.localStorageService = stubLocalStorageService({'user-id': [1, 2, 3, 4, 5]});
      recentObjectsService.removeRecentObject({id: 3});
      expect(recentObjectsService.localStorageService.set.getCall(0).args[1]['user-id']).to.deep.eq([1, 2, 4, 5]);
    });

    it('should fire an event about removing a recent object', () => {
      recentObjectsService.localStorageService = stubLocalStorageService({'user-id': [1, 2, 3, 4, 5]});
      recentObjectsService.removeRecentObject({id: 3});
      let payload = recentObjectsService.eventbus.publish.getCall(0).args[0];
      expect(payload instanceof RecentObjectRemovedEvent).to.be.true;
      expect(payload.getData()).to.deep.equal({id: 3});
    });

    it('should not fire an event about removing a recent object if it is not in the recent objects', () => {
      recentObjectsService.localStorageService = stubLocalStorageService({'user-id': [1, 2, 3, 4, 5]});
      recentObjectsService.removeRecentObject({id: 6});
      expect(recentObjectsService.eventbus.publish.called).to.be.false;
    });
  });

  describe('insertRecentObject(list, instance)', () => {
    it('should update the list with the provided instance to be on top', () => {
      let instances = [{id: 1}, {id: 2}, {id: 3}];
      recentObjectsService.insertRecentObject(instances, {id: 4});
      expect(instances).to.deep.equal([{id: 4}, {id: 1}, {id: 2}, {id: 3}]);
    });

    it('should update the list with the provided instance to be on top if it was previously in the list', () => {
      let instances = [{id: 1}, {id: 2}, {id: 3}];
      recentObjectsService.insertRecentObject(instances, {id: 2});
      expect(instances).to.deep.equal([{id: 2}, {id: 1}, {id: 3}]);
    });
  });

  describe('removeRecentObjectFromList(list, instance)', () => {
    it('should remove the provided instance from the list', () => {
      let instances = [{id: 1}, {id: 2}, {id: 3}];
      recentObjectsService.removeRecentObjectFromList(instances, {id: 2});
      expect(instances).to.deep.equal([{id: 1}, {id: 3}]);
    });
  });

  function stubConfiguration(recentObjectsSize = 25) {
    let configuration = stub(Configuration);
    configuration.get.withArgs(Configuration.USER_RECENT_OBJECTS_SIZE).returns(recentObjectsSize);
    return configuration;
  }

  function stubUserService() {
    let userServiceStub = stub(UserService);
    userServiceStub.getCurrentUser.returns(PromiseStub.resolve({
      id: 'user-id'
    }));
    return userServiceStub;
  }

  function stubLocalStorageService(objectsPerUser = {}) {
    let localStorageStub = stub(LocalStorageService);
    localStorageStub.getJson.withArgs(LocalStorageService.RECENT_OBJECTS).returns(objectsPerUser);
    return localStorageStub;
  }
});
