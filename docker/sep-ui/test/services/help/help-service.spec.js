import {HelpService, HELP_INSTANCE_TYPE} from 'services/help/help-service';
import {InstanceCreatedEvent} from 'idoc/events/instance-created-event';
import {ActionExecutedEvent} from 'services/actions/events';

import {PromiseStub} from 'test/promise-stub';

describe('HelpService', () => {

  var helpService;
  beforeEach(() => {
    var restClient = mockRestClient();
    var eventbus = mockEventbus();
    helpService = new HelpService(restClient, eventbus);
  });

  it('should initialize an empty mapping by default', () => {
    expect(helpService.helpTargetMapping).to.exist;
    expect(helpService.helpTargetMapping).to.deep.equal({});
  });

  it('should load the help mapping from the correct server service', () => {
    helpService.initialize();
    expect(helpService.restClient.get.called).to.be.true;
    expect(helpService.restClient.get.getCall(0).args[0]).to.equal('/help');
  });

  it('should return instance id by its target from the mapping', () => {
    helpService.initialize();
    var instanceId = helpService.getHelpInstanceId('helpTarget');
    expect(instanceId).to.equal('helpInstanceId');
  });

  it('should not return instance id by for missing target in the mapping', () => {
    helpService.initialize();
    var instanceId = helpService.getHelpInstanceId('missing');
    expect(instanceId).to.not.exist;
  });

  it('should register for instance create event', () => {
    expect(helpService.eventbus.subscribe.called).to.be.true;
    expect(helpService.eventbus.subscribe.getCall(0).args[0]).to.equal(InstanceCreatedEvent);
  });

  it('should register for action executed event', () => {
    expect(helpService.eventbus.subscribe.called).to.be.true;
    expect(helpService.eventbus.subscribe.getCall(1).args[0]).to.equal(ActionExecutedEvent);
  });

  it('should refresh the mapping if a help instance is created', () => {
    var handler = helpService.eventbus.subscribe.getCall(0).args[1];
    helpService.restClient.get.reset();
    handler([{
      currentObject: {
        instanceType: HELP_INSTANCE_TYPE
      }
    }]);
    expect(helpService.restClient.get.calledOnce).to.be.true;
  });

  it('should not refresh the mapping if instance different than help is created', () => {
    var handler = helpService.eventbus.subscribe.getCall(0).args[1];
    helpService.restClient.get.reset();
    handler([{
      currentObject: {
        instanceType: 'other-type'
      }
    }]);
    expect(helpService.restClient.get.called).to.be.false;
  });

  it('should refresh the mapping if an action is executed upon a help instance', () => {
    var handler = helpService.eventbus.subscribe.getCall(1).args[1];
    helpService.restClient.get.reset();
    handler({
      context: {
        currentObject: {
          instanceType: HELP_INSTANCE_TYPE
        }
      }
    });
    expect(helpService.restClient.get.calledOnce).to.be.true;
  });

  it('should not refresh the mapping if an action is executed upon instance different than help', () => {
    var handler = helpService.eventbus.subscribe.getCall(1).args[1];
    helpService.restClient.get.reset();
    handler({
      context: {
        currentObject: {
          instanceType: 'other-type'
        }
      }
    });
    expect(helpService.restClient.get.called).to.be.false;
  });

  function mockRestClient() {
    return {
      get: sinon.spy(() => {
        return PromiseStub.resolve({
          data: {
            'helpTarget': 'helpInstanceId'
          }
        });
      })
    };
  }

  function mockEventbus() {
    return {
      subscribe: sinon.spy()
    };
  }

});