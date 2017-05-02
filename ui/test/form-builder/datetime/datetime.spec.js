import {Datetime} from 'form-builder/datetime/datetime';
import {DatetimePicker} from 'components/datetimepicker/datetimepicker';
import {MomentAdapter} from 'adapters/moment-adapter';
import {DefinitionModelProperty} from 'models/definition-model';
import {InstanceModel} from 'models/instance-model';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';

describe('Datetime', () => {

  var configService = {
    get: (key) => {
      return configService[key];
    },
    'ui.date.format': 'MMMM/DD/YYYY',
    'ui.time.format': 'HH:mm'
  };

  var $scope = {
    '$watch': sinon.stub()
  };

  describe('on init', () => {
    Datetime.prototype.fieldViewModel = {
      identifier: 'field1',
      dataType: 'date'
    };
    Datetime.prototype.validationModel = {
      field1: {
        value: new Date('2015/12/22').toISOString()
      }
    };

    it('should assemble date format pattern if dataType=date', () => {
      Datetime.prototype.fieldViewModel.dataType = 'date';
      var datetime = new Datetime(configService, $scope, new MomentAdapter());
      expect(datetime.pattern).to.equal('MMMM/DD/YYYY');
    });

    it('should assemble datetime format pattern if dataType=datetime', () => {
      Datetime.prototype.fieldViewModel.dataType = 'datetime';
      var datetime = new Datetime(configService, $scope, new MomentAdapter());
      expect(datetime.pattern).to.equal('MMMM/DD/YYYY HH:mm');
    });
  });

  describe('event emitter for disabled attribute', () => {
    it('should toggle config.disabled parameter passed to datepicker', () => {
      //field view model is wrapped as an emmitable object , so when a property changes, it emits an event.
      let viewModel = {
        identifier: 'field1',
        dataType: 'datetime',
        disabled: false
      };
      Datetime.prototype.widgetConfig = {formViewMode: {}};
      //fieldViewModel is passed as an instance of DefinitionModelProperty
      Datetime.prototype.fieldViewModel = new DefinitionModelProperty(viewModel);
      //validationModel is passed as an instance of InstanceModel
      Datetime.prototype.validationModel = new InstanceModel({
        field1: {
          value: new Date('2015/12/22').toISOString()
        }
      });
      let fakeElement = {
        find: (elem)=> {
          return {
            addClass: ()=> {},
            removeClass: ()=> {},
            attr: () => {}
          }
        },
        addClass: ()=> {},
        removeClass: ()=> {},
        attr: () => {}
      };
      var datetime = new Datetime(configService, {}, new MomentAdapter(), fakeElement);
      //init the datetime
      datetime.ngOnInit();
      expect(datetime.config.disabled).to.be.false;
      datetime.fieldViewModel.disabled = true;
      expect(datetime.config.disabled).to.be.true;
    });
  });

  it('getFormattedDate() should format value using configured pattern', () => {
    Datetime.prototype.fieldViewModel = {
      identifier: 'field1',
      dataType: 'datetime'
    };
    Datetime.prototype.validationModel = {
      field1: {
        value: new Date('2015/12/22').toISOString()
      }
    };
    var datetime = new Datetime(configService, $scope, new MomentAdapter());
    var formatted = datetime.getFormattedDate();
    expect(formatted).to.equal('December/22/2015 00:00');
  });

  it('#setDisabled should set in config disabled=true if view model has disabled=true', () => {
    Datetime.prototype.fieldViewModel = {
      identifier: 'field1',
      disabled: true
    };
    Datetime.prototype.validationModel = {
      field1: {
        value: null
      }
    };
    var datetime = new Datetime(configService, $scope);
    datetime.setDisabled();
    expect(datetime.config.disabled).to.be.true;
  });

  it('#getFieldConfig should populate field\' config with proper values', () => {
    Datetime.prototype.fieldViewModel = {
      identifier: 'field1',
      disabled: false,
      dataType: 'date'
    };
    Datetime.prototype.validationModel = {
      field1: {
        value: 123
      }
    };
    var datetime = new Datetime(configService, $scope);
    var config = datetime.getFieldConfig();
    expect(config).to.deep.equal({
      defaultValue: 123,
      dateFormat: 'MMMM/DD/YYYY',
      timeFormat: 'HH:mm',
      hideTime: true,
      cssClass: 'form-field datetime-field',
      disabled: false,
      widgetParent: 'body'
    });
  });

});
