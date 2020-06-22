import {AdvancedSearchActionCriteria} from 'search/components/advanced/criteria/advanced-search-action-criteria';
import {RoleManagementService} from 'services/rest/role-management-service';
import {TranslateService} from 'services/i18n/translate-service';

import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';
import {PromiseStub} from 'test/promise-stub';
import {PromiseAdapterMock} from 'test/adapters/angular/promise-adapter-mock';
import {stub} from 'test/test-utils';

describe('AdvancedSearchActionCriteria', () => {

  let advancedSearchActionCriteria;

  beforeEach(() => {
    advancedSearchActionCriteria = new AdvancedSearchActionCriteria(mock$scope(), mockRoleManagmentService(), PromiseAdapterMock.mockImmediateAdapter(), mockTranstaseService());
    advancedSearchActionCriteria.config = {disabled: false};
  });

  it('should not be disabled by default', () => {
    expect(advancedSearchActionCriteria.config.disabled).to.be.false;
  });

  it('should build proper select config', () => {
    let expectedData = [
      {
        id: 'addRelation',
        text: 'label.addRelation'
      },
      {
        id: 'changePassword',
        text: 'label.changePassword'
      },
      {
        id: 'login',
        text: 'label.login'
      },
      {
        id: 'logout',
        text: 'label.logout'
      },
      {
        id: 'removeRelation',
        text: 'label.removeRelation'
      }
    ];
    expect(advancedSearchActionCriteria.selectConfig.data).to.deep.equal(expectedData);
    expect(advancedSearchActionCriteria.selectConfig.multiple).to.be.true;
    expect(advancedSearchActionCriteria.selectConfig.selectOnClose).to.be.true;
    expect(advancedSearchActionCriteria.selectConfig.isDisabled).to.exist;
  });

  function mockRoleManagmentService() {
    var service = stub(RoleManagementService);
    service.getRoleActions = () => {
      return PromiseStub.resolve({
        data: {
          actions: []
        }
      });
    };
    return service;
  }

  function mockTranstaseService() {
    var service = stub(TranslateService);
    service.translateInstant = (label) => {
      return label;
    };
    return service;
  }
});

