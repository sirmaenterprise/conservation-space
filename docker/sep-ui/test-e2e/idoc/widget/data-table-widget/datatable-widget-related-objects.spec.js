'use strict';

let DatatableWidget = require('./datatable-widget').DatatableWidget;
let DatatableWidgetConfigDialog = require('./datatable-widget').DatatableWidgetConfigDialog;
let IdocPage = require('../../idoc-page').IdocPage;

const TAB_NUMBER = 1;

describe('Related objects in DatatableWidget', () => {

  let widgetElement;
  let widgetConfig;

  let idocPage = new IdocPage();

  it('should allow selection of related object properties and rendering them as columns', () => {
    // Given I have inserted a DTW
    idocPage.open(true);
    widgetElement = idocPage.getTabEditor(TAB_NUMBER).insertWidget(DatatableWidget.WIDGET_NAME);
    widgetConfig = new DatatableWidgetConfigDialog();

    // And The widget is configured with selection mode MANUALLY
    let objectSelector = widgetConfig.selectObjectSelectTab();

    // And I have selected an object
    let search = objectSelector.getSearch();
    search.getCriteria().getSearchBar().search();
    search.getResults().clickResultItem(8);

    // And I have selected properties 'Title', 'Modified by', 'References'
    widgetConfig.selectObjectDetailsTab().selectProperties(['title', 'emf:modifiedBy', 'emf:references']);

    // When I select related properties 'First name' and 'Last name' for 'Modified by'
    widgetConfig.selectObjectDetailsTab().selectRelationVisibility('emf:modifiedBy', true);
    widgetConfig.selectObjectDetailsTab().selectSubproperty('emf:modifiedBy', 'firstName');
    widgetConfig.selectObjectDetailsTab().selectSubproperty('emf:modifiedBy', 'lastName');

    // And I select related properties 'First name' and 'Last name' for 'References'
    widgetConfig.selectObjectDetailsTab().selectRelationVisibility('emf:references', true);
    widgetConfig.selectObjectDetailsTab().selectSubproperty('emf:references', 'firstName');
    widgetConfig.selectObjectDetailsTab().selectSubproperty('emf:references', 'lastName');

    // When I save widget config
    widgetConfig.save();
    let widget = new DatatableWidget(widgetElement);

    // Then I expect in the widget to have a single row
    let rowsCount = widget.getRowsCount();
    expect(rowsCount).to.eventually.equal(1);

    // And I expect 8 columns
    let header = widget.getHeader();
    let columnsCount = header.getColumnsCount();
    expect(columnsCount).to.eventually.equal(8);
    header.getHeaderLabels().then((headers) => {
      expect(headers).to.eql([ 'Entity', 'Title', 'Modified by', 'Modified by: First name', 'Modified by: Last name', 'References', 'References: First name', 'References: Last name' ]);
    });

    // And I expect single valued related properties to be displayed
    let firstRow = widget.getRow(1);
    const modifiedByFistName = firstRow.getCell(4).getInputField('emf\:modifiedBy\:firstName');
    const modifiedByLastName = firstRow.getCell(5).getInputField('emf\:modifiedBy\:lastName');
    expect(modifiedByFistName.getPreviewValue()).to.eventually.equal('At');
    expect(modifiedByLastName.getPreviewValue()).to.eventually.equal('Manager');

    // And I expect instead of multi-valued related properties to see "Multiple objects" text.
    const referencesFirstName = firstRow.getCell(7).getInputField('emf\:references\:firstName');
    const referencesLastName = firstRow.getCell(8).getInputField('emf\:references\:lastName');
    expect(referencesFirstName.getPreviewValue()).to.eventually.equal('Multiple objects');
    expect(referencesLastName.getPreviewValue()).to.eventually.equal('Multiple objects');

    // And I expect all related properties to be in preview mode
    expect(modifiedByFistName.isPreview()).to.eventually.be.true;
    expect(modifiedByLastName.isPreview()).to.eventually.be.true;
    expect(referencesFirstName.isPreview()).to.eventually.be.true;
    expect(referencesLastName.isPreview()).to.eventually.be.true;
  });

});