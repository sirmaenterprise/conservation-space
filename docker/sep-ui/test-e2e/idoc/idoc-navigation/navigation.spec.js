var NavigationSandboxPage = require('./navigation').NavigationSandboxPage;

describe('Test for idoc navigation', function () {

  var sandboxPage = new NavigationSandboxPage();

  it('should expand all collapsed headings and remove the collapsed container when the navigation is disabled', function () {
    sandboxPage.open();
    var contentArea = sandboxPage.getContentArea();

    sandboxPage.collapseFirstHeading();
    var heading2 = sandboxPage.getHeading2();
    browser.wait(EC.stalenessOf(heading2), DEFAULT_TIMEOUT);

    // When i disable the navigation
    sandboxPage.toggleNavigation();

    // Then i should have all collapsed content expanded and collapse containers deleted
    expect(contentArea.getText()).to.eventually.equal('H1\nH2');
    expect($('#collapse-container-mockEditor').isPresent()).to.eventually.be.false;
  });

});
