var CommentsWidgetPage = require('./comments-widget').CommentsWidgetSandboxPage;
var CommentsWidget = require('./comments-widget').CommentsWidget;
var CommentsWidgetConfigDialog = require('./comments-widget').CommentsWidgetConfigDialog;
var IdocPage = require('../../idoc-page').IdocPage;

const TAB_NUMBER = 1;

describe('CommentsWidget', function () {
  describe('Comments in Idoc page', () => {
    var idocPage = new IdocPage();

    beforeEach(() => {
      idocPage.open(true);
    });

    it('should load the comments widget', () => {
      idocPage.getTabEditor(TAB_NUMBER).insertWidget(CommentsWidget.WIDGET_NAME);
      var idocContent = idocPage.getTabEditor(TAB_NUMBER);
      var commentsWidget = new CommentsWidget(idocContent.getWidgetByNameAndOrder(CommentsWidget.WIDGET_NAME, 0));
      var widgetConfig = commentsWidget.getCommentsWidgetConfig();

      widgetConfig.save();

      expect(commentsWidget.getCommentsWidgetFooter().isDisplayed()).to.eventually.be.false;
      expect(commentsWidget.getErrorMessage().isDisplayed()).to.eventually.be.true;
    });

    it('should display the recent comments for a given object', () => {
      idocPage.getTabEditor(TAB_NUMBER).insertWidget(CommentsWidget.WIDGET_NAME);
      var idocContent = idocPage.getTabEditor(TAB_NUMBER);
      var commentsWidget = new CommentsWidget(idocContent.getWidgetByNameAndOrder(CommentsWidget.WIDGET_NAME, 0));
      var widgetConfig = commentsWidget.getCommentsWidgetConfig();

      var search = widgetConfig.getSearch();
      search.getCriteria().getSearchBar().search();
      var results = search.getResults();
      results.waitForResults();
      results.clickResultItem(1);
      widgetConfig.save();

      expect(commentsWidget.getCommentsSection().isDisplayed()).to.eventually.be.true;
    });

  });

  describe('Comments widget specific', () => {
    var commentsWidgetPage = new CommentsWidgetPage();

    beforeEach(() => {
      commentsWidgetPage.open();
    });

    it('should not show widget content if modelling mode is set and current object comments are displayed', () => {
      commentsWidgetPage.toggleModellingMode();
      commentsWidgetPage.toggleCreateButton();

      var widgetConfig = new CommentsWidgetConfigDialog();
      widgetConfig.waitUntilOpened();

      var search = widgetConfig.getSearch();
      search.getCriteria().getSearchBar().search();
      var results = search.getResults();
      results.waitForResults();
      results.clickResultItem(1);

      widgetConfig.includeCurrentObject();
      widgetConfig.save();

      var widget = new CommentsWidget($(`[widget="${CommentsWidget.WIDGET_NAME}"]`));
      expect(widget.getCommeentsWithoutWait().hasComments()).to.eventually.be.false;
    });

    it('should not show widget content if modelling mode is set and automatic selection is selected', () => {
      commentsWidgetPage.toggleModellingMode();
      commentsWidgetPage.toggleCreateButton();

      var widgetConfig = new CommentsWidgetConfigDialog();
      widgetConfig.waitUntilOpened();

      var search = widgetConfig.getSearch();
      widgetConfig.automaticallySelectObject();

      search.getCriteria().getSearchBar().search();
      widgetConfig.save();

      var widget = new CommentsWidget($(`[widget="${CommentsWidget.WIDGET_NAME}"]`));
      expect(widget.getCommeentsWithoutWait().hasComments()).to.eventually.be.false;
    });
  });
});