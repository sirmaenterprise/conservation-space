var SandboxPage = require('../../../page-object').SandboxPage;

var MAIN_MENU_ID = 'mainMenu';
var MAIN_MENU_MOCK_ENTRY_ID = 'mockEntry';
const URL = 'sandbox/layout/top-header/main-menu';

var page = new SandboxPage();

describe('Test for "main-menu" ', function () {

  beforeEach(function() {
    page.open(URL);
  });

  it('should show the main menu', function () {
    var mainMenu = element(by.id(MAIN_MENU_ID));
    mainMenu.isPresent().then(function (present) {
      return expect(present).to.equal(true);
    });
  });

  it('should provide extension point', function () {
    var mainMenuEntry = element(by.id(MAIN_MENU_ID));
    mainMenuEntry.getAttribute('extension-point').then(function (value) {
      return expect(value).to.equal('main-menu');
    });

  });
});
