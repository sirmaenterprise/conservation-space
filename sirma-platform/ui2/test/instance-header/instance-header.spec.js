import {InstanceHeader} from 'instance-header/instance-header';
import {HEADER_DEFAULT} from 'instance-header/header-constants';
import {InstanceObject} from 'models/instance-object';
import {Configuration} from 'common/application-config';
import {MomentAdapter} from 'adapters/moment-adapter';
import {MockEventbus} from 'test/test-utils';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';
import {PromiseStub} from 'test/promise-stub';

describe('InstanceHeader', () => {

  let instanceHeader;
  let currentObject;
  let scopeMock;
  let compileMock;

  beforeEach(() => {
    scopeMock = mock$scope();
    compileMock = mock$compile();

    let appendSpy = sinon.spy();
    let element = getElementMock(appendSpy);
    let configuration = new Map();
    configuration.set(Configuration.UI_DATE_FORMAT, 'DD.MM.YY');
    configuration.set(Configuration.UI_TIME_FORMAT, 'HH.mm');

    instanceHeader = new InstanceHeader(scopeMock, element, configuration, new MomentAdapter(), new MockEventbus(), compileMock);

    currentObject = new InstanceObject('id', {validationModel: {}, viewModel: {fields: []}});
    currentObject.instanceType = 'documentinstance';

    instanceHeader.context = {
      getCurrentObject: () => {
        return PromiseStub.resolve(currentObject);
      }
    };
    instanceHeader.headerType = HEADER_DEFAULT;

    instanceHeader.ngOnInit();
  });

  describe('on init', () => {
    it('should calculate icon size', () => {
      expect(instanceHeader.iconSize).to.equal(64);
    });
  });

  describe('getPropertyDisplayValue', () => {
    it('should return object headers for object properties', () => {
      let propertyName = 'objectProperty';
      currentObject.models.validationModel[propertyName] = {
        value: {
          results: ['emf:1', 'emf:2', 'emf:3']
        }
      };
      instanceHeader.flatViewModel = new Map();
      instanceHeader.flatViewModel.set(propertyName, {isDataProperty: false});
      instanceHeader.headerBoundProperties[propertyName] = {
        instanceSelectorTemplate: '<seip-instance-selector></seip-instance-selector>'
      };

      let value = instanceHeader.getPropertyDisplayValue(currentObject, propertyName);
      expect(value).to.equal('<seip-instance-selector></seip-instance-selector>');
    });

    it('should return valueLabel if such exists', () => {
      let propertyName = 'dataProperty';
      currentObject.models.validationModel[propertyName] = {
        valueLabel: 'value label',
        value: 'raw value'
      };
      instanceHeader.flatViewModel = new Map();
      instanceHeader.flatViewModel.set(propertyName, {isDataProperty: true});

      let value = instanceHeader.getPropertyDisplayValue(currentObject, propertyName);
      expect(value).to.equal('value label');
    });

    it('should return value.label if such exists', () => {
      let propertyName = 'dataProperty';
      currentObject.models.validationModel[propertyName] = {
        value: {
          label: 'value label',
          id: 'raw value'
        }
      };
      instanceHeader.flatViewModel = new Map();
      instanceHeader.flatViewModel.set(propertyName, {isDataProperty: true});

      let value = instanceHeader.getPropertyDisplayValue(currentObject, propertyName);
      expect(value).to.equal('value label');
    });

    it('should return value as is if there is no label', () => {
      let propertyName = 'dataProperty';
      currentObject.models.validationModel[propertyName] = {
        value: 'raw value'
      };
      instanceHeader.flatViewModel = new Map();
      instanceHeader.flatViewModel.set(propertyName, {isDataProperty: true});

      let value = instanceHeader.getPropertyDisplayValue(currentObject, propertyName);
      expect(value).to.equal('raw value');
    });

    it('should return formatted date time if property is of type dateTime', () => {
      let propertyName = 'dateTimeProperty';
      let testDate = new Date('2015/12/22').toISOString();
      currentObject.models.validationModel[propertyName] = {
        value: testDate
      };
      instanceHeader.flatViewModel = new Map();
      instanceHeader.flatViewModel.set(propertyName, {isDataProperty: true, dataType: 'datetime'});

      let value = instanceHeader.getPropertyDisplayValue(currentObject, propertyName, 'DD.MM.YY HH.mm');
      expect(value).to.equal('22.12.15 00.00');
    });

    it('should return formatted date if property is of type date', () => {
      let propertyName = 'dateProperty';
      let testDate = new Date('2015/12/22').toISOString();
      currentObject.models.validationModel[propertyName] = {
        value: testDate
      };
      instanceHeader.flatViewModel = new Map();
      instanceHeader.flatViewModel.set(propertyName, {isDataProperty: true, dataType: 'date'});

      let value = instanceHeader.getPropertyDisplayValue(currentObject, propertyName, 'DD.MM.YY');
      expect(value).to.equal('22.12.15');
    });
  });

  it('formatDatetime should return formatted date string', () => {
    expect(instanceHeader.formatDatetime(new Date('2015/12/22'), true, 'DD.MM.YY HH.mm')).to.equals('22.12.15 00.00');
    expect(instanceHeader.formatDatetime(new Date('2015/12/22'), false, 'DD.MM.YY')).to.equals('22.12.15');
  });
});

function getElementMock(appendSpy) {
  let elementMock = {};
  elementMock.find = () => {
    return elementMock;
  };
  elementMock.empty = () => {
    return elementMock;
  };
  elementMock.append = appendSpy;
  return elementMock;
}

function mock$compile() {
  return (arg) => {
    let compileString = arg;
    return () => {
      return compileString;
    };
  };
}