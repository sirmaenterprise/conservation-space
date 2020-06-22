var ContextualHelpSandboxPage = require('./contextual-help.js').ContextualHelpSandboxPage;

describe('ContextualHelp', () => {

  var page;
  beforeEach(() => {
    page = new ContextualHelpSandboxPage();
    page.open();
  });

  describe('When configuring a contextual help for existing help target', () => {
    it('should render the contextual help component', () => {
      var existingHelp = page.getExistingContextualHelp();
      return expect(existingHelp.isRendered()).to.eventually.be.true;
    });
  });

  describe('When configuring a contextual help for missing help target', () => {
    it('should NOT render the contextual help component', () => {
      var missingHelp = page.getMissingContextualHelp();
      return expect(missingHelp.isRendered()).to.eventually.be.false;
    });
  });
});