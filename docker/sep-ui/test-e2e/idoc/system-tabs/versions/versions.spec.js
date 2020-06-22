var VersionsSandboxPage = require('./versions').VersionsSandboxPage;

describe('Versions', function () {
  var page = new VersionsSandboxPage();

  beforeEach(() => {
    page.open();
  });

  it('should select two version for comparison', ()=> {
    var versionsPanel = page.getVersionsPanel();

    // When I select two versions
    versionsPanel.selectVersion(1);
    versionsPanel.selectVersion(2);

    // Then the compare button is enabled.
    expect(versionsPanel.getCompareButton().isEnabled()).to.eventually.be.true;
  });

});