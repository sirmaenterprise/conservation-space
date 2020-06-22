import {AdvancedSearchDateCriteria} from 'search/components/advanced/criteria/advanced-search-date-criteria';
import {AdvancedSearchCriteriaOperators} from 'search/components/advanced/criteria/advanced-search-criteria-operators';
import {Configuration} from 'common/application-config';
import 'moment';

import {AdvancedSearchMocks} from 'test/search/components/advanced/advanced-search-mocks'
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';
import {stub} from 'test/test-utils';

describe('AdvancedSearchDateCriteria', () => {

  var criteria;
  var advancedSearchDateCriteria;
  beforeEach(() => {
    advancedSearchDateCriteria = getComponentInstance();
    advancedSearchDateCriteria.ngOnInit();
  });

  function getComponentInstance() {
    var configurationStub = stub(Configuration);
    configurationStub.get.returns('config');

    var advancedSearchDateCriteria = new AdvancedSearchDateCriteria(mock$scope(), undefined, configurationStub);
    advancedSearchDateCriteria.criteria = AdvancedSearchMocks.getCriteria().rules[0];
    advancedSearchDateCriteria.$scope.$apply = sinon.spy();
    return advancedSearchDateCriteria;
  }

  it('should configure the component to be enabled by default', () => {
    expect(advancedSearchDateCriteria.config.disabled).to.be.false;
  });

  describe('assignValidModel()', () => {
    beforeEach(() => {
      advancedSearchDateCriteria = getComponentInstance();
    });

    it('should reset the criteria value if it is a string and the operator is "within"', () => {
      advancedSearchDateCriteria.criteria.operator = AdvancedSearchCriteriaOperators.IS_WITHIN.id;
      advancedSearchDateCriteria.criteria.value = '';
      advancedSearchDateCriteria.ngOnInit();
      expect(advancedSearchDateCriteria.criteria.value).to.deep.equal(['', '', '']);
    });

    it('should reset the criteria value if it is an array with incorrect size for is "within" operator', () => {
      advancedSearchDateCriteria.criteria.operator = AdvancedSearchCriteriaOperators.IS_WITHIN.id;
      advancedSearchDateCriteria.criteria.value = [''];
      advancedSearchDateCriteria.ngOnInit();
      expect(advancedSearchDateCriteria.criteria.value).to.deep.equal(['', '', '']);
    });

    it('should reset the criteria value if it is a string and the operator is for a date range', () => {
      advancedSearchDateCriteria.criteria.operator = AdvancedSearchCriteriaOperators.IS_BETWEEN.id;
      advancedSearchDateCriteria.criteria.value = '';
      advancedSearchDateCriteria.ngOnInit();
      expect(advancedSearchDateCriteria.criteria.value).to.deep.equal(['', '']);
    });

    it('should reset the criteria value if it is an array with incorrect size for a date range operator', () => {
      advancedSearchDateCriteria.criteria.operator = AdvancedSearchCriteriaOperators.IS_BETWEEN.id;
      advancedSearchDateCriteria.criteria.value = [''];
      advancedSearchDateCriteria.ngOnInit();
      expect(advancedSearchDateCriteria.criteria.value).to.deep.equal(['', '']);
    });
  });

  it('should reset the default value when switched from within to single picker', () => {
    advancedSearchDateCriteria.criteria.operator = 'within';
    advancedSearchDateCriteria.createPickerConfigurations();
    expect(advancedSearchDateCriteria.singlePickerConfig.defaultValue).to.equal('');
  });

  it('should reset the default value when switched from isBefore to isAfter', () => {
    advancedSearchDateCriteria.criteria.operator = 'before';
    advancedSearchDateCriteria.resetModelValue();
    expect(advancedSearchDateCriteria.singlePickerConfig.defaultValue).to.equal('');
  });

  it('should register a watcher for the criteria operator', () => {
    advancedSearchDateCriteria.$scope.$watch = sinon.spy();
    advancedSearchDateCriteria.registerOperatorWatcher();
    expect(advancedSearchDateCriteria.$scope.$watch.called).to.be.true;
  });

  it('should reset model value on operator change', () => {
    advancedSearchDateCriteria.resetModelValue = sinon.spy();
    advancedSearchDateCriteria.criteria.operator = 'new-operator';
    advancedSearchDateCriteria.$scope.$digest();
    expect(advancedSearchDateCriteria.resetModelValue.called).to.be.true;
  });

  it('should reset model value to an array', () => {
    advancedSearchDateCriteria.criteria.value = undefined;
    advancedSearchDateCriteria.criteria.operator = AdvancedSearchCriteriaOperators.IS_BETWEEN.id;
    advancedSearchDateCriteria.resetModelValue();
    expect(advancedSearchDateCriteria.criteria.value).to.deep.equal(['', '']);
  });

  it('should reset model value to a string', () => {
    advancedSearchDateCriteria.criteria.value = undefined;
    advancedSearchDateCriteria.criteria.operator = AdvancedSearchCriteriaOperators.IS_AFTER.id;
    advancedSearchDateCriteria.resetModelValue();
    expect(advancedSearchDateCriteria.criteria.value).to.equal('');
  });

  it('should reset model value to an array of 3 elements', () => {
    advancedSearchDateCriteria.criteria.value = undefined;
    advancedSearchDateCriteria.criteria.operator = AdvancedSearchCriteriaOperators.IS_WITHIN.id;
    advancedSearchDateCriteria.resetModelValue();
    expect(advancedSearchDateCriteria.criteria.value).to.deep.equal(['', '', '']);
  });

  it('should construct single date picker configuration', () => {
    expect(advancedSearchDateCriteria.singlePickerConfig).to.exist;
  });

  it('should set default single date picker value', () => {
    advancedSearchDateCriteria.criteria.operator = AdvancedSearchCriteriaOperators.IS_AFTER.id;
    advancedSearchDateCriteria.criteria.value = 'some-date';
    advancedSearchDateCriteria.createPickerConfigurations();
    expect(advancedSearchDateCriteria.singlePickerConfig.defaultValue).to.equal('some-date');
  });

  it('should construct single day range date picker configuration', () => {
    expect(advancedSearchDateCriteria.singleDayRangePicker).to.exist;
    expect(advancedSearchDateCriteria.singleDayRangePicker.hideTime).to.be.true;
    expect(advancedSearchDateCriteria.singleDayRangePicker.listeners).to.exist;
  });

  it('should set default single day range date picker value', () => {
    advancedSearchDateCriteria.criteria.operator = AdvancedSearchCriteriaOperators.IS.id;
    advancedSearchDateCriteria.criteria.value = ['some-date'];
    advancedSearchDateCriteria.createPickerConfigurations();
    expect(advancedSearchDateCriteria.singleDayRangePicker.defaultValue).to.equal('some-date');
  });

  it('should set date range criteria on single day range date change', () => {
    var date = moment(new Date(2016, 5, 30, 11));
    var listener = advancedSearchDateCriteria.singleDayRangePicker.listeners['dp.change'][0];
    expect(listener).to.exist;

    listener({date: date});

    var value = advancedSearchDateCriteria.criteria.value;
    expect(value).to.exist;
    expect(value.length).to.equal(2);
    expect(moment(value[0], moment.ISO_8601, true).isValid()).to.be.true;
    expect(moment(value[1], moment.ISO_8601, true).isValid()).to.be.true;
  });

  it('should construct linked date pickers configurations', () => {
    expect(advancedSearchDateCriteria.linkedPickersConfig).to.exist;
    expect(advancedSearchDateCriteria.linkedPickersConfig.from).to.exist;
    expect(advancedSearchDateCriteria.linkedPickersConfig.from.useCurrent).to.be.false;
    expect(advancedSearchDateCriteria.linkedPickersConfig.to).to.exist;
    expect(advancedSearchDateCriteria.linkedPickersConfig.to.useCurrent).to.be.false;
  });

  it('should set default values for linked pickers', () => {
    advancedSearchDateCriteria.criteria.operator = AdvancedSearchCriteriaOperators.IS_BETWEEN.id;
    advancedSearchDateCriteria.criteria.value = ['some-date', 'another-date'];
    advancedSearchDateCriteria.createPickerConfigurations();
    expect(advancedSearchDateCriteria.linkedPickersConfig.from.defaultValue).to.equal('some-date');
    expect(advancedSearchDateCriteria.linkedPickersConfig.to.defaultValue).to.equal('another-date');
  });

  it('should set min date for linked pickers', () => {
    var minDateSpy = sinon.spy();
    var maxDateSpy = sinon.spy();
    advancedSearchDateCriteria.$element = mockElement(minDateSpy, maxDateSpy);

    var testObject = {};
    var listener = advancedSearchDateCriteria.linkedPickersConfig.from.listeners['dp.change'][0];
    listener({date: testObject});

    expect(maxDateSpy.called).to.be.false;
    expect(minDateSpy.called).to.be.true;
    expect(minDateSpy.getCall(0).args[0]).to.equal(testObject);
  });

  it('should set max date for linked pickers', () => {
    var minDateSpy = sinon.spy();
    var maxDateSpy = sinon.spy();
    advancedSearchDateCriteria.$element = mockElement(minDateSpy, maxDateSpy);

    var testObject = {};
    var listener = advancedSearchDateCriteria.linkedPickersConfig.to.listeners['dp.change'][0];
    listener({date: testObject});

    expect(minDateSpy.called).to.be.false;
    expect(maxDateSpy.called).to.be.true;
    expect(maxDateSpy.getCall(0).args[0]).to.equal(testObject);
  });

  it('should assign date and time formats to the picker configurations', () => {
    expect(advancedSearchDateCriteria.singlePickerConfig.dateFormat).to.equal('config');
    expect(advancedSearchDateCriteria.singlePickerConfig.timeFormat).to.equal('config');

    expect(advancedSearchDateCriteria.singleDayRangePicker.dateFormat).to.equal('config');
    expect(advancedSearchDateCriteria.singleDayRangePicker.timeFormat).to.not.exist;

    expect(advancedSearchDateCriteria.linkedPickersConfig.from.dateFormat).to.equal('config');
    expect(advancedSearchDateCriteria.linkedPickersConfig.from.timeFormat).to.equal('config');
    expect(advancedSearchDateCriteria.linkedPickersConfig.to.dateFormat).to.equal('config');
    expect(advancedSearchDateCriteria.linkedPickersConfig.to.timeFormat).to.equal('config');
  });

  it('should configure the date pickers with a function which updates their disabled state', () => {
    advancedSearchDateCriteria.config.disabled = true;
    expect(advancedSearchDateCriteria.singlePickerConfig.isDisabled()).to.be.true;
    expect(advancedSearchDateCriteria.singleDayRangePicker.isDisabled()).to.be.true;
    expect(advancedSearchDateCriteria.linkedPickersConfig.from.isDisabled()).to.be.true;
    expect(advancedSearchDateCriteria.linkedPickersConfig.to.isDisabled()).to.be.true;
  });

  function mockElement(minDateSpy, maxDateSpy) {
    return {
      find: () => {
        return {
          data: () => {
            return {
              minDate: minDateSpy,
              maxDate: maxDateSpy
            };
          }
        };
      }
    };
  }

});