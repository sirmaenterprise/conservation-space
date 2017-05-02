import {AdvancedSearchCriteria} from 'search/components/advanced/advanced-search-criteria';
import {AdvancedSearchMocks} from './advanced-search-mocks'

describe('AdvancedSearchCriteria', () => {

  var criteria;
  var advancedSearchCriteria;
  beforeEach(() => {
    // Fix scope mixing issue in karma
    AdvancedSearchCriteria.prototype.criteria = undefined;
    AdvancedSearchCriteria.prototype.config = undefined;

    criteria = AdvancedSearchMocks.getCriteria();
    advancedSearchCriteria = new AdvancedSearchCriteria();
    advancedSearchCriteria.criteria = criteria;
  });

  it('should assign by default max levels value in the configuration', () => {
    expect(advancedSearchCriteria.config.maxLevels).to.equal(3);
  });

  it('should configure by default that the component is not disabled', () => {
    expect(advancedSearchCriteria.config.disabled).to.be.false;
  });

  describe('isRule()', () => {
    it('should correctly decide if given criteria is a rule', () => {
      advancedSearchCriteria.criteria = criteria.rules[0];
      expect(advancedSearchCriteria.isRule()).to.be.true;
    });

    it('should correctly decide if given criteria is not a rule', () => {
      var innerCriteria = criteria.rules[1];
      advancedSearchCriteria.criteria = innerCriteria;
      expect(advancedSearchCriteria.isRule()).to.be.false;
    });
  });

  describe('getNextLevel()', () => {
    it('should return the next level', () => {
      advancedSearchCriteria.criteriaLevel = 1;
      expect(advancedSearchCriteria.getNextLevel()).to.equal(2);
    });
  });
});