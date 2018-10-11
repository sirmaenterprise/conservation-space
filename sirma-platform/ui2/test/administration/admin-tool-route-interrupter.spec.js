import {AdminToolRouteInterrupter} from 'administration/admin-tool-route-interrupter';
import {AdminToolRegistry} from 'administration/admin-tool-registry';
import {Router} from 'adapters/router/router';
import {ADMINISTRATION_STATE} from 'administration/admin-configuration';
import {stub} from 'test/test-utils';

describe('AdminToolRouteInterrupter', () => {

  let routeInterrupter;
  beforeEach(() => {
    routeInterrupter = new AdminToolRouteInterrupter(stubAdminToolRegistry());
  });

  it('should not interrupt if the current state is not the administrations', () => {
    expect(routeInterrupter.shouldInterrupt(stubRouter('dashboard'))).to.be.false;
  });

  it('should not interrupt if there is no unsaved state in the administration tools registry', () => {
    expect(routeInterrupter.shouldInterrupt(stubRouter(ADMINISTRATION_STATE))).to.be.false;
  });

  it('should interrupt if there is unsaved state in the administration tools registry', () => {
    routeInterrupter.adminToolRegistry = stubAdminToolRegistry(true);
    expect(routeInterrupter.shouldInterrupt(stubRouter(ADMINISTRATION_STATE))).to.be.true;
  });

  function stubAdminToolRegistry(hasUnsaved = false) {
    let registryStub = stub(AdminToolRegistry);
    registryStub.hasUnsavedState.returns(hasUnsaved);
    return registryStub;
  }

  function stubRouter(currentState) {
    let routerStub = stub(Router);
    routerStub.getCurrentState.returns(currentState);
    return routerStub;
  }
});