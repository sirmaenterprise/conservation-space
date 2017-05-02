import {RecentObjectsList} from 'recent-objects/recent-objects-list';
import {RecentObjectAddedEvent} from 'recent-objects/events';
import {AfterIdocDeleteEvent} from 'idoc/actions/events/after-idoc-delete-event';

describe('RecentObjectsList', function() {

  var list;

  beforeEach(function() {
    list = new RecentObjectsList(storage, userService, mockEventbus(), instanceService);
  });

  it('should have default component configuration', () => {
    expect(list.config).to.exist;
    expect(list.config.selectableItems).to.be.false;
    expect(list.config.singleSelection).to.be.false;
    expect(list.config.emptyListMessage).to.exist;
  });

  it('should unsubscribe from event when component is destroyed', () => {
    let unsubscribeSpy = {
      unsubscribe: sinon.spy()
    };
    list.eventbus = {
      subscribe: () => {
        return unsubscribeSpy
      }
    };
    list.ngOnInit();
    list.ngOnDestroy();
    expect(unsubscribeSpy.unsubscribe.calledTwice).to.be.true;
  });

  it('should get recent objects identifiers and set them to identifiers property', function() {
    expect(list.identifiers).to.deep.eq([1, 2, 3]);
  });

  describe('ngOnInit()', function() {

    it('should subscribe to RecentObjectAddedEvent and RecentObjectRemovedEvent', function() {
      list.ngOnInit();
      expect(list.eventbus.subscribe.calledTwice).to.be.true;
      expect(list.eventbus.subscribe.getCall(0).args[0]).to.eq(RecentObjectAddedEvent);
      expect(list.eventbus.subscribe.getCall(1).args[0]).to.eq(AfterIdocDeleteEvent);
    });
  });

  describe('updateList(id)', function() {

    it('should load the instance and add it to the list of instances', function() {
      list.updateList(1);
      expect(list.instances).to.deep.eq([{id: 1}]);
    });
  });

  describe('removeFromList(id)', function() {

    it('should remove instance from the list if it is available in the list', function() {
      list.instances = [{id: 1}, {id: 2}, {id: 3}, {id: 4}];
      list.removeFromList({id: 2});
      expect(list.instances.length).to.eq(3);
    });
  });
});

var userService = {
  getCurrentUser: function() {
    return {
      then: function(cb) {
        cb({id: 'user-id'});
      }
    };
  }
};

var storage = {
  getJson: function() {
    return {'user-id': [1, 2, 3]};
  }
};

var instanceService = {
  load: function(id) {
    return {
      then: function(cb) {
        cb({data: {id: id}});
      }
    }
  }
};

function mockEventbus() {
  return {
    subscribe: sinon.spy()
  }
}