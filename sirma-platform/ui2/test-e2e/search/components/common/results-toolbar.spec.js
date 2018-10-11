var ResultsToolbarSandboxPage = require('./results-toolbar').ResultsToolbarSandboxPage;

describe('Results Toolbar', () => {

  var resultsToolbar;
  var page = new ResultsToolbarSandboxPage();

  beforeEach(()=> {
    page.open();
  });

  describe('When the search control is pressed', () => {
    it('should show & render the results toolbar', () => {
      page.clickSearchButton();
      resultsToolbar = page.getResultsToolbar();
    });

    it('should show correct count message', () => {
      page.clickSearchButton();
      resultsToolbar = page.getResultsToolbar();
      expect(resultsToolbar.getCountMessageText()).to.eventually.equal('500');
    });

    it('should show correct results message', () => {
      page.clickSearchButton();
      resultsToolbar = page.getResultsToolbar();
      expect(resultsToolbar.getBoundsMessageText()).to.eventually.equal('1 - 50');
    });

    it('should show correct type message', () => {
      page.clickSearchButton();
      resultsToolbar = page.getResultsToolbar();
      expect(resultsToolbar.getTypeMessageText()).to.eventually.equal('Document');
    });

    it('should show correct context message', () => {
      page.clickSearchButton();
      resultsToolbar = page.getResultsToolbar();
      expect(resultsToolbar.getContextMessageText()).to.eventually.equal('Header-emf:123456');
    });

    it('should show correct fts message', () => {
      page.clickSearchButton();
      resultsToolbar = page.getResultsToolbar();
      expect(resultsToolbar.getFtsMessageText()).to.eventually.equal('free text search');
    });
  });

  describe('When the page controls are pressed', () => {
    it('should show toolbar & offset results when next page is pressed', () => {
      page.clickNextPageButton();
      resultsToolbar = page.getResultsToolbar();
      expect(resultsToolbar.getBoundsMessageText()).to.eventually.equal('51 - 100');
    });

    it('should show toolbar & offset results when prev page is pressed', () => {
      // click the next button a few times
      for (var i = 0; i < 3; ++i) {
        // click next page button
        page.clickNextPageButton();
      }
      resultsToolbar = page.getResultsToolbar();
      expect(resultsToolbar.getBoundsMessageText()).to.eventually.equal('151 - 200');

      // click prev button once & assert result
      page.clickPrevPageButton();
      expect(resultsToolbar.getBoundsMessageText()).to.eventually.equal('101 - 150');
    });
  });

  describe('When the toggle controls are pressed', () => {
    it('should not show results message when toggle results is pressed', () => {
      page.clickSearchButton();
      resultsToolbar = page.getResultsToolbar();
      page.clickToggleResultsButton();
      expect(resultsToolbar.getBoundsMessage().isPresent()).to.eventually.be.false;
    });

    it('should not show type message when toggle type is pressed', () => {
      page.clickSearchButton();
      resultsToolbar = page.getResultsToolbar();
      page.clickToggleTypeButton();
      expect(resultsToolbar.getTypeMessage().isPresent()).to.eventually.be.false;
    });

    it('should not show context message when toggle context is pressed', () => {
      page.clickSearchButton();
      resultsToolbar = page.getResultsToolbar();
      page.clickToggleContextButton();
      expect(resultsToolbar.getContextMessage().isPresent()).to.eventually.be.false;
    });

    it('should not show fts message when toggle fts is pressed', () => {
      page.clickSearchButton();
      resultsToolbar = page.getResultsToolbar();
      page.clickToggleFtsButton();
      expect(resultsToolbar.getFtsMessage().isPresent()).to.eventually.be.false;
    });
  });
});