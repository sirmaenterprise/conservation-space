import {RecentObjectsListener} from 'recent-objects/recent-objects-listener';
import {Eventbus} from 'services/eventbus/eventbus';
import {AfterIdocLoadedEvent} from 'idoc/events/after-idoc-loaded-event';
import {InstanceCreatedEvent} from 'idoc/events/instance-created-event';
import {ActionExecutedEvent} from 'services/actions/events';
import {UploadCompletedEvent} from 'file-upload/events';
import {AfterIdocDeleteEvent} from 'idoc/actions/events/after-idoc-delete-event';
import {SavedSearchLoadedEvent, SavedSearchCreatedEvent, SavedSearchUpdatedEvent} from 'search/components/saved/events';
import {RecentObjectsService} from 'services/recent/recent-objects-service';

import {stub} from 'test/test-utils';

describe('RecentObjectsListener', () => {

  let listener;
  let eventbus;
  beforeEach(() => {
    eventbus = stub(Eventbus);
    let recentObjectsService = stub(RecentObjectsService);
    listener = new RecentObjectsListener(eventbus, recentObjectsService);
  });

  it('should subscribe to events in the eventbus', () => {
    expect(eventbus.subscribe.calledWith(AfterIdocLoadedEvent)).to.be.true;
    expect(eventbus.subscribe.calledWith(InstanceCreatedEvent)).to.be.true;
    expect(eventbus.subscribe.calledWith(ActionExecutedEvent)).to.be.true;
    expect(eventbus.subscribe.calledWith(UploadCompletedEvent)).to.be.true;
    expect(eventbus.subscribe.calledWith(SavedSearchLoadedEvent)).to.be.true;
    expect(eventbus.subscribe.calledWith(SavedSearchCreatedEvent)).to.be.true;
    expect(eventbus.subscribe.calledWith(SavedSearchUpdatedEvent)).to.be.true;
    expect(eventbus.subscribe.calledWith(AfterIdocDeleteEvent)).to.be.true;
  });

  describe('handleIdocEvent(payload)', () => {
    it('should call addRecentObject with current object id', () => {
      let testObject = {id: 1};
      listener.handleIdocEvent([{currentObject: testObject}]);

      expect(listener.recentObjectsService.addRecentObject.calledOnce).to.be.true;
      expect(listener.recentObjectsService.addRecentObject.getCall(0).args[0]).to.deep.eq(testObject);
    });
  });

  describe('handleActionEvent(payload)', () => {
    it('should call addRecentObject with current object id', () => {
      let testObject = {id: 1};
      listener.handleActionEvent({context: {currentObject: testObject}});

      expect(listener.recentObjectsService.addRecentObject.calledOnce).to.be.true;
      expect(listener.recentObjectsService.addRecentObject.getCall(0).args[0]).to.deep.eq(testObject);
    });

    it('should call with id from context if response data is falsy', () => {
      let testObject = {id: 1};
      listener.handleActionEvent({context: {currentObject: testObject}, response: {}});

      expect(listener.recentObjectsService.addRecentObject.calledOnce).to.be.true;
      expect(listener.recentObjectsService.addRecentObject.getCall(0).args[0]).to.deep.eq(testObject);
    });

    it('should call with id from context if response data is not array', () => {
      let testObject = {id: 1};
      listener.handleActionEvent({context: {currentObject: testObject}, response: {data: {}}});

      expect(listener.recentObjectsService.addRecentObject.calledOnce).to.be.true;
      expect(listener.recentObjectsService.addRecentObject.getCall(0).args[0]).to.deep.eq(testObject);
    });

    it('should call addRecentObject with response.data', () => {
      let testObject1 = {id: 1};
      let testObject2 = {id: 2};
      listener.handleActionEvent({
        response: {data: [testObject1, testObject2]}
      });

      expect(listener.recentObjectsService.addRecentObject.callCount).to.eq(2);
      expect(listener.recentObjectsService.addRecentObject.getCall(0).args[0]).to.deep.eq(testObject1);
      expect(listener.recentObjectsService.addRecentObject.getCall(1).args[0]).to.deep.eq(testObject2);
    });

    it('should not register recent objects if the action lacks valid instances', () => {
      listener.handleActionEvent({
        response: {data: [{}, undefined]}
      });
      expect(listener.recentObjectsService.addRecentObject.called).to.be.false;
    });
  });

  describe('removeFromList()', () => {
    it('should remove the deleted object', () => {
      let testObject = {id: 1};
      listener.removeFromList([testObject]);

      expect(listener.recentObjectsService.removeRecentObject.calledOnce).to.be.true;
      expect(listener.recentObjectsService.removeRecentObject.getCall(0).args[0]).to.eq(testObject);
    });
  });

});
