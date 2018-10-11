import {BpmService} from 'services/rest/bpm-service';
import {InstanceObject} from 'models/instance-object';
import {InstanceModel} from 'models/instance-model';
import {DefinitionModel} from 'models/definition-model';

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
    };
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
      properties: {}
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

  it('generateXMLURL', () => {
    let url = service.generateXmlURL('/camunda/api/engine', 'EOOO2');
    expect(url).to.equal('/camunda/api/engine/process-definition/EOOO2/xml');
  });

  it('generateKeyXMLURL', () => {
    let url = service.generateKeyXmlURL('/camunda/api/engine', 'EOOO2');
    expect(url).to.equal('/camunda/api/engine/process-definition/key/EOOO2/xml');
  });

  it('executeCustomProcessRequestGet', () => {
    service.executeCustomProcessRequestGet('/custom/url', {});
    expect(restClient.get.calledOnce).to.be.true;
    expect(restClient.get.getCall(0).args[0]).to.equal('/custom/url');
  });

  it('generateProcessInstanceURL', () => {
    expect(service.generateProcessInstanceURL('/camunda/api/engine', 'instanceId1')).to.equal('/camunda/api/engine/process-instance/instanceId1');
  });

  it('generateProcessInstanceHistoryURL', () => {
    expect(service.generateProcessInstanceHistoryURL('/camunda/api/engine', 'instanceId1')).to.equal('/camunda/api/engine/history/process-instance/instanceId1');
  });

  it('generateActivityURL', () => {
    expect(service.generateActivityURL('/camunda/api/engine', 'instanceId1')).to.equal('/camunda/api/engine/process-instance/instanceId1/activity-instances');
  });

  it('generateVersionXmlUrl', () => {
    expect(service.generateVersionXmlUrl('/camunda/api/engine', 'instanceId1')).to.equal('/camunda/api/engine/process-definition/instanceId1/xml');
  });

  describe('getInstanceData', () => {
    it('should return empty object when validation model is empty to prevent errors', () => {
      let instance = {models: {}};
      let data = service.getInstanceData(instance);
      expect(data).to.eql({});
    });

    it('should collect all instance properties values from the model', () => {
      let instance = {
        models: {
          validationModel: new InstanceModel({
            // this one should be skipped by the method
            'rdf:type': {value: 'emf:Document'},
            // this one should be skipped as well
            'status': {value: [], defaultValue: []},
            'references': {value: {results: ['1', '2']}, defaultValue: {results: ['1', '2']}},
            'textField': {value: 'text value', defaultValue: 'text value'}
          }),
          viewModel: new DefinitionModel({
            fields: [
              {
                identifier: 'rdf:type'
              },
              {
                identifier: 'status',
                isDataProperty: true
              },
              {
                identifier: 'references',
                isDataProperty: false
              },
              {
                identifier: 'textField',
                isDataProperty: true
              }
            ]
          })
        }
      };
      let data = service.getInstanceData(instance);
      expect(data).to.eql({
        'references': {add: ['1', '2'], remove: []},
        'textField': 'text value'
      });
    });
  });

  describe('getPropertyValue', () => {
    describe('when property type is object property', () => {
      it('should return empty change-set when value is undefined', () => {
        let propertyViewModel = {identifier: 'references', isDataProperty: false};
        let propertyValidationModel = {};
        let value = BpmService.getPropertyValue(propertyViewModel, propertyValidationModel);
        expect(value).to.eql({value: {add: [], remove: []}, defaultValue: {add: [], remove: []}});
      });

      it('should return empty change-set when value is empty', () => {
        let propertyViewModel = {identifier: 'references', isDataProperty: false};
        let propertyValidationModel = {value: {}};
        let value = BpmService.getPropertyValue(propertyViewModel, propertyValidationModel);
        expect(value).to.eql({value: {add: [], remove: []}, defaultValue: {add: [], remove: []}});

        propertyValidationModel = {value: {results: []}};
        value = BpmService.getPropertyValue(propertyViewModel, propertyValidationModel);
        expect(value).to.eql({value: {add: [], remove: []}, defaultValue: {add: [], remove: []}});
      });

      it('should return the original change-set when value has been changed', () => {
        let propertyViewModel = {identifier: 'references', isDataProperty: false};
        let propertyValidationModel = {value: {results: ['1'], add: ['1'], remove: []}};
        let value = BpmService.getPropertyValue(propertyViewModel, propertyValidationModel);
        expect(value).to.eql({value: {add: ['1'], remove: []}, defaultValue: {add: [], remove: []}});
      });

      // When The property hasn't been changed, the change-set would be empty, but here we want the actual value (the
      // default one) to be returned back
      it('should build new change-set when value is not changed', () => {
        let propertyViewModel = {identifier: 'references', isDataProperty: false};
        let propertyValidationModel = {value: {results: ['2'], add: [], remove: []}};
        let value = BpmService.getPropertyValue(propertyViewModel, propertyValidationModel);
        expect(value).to.eql({value: {add: ['2'], remove: []}, defaultValue: {add: [], remove: []}});
      });
    });

    describe('when property type is numeric', () => {
      it('should return parsed to number value', () => {
        let propertyViewModel = {identifier: 'someNumber', isDataProperty: true, dataType: 'int'};
        let propertyValidationModel = {value: '123', defaultValue: '123'};
        let value = BpmService.getPropertyValue(propertyViewModel, propertyValidationModel);
        expect(value).to.eql({value: 123, defaultValue: 123});
      });
    });

    describe('when property type is string', () => {
      it('should return value as is', () => {
        let propertyViewModel = {identifier: 'textField', isDataProperty: true, dataType: 'text'};
        let propertyValidationModel = {value: 'text value', defaultValue: 'text value'};
        let value = BpmService.getPropertyValue(propertyViewModel, propertyValidationModel);
        expect(value).to.eql({value: 'text value', defaultValue: 'text value'});
      });
    });
  });
});

function mockRestClient() {
  return {
    get: sinon.spy(() => {
      return '';
    }),
    post: sinon.spy(() => {
      return '';
    })
  };
}
