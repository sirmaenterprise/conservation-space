import {InstanceByTypeFilter} from 'filters/instance-by-type-filter';

describe('InstanceByTypeFilter', function() {

  describe('filter(instances, types)', function() {
    var filter;
    beforeEach(function() {
      filter = new InstanceByTypeFilter();
    });

    it('should return the passed in array of instances if they are falsy or zero length', function() {
      expect(filter.filter(null)).to.be.null;
      expect(filter.filter([])).to.deep.eq([]);
    });

    it('should return the passed in array of instances if types are falsy or zero length', function() {
      var time = new Date().getTime();

      expect(filter.filter(time, null)).to.eq(time);
      expect(filter.filter(time, [])).to.eq(time);
    });

    it('should return a filtered array of instances', function() {
      var instances = [
        {
          properties: {
            semanticHierarchy: [1, 2],
          },
        },
        {
          properties: {
            semanticHierarchy: [3],
          },
        },
      ];

      expect(filter.filter(instances, [3])).to.deep.eq([{
        properties: {
          semanticHierarchy: [3],
        },
      }]);
    });
  });

  describe('hasAnyType(types, instance)', function() {
    it('should return false if instance has no semantic hierarchy', function() {
      expect(InstanceByTypeFilter.hasAnyType(null, {})).to.be.false;
      expect(InstanceByTypeFilter.hasAnyType(null, {properties: {}})).to.be.false;
    });

    it('should return false there are no intersecting types', function() {
      expect(InstanceByTypeFilter.hasAnyType([], {
        properties: {
          semanticHierarchy: [],
        },
      })).to.be.false;

      expect(InstanceByTypeFilter.hasAnyType([], {
        properties: {
          semanticHierarchy: [1],
        },
      })).to.be.false;

      expect(InstanceByTypeFilter.hasAnyType([1], {
        properties: {
          semanticHierarchy: [],
        },
      })).to.be.false;

      expect(InstanceByTypeFilter.hasAnyType([1], {
        properties: {
          semanticHierarchy: [2],
        },
      })).to.be.false;
    });

    it('should return true if there is at least one intersecting type', function() {
      expect(InstanceByTypeFilter.hasAnyType([1], {
        properties: {
          semanticHierarchy: [1],
        },
      })).to.be.true;

      expect(InstanceByTypeFilter.hasAnyType([1, 2, 3], {
        properties: {
          semanticHierarchy: [1, 3],
        },
      })).to.be.true;
    });
  });
});