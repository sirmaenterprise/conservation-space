import {AdminConfiguration} from 'administration/admin-configuration';
import {PromiseStub} from 'test/promise-stub';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';

describe('AdminConfiguration', () => {

  var adminConfiguration;
  beforeEach(() => {
    var userService = mockUserService(true);
    var locationAdapter = mockLocationAdapter();
    adminConfiguration = new AdminConfiguration(mock$scope(), userService, locationAdapter);
  });

  afterEach(() => {
    adminConfiguration.tabsConfig = undefined;
  });

  it('should have the menu rendered equal to true if the user is admin', () => {
    adminConfiguration.userService = mockUserService(true);
    adminConfiguration.setupAdminPanel();
    expect(adminConfiguration.renderMenu).to.equal(true);
  });

  it('should have the menu rendered equal to false if the user is not admin', () => {
    adminConfiguration.userService = mockUserService(false);
    adminConfiguration.setupAdminPanel();
    expect(adminConfiguration.renderMenu).to.equal(false);
  });

  it('should have tabs correctly initialized', () => {
    adminConfiguration.setupTabsConfig();
    expect(adminConfiguration.tabsConfig.tabs.length).to.equal(2);
  });

  it('should not activate tab when no url parameter', () => {
    adminConfiguration.setupTabsConfig();
    expect(adminConfiguration.tabsConfig.activeTab).to.equal('tenantConfig');
  });

  it('should activate tab when there is a url parameter with existing tab id', () => {
    adminConfiguration.locationAdapter = mockLocationAdapter('#roleActionsTable');
    adminConfiguration.setupTabsConfig();
    expect(adminConfiguration.tabsConfig.activeTab).to.equal('roleActionsTable');
  });

  it('should not activate tab when there is a url parameter with not existing tab id', () => {
    adminConfiguration.locationAdapter = mockLocationAdapter('#test');
    adminConfiguration.setupTabsConfig();
    expect(adminConfiguration.tabsConfig.activeTab).to.equal('tenantConfig');
  });

  function mockUserService(isAdmin) {
    return {
      getCurrentUser: () => {
        return PromiseStub.resolve({
          isAdmin: isAdmin
        });
      }
    };
  }

  function mockLocationAdapter(hash) {
    return {
      url: () => {
        return 'administration' + hash;
      }
    }
  }
});