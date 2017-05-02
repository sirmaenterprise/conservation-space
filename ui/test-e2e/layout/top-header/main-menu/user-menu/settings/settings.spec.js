var SandboxPage = require('../../../../../page-object').SandboxPage;

var URL = 'sandbox/layout/top-header/main-menu/user-menu/settings';

var page = new SandboxPage();

describe('Test for "user settings menu" ', function () {

  beforeEach(function() {
    page.open(URL);
  });

  it('should show user settings menu', function () {
    var menu = element(by.id('userSettingsMenu'));
    expect(menu.isPresent()).to.eventually.be.true;
  });

  it('should link contain user name in title', function () {
    var link = $('#userSettingsMenu > a');
    expect(link.getAttribute('title')).to.eventually.not.be.empty;
  });

});
