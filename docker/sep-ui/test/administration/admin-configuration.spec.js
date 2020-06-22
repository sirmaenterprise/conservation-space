import {
  AdminConfiguration,
  ADMIN_PANEL_QUERY_PARAMETER,
  ADMIN_PANEL_EXTENSION_POINT,
  USER_MANAGEMENT_EXTENSION,
  GROUP_MANAGEMENT_EXTENSION,
  MODELS_MANAGEMENT_EXTENSION
} from 'administration/admin-configuration';
import {Eventbus} from 'services/eventbus/eventbus';
import {UserService} from 'security/user-service';
import {TranslateService} from 'services/i18n/translate-service';
import {StateParamsAdapter} from 'adapters/router/state-params-adapter';
import {Router} from 'adapters/router/router';
import {AdminToolRegistry} from 'administration/admin-tool-registry';
import {USER, GROUP} from 'administration/resources-management/resource-management';
import {RouterStateChangeSuccessEvent} from 'common/router/router-state-change-success-event';

import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';
import {stubConfirmationDialogService} from 'test/components/dialog/confirmation-dialog-service.stub';

describe('AdminConfiguration', () => {

  let adminConfiguration;

  beforeEach(() => {
    adminConfiguration = new AdminConfiguration(stubUserService(true), stub(Eventbus), stub(Router),
      stubStateParamsAdapter(), stub(TranslateService), stubAdminToolRegistry(), stubConfirmationDialogService());
  });

  it('should construct the panelConfig if the user is admin', () => {
    adminConfiguration.userService = stubUserService(true);
    adminConfiguration.ngOnInit();
    expect(adminConfiguration.panelConfig).to.exists;
  });

  it('should not construct the panelConfig if the user is not admin', () => {
    adminConfiguration.userService = stubUserService(false);
    adminConfiguration.ngOnInit();
    expect(adminConfiguration.panelConfig).to.not.exist;
  });

  it('should have tabs correctly initialized', () => {
    adminConfiguration.stateParamsAdapter = stubStateParamsAdapter('test-tab-name');
    adminConfiguration.ngOnInit();
    expect(adminConfiguration.panelConfig.sortComparator).to.exist;
    expect(adminConfiguration.panelConfig.defaultTab).to.eq('test-tab-name');
    expect(adminConfiguration.panelConfig.extensionPoint).to.eq(ADMIN_PANEL_EXTENSION_POINT);
  });

  it('should not activate tab when no url parameter', () => {
    adminConfiguration.ngOnInit();
    expect(adminConfiguration.panelConfig.defaultTab).to.equal(undefined);
  });

  it('should activate tab when there is a url parameter with existing tab id', () => {
    adminConfiguration.stateParamsAdapter = stubStateParamsAdapter('role-actions-table');
    adminConfiguration.ngOnInit();
    expect(adminConfiguration.panelConfig.defaultTab).to.equal('role-actions-table');
  });

  it('should configure extensions configurations properly', () => {
    let expected = {
      [USER_MANAGEMENT_EXTENSION]: {
        resourceType: USER
      },
      [GROUP_MANAGEMENT_EXTENSION]: {
        resourceType: GROUP
      },
      [MODELS_MANAGEMENT_EXTENSION]: {
        tenantId: 'john.smith'
      }
    };
    adminConfiguration.ngOnInit();
    expect(adminConfiguration.panelConfig.extensions).to.deep.equal(expected);
  });

  it('should subscribe the route event', () => {
    adminConfiguration.ngOnInit();
    expect(adminConfiguration.eventbus.subscribe.calledOnce).to.be.true;
    expect(adminConfiguration.eventbus.subscribe.getCall(0).args[0]).to.eq(RouterStateChangeSuccessEvent);
  });

  it('should properly evaluate default on RouterStateChangeSuccessEvent when invalid parameter is specified', () => {
    adminConfiguration.stateParamsAdapter = stubStateParamsAdapter('role-actions-table');
    adminConfiguration.ngOnInit();
    adminConfiguration.stateParamsAdapter = stubStateParamsAdapter(undefined);
    // Mock event firing
    adminConfiguration.eventbus.subscribe.getCall(0).args[1]();
    expect(adminConfiguration.panelConfig.defaultTab).to.eq(undefined);
  });

  it('should properly evaluate default on RouterStateChangeSuccessEvent when valid parameter is specified', () => {
    adminConfiguration.stateParamsAdapter = stubStateParamsAdapter('role-actions-table');
    adminConfiguration.ngOnInit();
    // Mock event firing
    adminConfiguration.eventbus.subscribe.getCall(0).args[1]();
    expect(adminConfiguration.panelConfig.defaultTab).to.eq('role-actions-table');
  });

  it('should un-subscribe the route event on destroy if event is properly initialized', () => {
    adminConfiguration.routerStateEvent = {
      unsubscribe: sinon.spy()
    };
    adminConfiguration.ngOnDestroy();
    expect(adminConfiguration.routerStateEvent.unsubscribe.calledOnce).to.be.true;
  });

  it('should properly compare admin panel tab labels', () => {
    adminConfiguration.translateService.translateInstant = (key) => key;

    expect(adminConfiguration.adminPanelTabsComparator({label: 'b'}, {label: 'b'})).to.eq(0);
    expect(adminConfiguration.adminPanelTabsComparator({label: 'b'}, {label: 'a'})).to.eq(1);
    expect(adminConfiguration.adminPanelTabsComparator({label: 'a'}, {label: 'b'})).to.eq(-1);

    expect(adminConfiguration.adminPanelTabsComparator({label: 'A'}, {label: 'b'})).to.eq(-1);
    expect(adminConfiguration.adminPanelTabsComparator({label: 'b'}, {label: 'A'})).to.eq(1);
    expect(adminConfiguration.adminPanelTabsComparator({label: 'A'}, {label: 'B'})).to.eq(-1);
  });

  describe('onTabChanged(newTab, oldTab, tabConfig)', () => {
    it('should update the state parameters with the newly selected tab', () => {
      adminConfiguration.ngOnInit();
      adminConfiguration.onTabChanged({id: 'one'}, {id: 'two'}, {activeTab: 'one'});
      expect(adminConfiguration.stateParamsAdapter.setStateParam.calledOnce).to.be.true;
      expect(adminConfiguration.stateParamsAdapter.setStateParam.calledWith(ADMIN_PANEL_QUERY_PARAMETER, 'one')).to.be.true;
      expect(adminConfiguration.router.navigate.calledOnce).to.be.true;
      expect(adminConfiguration.router.navigate.calledWith(ADMIN_PANEL_EXTENSION_POINT)).to.be.true;
    });

    it('should ask for confirmation if there are administration tools in dirty state', () => {
      adminConfiguration.adminToolRegistry = stubAdminToolRegistry(true);
      adminConfiguration.confirmationDialogService = stubConfirmationDialogService(true);
      adminConfiguration.onTabChanged({id: 'one'}, {id: 'two'}, {activeTab: 'one'});
      expect(adminConfiguration.stateParamsAdapter.setStateParam.calledOnce).to.be.true;
      expect(adminConfiguration.router.navigate.calledOnce).to.be.true;
    });

    it('should revert the newly selected tab if the confirmation is rejected', () => {
      adminConfiguration.adminToolRegistry = stubAdminToolRegistry(true);
      adminConfiguration.confirmationDialogService = stubConfirmationDialogService(false);
      let tabsConfig = {activeTab: 'one'};
      adminConfiguration.onTabChanged({id: 'one'}, {id: 'two'}, tabsConfig);
      expect(adminConfiguration.stateParamsAdapter.setStateParam.called).to.be.false;
      expect(adminConfiguration.router.navigate.called).to.be.false;
      expect(tabsConfig.activeTab).to.equal('two');
    });
  });

  function stubUserService(isAdmin = true, tenantId = 'john.smith') {
    let userServiceStub = stub(UserService);
    userServiceStub.getCurrentUser.returns(PromiseStub.resolve(getUser(isAdmin, tenantId)));
    return userServiceStub;
  }

  function stubStateParamsAdapter(toolStateParam) {
    let stateParamsStub = stub(StateParamsAdapter);
    stateParamsStub.getStateParam.withArgs(ADMIN_PANEL_QUERY_PARAMETER).returns(toolStateParam);
    return stateParamsStub;
  }

  function stubAdminToolRegistry(hasUnsaved = false) {
    let registryStub = stub(AdminToolRegistry);
    registryStub.getState.returns(hasUnsaved);
    return registryStub;
  }

  function getUser(isAdmin, tenantId) {
    return {isAdmin, tenantId};
  }

});