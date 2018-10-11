import {Datetime} from 'form-builder/datetime/datetime';
import {MomentAdapter} from 'adapters/moment-adapter';
import {DefinitionModelProperty} from 'models/definition-model';
import {InstanceModel} from 'models/instance-model';
import {EventEmitter} from 'common/event-emitter';
import {stub} from 'test-utils';
import {mockFormWrapper} from 'test/form-builder/form-wrapper-mock';

describe('Datetime', () => {

  let configService = {
    get: (key) => {
      return configService[key];
    },
    'ui.date.format': 'MMMM/DD/YYYY',
    'ui.time.format': 'HH:mm'
  };

  let $scope = {
    '$watch': sinon.stub()
  };

  let fakeElement = {
    find: ()=> {
      return {
        addClass: ()=> {
        },
        removeClass: ()=> {
        },
        attr: () => {
        },
        on: () => {
        },
        find: () => {
          return {
            on: () => {
            }
          };
        }
      };
    },
    addClass: ()=> {
    },
    removeClass: ()=> {
    },
    attr: () => {
    }
  };

  describe('on init', () => {
    Datetime.prototype.formWrapper =  mockFormWrapper()
      .setFieldsMap({
        field1: {
          identifier: 'field1',
          dataType: 'date'
        }
      })
      .setValidationModel({
        field1: {
          value: new Date('2015/12/22').toISOString()
        }
      })
      .get();

    Datetime.prototype.identifier = 'field1';

    it('should assemble date format pattern if dataType=date', () => {
      Datetime.prototype.formWrapper.fieldsMap['field1'].dataType = 'date';
      let datetime = new Datetime(configService, $scope, new MomentAdapter());
      expect(datetime.pattern).to.equal('MMMM/DD/YYYY');
    });

    it('should assemble datetime format pattern if dataType=datetime', () => {
      Datetime.prototype.formWrapper.fieldsMap['field1'].dataType = 'datetime';
      let datetime = new Datetime(configService, $scope, new MomentAdapter());
      expect(datetime.pattern).to.equal('MMMM/DD/YYYY HH:mm');
    });
  });

  describe('event emitter for disabled attribute', () => {
    it('should toggle config.disabled parameter passed to datepicker', () => {
      //field view model is wrapped as an emmitable object , so when a property changes, it emits an event.
      Datetime.prototype.formWrapper =  mockFormWrapper()
        .setFieldsMap({
          field1: new DefinitionModelProperty({
            identifier: 'field1',
            dataType: 'datetime',
            disabled: false
          })
        })
        .setValidationModel(new InstanceModel({
          field1: {
            value: new Date('2015/12/22').toISOString()
          }
        }))
        .setConfig({
          formViewMode: {}
        })
        .get();

      Datetime.prototype.identifier = 'field1';

      let datetime = new Datetime(configService, {}, new MomentAdapter(), fakeElement);
      //init the datetime
      datetime.ngOnInit();
      expect(datetime.config.disabled).to.be.false;
      datetime.fieldViewModel.disabled = true;
      expect(datetime.config.disabled).to.be.true;
    });
  });

  it('getFormattedDate() should format value using configured pattern', () => {
    Datetime.prototype.formWrapper =  mockFormWrapper()
      .setFieldsMap({
        'field1': {
          identifier: 'field1',
          dataType: 'datetime'
        }
      })
      .setValidationModel({
        field1: {
          value: new Date('2015/12/22').toISOString()
        }
      })
      .get();

    Datetime.prototype.identifier = 'field1';

    let datetime = new Datetime(configService, $scope, new MomentAdapter());
    let formatted = datetime.getFormattedDate();
    expect(formatted).to.equal('December/22/2015 00:00');
  });

  it('#setDisabled should set in config disabled=true if view model has disabled=true', () => {
    Datetime.prototype.formWrapper =  mockFormWrapper()
      .setFieldsMap({
        'field1': {
          identifier: 'field1',
          disabled: true
        }
      })
      .setValidationModel({
        field1: {
          value: null
        }
      })
      .get();

    Datetime.prototype.identifier = 'field1';

    let datetime = new Datetime(configService, $scope);
    datetime.setDisabled();
    expect(datetime.config.disabled).to.be.true;
  });

  it('#getFieldConfig should populate field\' config with proper values', () => {
    Datetime.prototype.formWrapper =  mockFormWrapper()
      .setFieldsMap({
        'field1': {
          identifier: 'field1',
          disabled: false,
          dataType: 'date'
        }
      })
      .setValidationModel({
        field1: {
          value: 123
        }
      })
      .get();

    Datetime.prototype.identifier = 'field1';

    let datetime = new Datetime(configService, $scope);
    let config = datetime.getFieldConfig();
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

  describe('#ngAfterViewInit', () => {
    it('should not emit event by default to formWrapper', () => {
      let datetime = new Datetime(configService, $scope);
      datetime.formEventEmitter = stub(EventEmitter);
      datetime.ngAfterViewInit();
      expect(datetime.formEventEmitter.publish.calledOnce).to.be.true;
    });
  });
});
