import {ExcludedOperatorFilter} from 'search/components/advanced/filters/excluded-operator-filter';

describe('ExcludedOperatorFilter', () => {

  var filter;
  beforeEach(() => {
    filter = new ExcludedOperatorFilter();
  });

  describe('filter()', () => {
    it('should leave only operators that are defined in the property', () => {
      var property = {
        id: 'title',
        type: 'string',
        operators: ['contains']
      };
      var operators = getOperators();
      expect(filter.filter({}, property, operators[0])).to.be.true;
      expect(filter.filter({}, property, operators[1])).to.be.false;
    });

    it('should not filter if the property has no operators defined', () => {
      var property = {
        id: 'title',
        type: 'string'
      };
      var operators = getOperators();
      expect(filter.filter({}, property, operators[0])).to.be.true;
      expect(filter.filter({}, property, operators[1])).to.be.true;
    });
  });

  function getOperators() {
    return [{
      id: 'contains',
      text: 'Contains'
    }, {
      id: 'equals',
      text: 'Equals'
    }];
  }
});