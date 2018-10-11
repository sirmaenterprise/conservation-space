"use strict";

var TestUtils = require('../../../test-utils');
var Widget = require('../widget').Widget;
var WidgetHeader = require('../widget').WidgetHeader;
var FormWrapper = require('../../../form-builder/form-wrapper').FormWrapper;
var InputField = require('../../../form-builder/form-control').InputField;
var ObjectSelector = require('./../object-selector/object-selector.js').ObjectSelector;
var WidgetConfigDialog = require('../widget').WidgetConfigDialog;
var PropertiesSelector = require('../properties-selector/properties-selector.js').PropertiesSelector;
var StaticInstanceHeader = require('../../../instance-header/static-instance-header/static-instance-header').StaticInstanceHeader;
var DTWOptionsTab = require('./datatable-widget-options-tab.js').DTWOptionsTab;
var Pagination = require('../../../search/components/common/pagination');
var SandboxPage = require('../../../page-object').SandboxPage;

class DatatableWidgetSandboxPage extends SandboxPage {
  open() {
    super.open('/sandbox/idoc/widget/datatable-widget');
    this.insertButton = $('#insert-widget-btn');
    this.waitUntilOpened();
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.insertButton), DEFAULT_TIMEOUT);
  }

  insertWidget() {
    this.insertButton.click();
  }

  getWidget() {
    return new DatatableWidget($('.datatable-widget'));
  }
}

class DatatableWidget extends Widget {

  constructor(widgetElement) {
    super(widgetElement);
  }

  getHeader() {
    return new DatatableHeader(this.widgetElement.$('.table-header'));
  }

  getWidgetHeader() {
    return new DatatableWidgetHeader(this.widgetElement);
  }

  /**
   * Find row and return it wrapped in DatatableRow.
   * @param index The index starts from 1.
   * @returns DatatableRow
   */
  getRow(index) {
    const element = this.widgetElement.$('.form-wrapper:nth-child(' + index + ')');
    browser.wait(EC.visibilityOf(element), DEFAULT_TIMEOUT);
    return new DatatableRow(element);
  }

  getRowsCount() {
    return this.widgetElement.$$('.form-wrapper').then((rows) => {
      return rows.length;
    });
  }

  getTooltip() {
    return this.widgetElement.$('.tooltip');
  }

  getFooter() {
    browser.wait(EC.visibilityOf(this.widgetElement.$('.table-footer')), DEFAULT_TIMEOUT);
    return new DatatableFooter(this.widgetElement.$('.table-footer'));
  }

  getToolbar() {
    return new DatatableToolbar(this.widgetElement.$('.table-toolbar'));
  }

  openConfig() {
    this.widgetElement.$('.config-button').click();
    // wait for the config to appear
    new WidgetConfigDialog();
  }

  openWidget() {
    this.getWidgetHeader().getHeaderResults().getTotalResults().click();
    return new DatatableWidgetBasicDialog();
  }

  getErrorMessage(){
    return this.widgetElement.$('.message.error').getText();
  }

  isStriped() {
    let tableBody = this.widgetElement.$('.table-body');
    browser.wait(EC.visibilityOf(tableBody), DEFAULT_TIMEOUT);
    return TestUtils.hasClass(tableBody, 'stripe');
  }
}

class DatatableWidgetHeader extends WidgetHeader{
  getHeaderResults() {
    return new DatatableHeaderResults(this.headerElement.$('.header-results'));
  }

  getHeaderCreateItem() {
    return new DatatableHeaderCreateItem(this.headerElement.$('.datatable-header-create-item'));
  }
}

class DatatableHeaderResults {
  constructor(element) {
    this.element = element;
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  getTotalResults(){
    return this.element.$('.total-results').getText();
  }
}

class DatatableHeaderCreateItem {
  constructor(element) {
    this.element = element;
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  getButton(){
    return this.element.$('.create-item-button');
  }
}

class DatatableHeader {
  constructor(element) {
    this.element = element;
  }

  getCell(index) {
    return this.element.$$('.header-cell').then((columns) => {
      return columns[index];
    });
  }

  /**
   * Returns a TableHeaderCell PO wrapping the header cell at given index.
   * @param index
   * @returns {*}
   */
  getTableHeaderCell(index) {
    return new TableHeaderCell(this.element.$$('.header-cell').get(index) , index);
  }

  /**
   * Returns a promise that resolves with an array containing TableHeaderCell instances for
   * every rendered column.
   * @returns {wdpromise.Promise<any[]>}
   */
  getHeaderCells() {
    return this.element.$$('.header-cell').then((headerCells) => {
      return headerCells.map((el, index) => {
        return new TableHeaderCell(el, index);
      });
    });
  }

  /**
   * Returns a promise that resolves with an array containing the header labels for
   * every rendered column.
   * @returns {wdpromise.Promise<any[]>}
   */
  getHeaderLabels() {
    return this.element.$$('.header-cell').then((headerCells) => {
      let promises = headerCells.map((el, index) => {
        return el.getText();
      });
      return Promise.all(promises).then((labels) => {
        return labels;
      });
    });
  }

  getColumnsCount() {
    return this.element.$$('.header-cell').then((columns) => {
      return columns.length;
    });
  }

  isDisplayed() {
    return this.element.isDisplayed();
  }
}

class TableHeaderCell {
  constructor(element, index) {
    this.element = element;
    this.index = index;
  }

  getTitle() {
    return this.element.$('.title').getText();
  }

  hasSortIcon() {
    return this.element.$('.sort-icon').isPresent();
  }

  isSortAscending() {
    return this.element.$('.sort-icon > .fa-sort-asc').isPresent();
  }

  isSortDescending() {
    return this.element.$('.sort-icon > .fa-sort-desc').isPresent();
  }

  isSortDefault() {
    return this.element.$('.sort-icon > .fa-sort').isPresent();
  }

  toggleSort() {
    let sortElement = this.element.$('.sort-icon');
    browser.wait(EC.elementToBeClickable(sortElement), DEFAULT_TIMEOUT);
    sortElement.click();
  }
}

class TableBodyCell {
  constructor(element) {
    this.element = element;
  }

  // How to get and return proper PO for the underlying form field.
  // - Pass argument and with if-else
  // - Automatically resolve the field type by some criteria: css class
  getField() {
    return this.resolveFieldType(this.element);
  }

  getCell() {
    return this.element;
  }

  getInstanceHeaderCell() {
    var instanceHeader = this.element.$('.instance-header');
    return new StaticInstanceHeader(instanceHeader);
  }

  getInputField() {
    return new InputField(this.element);
  }

  resolveFieldType(cell) {
    var field = cell.$('> span, > div');
    if (hasClass(field, 'input-text-wrapper') || hasClass(field, 'input-textarea-wrapper')) {
      var field = field.$('.form-control');
      return new InputField(field);
    }
  }
}

class DatatableRow {
  constructor(element) {
    this.element = element;
  }

  /**
   * Builds and returns a TableBodyCell object.
   * @param index The index starts from 1.
   * @returns {TableBodyCell}
   */
  getCell(index) {
    var formGroup = this.element.$('.form-group:nth-child(' + index + ')');
    browser.wait(EC.presenceOf(formGroup), DEFAULT_TIMEOUT);
    return new TableBodyCell(formGroup);
  }

  getForm() {
    let formWrapper = new FormWrapper(this.element, true);
    formWrapper.waitUntilVisible();
    return formWrapper;
  }

  getBackgroundColor() {
    return this.element.getCssValue('background-color');
  }
}

class DatatableFooter {
  constructor(element) {
    this.element = element;
  }

  isPaginationVisible() {
    return this.getPaginationElement().isDisplayed();
  }

  getPagination() {
    return new Pagination(this.getPaginationElement());
  }

  getPaginationElement() {
    return this.element.$(Pagination.COMPONENT_SELECTOR);
  }
}

class DatatableToolbar {
  constructor(element) {
    this.element = element;
  }

  filter(keyword) {

  }
}

class DatatableWidgetBasicDialog {

  isSelectObjectsVisable() {
    return $('.datatable-widget-config .select-object-tab-handler').isPresent();
  }

  isObjectDetailstabVisable() {
    return $('.datatable-widget-config .object-details-tab-handler').isPresent();
  }

  isDisplayOptionsVisable() {
    return $('.datatable-widget-config .display-options-tab-handler').isPresent();
  }

  isSearchResultSelectable() {
    return $('.selected-item > .checkbox').isPresent();
  }

}

class DatatableWidgetConfigDialog extends WidgetConfigDialog {

  constructor() {
    super(DatatableWidget.WIDGET_NAME);
    this.selectObjectTab = $('.datatable-widget-config .select-object-tab-handler');
    this.objectDetailsTab = $('.datatable-widget-config .object-details-tab-handler');
    this.displayOptionsTab = $('.datatable-widget-config .display-options-tab-handler');
  }

  selectObjectSelectTab() {
    this.selectObjectTab.click();
    return new ObjectSelector();
  }

  selectObjectDetailsTab() {
    this.objectDetailsTab.click();
    let propertiesSelector = new PropertiesSelector();
    propertiesSelector.waitUntilOpened();
    return propertiesSelector;
  }

  selectDisplayOptionTab() {
    this.displayOptionsTab.click();
    let dtwOptionsTab = new DTWOptionsTab();
    dtwOptionsTab.waitUntilOpened();
    return dtwOptionsTab;
  }

}

DatatableWidget.WIDGET_NAME = 'datatable-widget';

module.exports.DatatableWidgetSandboxPage = DatatableWidgetSandboxPage;
module.exports.DatatableWidget = DatatableWidget;
module.exports.DatatableHeader = DatatableHeader;
module.exports.TableHeaderCell = TableHeaderCell;
module.exports.DatatableWidgetConfigDialog = DatatableWidgetConfigDialog;