var LibrariesSandboxPage = require('./libraries').LibrariesSandboxPage;

describe('BrowseLibraries', function () {

  var page = new LibrariesSandboxPage();

  beforeEach(() => {
    page.open();
  });

  it('should check the count of the libraries', ()=> {
    var librariesPanel = page.getLibrariesPanel();
    expect(librariesPanel.libs.getItemsCount()).to.eventually.equal(2);
  });
});