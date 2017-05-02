import {RecentObjectsListener} from 'recent-objects/recent-objects-listener';
import {AfterIdocLoadedEvent} from 'idoc/events/after-idoc-loaded-event';
import {InstanceCreatedEvent} from 'idoc/events/instance-created-event';
import {ActionExecutedEvent} from 'services/actions/events';
import {RecentObjectAddedEvent, RecentObjectRemovedEvent} from 'recent-objects/events';
import {UploadCompletedEvent} from 'file-upload/events';
import {SavedSearchLoadedEvent, SavedSearchCreatedEvent, SavedSearchUpdatedEvent} from 'search/components/saved/events';
import _ from 'lodash';

function mockEventbus(subSpy, pubSpy) {
  return {
    subscribe: subSpy,
    publish: pubSpy
  }
}

var configuration = {
  get: () => 2
};

var userService = {
  getCurrentUser: () => {
    return {
      then: (cb) => cb({id: 'user-id'})
    };
  }
};

function mockStoreService(setSpy) {
  return {
    set: setSpy
  }
}

function buildObjectsArray(start, end) {
  return _.range(start, end).map((id) => {
    return {id};
  });
}

describe('RecentObjectsListener', () => {
  var listener;
  var eventbus;

  beforeEach(() => {
    eventbus = mockEventbus(sinon.spy(), sinon.spy());
    listener = new RecentObjectsListener(configuration, eventbus, {
      getJson: () => {
        return {};
      }
    }, userService);
  });

  it('should reduce the size of recent objects for user if config is less than objects length', () => {
    let config = {
      get: () => 5
    };
    let userServiceMock = {
      getCurrentUser: () => {
        return {
          then: (cb) => cb({id: 'userId1'})
        };
      }
    };
    let setSpy = sinon.spy();
    let storage = {
      getJson: () => {
        return {
          'userId1': [{id: 1}, {id: 2}, {id: 3}, {id: 4}, {id: 5}, {id: 6}, {id: 7}, {id: 8}, {id: 9}, {id: 10}],
          'userId2': [{id: 11}, {id: 12}, {id: 13}, {id: 14}, {id: 15}, {id: 16}, {id: 17}, {id: 18}, {id: 19}, {id: 20}]
        }
      },
      set: setSpy
    };

    listener = new RecentObjectsListener(config, eventbus, storage, userServiceMock);

    expect(setSpy.calledOnce).to.be.true;
    expect(setSpy.getCall(0).args[1]['userId1']).to.deep.equal([{id: 1}, {id: 2}, {id: 3}, {id: 4}, {id: 5}]);
    expect(setSpy.getCall(0).args[1]['userId2'].length).to.equal(10);
  });

  it('should not reduce the size of recent objects for user if config is greater than objects length', () => {
    let config = {
      get: () => 25
    };
    let userServiceMock = {
      getCurrentUser: () => {
        return {
          then: (cb) => cb({id: 'userId1'})
        };
      }
    };
    let setSpy = sinon.spy();
    let storage = {
      getJson: () => {
        return {
          'userId1': [{id: 1}, {id: 2}, {id: 3}, {id: 4}, {id: 5}, {id: 6}, {id: 7}, {id: 8}, {id: 9}, {id: 10}],
          'userId2': [{id: 11}, {id: 12}, {id: 13}, {id: 14}, {id: 15}, {id: 16}, {id: 17}, {id: 18}, {id: 19}, {id: 20}]
        }
      },
      set: setSpy
    };

    listener = new RecentObjectsListener(config, eventbus, storage, userServiceMock);

    expect(setSpy.called).to.be.false;
  });

  it('should reduce the size of recent objects for user when objects size is greater than config', () => {
    let config = {
      get: () => 2
    };
    let userServiceMock = {
      getCurrentUser: () => {
        return {
          then: (cb) => cb({id: 'userId1'})
        };
      }
    };
    let setSpy = sinon.spy();
    let storage = {
      getJson: () => {
        return {
          'userId1': buildObjectsArray(1, 11)
        }
      },
      set: setSpy
    };

    listener = new RecentObjectsListener(config, eventbus, storage, userServiceMock);

    expect(setSpy.calledOnce).to.be.true;
    expect(setSpy.getCall(0).args[1]['userId1']).to.deep.equal([{id: 1}, {id: 2}]);
  });

  describe('events', () => {

    it('should subscribe to events', () => {
      var spy = eventbus.subscribe;

      expect(spy.callCount).to.eq(7);
      expect(spy.getCall(0).args[0]).to.eq(AfterIdocLoadedEvent);
      expect(spy.getCall(1).args[0]).to.eq(InstanceCreatedEvent);
      expect(spy.getCall(2).args[0]).to.eq(ActionExecutedEvent);
      expect(spy.getCall(3).args[0]).to.eq(UploadCompletedEvent);
      expect(spy.getCall(4).args[0]).to.eq(SavedSearchLoadedEvent);
      expect(spy.getCall(5).args[0]).to.eq(SavedSearchCreatedEvent);
      expect(spy.getCall(6).args[0]).to.eq(SavedSearchUpdatedEvent);
    });
  });

  describe('handleIdocEvent(payload)', () => {
    it('should call addRecentObject with current object id', () => {
      listener.addRecentObject = sinon.spy();
      let testObject = {id: 1};
      listener.handleIdocEvent([{currentObject: testObject}]);

      expect(listener.addRecentObject.calledOnce).to.be.true;
      expect(listener.addRecentObject.getCall(0).args[0]).to.eq(testObject);
    });
  });

  describe('handleActionEvent(payload)', () => {

    it('should call addRecentObject with current object id', () => {
      listener.addRecentObject = sinon.spy();
      let testObject = {id: 1};
      listener.handleActionEvent({context: {currentObject: testObject}});

      expect(listener.addRecentObject.calledOnce).to.be.true;
      expect(listener.addRecentObject.getCall(0).args[0]).to.eq(testObject);
    });

    it('should call with id from context if response data is falsy', () => {
      listener.addRecentObject = sinon.spy();
      let testObject = {id: 1};
      listener.handleActionEvent({context: {currentObject: testObject}, response: {}});

      expect(listener.addRecentObject.calledOnce).to.be.true;
      expect(listener.addRecentObject.getCall(0).args[0]).to.eq(testObject);
    });

    it('should call with id from context if response data is not array', () => {
      listener.addRecentObject = sinon.spy();
      let testObject = {id: 1};
      listener.handleActionEvent({context: {currentObject: testObject}, response: {data: {}}});

      expect(listener.addRecentObject.calledOnce).to.be.true;
      expect(listener.addRecentObject.getCall(0).args[0]).to.eq(testObject);
    });

    it('should call addRecentObject with response.data', () => {
      listener.addRecentObject = sinon.spy();
      let testObject1 = {id: 1};
      let testObject2 = {id: 2};
      var data = [testObject1, testObject2];
      listener.handleActionEvent({
        response: {data}
      });

      expect(listener.addRecentObject.callCount).to.eq(2);
      expect(listener.addRecentObject.getCall(0).args[0]).to.eq(testObject1);
      expect(listener.addRecentObject.getCall(1).args[0]).to.eq(testObject2);
    });
  });

  describe('handleUploadEvent(payload)', () => {
    it('should call addRecentObject with object id', () => {
      var id = new Date().getTime();
      let testObject = {id};
      listener.addRecentObject = sinon.spy();
      listener.handleUploadEvent(testObject);

      expect(listener.addRecentObject.calledOnce).to.be.true;
      expect(listener.addRecentObject.getCall(0).args[0]).to.eq(testObject);
    });
  });

  describe('handleSavedSearchEvent(payload)', () => {
    it('should call addRecentObject with object id', () => {
      var id = new Date().getTime();
      let testObject = {id};
      listener.addRecentObject = sinon.spy();
      listener.handleSavedSearchEvent(testObject);

      expect(listener.addRecentObject.calledOnce).to.be.true;
      expect(listener.addRecentObject.getCall(0).args[0]).to.eq(testObject);
    });
  });

  describe('addRecentObject(instance)', () => {
    var setSpy;
    beforeEach(() => {
      setSpy = sinon.spy();
      listener.localStorageService.set = setSpy;
    });

    it('should not set objects if the instance is a version', () => {
      let instance = {
        id: 'instance-id',
        isVersion: () => {
          return true;
        }
      };
      listener.addRecentObject(instance);
      expect(setSpy.called).to.be.false;
    });

    it('should not set objects if id is falsy', () => {
      listener.addRecentObject({id: ''});
      expect(setSpy.called).to.be.false;
    });

    it('should not set objects if id equals current user id', () => {
      listener.addRecentObject({id: 'user-id'});
      expect(setSpy.called).to.be.false;
    });

    it('should not set objects if id is already contained in recent objects', () => {
      listener.localStorageService.getJson = () => {
        return {'user-id': [1]};
      };

      listener.addRecentObject({id: 1});
      expect(setSpy.called).to.be.false;
    });

    it('should add to the beginning of the list', () => {
      listener.localStorageService.getJson = () => {
        return {'user-id': [1]};
      };

      listener.addRecentObject({id: 2});
      expect(setSpy.calledOnce).to.be.true;
      expect(setSpy.getCall(0).args[1]).to.deep.eq({'user-id': [2, 1]});
    });

    it('should remove last element if size exceeds the configured', () => {
      listener.localStorageService.getJson = () => {
        return {'user-id': [2, 1]};
      };

      listener.addRecentObject({id: 3});
      expect(setSpy.calledOnce).to.be.true;
      expect(setSpy.getCall(0).args[1]).to.deep.eq({'user-id': [3, 2]});
    });

    it('should add recent objects on per user basis', () => {
      listener.localStorageService.getJson = () => {
        return {'user-id': [1]};
      };

      listener.addRecentObject({id: 2});
      listener.currentUserId = 'another-user-id';
      listener.addRecentObject({id: 1});

      expect(setSpy.calledTwice).to.be.true;
      expect(setSpy.getCall(0).args[1]).to.deep.eq({'user-id': [2, 1]});
      expect(setSpy.getCall(1).args[1]).to.deep.eq({
        'user-id': [1],
        'another-user-id': [1]
      });
    });

    it('should fire RecentObjectAddedEvent', () => {
      listener.localStorageService.getJson = () => {
        return {'user-id': []}
      };
      listener.addRecentObject({id: 1});

      expect(listener.eventbus.publish.calledOnce).to.be.true;

      var payload = listener.eventbus.publish.getCall(0).args[0];
      expect(payload instanceof RecentObjectAddedEvent).to.be.true;
      expect(payload.getData()).to.eq(1);
    });

    it('should remove all objects over the configured size', () => {
      listener.configuration.get = () => 5;
      listener.localStorageService.getJson = () => {
        return {'user-id': [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]};
      };
      listener.addRecentObject({id: 11});
      expect(setSpy.getCall(0).args[1]['user-id']).to.deep.eq([11, 1, 2, 3, 4]);
    });
  });
});
