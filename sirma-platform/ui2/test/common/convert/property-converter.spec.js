import {
  BooleanTypeConverter,
  StringTypeConverter,
  NumericTypeConverter,
  ObjectTypeConverter,
  DefaultTypeConverter
} from 'common/convert/property-converter';

describe('Property Data Type converter', () => {

  describe('BooleanTypeConverter', () => {
    it('should properly handle boolean data types', () => {
      let testData = [
        {value: 'boolean', expected: true},
        {value: 'invalid', expected: false},
        {value: 'true', expected: false},
        {value: 'false', expected: false},
        {value: 123, expected: false},
        {value: {}, expected: false},
        {value: [], expected: false},
        {value: null, expected: false},
        {value: undefined, expected: false}
      ];
      let converter = new BooleanTypeConverter();
      testData.forEach((data) => {
        expect(converter.canHandle(data.value), `Value '${data.value}' have to be handled=${data.expected}`).to.equal(data.expected);
      });
    });

    it('should properly convert boolean data types', () => {
      let data = {value: 'boolean', expected: 'boolean'};
      let converter = new BooleanTypeConverter();
      expect(converter.handle(data.value), `Value '${data.value}' have to be converted to ${data.expected}`).to.equal(data.expected);
    });
  });

  describe('StringTypeConverter', () => {
    it('should properly handle text data types', () => {
      let testData = [
        {value: 'text', expected: true},
        {value: 'string', expected: false},
        {value: 'invalid', expected: false},
        {value: 'true', expected: false},
        {value: 'false', expected: false},
        {value: 123, expected: false},
        {value: {}, expected: false},
        {value: [], expected: false},
        {value: null, expected: false},
        {value: undefined, expected: false}
      ];
      let converter = new StringTypeConverter();
      testData.forEach((data) => {
        expect(converter.canHandle(data.value), `Value '${data.value}' have to be handled=${data.expected}`).to.equal(data.expected);
      });
    });

    it('should properly convert text data types', () => {
      let data = {value: 'text', expected: 'string'};
      let converter = new StringTypeConverter();
      expect(converter.handle(data.value), `Value '${data.value}' have to be converted to ${data.expected}`).to.equal(data.expected);
    });
  });

  describe('NumberTypeConverter', () => {
    it('should properly handle numeric data types', () => {
      let testData = [
        {value: 'int', expected: true},
        {value: 'float', expected: true},
        {value: 'double', expected: true},
        {value: 'long', expected: true},
        {value: 'text', expected: false},
        {value: 'string', expected: false},
        {value: 'invalid', expected: false},
        {value: 'true', expected: false},
        {value: 'false', expected: false},
        {value: 123, expected: false},
        {value: {}, expected: false},
        {value: [], expected: false},
        {value: null, expected: false},
        {value: undefined, expected: false}
      ];
      let converter = new NumericTypeConverter();
      testData.forEach((data) => {
        expect(converter.canHandle(data.value), `Value '${data.value}' have to be handled=${data.expected}`).to.equal(data.expected);
      });
    });

    it('should properly convert numeric data types', () => {
      let testData = [
        {value: 'int', expected: 'numeric'},
        {value: 'float', expected: 'numeric'},
        {value: 'double', expected: 'numeric'},
        {value: 'long', expected: 'numeric'}
      ];
      let converter = new NumericTypeConverter();
      testData.forEach((data) => {
        expect(converter.handle(data.value), `Value '${data.value}' have to be converted to ${data.expected}`).to.equal(data.expected);
      });
    });
  });

  describe('ObjectTypeConverter', () => {
    it('should properly handle object data types', () => {
      let testData = [
        {value: 'any', expected: true},
        {value: 'float', expected: false},
        {value: 'double', expected: false},
        {value: 'long', expected: false},
        {value: 'text', expected: false},
        {value: 'string', expected: false},
        {value: 'invalid', expected: false},
        {value: 'true', expected: false},
        {value: 'false', expected: false},
        {value: 123, expected: false},
        {value: {}, expected: false},
        {value: [], expected: false},
        {value: null, expected: false},
        {value: undefined, expected: false}
      ];
      let converter = new ObjectTypeConverter();
      testData.forEach((data) => {
        expect(converter.canHandle(data.value), `Value '${data.value}' have to be handled=${data.expected}`).to.equal(data.expected);
      });
    });

    it('should properly convert object data types', () => {
      let data = {value: 'any', expected: 'object'};
      let converter = new ObjectTypeConverter();
      expect(converter.handle(data.value), `Value '${data.value}' have to be converted to ${data.expected}`).to.equal(data.expected);
    });
  });

  describe('DefaultTypeConverter', () => {
    it('should properly handle default data types', () => {
      let testData = [
        {value: 'any', expected: true},
        {value: 'float', expected: true},
        {value: 'double', expected: true},
        {value: 'long', expected: true},
        {value: 'text', expected: true},
        {value: 'string', expected: true},
        {value: {}, expected: false},
        {value: [], expected: false},
        {value: null, expected: false},
        {value: undefined, expected: false}
      ];
      let converter = new DefaultTypeConverter();
      testData.forEach((data) => {
        expect(converter.canHandle(data.value), `Value '${data.value}' have to be handled=${data.expected}`).to.equal(data.expected);
      });
    });

    it('should properly convert default data types', () => {
      let testData = [
        {value: 'any', expected: 'any'},
        {value: 'float', expected: 'float'},
        {value: 'double', expected: 'double'},
        {value: 'long', expected: 'long'},
        {value: 'text', expected: 'text'},
        {value: 'string', expected: 'string'}
      ];
      let converter = new DefaultTypeConverter();
      testData.forEach((data) => {
        expect(converter.handle(data.value), `Value '${data.value}' have to be converted to ${data.expected}`).to.equal(data.expected);
      });
    });
  });
});