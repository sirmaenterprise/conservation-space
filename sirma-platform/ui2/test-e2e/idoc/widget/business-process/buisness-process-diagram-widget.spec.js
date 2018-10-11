var ProcessSandboxPage = require('./buisness-process-diagram-widget').ProcessSandboxPage;
var BusinessProcessDiagramWidget = require('./buisness-process-diagram-widget').BusinessProcessDiagramWidget;
var BusinessProcessDiagramConfigDialog = require('./buisness-process-diagram-widget').BusinessProcessDiagramConfigDialog;
var IdocPage = require('../../idoc-page').IdocPage;
var ObjectSelector = require('./../object-selector/object-selector.js').ObjectSelector;

const TAB_NUMBER = 1;

describe('BusinessProcessDiagramWidget ', () => {

  var idocPage = new IdocPage();

  it('display message for not selected', () => {
    idocPage.open(true);
    var widget = insertProcessWidget(idocPage, false);
    widget.isMessagePresent();
    widget.getMesageText().then((text) => {
      expect(text).to.eq('No process is selected.');
    });
  });

  it('enter fullscreen mode ', () => {
    idocPage.open(true);
    var widget = insertProcessWidget(idocPage, true);
    var toolbar = idocPage.getActionsToolbar();
    toolbar.saveIdoc();
    widget.isFullscreenButtonPresent();
    var dialog = widget.clickFullscreenButton();
    dialog.isDiagramPresent();
  });

  /**
   * Inserts and configures a process-widget.
   *
   * @param idocPage the current page
   *
   * @param selection wheter or not to select an object
   *
   * @return the process-widget page object;
   */
  function insertProcessWidget(idocPage, selection) {
    idocPage.getTabEditor(TAB_NUMBER).insertWidget(BusinessProcessDiagramWidget.WIDGET_NAME);
    let widgetConfig = new BusinessProcessDiagramConfigDialog();
    if (selection) {
      var objectSelector = widgetConfig.getObjectSelector();
      objectSelector.selectObjectSelectionMode(ObjectSelector.MANUALLY);
      var search = objectSelector.getSearch();
      search.getCriteria().getSearchBar().search();
      search.getResults().waitForResults();
      search.getResults().clickResultItem(0);
    }
    widgetConfig.save();
    var process = new ProcessSandboxPage();
    var widget = process.getWidget();
    return widget;
  }

});
