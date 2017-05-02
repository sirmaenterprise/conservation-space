import {
  DynamicDateRange,
  DEFAULT_DATE_STEP,
  DEFAULT_DATE_OFFSET,
  DEFAULT_DATE_OFFSET_TYPE
} from 'search/components/advanced/dynamic-date-range/dynamic-date-range';
import 'moment';

import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';

describe('DynamicDateRange', () => {

  var dynamicDateRange;
  beforeEach(() => {
    var translateService = mockTranslateService();
    dynamicDateRange = new DynamicDateRange(mock$scope(), translateService);
  });

  it('should be enabled by default', () => {
    expect(dynamicDateRange.config.disabled).to.be.false;
  });

  it('should assign default offset values if they are not provided', () => {
    expect(dynamicDateRange.dateStep).to.equal(DEFAULT_DATE_STEP);
    expect(dynamicDateRange.dateOffset).to.equal(DEFAULT_DATE_OFFSET);
    expect(dynamicDateRange.dateOffsetType).to.equal(DEFAULT_DATE_OFFSET_TYPE);
  });

  it('should not assign default offset values if they are provided', () => {
    DynamicDateRange.prototype.dateStep = '1';
    DynamicDateRange.prototype.dateOffset = '2';
    DynamicDateRange.prototype.dateOffsetType = '3';

    var translateService = mockTranslateService();
    dynamicDateRange = new DynamicDateRange(mock$scope(), translateService);

    expect(dynamicDateRange.dateStep).to.equal('1');
    expect(dynamicDateRange.dateOffset).to.equal('2');
    expect(dynamicDateRange.dateOffsetType).to.equal('3');

    DynamicDateRange.prototype.dateStep = undefined;
    DynamicDateRange.prototype.dateOffset = undefined;
    DynamicDateRange.prototype.dateOffsetType = undefined;
  });

  it('should construct a select config for choosing a date step', () => {
    expect(dynamicDateRange.dateStepSelectConfig).to.exist;
    expect(dynamicDateRange.dateStepSelectConfig.data).to.exist;
  });

  it('should construct a select config for selecting the offset type', () => {
    expect(dynamicDateRange.dateOffsetTypeSelectConfig).to.exist;
    expect(dynamicDateRange.dateOffsetTypeSelectConfig.data).to.exist;
  });

  it('should update the select configurations if config.disabled is changed', () => {
    dynamicDateRange.config.disabled = true;
    dynamicDateRange.$scope.$digest();
    expect(dynamicDateRange.dateStepSelectConfig.disabled).to.be.true;
    expect(dynamicDateRange.dateOffsetTypeSelectConfig.disabled).to.be.true
  });

  it('should decide if the current date step is today', () => {
    dynamicDateRange.dateStep = DEFAULT_DATE_STEP;
    expect(dynamicDateRange.isToday()).to.be.true;
  });

  it('should decide if the current date step is not today', () => {
    dynamicDateRange.dateStep = 'not today';
    expect(dynamicDateRange.isToday()).to.be.false;
  });

  it('should assign a model watcher for the date offset property', () => {
    dynamicDateRange.dateOffset = 9001;
    dynamicDateRange.$scope.$watch = sinon.spy();
    dynamicDateRange.assignDateOffsetWatcher();
    expect(dynamicDateRange.$scope.$watch.called).to.be.true;
    var watchFunction = dynamicDateRange.$scope.$watch.getCall(0).args[0];
    expect(watchFunction()).to.equal(9001);
  });

  it('should revert the offset value if it is not a number', () => {
    // Initial offset
    dynamicDateRange.dateOffset = 5;
    dynamicDateRange.$scope.$digest();
    // New incorrect offset
    dynamicDateRange.dateOffset = 'someoffset';
    dynamicDateRange.$scope.$digest();
    expect(dynamicDateRange.dateOffset).to.equal(5);
  });

  it('should revert the offset value to the default value if the previous is not a number too', () => {
    dynamicDateRange.dateOffset = 'someoffset';
    dynamicDateRange.$scope.$digest();
    expect(dynamicDateRange.dateOffset).to.equal(DEFAULT_DATE_OFFSET);
  });

  it('should not revert the offset value if it is a number', () => {
    // Initial offset
    dynamicDateRange.dateOffset = 5;
    dynamicDateRange.$scope.$digest();
    // New correct offset
    dynamicDateRange.dateOffset = 15;
    dynamicDateRange.$scope.$digest();
    expect(dynamicDateRange.dateOffset).to.equal(15);
  });

  it('should not revert if the new value is null(empty string)', () => {
    // Initial offset
    dynamicDateRange.dateOffset = 5;
    dynamicDateRange.$scope.$digest();
    // New correct & empty offset
    dynamicDateRange.dateOffset = null;
    dynamicDateRange.$scope.$digest();
    expect(dynamicDateRange.dateOffset).to.equal(null);
  });

  describe('buildDateRange', () => {

    it('should build empty date range for unknown date step', () => {
      var offsetConfig = getDateOffsetConfig('uknown-step');
      var range = DynamicDateRange.buildDateRange(offsetConfig);
      expect(range).to.deep.equal(['', '']);
    });

    it('should build date range for today', () => {
      var offsetConfig = getDateOffsetConfig('today');
      var range = DynamicDateRange.buildDateRange(offsetConfig);
      expect(range).to.exists;
      expect(range.length).to.equal(2);

      var now = moment();
      expect(now.isBetween(range[0], range[1], null, '[]')).to.be.true;
    });

    it('should build date range for next', () => {
      var offsetConfig = getDateOffsetConfig('next', '5', 'days');
      var range = DynamicDateRange.buildDateRange(offsetConfig);
      expect(range).to.exists;
      expect(range.length).to.equal(2);

      var start = moment(range[0]);
      var end = moment(range[1]);
      expect(start.isBefore(end)).to.be.true;

      var now = moment();
      expect(now.isBetween(start, end, null, '[)')).to.be.true;
    });

    it('should build date range for last', () => {
      var now = moment();

      var offsetConfig = getDateOffsetConfig('last', '5', 'days');
      var range = DynamicDateRange.buildDateRange(offsetConfig);
      expect(range).to.exists;
      expect(range.length).to.equal(2);

      var start = moment(range[0]);
      var end = moment(range[1]);
      expect(start.isBefore(end)).to.be.true;
      expect(now.isBetween(start, end, null, '(]')).to.be.true;
    });

    it('should build date range for after', () => {
      var offsetConfig = getDateOffsetConfig('after', '5', 'days');
      var range = DynamicDateRange.buildDateRange(offsetConfig);
      expect(range).to.exists;
      expect(range.length).to.equal(2);
      expect(range[1]).to.equal('');

      var start = moment(range[0]);
      var now = moment();
      expect(now.isBefore(start)).to.be.true;
    });

    it('should build date range for before', () => {
      var offsetConfig = getDateOffsetConfig('before', '5', 'days');
      var range = DynamicDateRange.buildDateRange(offsetConfig);
      expect(range).to.exists;
      expect(range.length).to.equal(2);
      expect(range[0]).to.equal('');

      var end = moment(range[1]);
      var now = moment();
      expect(now.isAfter(end)).to.be.true;
    });
  });

  function getDateOffsetConfig(dateStep, offset, offsetType) {
    return {
      dateStep: dateStep,
      offset: offset,
      offsetType: offsetType
    };
  }

  function mockTranslateService() {
    return {
      translateInstant: () => {
        return 'translated';
      }
    };
  }
});