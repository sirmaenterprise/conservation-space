import {AdvancedSearchKeywordCriteria} from 'search/components/advanced/criteria/advanced-search-keyword-criteria';
import {FTS_CHANGE_EVENT} from 'search/components/search';
import {SearchMediator} from 'search/search-mediator';
import {stub} from 'test/test-utils';

describe('AdvancedSearchKeywordCriteria', () => {

  var advancedSearchKeywordCriteria;
  beforeEach(() => {
    advancedSearchKeywordCriteria = new AdvancedSearchKeywordCriteria();
    advancedSearchKeywordCriteria.config.searchMediator = stub(SearchMediator);
    advancedSearchKeywordCriteria.criteria = {};
  });

  it('should trigger FTS_CHANGE_EVENT with proper arguments when free text is changed', () => {
    advancedSearchKeywordCriteria.criteria.value = 'Free Text Search';
    advancedSearchKeywordCriteria.onChange();
    expect(advancedSearchKeywordCriteria.config.searchMediator.trigger.calledOnce).to.be.true;
    let triggerArgs = advancedSearchKeywordCriteria.config.searchMediator.trigger.getCall(0).args;
    expect(triggerArgs[0]).to.equal(FTS_CHANGE_EVENT);
    expect(triggerArgs[1]).to.equal('Free Text Search');
  });
});