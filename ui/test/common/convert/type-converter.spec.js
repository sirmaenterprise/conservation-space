import {BooleanConverter, StringConverter, NumberConverter} from 'common/convert/type-converter';

describe('Type converter', () => {

  describe('BooleanConverter', () => {
    it('should accepts requests for boolean values and true|false given as strings', () => {
      let testData = [
        { value: true, expected: true },
        { value: false, expected: true },
        { value: 'true', expected: true },
        { value: 'false', expected: true },
        { value: 123, expected: false },
        { value: 'string', expected: false },
        { value: {}, expected: false },
        { value: [], expected: false },
        { value: 0, expected: false },
        { value: '', expected: false },
        { value: null, expected: false },
        { value: undefined, expected: false }
      ];
      let converter = new BooleanConverter();
      testData.forEach((data) => {
        expect(converter.canHandle(data.value), `Value '${data.value}' have to be handled=${data.expected}`).to.equal(data.expected);
      });
    });

    it('should convert value to boolean', () => {
      let testData = [
        { value: true, expected: true },
        { value: false, expected: false },
        { value: 'true', expected: true },
        { value: 'false', expected: false }
      ];
      let converter = new BooleanConverter();
      testData.forEach((data) => {
        expect(converter.handle(data.value), `Value '${data.value}' have to be converted to ${data.expected}`).to.equal(data.expected);
      });
    });
  });

  describe('StringConverter', () => {
    it('should accepts requests for string values', () => {
      let testData = [
        { value: 'string', expected: true },
        { value: '', expected: true },
        { value: null, expected: false },
        { value: undefined, expected: false },
        { value: 123, expected: false },
        { value: true, expected: false },
        { value: false, expected: false },
        { value: {}, expected: false },
        { value: [], expected: false }
      ];
      let converter = new StringConverter();
      testData.forEach((data) => {
        expect(converter.canHandle(data.value), `Value '${data.value}' have to be handled=${data.expected}`).to.equal(data.expected);
      });
    });

    it('should convert value to string', () => {
      let testData = [
        { value: 'string', expected: 'string' },
        { value: '', expected: '' }
      ];
      let converter = new StringConverter();
      testData.forEach((data) => {
        expect(converter.handle(data.value), `Value '${data.value}' have to be converted to ${data.expected}`).to.equal(data.expected);
      });
    });
  });

  describe('NumberConverter', () => {
    it('should accepts requests for number values and numbers given as strings', () => {
      let testData = [
        { value: 123, expected: true },
        { value: 123.2, expected: true },
        { value: 0, expected: true },
        { value: -123, expected: true },
        { value: '123', expected: true },
        { value: '123.2', expected: true },
        { value: '0', expected: true },
        { value: '-123', expected: true },
        { value: 'string', expected: false },
        { value: true, expected: false },
        { value: false, expected: false },
        { value: {}, expected: false },
        { value: [], expected: false },
        { value: null, expected: false },
        { value: undefined, expected: false }
      ];
      let converter = new NumberConverter();
      testData.forEach((data) => {
        expect(converter.canHandle(data.value), `Value '${data.value}' have to be handled=${data.expected}`).to.equal(data.expected);
      });
    });

    it('should convert value to number', () => {
      let testData = [
        { value: 123, expected: 123 },
        { value: 123.2, expected: 123.2 },
        { value: 0, expected: 0 },
        { value: -123, expected: -123 },
        { value: '123', expected: 123 },
        { value: '123.2', expected: 123.2 },
        { value: '0', expected: 0 },
        { value: '-123', expected: -123 }
      ];
      let converter = new NumberConverter();
      testData.forEach((data) => {
        expect(converter.handle(data.value), `Value '${data.value}' have to be converted to ${data.expected}`).to.equal(data.expected);
      });
    });
  });
});