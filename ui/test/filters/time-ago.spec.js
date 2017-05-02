import moment from 'moment';
import {TimeAgo} from 'filters/time-ago';

describe('TimeAgo', function() {
  var filter;

  beforeEach(function() {
    filter = new TimeAgo();
  });

  describe('filter(date)', function() {
    it('should return empty string for falsy dates', function() {
      expect(filter.filter(undefined)).to.eq('');
      expect(filter.filter(null)).to.eq('');
      expect(filter.filter('')).to.eq('');
      expect(filter.filter(0)).to.eq('');
      expect(filter.filter(false)).to.eq('');
    });

    it('should support moment objects', function() {
      expect(filter.filter(moment())).to.contain('ago');
    });

    it('should support Date objects', function() {
      expect(filter.filter(new Date())).to.contain('ago');
    });

    it('should support iso string dates', function() {
      expect(filter.filter(moment().format())).to.contain('ago');
    });
  });
});