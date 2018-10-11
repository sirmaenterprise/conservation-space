import {Splitter} from 'components/splitter/splitter';
import {IdocMocks} from 'test/idoc/idoc-mocks';

describe('Splitter component', function () {
  it('should throw Error when no configuration is provided', function () {
    expect(function () {
      createSplitterInstance({});
    }).to.throw(Error);
  });

  it('should extract the size of pane string', function () {
    var splitter = createSplitterInstance({
      commands: {
        init: function () {
        }
      }
    });
    expect(splitter.parseSize('10px')).to.equal(10);
  });

  it('should test calculatePaneSizes(sizes) when there are no auto size', function () {
    var splitter = createSplitterInstance({
      commands: {
        init: function () {
        }
      }
    });
    expect(splitter.calculatePaneSizes(['100px', '200px', '300px'])).to.eql(['100px', '200px', '300px']);
  });

  it('should test calculatePaneSizes(sizes) when there is auto size at second position', function () {
    var splitter = createSplitterInstance({
      commands: {
        init: function () {
        }
      }
    });
    expect(splitter.calculatePaneSizes(['100px', 'auto'])).to.eql(['100px', 'calc(100% - 105px)']);
  });

  it('should test calculatePaneSizes(sizes) when there is auto size and more than two fixed pane sizes', function () {
    var splitter = createSplitterInstance({
      commands: {
        init: function () {
        }
      }
    });
    expect(splitter.calculatePaneSizes(['100px', 'auto', '200px'])).to.eql(['100px', 'calc(100% - 310px)', '200px']);
  });

  it('should test calculatePaneSizes(sizes) when there is only auto size', function () {
    var splitter = createSplitterInstance({
      commands: {
        init: function () {
        }
      }
    });
    expect(splitter.calculatePaneSizes(['auto'])).to.eql(['calc(100% - 0px)']);
  });

  function createSplitterInstance(config) {
    Splitter.prototype.config = config;
    var scope = {
      $watch: function () {
      }
    };
    var element = {};
    return new Splitter(scope, element, IdocMocks.mockStateParamsAdapter('id', 'edit'));
  }

});