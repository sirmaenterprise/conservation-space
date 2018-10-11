import {Converter} from 'common/convert/converter';

describe('Converter', function() {

  it('should throw error if a converter does not provide a convert function', function () {
    expect(function () {
      new DummyConverter()
    }).to.throw(TypeError);
  });

  class DummyConverter extends Converter {

  }
});