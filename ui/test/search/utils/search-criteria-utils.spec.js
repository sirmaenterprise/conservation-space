import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';

describe('SearchCriteriaUtils', () => {

  describe('getTypesFromCriteria()', () => {

    it('should return empty array if criteria is falsy', () => {
      expect(SearchCriteriaUtils.getTypesFromCriteria(null)).to.deep.eq([]);
    });

    it('should skip non type fileds from criteria', () => {
      expect(SearchCriteriaUtils.getTypesFromCriteria({
        condition: 'AND',
        rules: [
          {field: 'name', value: [1, 2, 3]}
        ]
      })).to.deep.eq([]);
    });

    it('should collect all type rules', () => {
      var criteria = {
        condition: 'AND',
        rules: [
          {field: 'types', value: [1, 2, 3]},
          {
            condition: 'OR',
            rules: [
              {field: 'types', value: [4, 5, 6]}
            ]
          }
        ]
      };

      expect(SearchCriteriaUtils.getTypesFromCriteria(criteria)).to.deep.eq([1, 2, 3, 4, 5, 6]);
    });

    it('should handle non array rule value', () => {
      expect(SearchCriteriaUtils.getTypesFromCriteria({
        condition: 'AND',
        rules: [
          {field: 'types', value: 1}
        ]
      })).to.deep.eq([1]);
    });
  });

  describe('replaceCriteria', () => {
    it('should not replace if one of the criteria object is undefined', () => {
      var original = {
        rules: [{id: '123'}]
      };
      SearchCriteriaUtils.replaceCriteria(original, undefined);
      expect(original.rules.length).to.equal(1);
      expect(original.rules[0].id).to.equal('123');
    });

    it('should replace the criteria with the provided one', () => {
      var original = {
        rules: [{id: '123'}]
      };
      var target = {
        rules: [{id: '456'}, {id: '789'}]
      };
      SearchCriteriaUtils.replaceCriteria(original, target);
      expect(original.rules.length).to.equal(2);
      expect(original.rules[0].id).to.equal('456');
      expect(original.rules[1].id).to.equal('789');
    });

    it('should replace the criteria with the provided one even if there are no rules in the original', () => {
      var original = {};
      var target = {
        rules: [{id: '456'}, {id: '789'}]
      };
      SearchCriteriaUtils.replaceCriteria(original, target);
      expect(original.rules.length).to.equal(2);
    });
  });

  describe('isCriteriaDefined()', () => {
    it('should determine that undefined is not a defined criteria', () => {
      expect(SearchCriteriaUtils.isCriteriaDefined(undefined)).to.be.false;
    });

    it('should determine that {} is not a defined criteria', () => {
      expect(SearchCriteriaUtils.isCriteriaDefined({})).to.be.false;
    });

    it('should determine that criteria with rules is a defined criteria', () => {
      expect(SearchCriteriaUtils.isCriteriaDefined({rules: []})).to.be.true;
    });
  });
});