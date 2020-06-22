import base64 from 'common/lib/base64';
import {JsonUtil} from 'common/json-util';

describe('Tests for JsonUtil', function () {
  it('should copy property with different object/source key', function () {
    const source = {'property1': 'value1'};
    const object = {};
    JsonUtil.copyProperty(object, 'new property name', source, 'property1');
    expect(object).to.have.property('new property name', source['property1']);
  });

  it('should copy property with same object/source key', function () {
    const source = {'property1': 'value1'};
    const object = {};
    JsonUtil.copyProperty(object, 'property1', source);
    expect(object).to.have.property('property1', source['property1']);
  });

  it('should override existing property', function () {
    const source = {'property1': 'value1'};
    const object = {'property1': 'value2'};
    JsonUtil.copyProperty(object, 'property1', source);
    expect(object).to.have.property('property1', 'value1');
  });

  it('should copy not existing property', function () {
    const source = {'property1': 'value1'};
    const object = {};
    JsonUtil.copyProperty(object, 'property2', source);
    expect(object).to.not.have.property('property1');
  });

  it('should encode an object to base64 when valid object is specified', function () {
    let object = {id: 12, label: 'test-label', text: 'some-text'};
    expect(JsonUtil.encode(object)).to.eq(base64.encode(JSON.stringify(object)));
  });

  it('should not encode an object to base64 when invalid object is specified', function () {
    expect(JsonUtil.encode()).to.eq(undefined);
  });

  it('should decode an object from base64 when valid object is specified', function () {
    let object = {id: 12, label: 'test-label', text: 'some-text'};
    let encoded = base64.encode(JSON.stringify(object));
    expect(JsonUtil.decode(encoded)).to.deep.eq(object);
  });

  it('should not decode an object from base64 when invalid object is specified', function () {
    let invalid = 'invalid-encoded-base64-string';
    expect(JsonUtil.decode(invalid)).to.eq(undefined);
    expect(JsonUtil.decode()).to.eq(undefined);
  });

  it('should deeply remove all specified properties from an object', function () {
    let nestedObject = {
      id: '1',
      text: 'text',
      label: 'label1',
      nested: [{
        id: '2',
        text: 'text',
        label: 'level2',
        nested: {
          id: '3',
          text: 'text',
          label: 'level3'
        }
      }, {
        id: '4',
        text: 'text',
        label: 'level2',
        nested: {
          id: '5',
          text: 'text',
          label: 'level3'
        }
      }]
    };

    let expectedObject = {
      text: 'text',
      nested: [{
        text: 'text',
        nested: {
          text: 'text',
        }
      }, {
        text: 'text',
        nested: {
          text: 'text',
        }
      }]
    };

    expect(JsonUtil.removeObjectProperties(nestedObject, ['id', 'label'])).to.deep.eq(expectedObject);
  });

  describe('isJson', () => {
    it('should validate if provided string is valid json or not', () => {
      let data = [
        {value: '3', valid: true},
        {value: '{}', valid: true},
        {value: '[]', valid: true},
        {value: 'true', valid: true},
        {value: 'false', valid: true},
        {value: '[1, 2]', valid: true},
        {value: '{"id": "123"}', valid: true},
        {value: null, valid: true},
        {value: 'invalid', valid: false},
        {value: '{invalid}', valid: false},
        {value: '{invalid', valid: false}
      ];
      data.forEach((set) => {
        expect(JsonUtil.isJson(set.value), `Value "${set.value}" is ${set.valid ? 'valid' : 'invalid'} json`).to.equal(set.valid);
      });
    });
  });
});