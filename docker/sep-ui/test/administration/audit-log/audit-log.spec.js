'use strict';

import {AuditLog} from 'administration/audit-log/audit-log';
import {Configuration} from 'common/application-config';
import {SearchMediator, EVENT_SEARCH} from 'search/search-mediator';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {AdvancedSearchCriteriaOperators} from 'search/components/advanced/criteria/advanced-search-criteria-operators';

import {IdocMocks} from 'test/idoc/idoc-mocks';
import {AuditLogService} from 'services/rest/audit-log-service';

import {stub} from 'test/test-utils';
import {PromiseAdapterMock} from 'test/adapters/angular/promise-adapter-mock';

describe('Audit log spec', () => {
  var auditLog;

  beforeEach(() => {
    auditLog = new AuditLog(stub(AuditLogService), IdocMocks.mockTranslateService(), PromiseAdapterMock.mockImmediateAdapter(), stub(Configuration));
    auditLog.ngOnInit();
  });

  it('should configure audit properly', () => {
    let paginationConfig = {
      showFirstLastButtons: true,
      page: 1,
      pageSize: undefined
    };
    expect(auditLog.criteria).to.exist;
    expect(auditLog.loaders).to.exist;
    expect(auditLog.config.searchMediator).to.exist;
    expect(auditLog.paginationCallback).to.exist;
    expect(auditLog.config.paginationConfig).to.deep.equal(paginationConfig);
  });

  it('should call search mediator', () => {
    auditLog.config.searchMediator.search = sinon.spy();
    auditLog.search();
    expect(auditLog.config.searchMediator.search.calledOnce).to.be.true;
  });

  it('should clear results and fire search event', () => {
    var emptySearchResults = SearchMediator.buildEmptySearchResults();
    var emptyResponse = SearchMediator.buildSearchResponse({}, emptySearchResults);
    auditLog.config.searchMediator.trigger = sinon.spy();
    auditLog.clear();
    expect(auditLog.config.searchMediator.trigger.calledWith(EVENT_SEARCH, emptyResponse)).to.be.true;
  });

  it('should properly filter audit fields', () => {
    expect(auditLog.auditTableFields.length).to.be.equals(9);
  });

  it('should disable components', () => {
    expect(auditLog.config.disabled).to.be.false;
    auditLog.disableComponents(true);
    expect(auditLog.config.disabled).to.be.true;
    expect(auditLog.config.paginationConfig.disabled).to.be.true;
  });

  it('should set default criteria', () => {
    let expectedCriteria = {
      condition: 'OR',
      rules: [{
        condition: 'AND',
        rules: []
      }]
    };
    let method = 'getDefaultAdvancedSearchCriteria';
    let stub = sinon.stub(SearchCriteriaUtils, method).returns(expectedCriteria);

    auditLog.assignCriteria();
    expect(auditLog.criteria.rules).to.deep.equal(expectedCriteria.rules[0].rules);
    expect(auditLog.criteria.condition).to.equal(expectedCriteria.rules[0].condition);
    expect(auditLog.config.searchMediator.queryBuilder.tree).to.deep.equal(expectedCriteria);
    stub.restore();
  });

  it('should assign a loader for the possible search form properties', () => {
    expect(auditLog.loaders.properties).to.exist;
    var loadedProperties;
    auditLog.loaders.properties().then((properties) => {
      loadedProperties = properties;
    });
    let expected = [{
      id: 'actionid',
      text: 'translated message',
      type: 'action'
    }, {
      id: 'objectsystemid',
      text: 'translated message',
      type: 'object',
      operators: [AdvancedSearchCriteriaOperators.SET_TO.id, AdvancedSearchCriteriaOperators.NOT_SET_TO.id, AdvancedSearchCriteriaOperators.EMPTY.id]
    }, {
      id: 'eventdate',
      text: 'translated message',
      type: 'dateTime'
    }, {
      id: 'username',
      text: 'translated message',
      type: 'authority'
    }];
    expect(loadedProperties).to.deep.equals(expected);
  });
});