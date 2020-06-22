import {PickerRestrictionsService, PICKER_RESTRICTIONS_POPOVER_TITLE, PICKER_RESTRICTIONS_POPOVER_BODY, PICKER_RESTRICTIONS_WARNING_MESSAGE} from 'services/picker/picker-restrictions-service';
import {PromiseStub} from 'test/promise-stub';

import {SearchService} from 'services/rest/search-service';
import {DialogService} from 'components/dialog/dialog-service';
import {TranslateService} from 'services/i18n/translate-service';
import {SearchCriteriaService} from 'services/search/search-criteria-service';

import {HEADER_DEFAULT} from 'instance-header/header-constants';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';

import {stub} from 'test/test-utils';
import {stubSearchService} from 'test/services/rest/search-service-mock';

describe('PickerRestrictionsService', () => {

  let restrictionsService;

  beforeEach(() => {
    restrictionsService = new PickerRestrictionsService(stub(SearchService), stub(DialogService), stub(TranslateService),
      stub(SearchCriteriaService));
  });

  it('should have default labels ready and pre-translated', () => {
    restrictionsService.translateService.translateInstant.returns({});
    restrictionsService.assignRestrictionLabels();

    expect(restrictionsService.dialogHeader).to.exist;
    expect(restrictionsService.dialogMessage).to.exist;
    expect(restrictionsService.warningMessage).to.exist;
  });

  it('should have search mediator initialized by default', () => {
    restrictionsService.assignSearchMediator();

    expect(restrictionsService.searchMediator).to.exist;
    expect(restrictionsService.searchMediator.arguments).to.deep.eq({properties: ['id', HEADER_DEFAULT]});
  });

  it('should assign the provided restriction rule as top level rule', () => {
    let criteria = SearchCriteriaUtils.getDefaultAdvancedSearchCriteria();
    let restrictions = SearchCriteriaUtils.buildRule('restricted_field', '', '', 'restricted_value');

    restrictionsService.assignRestrictionCriteria(criteria, restrictions);
    expect(criteria.rules.length).to.equal(2);
    expect(criteria.rules[1].field).to.equal('restricted_field');
  });

  it('should assign the provided restriction condition as top level rule', () => {
    let criteria = SearchCriteriaUtils.getDefaultAdvancedSearchCriteria();
    let restrictions = SearchCriteriaUtils.buildCondition();
    restrictions.rules.push(SearchCriteriaUtils.buildRule('restricted_field', '', '', 'restricted_value'));

    restrictionsService.assignRestrictionCriteria(criteria, restrictions);
    expect(criteria.rules.length).to.equal(2);
    expect(criteria.rules[1].rules.length).to.equal(1);
    expect(criteria.rules[1].rules[0].field).to.equal('restricted_field');
  });

  it('should not assign top level rule when restrictions are empty', () => {
    let criteria = SearchCriteriaUtils.getDefaultAdvancedSearchCriteria();

    restrictionsService.assignRestrictionCriteria(criteria, SearchCriteriaUtils.buildRule('restricted_field', '', '', ''));
    expect(criteria.rules.length).to.equal(1);
  });

  it('should assign default rules if provided criteria is empty', () => {
    let criteria = SearchCriteriaUtils.buildCondition();
    let restrictions = SearchCriteriaUtils.buildRule('restricted_field', '', '', 'restricted_value');

    restrictionsService.assignRestrictionCriteria(criteria, restrictions);
    expect(criteria.rules.length).to.equal(2);
  });

  it('should assign a complete restriction configuration to the provided config', () => {
    let config = {};

    restrictionsService.translateService.translateInstant.withArgs(PICKER_RESTRICTIONS_POPOVER_BODY).returns('Body');
    restrictionsService.translateService.translateInstant.withArgs(PICKER_RESTRICTIONS_POPOVER_TITLE).returns('Title');
    restrictionsService.translateService.translateInstant.withArgs(PICKER_RESTRICTIONS_WARNING_MESSAGE).returns('Message');

    restrictionsService.searchCriteriaService.translateSearchCriteria.returns(PromiseStub.resolve({}));
    restrictionsService.searchCriteriaService.stringifySearchCriteria.returns('stringified-criteria');

    restrictionsService.assignRestrictionLabels();
    restrictionsService.assignRestrictionMessage(config);

    expect(config.warningMessage).to.eq('Message');
    expect(config.warningPopover).to.deep.eq({
      style: {
        'max-width': 'none',
        'width': 'auto'
      },
      title: 'Title',
      placement: 'bottom',
      body: 'stringified-criteria'
    });
  });

  it('should build a search criteria based on instance identifiers and restrictions', () => {
    let identifiers = [1, 2, 3, 4];
    let restrictions = {field: 'emf:status', operator: 'in', value: ['DRAFT']};
    let criteria = restrictionsService.buildCriteria(identifiers, restrictions);

    expect(criteria.rules.length).to.eq(2);

    let instanceRule = criteria.rules[0];
    expect(instanceRule.operator).to.eq('in');
    expect(instanceRule.field).to.eq('instanceId');
    expect(instanceRule.value).to.deep.eq([1, 2, 3, 4]);

    let restrictionRule = criteria.rules[1];
    expect(restrictionRule.operator).to.eq('in');
    expect(restrictionRule.field).to.eq('emf:status');
    expect(restrictionRule.value).to.deep.eq(['DRAFT']);
  });

  it('should filter when instance identifiers and restrictions are provided', () => {
    let identifiers = [1, 2, 3];
    let instances = [{id: 1}, {id: 2}, {id: 3}];
    let restrictions = {field: 'emf:status', operator: 'in', value: ['DRAFT']};

    restrictionsService.buildCriteria = sinon.spy();
    restrictionsService.searchService = stubSearchService(instances, 3);
    restrictionsService.assignSearchMediator();

    restrictionsService.filterByRestrictions(identifiers, restrictions).then((result) => {
      expect(result).to.deep.eq(instances);
    });
    expect(restrictionsService.searchMediator.queryBuilder).to.exist;
    expect(restrictionsService.searchService.search.calledOnce).to.be.true;
    expect(restrictionsService.buildCriteria.calledWith(identifiers, restrictions)).to.be.true;
  });

  it('should call provided handle with handle selection when instance is filtered correctly', () => {
    let restrictions = {};
    let instance = {id: 1};
    let handler = sinon.spy(() => {
    });

    restrictionsService.buildDialog = sinon.spy();
    restrictionsService.filterByRestrictions = sinon.spy(() => {
      return PromiseStub.resolve([{id: 1}]);
    });
    restrictionsService.handleSelection(instance, restrictions, handler);

    expect(handler.calledOnce).to.be.true;
    expect(handler.calledWith(instance)).to.be.true;
    expect(restrictionsService.buildDialog.calledOnce).to.be.false;
    expect(restrictionsService.filterByRestrictions.calledOnce).to.be.true;
  });

  it('should not call provided handle with handle selection when instance is not filtered correctly', () => {
    let restrictions = {};
    let instance = {id: 1};
    let handler = sinon.spy(() => {
    });

    restrictionsService.buildDialog = sinon.spy();
    restrictionsService.filterByRestrictions = sinon.spy(() => {
      return PromiseStub.resolve([{id: 4}]);
    });
    restrictionsService.handleSelection(instance, restrictions, handler);

    expect(handler.calledOnce).to.be.false;
    expect(restrictionsService.buildDialog.calledOnce).to.be.true;
    expect(restrictionsService.filterByRestrictions.calledOnce).to.be.true;
  });

  it('should build a dialog using the dialogService', () => {
    restrictionsService.translateService.translateInstant.returns({});
    restrictionsService.assignRestrictionLabels();

    let dialogHeader = restrictionsService.dialogHeader;
    let dialogMessage = restrictionsService.dialogMessage;

    restrictionsService.buildDialog();
    expect(restrictionsService.dialogService.confirmation.calledWith(dialogMessage, dialogHeader)).to.be.true;
  });

  it('should build a search rule based on provided instance identifiers', () => {
    let rule = restrictionsService.buildInstanceRule([1, 2, 3, 4]);

    expect(rule.operator).to.eq('in');
    expect(rule.field).to.eq('instanceId');
    expect(rule.value).to.deep.eq([1, 2, 3, 4]);
  });
});