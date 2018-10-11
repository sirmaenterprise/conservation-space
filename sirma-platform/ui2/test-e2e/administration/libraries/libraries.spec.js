let LibrariesSandboxPage = require('./libraries').LibrariesSandboxPage;

describe('Browse Libraries', function () {

  let page = new LibrariesSandboxPage();

  beforeEach(() => {
    page.open();
  });

  it('should load libraries', function () {
    let librariesPanel = page.getLibrariesPanel();
    expect(librariesPanel.libs.getItemsCount()).to.eventually.equal(2);
  });

  it('should provide ontology and title filter fields', function () {
    let librariesPanel = page.getLibrariesPanel();

    librariesPanel.getTitleFilterField().sendKeys('Cl');
    librariesPanel.getOntologyFilterField().selectFromMenuByIndex(1);

    browser.wait(EC.textToBePresentInElement(page.getTitleFilter(), 'Cl'), DEFAULT_TIMEOUT);
    browser.wait(EC.textToBePresentInElement(page.getOntologyFilter(), '["http://dublincore.org/documents/dces/"]'), DEFAULT_TIMEOUT);
  });
});