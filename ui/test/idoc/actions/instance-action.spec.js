import {InstanceAction} from 'idoc/actions/instance-action';

describe('InstanceAction', () => {
  it('buildActionPayload() should return configured payload object using provided data', () => {

    let handler = new SomeApproveAction({});
    var actionDefinition = {action: 'approve'};
    var currentObject = {
      getContextPathIds: () => {
        return ['emf:123456'];
      },
      getModels: () => {
        return {definitionId: 'ET220001'};
      },
      getChangeset: () => {
        return {}
      }
    };
    var operation = 'transition';
    let payload = handler.buildActionPayload(actionDefinition, currentObject, operation);
    expect(payload).to.eql({
      operation: 'transition',
      userOperation: 'approve',
      contextPath: ['emf:123456'],
      targetInstance: {
        definitionId: 'ET220001',
        properties: {}
      }
    })
  });
});

class SomeApproveAction extends InstanceAction {
  execute() {
  }
}