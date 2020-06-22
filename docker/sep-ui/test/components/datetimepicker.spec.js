import {DatetimePicker} from 'components/datetimepicker/datetimepicker';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';

var sandbox;
beforeEach(function () {
  sandbox = sinon.sandbox.create();
});

afterEach(function () {
  sandbox.restore();
});

describe('Tests for DatetimePicker component', () => {
  it('Test that default configuration is applied', () => {
    sandbox.stub(DatetimePicker.prototype, 'bindToModel', () => {
    });
    let datetimePicker = instantiateDatetimePicker();
    expect(datetimePicker.config).to.have.property('dateFormat', 'MMMM/DD/YYYY');
    expect(datetimePicker.config).to.have.property('timeFormat', 'HH:mm');
    expect(datetimePicker.config).to.have.property('hideDate', false);
    expect(datetimePicker.config).to.have.property('hideTime', false);
    expect(datetimePicker.config).to.have.property('dateTimeSeparator', ' ');
    expect(datetimePicker.config).to.have.property('showTodayButton', true);
    expect(datetimePicker.config).to.have.property('showClear', true);
  });

  it('Test that default configuration does not override user configuration', () => {
    DatetimePicker.prototype.config = {
      'dateFormat': 'DD/MM/YY',
      'hideDate': true
    };
    sandbox.stub(DatetimePicker.prototype, 'bindToModel', () => {
    });
    let datetimePicker = instantiateDatetimePicker();
    expect(datetimePicker.config).to.have.property('dateFormat', 'DD/MM/YY');
    expect(datetimePicker.config).to.have.property('hideDate', true);
    // Check that at least one default property is applied
    expect(datetimePicker.config).to.have.property('showTodayButton', true);
  });

  it('Test that createActualConfig() creates proper object', () => {
    DatetimePicker.prototype.config = {
      'defaultValue': '1995-12-17T03:24:00'
    };
    sandbox.stub(DatetimePicker.prototype, 'bindToModel', () => {
    });
    let datetimePicker = instantiateDatetimePicker();
    expect(datetimePicker.actualConfig.defaultDate.toISOString()).to.equals(new Date('1995-12-17T03:24:00').toISOString());
    expect(datetimePicker.actualConfig).to.have.property('format', 'MMMM/DD/YYYY HH:mm');
    expect(datetimePicker.actualConfig).to.have.property('showTodayButton', true);
    expect(datetimePicker.actualConfig).to.have.property('showClear', true);
  });

  it('Test that buildDateTimeFormat() creates correct time only format', () => {
    DatetimePicker.prototype.config = {
      'hideDate': true
    };
    sandbox.stub(DatetimePicker.prototype, 'bindToModel', () => {
    });
    let datetimePicker = instantiateDatetimePicker();
    expect(datetimePicker.buildDateTimeFormat(datetimePicker.config)).to.equals('HH:mm');
  });

  it('Test that buildFormatPlaceholder() creates correct placeholder', () => {
    DatetimePicker.prototype.config = {
      'hideDate': true
    };
    sandbox.stub(DatetimePicker.prototype, 'bindToModel', () => {
    });
    let datetimePicker = instantiateDatetimePicker();

    // when no placeholder is specified the format string should not be indented
    expect(datetimePicker.buildFormatPlaceholder('dd/mm/yy hh:mm')).to.equals('dd/mm/yy hh:mm');

    // when a placeholder is specified an indent should be appended to the front of the format string
    expect(datetimePicker.buildFormatPlaceholder('dd/mm/yy hh:mm', 'Select a date')).to.equals(' (dd/mm/yy hh:mm)');
  });

  it('Test that buildDateTimeFormat() creates correct full format', () => {
    DatetimePicker.prototype.config = {
      'dateTimeSeparator': ' | '
    };
    sandbox.stub(DatetimePicker.prototype, 'bindToModel', () => {
    });
    let datetimePicker = instantiateDatetimePicker();
    expect(datetimePicker.buildDateTimeFormat(datetimePicker.config)).to.equals('MMMM/DD/YYYY | HH:mm');
  });

  it('Test that setModelValue() properly updates underlying model', () => {
    sandbox.stub(DatetimePicker.prototype, 'bindToModel', () => {
    });
    let datetimePicker = instantiateDatetimePicker();
    datetimePicker.ngModel.$setViewValue = sinon.spy();
    datetimePicker.onChanged = sinon.spy();
    datetimePicker.setModelValue('new value');
    expect(datetimePicker.ngModel.$setViewValue.calledOnce);
    expect(datetimePicker.ngModel.$setViewValue.getCall(0).args[0]).to.equal('new value');
    expect(datetimePicker.onChanged.calledOnce);
    expect(datetimePicker.onChanged.getCall(0).args[0]).to.eql({newValue: 'new value'});
  });

  it('should calculate correct picker position when element is placed on bottom', () => {
    let element = {
      offset: function () {
        return {
          left: 50,
          top: 20
        }
      },
      outerHeight: function () {
        return 2
      }
    };
    let stubCalculatePickerPosition = sinon.stub(DatetimePicker, 'calculatePickerPosition');
    let jQueryStub = sinon.stub($.fn, 'find');
    jQueryStub.withArgs('.bootstrap-datetimepicker-widget:last').returns({
      hasClass: function (cls) {
        return cls === 'bottom'
      },
      css: function () {
      }
    });
    DatetimePicker.updatePickerPosition('body', element);
    expect(stubCalculatePickerPosition.calledOnce).to.be.true;
    expect(stubCalculatePickerPosition.args[0][2]).to.equal(22);
    expect(stubCalculatePickerPosition.args[0][1].offset().left).to.equal(50);
    expect(stubCalculatePickerPosition.args[0][1].offset().top).to.equal(20);
    jQueryStub.restore();
    stubCalculatePickerPosition.restore();
  });

  it('should calculate correct picker position when element is placed on top', () => {
    let element = {
      offset: function () {
        return {
          left: 50,
          top: 20
        }
      }
    };
    let stubCalculatePickerPosition = sinon.stub(DatetimePicker, 'calculatePickerPosition');
    let jQueryStub = sinon.stub($.fn, 'find');
    jQueryStub.withArgs('.bootstrap-datetimepicker-widget:last').returns({
      hasClass: function (cls) {
        return cls === 'top'
      },
      css: function () {
      },
      outerHeight: function () {
        return 2
      }
    });
    DatetimePicker.updatePickerPosition('body', element);
    expect(stubCalculatePickerPosition.calledOnce).to.be.true;
    expect(stubCalculatePickerPosition.args[0][2]).to.equal(18);
    expect(stubCalculatePickerPosition.args[0][1].offset().left).to.equal(50);
    expect(stubCalculatePickerPosition.args[0][1].offset().top).to.equal(20);
    expect(stubCalculatePickerPosition.args[0][0].outerHeight()).to.equal(2);
    jQueryStub.restore();
    stubCalculatePickerPosition.restore();
  });

  it('should not call calculatePickerPosition() if bottom or top class is missing', () => {
    let stubCalculatePickerPosition = sinon.stub(DatetimePicker, 'calculatePickerPosition');
    let jQueryStub = sinon.stub($.fn, 'find');
    jQueryStub.withArgs('.bootstrap-datetimepicker-widget:last').returns({
      hasClass: function (cls) {
        return false
      }
    });
    DatetimePicker.updatePickerPosition('body', {});
    expect(stubCalculatePickerPosition.calledOnce).to.be.false;
    jQueryStub.restore();
    stubCalculatePickerPosition.restore();
  });

  it('should calculate new picker position', () => {
    let element = {
      offset: function () {
        return {left: 50}
      }
    };
    let datepicker = {
      css: function (obj) {
        datepicker.top = obj.top;
        datepicker.bottom = obj.bottom;
        datepicker.left = obj.left;
      },
      outerWidth: () => {
        return 100;
      }
    };
    DatetimePicker.calculatePickerPosition(datepicker, element, 20);
    expect(datepicker.top).to.equal('20px');
    expect(datepicker.bottom).to.equal('auto');
    expect(datepicker.left).to.equal('50px');
  });

  it('should position picker on the left if it goes out of the screen', () => {
    let element = {
      offset: () => {
        return {left: 100};
      },
      width: () => 30
    };
    let datepicker = {
      css: (obj) => {
        datepicker.top = obj.top;
        datepicker.bottom = obj.bottom;
        datepicker.left = obj.left;
      },
      outerWidth: () => {
        return 100;
      }
    };

    let getWindowWidthStub = sinon.stub(DatetimePicker, 'getWindowWidth', () => {
      return 170;
    });

    DatetimePicker.calculatePickerPosition(datepicker, element, 20);
    expect(datepicker.top).to.equal('20px');
    expect(datepicker.bottom).to.equal('auto');
    expect(datepicker.left).to.equal('30px');

    getWindowWidthStub.restore();
  });

  it('should correctly update date value when its changed', () => {
    let date = sinon.spy();
    let data = sinon.spy(() => {
      return {
        date: date
      }
    });
    let element = {
      controller: () => {
        return {
          $setViewValue: () => {
          },
          $viewValue: '1999-12-17T03:24:00'
        };
      },
      find: () => {
        return {
          datetimepicker: () => {
          },
          on: () => {
          },
          data: data
        }
      },
      on: () => {
      }
    };
    let scope = mock$scope();
    let timeout = (f) => {
      f()
    };

    let datetimePicker = new DatetimePicker(scope, timeout, element);
    datetimePicker.ngModel.$viewValue = '2017-12-17T03:24:00';
    scope.$digest();
    expect(date.calledOnce).to.be.true;
    expect(date.getCall(0).args[0]).to.be.instanceof(Date);
    expect(date.getCall(0).args[0].getFullYear()).to.equal(2017);

    datetimePicker.ngModel.$viewValue = undefined;
    scope.$digest();
    expect(date.getCall(1).args[0]).to.be.null;

    datetimePicker.ngModel.$viewValue = '';
    scope.$digest();
    expect(date.getCall(2).args[0]).to.be.null;
  });

  it('should use the configuration property for disabled state', () => {
    sandbox.stub(DatetimePicker.prototype, 'bindToModel', () => {
    });
    let datetimePicker = instantiateDatetimePicker();
    datetimePicker.config.disabled = true;
    expect(datetimePicker.isDisabled()).to.be.true;
  });

  it('should use the configuration function for disabled state', () => {
    sandbox.stub(DatetimePicker.prototype, 'bindToModel', () => {
    });
    let datetimePicker = instantiateDatetimePicker();
    datetimePicker.config.disabled = false;
    datetimePicker.config.isDisabled = sinon.spy(() => true);
    expect(datetimePicker.isDisabled()).to.be.true;
    expect(datetimePicker.config.isDisabled.calledOnce).to.be.true;
  });

  it('should consider undefined as false when config.isDisabled() is misconfigured', () => {
    sandbox.stub(DatetimePicker.prototype, 'bindToModel', () => {
    });
    let datetimePicker = instantiateDatetimePicker();
    datetimePicker.config.disabled = true;
    datetimePicker.config.isDisabled = sinon.spy(() => {
    });
    expect(datetimePicker.isDisabled()).to.be.false;
  });

  function instantiateDatetimePicker() {
    return new DatetimePicker(undefined, undefined, mockElement());
  }

  function mockElement() {
    return {
      controller: () => {
        return {
          $setViewValue: () => {
          }
        };
      },
      find: () => {
        return {
          datetimepicker: () => {
          }
        };
      },
      on: () => {
      }
    };
  }
});
