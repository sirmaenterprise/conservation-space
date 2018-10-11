'use strict';

let IdocPage = require('../../idoc-page').IdocPage;
let DatatableWidget = require('./datatable-widget.js').DatatableWidget;
let DatatableWidgetConfigDialog = require('./datatable-widget').DatatableWidgetConfigDialog;

describe('Conditions in DTW', function () {

  let idocPage = new IdocPage('/sandbox/idoc/idoc-page/condition-validator');

  beforeEach(() => {
    idocPage.open(true);
  });

  // regression test for CMF-19613
  it('should hide region fields if HIDDEN condition is applied to region @slow', () => {
    // Given I have selected to create an idoc
    // And I have inserted an DTW found an object and selected to display the checkboxTrigger field and a regionField1
    // (the checkbox field is a trigger for HIDDEN condition applied to region1, see
    // instance-service.data.json where is the model for the sandbox used in this test)
    let dtw = insertDatatableWidget(idocPage, true, true);
    // When The widget config is saved
    // Then There have to be found 6 columns in the DTW
    let columnsCount = dtw.getHeader().getColumnsCount();
    expect(columnsCount).to.eventually.equal(8);
    // And the regionField1 should hidden
    dtw.getRow(1).getCell(6).getInputField().getElement();
  });

  function insertDatatableWidget(idocPage, withObject, withFields) {
    // When I select Data table widget from the widget menu
    let widgetElement = idocPage.getTabEditor(1).insertWidget(DatatableWidget.WIDGET_NAME);
    let widgetConfig = new DatatableWidgetConfigDialog();
    if (withObject) {
      // And I execute a search
      let objectSelector = widgetConfig.selectObjectSelectTab();
      let search = objectSelector.getSearch();
      search.getCriteria().getSearchBar().search();
      // And I select the first object to be visualized in the widget
      search.getResults().clickResultItem(0);
      if (withFields) {
        widgetConfig.selectObjectDetailsTab().selectAllProperties();
      }
    }
    // And I select ok from the DTW configuration dialog
    widgetConfig.save();
    return new DatatableWidget(widgetElement);
  }
});
