import {ModelManagementRouteInterrupter} from 'administration/model-management/services/model-management-route-interrupter';
import {ModelManagementStateRegistry} from 'administration/model-management/services/model-management-state-registry';
import {MODEL_MANAGEMENT_EXTENSION_POINT} from 'administration/model-management/model-management';
import {stub} from 'test/test-utils';

describe('ModelManagementRouteInterrupter', () => {

  let stateRegistry;
  let modelManagementInterrupter;

  beforeEach(() => {
    stateRegistry = stub(ModelManagementStateRegistry);
    modelManagementInterrupter = new ModelManagementRouteInterrupter(stateRegistry);
  });

  it('should NOT interrupt if the state is not the model management one', () => {
    stateRegistry.hasDirtyState.returns(false);
    expect(modelManagementInterrupter.shouldInterrupt(stubRouter('other-route'))).to.be.false;
  });

  it('should NOT interrupt if the state is the model management one but the registry has not dirty state', () => {
    stateRegistry.hasDirtyState.returns(false);
    expect(modelManagementInterrupter.shouldInterrupt(stubRouter(MODEL_MANAGEMENT_EXTENSION_POINT))).to.be.false;
  });

  it('should interrupt if the state is the model management one and the registry has a dirty state', () => {
    stateRegistry.hasDirtyState.returns(true);
    expect(modelManagementInterrupter.shouldInterrupt(stubRouter(MODEL_MANAGEMENT_EXTENSION_POINT))).to.be.true;
  });

  function stubRouter(state) {
    return {
      getCurrentState: () => state
    };
  }

});