var SandboxPage = require('../../page-object').SandboxPage;

var page = new SandboxPage();

describe('Splitter component', function () {

  beforeEach(function () {
    page.open('sandbox/components/splitter');
  });

  it('should pass if the splitter has been initialised', function () {
    var initButton = element(by.className('initA'));
    initButton.click();
    var present = element(by.className('gutter')).isPresent();
    return expect(present).to.be.eventually.true;
  });

  it('should destroy splitter B and reinit it', function () {
    var initButton = element(by.className('initB'));
    initButton.click();
    var destroyButton = element(by.className('destroyB'));
    destroyButton.click();
    var present = element(by.className('gutter')).isPresent();
    return expect(present).to.be.eventually.true;
  });

});
