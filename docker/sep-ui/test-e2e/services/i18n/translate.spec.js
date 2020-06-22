var SandboxPage = require('../../page-object').SandboxPage;

describe('Test i18n and l10n functionalities', function () {

  var page = new SandboxPage();

  it('should translate', function () {
    page.open('/sandbox/services/i18n');

    expect(element(by.className('splash')).isPresent()).to.eventually.be.true;
    browser.wait(EC.invisibilityOf(element(by.className('splash'))), 1000);

    var expectedTexts = ['Welcome John Doe. Current date is Friday, July 10, 2015.', 'Dashboard', 'homepage',
      'homepage.unbind', 'Menu', 'Menu unbind',
      'missing', 'Friday, October 29, 2010', 'Monday, October 13, 2014',
      'Monday, June 6, 1983', 'Fallback'];

    for (var int = 0; int < expectedTexts.length; int++) {
      expect(element(by.id('translate-' + int)).getText()).to.eventually.equal(expectedTexts[int]);
    }

    element(by.id('lang-bg')).click();
    // Wait for labels to be loaded
    browser.wait(EC.textToBePresentInElement(element(by.id('translate-1')), 'Работен плот'), 1000);

    expectedTexts = ['Добре дошли John Doe. Текущата дата е Петък, Юли 10, 2015.', 'Работен плот', 'Начална страница',
      'homepage.unbind', 'Меню', 'Menu unbind',
      'missing', 'Петък, Октомври 29, 2010', 'Понеделник, Октомври 13, 2014',
      'Понеделник, Юни 6, 1983', 'Fallback'];

    for (var int = 0; int < expectedTexts.length; int++) {
      expect(element(by.id('translate-' + int)).getText()).to.eventually.equal(expectedTexts[int]);
    }
  });
});
