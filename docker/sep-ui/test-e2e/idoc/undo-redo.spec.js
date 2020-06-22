'use strict';

let IdocPage = require('./idoc-page').IdocPage;
let TEXT_STYLE = require('./idoc-page').ListEditorMenu.TEXT_STYLE;
let TestUtils = require('../test-utils');

let ObjectDataWidget = require('./widget/object-data-widget/object-data-widget.js').ObjectDataWidget;
let ObjectDataWidgetConfig = require('./widget/object-data-widget/object-data-widget.js').ObjectDataWidgetConfig;
let DatatableWidget = require('./widget/data-table-widget/datatable-widget.js').DatatableWidget;
let DatatableWidgetConfig = require('./widget/data-table-widget/datatable-widget.js').DatatableWidgetConfigDialog;
let ObjectSelector = require('./widget/object-selector/object-selector.js').ObjectSelector;
let BusinessProcessDiagramWidget = require('./widget/business-process/buisness-process-diagram-widget').BusinessProcessDiagramWidget;
let BusinessProcessDiagramConfigDialog = require('./widget/business-process/buisness-process-diagram-widget').BusinessProcessDiagramConfigDialog;
let ChartViewWidget = require('./widget/chart-view-widget/chart-view-widget').ChartViewWidget;
let ChartViewWidgetConfigDialog = require('./widget/chart-view-widget/chart-view-widget').ChartViewWidgetConfigDialog;
let CommentsWidget = require('./widget/comments-widget/comments-widget').CommentsWidget;
let ImageWidget = require('./widget/image-widget/image-widget').ImageWidget;
let ImageWidgetConfigDialog = require('./widget/image-widget/image-widget').ImageWidgetConfigDialog;
let ObjectLinkWidget = require('./widget/object-link-widget/object-link-widget').ObjectLinkWidget;
let ObjectLinkWidgetConfigDialog = require('./widget/object-link-widget/object-link-widget').ObjectLinkWidgetConfigDialog;
let HeadingMenu = require('./idoc-page').HeadingMenu;

const IDOC_ID = 'emf:123456';
let idocPage = new IdocPage();


describe('undo/redo', () => {
  const FIELDS = {
    FIELD_ONE: 'field1',
    FIELD_TWO: 'field2',
    FIELD_THREE: 'field3'
  };
  let DISABLED = 'cke_button_disabled';
  let ACTIVE = 'cke_button_off';

  it('should undo formatting properly', () => {
    idocPage.open(true, IDOC_ID);
    let undoRedoToolbar = idocPage.getEditorToolbar(1).getUndoRedoToolbar();
    let undoButton = undoRedoToolbar.getUndoButton();
    let contentArea = idocPage.getTabEditor(1);
    // inserting various ckeditor elements must be undoable
    let toolbar = idocPage.getEditorToolbar(1).getTextOptionsToolbar();
    contentArea.clear().click();

    // bold text
    contentArea.type('this text is bold');
    contentArea.selectAll();
    toolbar.boldText();
    expect($$('strong').count()).to.eventually.equal(1);
    undoButton.click();
    expect($$('strong').count(), 'bold effect must be removed after undo').to.eventually.equal(0);
    expect(contentArea.getAsText()).to.eventually.equal('this text is bold');
    contentArea.clear().click();
    // toggle back
    toolbar.boldText();

    // italic text
    contentArea.type('this text is italic');
    contentArea.selectAll();
    toolbar.italicText();
    expect($$('em').count()).to.eventually.equal(1);
    undoButton.click();
    expect($$('em').count(), 'italic effect must be removed after undo').to.eventually.equal(0);
    expect(contentArea.getAsText()).to.eventually.equal('this text is italic');
    contentArea.clear().click();
    toolbar.italicText();

    // striked text
    contentArea.type('this text is striked');
    contentArea.selectAll();
    toolbar.strikeText(TEXT_STYLE.STRIKE);
    expect($$('s').count()).to.eventually.equal(1);
    undoButton.click();
    expect($$('s').count(), 'striked effect must be removed after undo').to.eventually.equal(0);
    expect(contentArea.getAsText()).to.eventually.equal('this text is striked')
    contentArea.clear().click();

    // lists
    // bulleted lists
    contentArea.type('this text is in a bulleted list');
    contentArea.selectAll();
    toolbar.insertbulletedList();
    expect(contentArea.getContentElement().$$('li').count()).to.eventually.equal(1);
    expect(contentArea.getAsText()).to.eventually.equal('this text is in a bulleted list');
    undoButton.click();
    // to remove selection
    contentArea.newLine();
    toolbar.insertbulletedList();
    contentArea.type('this is the second bulleted item');
    expect(contentArea.getContentElement().$$('li').count()).to.eventually.equal(1);
    undoButton.click();
    expect(contentArea.getContentElement().$$('li').count()).to.eventually.equal(1);
    expect(contentArea.getAsText()).to.eventually.not.equal('this is the second bulleted item');
    contentArea.clear().click();

    // numbered lists
    contentArea.type('this text is in a bulleted list');
    contentArea.selectAll();
    toolbar.insertNumberedList();
    expect(contentArea.getContentElement().$$('li').count()).to.eventually.equal(1);
    expect(contentArea.getAsText()).to.eventually.equal('this text is in a bulleted list');
    undoButton.click();
    // to remove selection
    contentArea.newLine();
    toolbar.insertNumberedList();
    contentArea.type('this is the second bulleted item');
    expect(contentArea.getContentElement().$$('li').count()).to.eventually.equal(1);
    undoButton.click();
    expect(contentArea.getContentElement().$$('li').count()).to.eventually.equal(1);
    expect(contentArea.getAsText()).to.eventually.not.equal('this is the second bulleted item');
    contentArea.clear().click();

    // horizontal rule
    contentArea.type('Above the horizontal rule');
    toolbar.insertHorizontalRule();
    contentArea.type('Below the horizontal rule');
    expect(contentArea.getContentElement().$$('hr').count()).to.eventually.equal(1);
    undoButton.click();
    undoButton.click();
    expect(contentArea.getContentElement().$$('hr').count(), 'horizontal line must be removed after undo').to.eventually.equal(0);
    expect(contentArea.getAsText()).to.eventually.equal('Above the horizontal rule');
    contentArea.clear().click();

    // blockquote
    contentArea.type('This should be blockQuoted');
    contentArea.selectAll();
    toolbar.insertBlockQuote();
    expect(contentArea.getContentElement().$$('blockquote').count()).to.eventually.equal(1);
    undoButton.click();
    expect(contentArea.getContentElement().$$('blockquote', 'blockquote must be removed after undo').count()).to.eventually.equal(0);
    expect(contentArea.getAsText()).to.eventually.equal('This should be blockQuoted');
    contentArea.clear().click();

    // page break
    contentArea.type('Above the page break');
    toolbar.insertPageBreak();
    contentArea.type('Below the page break');
    undoButton.click();
    expect(contentArea.getContentElement().$$('.cke_pagebreak').count()).to.eventually.equal(1);
    undoButton.click();
    expect(contentArea.getContentElement().$$('.cke_pagebreak').count(), ' pagebreak must be removed after undo.').to.eventually.equal(0);
    expect(contentArea.getAsText()).to.eventually.equal('Above the page break');
  });

  it('should enable and disable buttons properly', () => {
    idocPage.open(true, IDOC_ID);

    let contentArea = idocPage.getTabEditor(1);
    let undoRedoToolbar = idocPage.getEditorToolbar(1).getUndoRedoToolbar();
    let undoButton = undoRedoToolbar.getUndoButton();
    let redoButton = undoRedoToolbar.getRedoButton();
    expect(TestUtils.hasClass(undoButton, DISABLED), 'initial condition must be both buttons disabled').to.eventually.be.true;
    expect(TestUtils.hasClass(redoButton, DISABLED), 'initial condition must be both buttons disabled').to.eventually.be.true;

    contentArea.setContent('test content');
    browser.wait(TestUtils.hasClass(undoButton, ACTIVE), DEFAULT_TIMEOUT);
    expect(TestUtils.hasClass(redoButton, DISABLED), 'after content is inserted, the undo button must be active').to.eventually.be.true;
    contentArea.setContent('more test content');

    undoRedoToolbar.undo(contentArea.getContentElement());
    browser.wait(TestUtils.hasClass(redoButton, ACTIVE), DEFAULT_TIMEOUT);
    expect(TestUtils.hasClass(undoButton, ACTIVE), 'after initial undo, both buttons must be active').to.eventually.be.true;

    undoRedoToolbar.undo(contentArea.getContentElement());
    browser.wait(TestUtils.hasClass(undoButton, DISABLED), DEFAULT_TIMEOUT);
    expect(TestUtils.hasClass(redoButton, ACTIVE), 'after the snapshots are exhausted, only redo must be active').to.eventually.be.true;

    undoRedoToolbar.redo(contentArea.getContentElement());
    undoRedoToolbar.redo(contentArea.getContentElement());
    browser.wait(TestUtils.hasClass(redoButton, DISABLED), DEFAULT_TIMEOUT);
    expect(TestUtils.hasClass(undoButton, ACTIVE), 'after redo is exhausted, only undo must be active').to.eventually.be.true;
  });

  it('should undo to content containing widget', () => {
    idocPage.open(true, IDOC_ID);
    let contentArea = idocPage.getTabEditor(1);
    let undoRedoToolbar = idocPage.getEditorToolbar(1).getUndoRedoToolbar();
    let undoButton = undoRedoToolbar.getUndoButton();
    let redoButton = undoRedoToolbar.getRedoButton();

    contentArea.insertWidget(ObjectDataWidget.WIDGET_NAME);
    let widgetConfig = new ObjectDataWidgetConfig();
    widgetConfig.selectObjectSelectTab().selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
    let propertiesSelector = widgetConfig.selectObjectDetailsTab();
    propertiesSelector.selectProperty(FIELDS.FIELD_ONE);
    widgetConfig.save();

    // undo must be active after widget insert
    browser.wait(TestUtils.hasClass(undoButton, ACTIVE), DEFAULT_TIMEOUT);
    undoButton.click();
    // after undo is exhausted, only redo must be active
    browser.wait(TestUtils.hasClass(undoButton, DISABLED), DEFAULT_TIMEOUT);
    browser.wait(TestUtils.hasClass(redoButton, ACTIVE), DEFAULT_TIMEOUT);
    expect(contentArea.getAsText()).to.eventually.equal('Content tab 0');
    redoButton.click();
    browser.wait(EC.visibilityOf($('#field1')), DEFAULT_TIMEOUT);
    contentArea.type('test-message');
    // after redo, selection must remain below widget.
    expect(contentArea.getAsText()).to.eventually.equal('Content tab 0\nField 1 *\nSee more\ntest-message');
    expect(TestUtils.hasClass(undoButton, ACTIVE)).to.eventually.be.true;
    expect(TestUtils.hasClass(redoButton, DISABLED), 'after undo and insertion of new text, redo must be inactive').to.eventually.be.true;
    idocPage.insertLayout(10);

    let firstColumn = $('.layout-column-one');
    // select the first layout column
    firstColumn.$('p').click();
    firstColumn.sendKeys('test-message');
    contentArea.insertWidget(DatatableWidget.WIDGET_NAME);
    widgetConfig = new DatatableWidgetConfig();
    let objectSelector = widgetConfig.selectObjectSelectTab();
    let search = objectSelector.getSearch();
    search.getCriteria().getSearchBar().search();
    // And I select the first object to be visualized in the widget
    search.getResults().clickResultItem(0);
    widgetConfig.selectObjectDetailsTab().selectProperties(['field1', 'field2', 'field3', 'field4', 'field5']);

    widgetConfig.save();
    undoButton.click();
    expect(contentArea.widgetsCount()).to.eventually.equal(1);
    expect($('.layoutmanager').isPresent(), 'layout should not be removed on undo').to.eventually.be.true;

    // this will remove selection
    undoButton.click();
    // should remove layout
    undoButton.click();
    expect(contentArea.widgetsCount()).to.eventually.equal(1);
    expect($('.layoutmanager').isPresent(), 'layout should be removed on sequential undo').to.eventually.be.false;

    // this should remove text
    undoButton.click();
    // this should remove widget and reset undo plugin
    undoButton.click();
    browser.wait(TestUtils.hasClass(undoButton, DISABLED), DEFAULT_TIMEOUT);
    expect(contentArea.widgetsCount()).to.eventually.equal(0);
  });

  it('should undo after each widget insert', () => {
    idocPage.open(true, IDOC_ID);
    let contentArea = idocPage.getTabEditor(1);
    let undoRedoToolbar = idocPage.getEditorToolbar(1).getUndoRedoToolbar();
    let undoButton = undoRedoToolbar.getUndoButton();
    let redoButton = undoRedoToolbar.getRedoButton();

    // ODW
    contentArea.insertWidget(ObjectDataWidget.WIDGET_NAME);
    let widgetConfig = new ObjectDataWidgetConfig();
    widgetConfig.selectObjectSelectTab().selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
    let propertiesSelector = widgetConfig.selectObjectDetailsTab();
    propertiesSelector.selectProperty(FIELDS.FIELD_ONE);
    widgetConfig.save();
    browser.wait(TestUtils.hasClass(redoButton, DISABLED), DEFAULT_TIMEOUT);
    undoRedoToolbar.undo(contentArea.getContentElement());
    browser.wait(TestUtils.hasClass(undoButton, DISABLED), DEFAULT_TIMEOUT);
    expect(contentArea.widgetsCount(), 'undo should remove ODW').to.eventually.equal(0);

    // DTW
    contentArea.insertWidget(DatatableWidget.WIDGET_NAME);
    widgetConfig = new DatatableWidgetConfig();
    let objectSelector = widgetConfig.selectObjectSelectTab();
    let search = objectSelector.getSearch();
    search.getCriteria().getSearchBar().search();
    // And I select the first object to be visualized in the widget
    search.getResults().clickResultItem(0);
    widgetConfig.selectObjectDetailsTab().selectProperties(['field1', 'field2', 'field3', 'field4', 'field5']);
    widgetConfig.save();
    // undo button should be active, but insertion of new widget should disable redo
    browser.wait(TestUtils.hasClass(redoButton, DISABLED), DEFAULT_TIMEOUT);
    undoRedoToolbar.undo(contentArea.getContentElement());
    browser.wait(TestUtils.hasClass(undoButton, DISABLED), DEFAULT_TIMEOUT);
    expect(contentArea.widgetsCount(), 'undo should remove DTW').to.eventually.equal(0);

    // Image Widget
    contentArea.insertWidget(ImageWidget.WIDGET_NAME);
    widgetConfig = new ImageWidgetConfigDialog();
    widgetConfig.save();
    browser.wait(TestUtils.hasClass(redoButton, DISABLED), DEFAULT_TIMEOUT);
    undoButton.click();
    browser.wait(TestUtils.hasClass(undoButton, DISABLED), DEFAULT_TIMEOUT);
    expect(contentArea.widgetsCount(), 'undo should remove image widget').to.eventually.equal(0);

    // object-links
    contentArea.executeWidgetCommand(ObjectLinkWidget.COMMAND);
    widgetConfig = new ObjectLinkWidgetConfigDialog();
    search = widgetConfig.getSearch();
    let searchResults = search.getResults();
    searchResults.waitForResults();
    searchResults.clickResultItem(0);
    widgetConfig.save();
    browser.wait(TestUtils.hasClass(redoButton, DISABLED), DEFAULT_TIMEOUT);
    undoRedoToolbar.undo(contentArea.getContentElement());
    browser.wait(TestUtils.hasClass(undoButton, DISABLED), DEFAULT_TIMEOUT);
    expect(contentArea.widgetsCount()).to.eventually.equal(0);

    // comments widget
    contentArea.insertWidget(CommentsWidget.WIDGET_NAME);
    let commentsWidget = new CommentsWidget(contentArea.getWidgetByNameAndOrder(CommentsWidget.WIDGET_NAME, 0));
    widgetConfig = commentsWidget.getCommentsWidgetConfig();
    widgetConfig.save();
    browser.wait(TestUtils.hasClass(redoButton, DISABLED), DEFAULT_TIMEOUT);
    undoRedoToolbar.undo(contentArea.getContentElement());
    browser.wait(TestUtils.hasClass(undoButton, DISABLED), DEFAULT_TIMEOUT);
    expect(contentArea.widgetsCount(), 'undo should remove comments widget').to.eventually.equal(0);

    //chart view widget
    contentArea.insertWidget(ChartViewWidget.WIDGET_NAME);
    widgetConfig = new ChartViewWidgetConfigDialog();
    objectSelector = widgetConfig.selectObjectSelectTab();
    search = objectSelector.getSearch();
    search.getCriteria().getSearchBar().search();
    search.getResults().waitForResults();
    search.getResults().clickResultItem(0);
    search.getResults().clickResultItem(1);
    let chartConfigTab = widgetConfig.selectChartConfiguration();
    chartConfigTab.selectGroupBy('Type');
    widgetConfig.save();
    browser.wait(TestUtils.hasClass(redoButton, DISABLED), DEFAULT_TIMEOUT);
    undoRedoToolbar.undo(contentArea.getContentElement());
    browser.wait(TestUtils.hasClass(undoButton, DISABLED), DEFAULT_TIMEOUT);
    expect(contentArea.widgetsCount(), 'undo should remove chart view widget').to.eventually.equal(0);

    // business process
    contentArea.insertWidget(BusinessProcessDiagramWidget.WIDGET_NAME);
    widgetConfig = new BusinessProcessDiagramConfigDialog();
    objectSelector = widgetConfig.getObjectSelector();
    objectSelector.selectObjectSelectionMode(ObjectSelector.MANUALLY);
    search = objectSelector.getSearch();
    search.getCriteria().getSearchBar().search();
    search.getResults().waitForResults();
    search.getResults().clickResultItem(0);
    widgetConfig.save();
    browser.wait(TestUtils.hasClass(redoButton, DISABLED), DEFAULT_TIMEOUT);
    undoRedoToolbar.undo(contentArea.getContentElement());
    browser.wait(TestUtils.hasClass(undoButton, DISABLED), DEFAULT_TIMEOUT);
    expect(contentArea.widgetsCount(), 'undo should remove business process widget').to.eventually.equal(0);

    // at least two widgets in one doc, undo should remove one widget
    contentArea.insertWidget(ObjectDataWidget.WIDGET_NAME);
    widgetConfig = new ObjectDataWidgetConfig();
    widgetConfig.selectObjectSelectTab().selectObjectSelectionMode(ObjectSelector.CURRENT_OBJECT);
    propertiesSelector = widgetConfig.selectObjectDetailsTab();
    propertiesSelector.selectProperty(FIELDS.FIELD_ONE);
    widgetConfig.save();

    contentArea.insertWidget(DatatableWidget.WIDGET_NAME);
    widgetConfig = new DatatableWidgetConfig();
    objectSelector = widgetConfig.selectObjectSelectTab();
    search = objectSelector.getSearch();
    search.getCriteria().getSearchBar().search();
    // And I select the first object to be visualized in the widget
    search.getResults().clickResultItem(0);
    widgetConfig.selectObjectDetailsTab().selectProperties(['field1', 'field2', 'field3', 'field4', 'field5']);
    widgetConfig.save();
    undoRedoToolbar.undo(contentArea.getContentElement());
    expect(contentArea.widgetsCount(), 'undo operation removes 1 widget').to.eventually.equal(1);
    undoRedoToolbar.redo(contentArea.getContentElement());
    browser.wait(TestUtils.hasClass(redoButton, DISABLED), DEFAULT_TIMEOUT);
    expect(contentArea.widgetsCount(), 'redo should return widgets to two').to.eventually.equal(2);
  });

  it.skip('should undo content containing widget and lists', () => {
    idocPage.open(true, IDOC_ID);
    let contentArea = idocPage.getTabEditor(1);
    let undoRedoToolbar = idocPage.getEditorToolbar(1).getUndoRedoToolbar();
    let undoButton = undoRedoToolbar.getUndoButton();

    contentArea.insertWidget(DatatableWidget.WIDGET_NAME);
    new DatatableWidgetConfig().save();

    idocPage.getEditorToolbar(1).getTextOptionsToolbar().insertNumberedList();
    contentArea.type('list item 1');
    undoButton.click();
    expect(contentArea.getContentElement().$$('li').count()).to.eventually.equal(0);
    undoButton.click();
    expect(contentArea.widgetsCount()).to.eventually.equal(0);
  });

  it.skip('should undo content containing widget in section', () => {
    idocPage.open(true, IDOC_ID);
    let contentArea = idocPage.getTabEditor(1);
    let undoRedoToolbar = idocPage.getEditorToolbar(1).getUndoRedoToolbar();
    let undoButton = undoRedoToolbar.getUndoButton();

    contentArea.insertWidget(DatatableWidget.WIDGET_NAME);
    let widgetConfig = new DatatableWidgetConfig();
    let objectSelector = widgetConfig.selectObjectSelectTab();
    let search = objectSelector.getSearch();
    search.getCriteria().getSearchBar().search();
    search.getResults().clickResultItem(0);
    widgetConfig.selectObjectDetailsTab().selectProperties(['field1', 'field2', 'field3', 'field4', 'field5']);
    widgetConfig.save();
    contentArea.type('heading 1');
    let toolbar = idocPage.getEditorToolbar(1);
    let headingMenu = toolbar.getHeadingMenu();
    headingMenu.select(HeadingMenu.HEADING1);
    contentArea.newLine();
    idocPage.getEditorToolbar(1).getTextOptionsToolbar().boldText();
    contentArea.type('123');
    // undo inserted bold text '123'
    undoButton.click();
    expect($$('strong').count()).to.eventually.equal(0);
    // undo new line
    undoButton.click();
    // undo section creation
    undoButton.click();
    expect($$('h1').count(), 'heading should be removed after undo').to.eventually.equal(0);
  });
});
