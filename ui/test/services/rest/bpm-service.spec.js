import {BpmService} from 'services/rest/bpm-service'
import {InstanceObject} from 'idoc/idoc-context';

describe('BpmService ', () => {

  let service;
  let restClient;
  let objects;

  beforeEach(() => {
    restClient = mockRestClient();
    let obj = new InstanceObject({}, {});
    obj.id = 'ID';
    obj.getChangeset = () => {
      return {
        prop1: 'prop1'
      };
    };
    obj.getModels = () => {
      return {
        definitionId: 'id'
      };
    }
    objects = [];
    objects.push(obj);
    service = new BpmService(restClient);
  });

  it('Test service  payload', () => {
    let actionDefinition = {
      action: 'bpmTransition'
    };
    let expectedObject = {
      instanceId: 'ID',
      definitionId: 'id',
      properties: {
        prop1: 'prop1'
      }
    };
    let result = service.buildBPMActionPayload('emf:id', actionDefinition, objects, 'BPMTranstion');
    expect(result).to.deep.equal({
      operation: 'BPMTranstion',
      userOperation: 'bpmTransition',
      targetInstances: [expectedObject],
      currentInstance: 'emf:id'
    });
  });

  it('loadModel', () => {
        service.loadModel('emf:id', 'BPMTranstion');
        expect(restClient.get.calledOnce).to.be.true;
        expect(restClient.get.getCall(0).args[0]).to.equal('/instances/emf:id/model/bpm');
  });

  it('executeTransition', () => {
        service.executeTransition('emf:id', {});
        expect(restClient.post.calledOnce).to.be.true;
        expect(restClient.post.getCall(0).args[0]).to.equal('/instances/emf:id/actions/bpmTransition');
  });

  it('startBpm', () => {
        service.startBpm('emf:id', {});
        expect(restClient.post.calledOnce).to.be.true;
        expect(restClient.post.getCall(0).args[0]).to.equal('/instances/emf:id/actions/bpmStart');
  });

  it('stopBpm', () => {
    service.stopBpm('emf:id', {});
    expect(restClient.post.calledOnce).to.be.true;
    expect(restClient.post.getCall(0).args[0]).to.equal('/instances/emf:id/actions/bpmStop');
  });

  it('claimBpm', () => {
    service.claimBpm('emf:id', {});
    expect(restClient.post.calledOnce).to.be.true;
    expect(restClient.post.getCall(0).args[0]).to.equal('/instances/emf:id/actions/bpmClaim');
  });

  it('releaseBpm', () => {
    service.releaseBpm('emf:id', {});
    expect(restClient.post.calledOnce).to.be.true;
    expect(restClient.post.getCall(0).args[0]).to.equal('/instances/emf:id/actions/bpmRelease');
  });

  it('getInfo', () => {
        service.getInfo('emf:id', {});
        expect(restClient.get.calledOnce).to.be.true;
        expect(restClient.get.getCall(0).args[0]).to.equal('/instances/emf:id/bpm/activity');
  });

  it('getEngine', () => {
      service.getEngine('/camunda/api/engine', {});
      expect(restClient.get.calledOnce).to.be.true;
      expect(restClient.get.getCall(0).args[0]).to.equal('/camunda/api/engine');
  });

  it('executeCustomProcessRequestGet', () => {
      service.executeCustomProcessRequestGet('/custom/url', {});
      expect(restClient.get.calledOnce).to.be.true;
      expect(restClient.get.getCall(0).args[0]).to.equal('/custom/url');
  });

  function mockRestClient() {
    return {
      get: sinon.spy((uri, config) => {
        return '';
      }),
      post: sinon.spy((uri, payload, config) => {
        return '';
      })
    };
  }
});
