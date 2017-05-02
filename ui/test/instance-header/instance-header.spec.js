import {InstanceHeader} from 'instance-header/instance-header';
import {HEADER_DEFAULT, HEADER_BREADCRUMB} from 'instance-header/header-constants';
import {InstanceObject} from 'idoc/idoc-context';
import {IconsService} from 'services/icons/icons-service';
import {Configuration} from 'common/application-config';
import {MomentAdapter} from 'adapters/moment-adapter';

import {mock$scope} from '../idoc/widget/object-data-widget/scope.mock';
import {PromiseStub} from 'test/promise-stub';

describe('InstanceHeader', () => {

  describe('getPropertyDisplayValue', () => {
    let instanceHeader;
    let currentObject;
    beforeEach(() => {
      currentObject = new InstanceObject('id', {validationModel: {}, viewModel: {fields: []}});
      currentObject.instanceType = 'documentinstance';
      InstanceHeader.prototype.context = {
        getCurrentObject: () => {
          return PromiseStub.resolve(currentObject)
        }
      };
      InstanceHeader.prototype.headerType = HEADER_DEFAULT;
      let appendSpy = sinon.spy();
      let element = getElementMock(appendSpy);
      let configuration = new Map();
      configuration.set(Configuration.UI_DATE_FORMAT, 'DD.MM.YY');
      configuration.set(Configuration.UI_TIME_FORMAT, 'HH.mm');
      instanceHeader = new InstanceHeader(mock$scope(), undefined, element, configuration, new MomentAdapter());
    });

    it('should return object headers for object properties', () => {
      let propertyName = 'objectProperty';
      currentObject.models.validationModel[propertyName] = {
        value: [{
          headers: {
            [HEADER_BREADCRUMB]: 'breadcrumbHeader1'
          }
        }, {
          headers: {
            [HEADER_BREADCRUMB]: 'breadcrumbHeader2'
          }
        }]
      };
      instanceHeader.flatViewModel = new Map();
      instanceHeader.flatViewModel.set(propertyName, {isDataProperty: false});
      let result = instanceHeader.getPropertyDisplayValue(currentObject, propertyName);
      expect(result).to.equals('breadcrumbHeader1, breadcrumbHeader2');
    });

    it('should return valueLabel if such exists', () => {
      let propertyName = 'dataProperty';
      currentObject.models.validationModel[propertyName] = {
        valueLabel: 'value label',
        value: 'raw value'
      };
      instanceHeader.flatViewModel = new Map();
      instanceHeader.flatViewModel.set(propertyName, {isDataProperty: true});
      let result = instanceHeader.getPropertyDisplayValue(currentObject, propertyName);
      expect(result).to.equals('value label');
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
      let result = instanceHeader.getPropertyDisplayValue(currentObject, propertyName);
      expect(result).to.equals('value label');
    });

    it('should return value as is if there is no label', () => {
      let propertyName = 'dataProperty';
      currentObject.models.validationModel[propertyName] = {
        value: 'raw value'
      };
      instanceHeader.flatViewModel = new Map();
      instanceHeader.flatViewModel.set(propertyName, {isDataProperty: true});
      let result = instanceHeader.getPropertyDisplayValue(currentObject, propertyName);
      expect(result).to.equals('raw value');
    });

    it('should return formatted date time if property is of type dateTime', () => {
      let propertyName = 'dateTimeProperty';
      let testDate = new Date('2015/12/22').toISOString();
      currentObject.models.validationModel[propertyName] = {
        value: testDate
      };
      instanceHeader.flatViewModel = new Map();
      instanceHeader.flatViewModel.set(propertyName, {isDataProperty: true, dataType: 'datetime'});
      let result = instanceHeader.getPropertyDisplayValue(currentObject, propertyName);
      expect(result).to.equals('22.12.15 00.00');
    });

    it('should return formatted date if property is of type date', () => {
      let propertyName = 'dateProperty';
      let testDate = new Date('2015/12/22').toISOString();
      currentObject.models.validationModel[propertyName] = {
        value: testDate
      };
      instanceHeader.flatViewModel = new Map();
      instanceHeader.flatViewModel.set(propertyName, {isDataProperty: true, dataType: 'date'});
      let result = instanceHeader.getPropertyDisplayValue(currentObject, propertyName);
      expect(result).to.equals('22.12.15');
    });
  });

  it('formatDatetime should return formatted date string', () => {
    InstanceHeader.prototype.context = {
      getCurrentObject: () => {
        var instance = new InstanceObject('id', {validationModel: {}, viewModel: {fields: []}});
        instance.instanceType = 'documentinstance';
        return PromiseStub.resolve(instance)
      }
    };
    let appendSpy = sinon.spy();
    let element = getElementMock(appendSpy);
    let configuration = new Map();
    configuration.set(Configuration.UI_DATE_FORMAT, 'DD.MM.YY');
    configuration.set(Configuration.UI_TIME_FORMAT, 'HH.mm');
    let instanceHeader = new InstanceHeader(mock$scope(), undefined, element, configuration, new MomentAdapter());
    expect(instanceHeader.formatDatetime(new Date('2015/12/22'), true)).to.equals('22.12.15 00.00');
    expect(instanceHeader.formatDatetime(new Date('2015/12/22'))).to.equals('22.12.15');
  });
});

function getElementMock(appendSpy) {
  var elementMock = {};
  elementMock.find = () => {
    return elementMock;
  };
  elementMock.empty = () => {
    return elementMock;
  };
  elementMock.append = appendSpy;
  return elementMock;
}