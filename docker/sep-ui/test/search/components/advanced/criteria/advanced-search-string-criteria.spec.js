import {AdvancedSearchStringCriteria} from "search/components/advanced/criteria/advanced-search-string-criteria";
import {TooltipAdapter} from "adapters/tooltip-adapter";
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {AdvancedSearchCriteriaOperators} from 'search/components/advanced/criteria/advanced-search-criteria-operators';

import {AdvancedSearchMocks} from "test/search/components/advanced/advanced-search-mocks";
import {IdocMocks} from "test/idoc/idoc-mocks";
import {stub} from 'test/test-utils';

describe('AdvancedSearchStringCriteria', () => {

  var criteria;
  var advancedSearchStringCriteria;
  var spyTooltip;

  beforeEach(() => {
    criteria = AdvancedSearchMocks.getCriteria().rules[0];

    var tooltipsAdapter = stub(TooltipAdapter);
    spyTooltip = tooltipsAdapter.tooltip;

    var element = {
      on: sinon.spy(()=> {
      })
    };

    advancedSearchStringCriteria = new AdvancedSearchStringCriteria(element, tooltipsAdapter, IdocMocks.mockTranslateService());
    advancedSearchStringCriteria.property = {};
    advancedSearchStringCriteria.criteria = criteria;
    advancedSearchStringCriteria.ngOnInit();
  });

  it('should construct the component config defaults', () => {
    expect(advancedSearchStringCriteria.config.disabled).to.be.false;
    expect(advancedSearchStringCriteria.config.debounce).to.be.eq(200);
  });

  it('should register an element watcher', () => {
    expect(advancedSearchStringCriteria.$element.on.calledOnce).to.be.true;
    expect(advancedSearchStringCriteria.$element.on.getCall(0).args[0]).to.equal('mouseenter');
  });

  it('should display a warning tooltip when searching by any field', () => {
    advancedSearchStringCriteria.criteria.field = SearchCriteriaUtils.ANY_FIELD;
    advancedSearchStringCriteria.showTooltip(undefined);

    var tooltipArgs = spyTooltip.getCall(0).args;
    expect(spyTooltip.callCount).to.equal(1);
    expect(tooltipArgs[1].title).to.equal('translated message');
  });

  it('should not display a warning tooltip when searching by a string criteria that is not any field', () => {
    advancedSearchStringCriteria.criteria.field = 'stringCriteria';
    advancedSearchStringCriteria.showTooltip(undefined);
    expect(spyTooltip.callCount).to.equal(0);
  });

  it('should assign default array if the criteria value is unassigned and is configured for multiple selection', () => {
    advancedSearchStringCriteria.criteria.value = undefined;
    advancedSearchStringCriteria.assignMultipleModel();
    expect(advancedSearchStringCriteria.criteria.value).to.deep.equal([]);
  });

  it('should assign default array if the criteria value is an empty string and is configured for multiple selection', () => {
    advancedSearchStringCriteria.criteria.value = '';
    advancedSearchStringCriteria.assignMultipleModel();
    expect(advancedSearchStringCriteria.criteria.value).to.deep.equal([]);
  });

  it('should convert to array if the criteria value is a string and is configured for multiple selection', () => {
    advancedSearchStringCriteria.criteria.value = 'a tag';
    advancedSearchStringCriteria.assignMultipleModel();
    expect(advancedSearchStringCriteria.criteria.value).to.deep.equal(['a tag']);
  });

  it('should not convert the model if it is not configured for multiple selection', () => {
    advancedSearchStringCriteria.criteria.value = 'a tag';
    advancedSearchStringCriteria.property.singleValued = true;
    advancedSearchStringCriteria.assignMultipleModel();
    expect(advancedSearchStringCriteria.criteria.value).to.equal('a tag');
  });

  it('should construct a select configuration for tagging', () => {
    expect(advancedSearchStringCriteria.selectConfig).to.exist;
    expect(advancedSearchStringCriteria.selectConfig.tags).to.exist;
    expect(advancedSearchStringCriteria.selectConfig.multiple).to.be.true;
    expect(advancedSearchStringCriteria.selectConfig.selectOnClose).to.be.true;
  });

  it('should construct the select configuration with the criteria value', () => {
    advancedSearchStringCriteria.selectConfig = undefined;
    advancedSearchStringCriteria.criteria.value = ['1', '2'];
    advancedSearchStringCriteria.createSelectConfig();
    expect(advancedSearchStringCriteria.selectConfig.tags).to.deep.equal(['1', '2']);
  });

  it('should construct the select with an update function for the disabled state', () => {
    expect(advancedSearchStringCriteria.selectConfig.isDisabled()).to.be.false;
    advancedSearchStringCriteria.config.disabled = true;
    expect(advancedSearchStringCriteria.selectConfig.isDisabled()).to.be.true;
  });

  it('should construct and assign model options', () => {
    advancedSearchStringCriteria.config.debounce = 100;
    advancedSearchStringCriteria.assignModelOptions();
    expect(advancedSearchStringCriteria.modelOptions).to.deep.equal({debounce: {'default': 100, 'blur': 0}});
  });

  describe('getMode()', () => {
    it('should determine that the current mode is "empty" if the operator is "empty"', () => {
      advancedSearchStringCriteria.criteria.operator = AdvancedSearchCriteriaOperators.EMPTY.id;
      expect(advancedSearchStringCriteria.getMode()).to.equal(AdvancedSearchCriteriaOperators.EMPTY.id);
    });

    it('should determine that the current mode is "single" if the property has "singleValued" field configured', () => {
      advancedSearchStringCriteria.property.singleValued = true;
      expect(advancedSearchStringCriteria.getMode()).to.equal(SearchCriteriaUtils.SINGLE);
    });

    it('should determine that the current mode is "multiple" if the property has not "singleValued" field configured', () => {
      expect(advancedSearchStringCriteria.getMode()).to.equal(SearchCriteriaUtils.MULTIPLE);
    });
  });

});