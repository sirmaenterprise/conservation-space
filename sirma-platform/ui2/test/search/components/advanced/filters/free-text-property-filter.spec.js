import {FreeTextPropertyFilter} from 'search/components/advanced/filters/free-text-property-filter';
import {SearchMediator} from 'search/search-mediator';
import {QueryBuilder} from 'search/utils/query-builder';
import {CRITERIA_FTS_RULE_FIELD} from 'search/utils/search-criteria-utils';

describe('FreeTextPropertyFilter', () => {
  var filter;

  beforeEach(() => {
    filter = new FreeTextPropertyFilter();
  });

  describe('filter()', () => {
    it('should filter out fts property when already contained', () => {
      let result = filter.filter(getConfig(true), {id: 2}, {id: CRITERIA_FTS_RULE_FIELD});
      expect(result).to.be.false;
    });

    it('should not filter out fts property when not contained', () => {
      let result = filter.filter(getConfig(false), {id: 1}, {id: CRITERIA_FTS_RULE_FIELD});
      expect(result).to.be.true;
    });

    it('should filter out fts property when contained and not topmost', () => {
      let result = filter.filter(getConfig(true), {id: 2}, {id: CRITERIA_FTS_RULE_FIELD});
      expect(result).to.be.false;
    });

    it('should filter out fts property when not contained and not topmost', () => {
      let result = filter.filter(getConfig(false), {id: 2}, {id: CRITERIA_FTS_RULE_FIELD});
      expect(result).to.be.false;
    });
  });

  function getConfig(provideFts) {
    return {
      searchMediator: new SearchMediator({}, new QueryBuilder(getSearchTree(provideFts)))
    };
  }

  function getSearchTree(provideFts) {
    let field = provideFts ? CRITERIA_FTS_RULE_FIELD : 'emf:title';

    return {
      condition: 'OR',
      rules: [
        {
          condition: 'AND',
          rules: [
            {
              field: 'types'
            }, {
              condition: 'AND',
              rules: [
                {
                  id: 1,
                  field: field
                },
                {
                  id: 2,
                  field: 'emf:createdBy'
                },
                {
                  id: 3,
                  field: 'emf:createdOn'
                }
              ]
            }
          ]
        }
      ]
    };
  }
});