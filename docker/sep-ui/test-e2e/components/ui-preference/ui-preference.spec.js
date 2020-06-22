var TestUtils = require('../../test-utils');
var SandboxPage = require('../../page-object').SandboxPage;

var page = new SandboxPage();

describe('Test ui-preference component', function () {

  beforeEach(() => {
    page.open('/sandbox/components/ui-preference');
  });

  it('should set top position after the passed selector', function () {
    var testElement = $('#testElement');
    expect(TestUtils.hasCss(testElement, 'top: 20px;')).to.eventually.be.true;
  });

  it('should set same width to the element as the given element', function () {
    var testElement = $('#testElement');
    expect(TestUtils.hasCss(testElement, 'width: 250px;')).to.eventually.be.true;
  });

  it('should set top and left positions after the passed selector', function () {
    var testElement = $('#testElement2');
    expect(TestUtils.hasCss(testElement, 'top: 70px;')).to.eventually.be.true;
    expect(TestUtils.hasCss(testElement, 'left: 250px;')).to.eventually.be.true;
  });

  it('should set such height that it should fill the whole free viewport', function () {
    var testElement = $('#testElement2');
    expect(TestUtils.hasCss(testElement, 'height: calc(100% - 70px);')).to.eventually.be.true;
  });

  it('should set same width to the element as its parent on init', function () {
    var testElement = $('#testElement2');
    expect(TestUtils.hasCss(testElement, 'width: 250px;')).to.eventually.be.true;
  });
});
