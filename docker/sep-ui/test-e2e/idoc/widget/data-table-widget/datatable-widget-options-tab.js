"use strict";

var RadioButtonGroup = require('../../../form-builder/form-control').RadioButtonGroup;
var SingleSelectMenu = require('../../../form-builder/form-control').SingleSelectMenu;

/**
 * PO for data table widget options tab. Contains logic for picking specific options for different widget configurations.
 *
 * @author A. Kunchev
 */
class DTWOptionsTab {

  constructor() {
    this._element = $('.display-options-tab');
    this.headersRadioBtnSelector = '.display-options-tab .headers-group';
    this.gridRadioBtnSelector = '.display-options-tab .grid-group';
    this.radioGroup = new RadioButtonGroup();
    this.pageSize = $('.display-options-tab .page-size');
    this.showFirstPageOnly = $('.display-options-tab .show-first-page-only');
    this.displayTableHeaderRow = $('.display-options-tab .display-table-header-row');
    this.stripeRows = $('.display-options-tab .stripe-rows');
    this.hideIcons = $('.display-options-tab .hide-icons');
    this.displayCreateAction = $('.display-options-tab .display-create-action');
    this.exportToMsExcelEnabled = $('.display-options-tab .export-to-ms-excel-enabled');
  }

  /**
   * Waits for the element to be loaded.
   */
  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this._element), DEFAULT_TIMEOUT);
  }

  /**
   * Selects one of the options for the object headers. The options are: <br />
   *  - default_header <br />
   *  - compact_header <br />
   *  - breadcrumb_header <br />
   *  - none
   *
   * @param optionToSelect the option, which should be selected
   */
  selectHeaderToBeDisplayed(optionToSelect) {
    browser.wait(EC.visibilityOf(element(by.css(this.headersRadioBtnSelector))), DEFAULT_TIMEOUT);
    this.radioGroup.selectValue(this.headersRadioBtnSelector, optionToSelect);
  }

  /**
   * Checks if specific option for objects headers is selected. The options could be: <br />
   *  - default_header <br />
   *  - compact_header <br />
   *  - breadcrumb_header <br />
   *  - none
   *
   * @param selectedOption the option that will be checked
   * @return {boolean} true if the option is selected, false otherwise
   */
  isHeaderOptionSelected(selectedOption) {
    return selectedOption === this.radioGroup.getSelectedValue(this.headersRadioBtnSelector);
  }

  toggleShowFirstPageOnly() {
    this.showFirstPageOnly.click();
    return this;
  }

  toggleExportToMsExcelEnabled() {
    this.exportToMsExcelEnabled.click();
    return this;
  }

  toggleDisplayTableHeaderRow() {
    this.displayTableHeaderRow.click();
    return this;
  }

  toggleDisplayIcons() {
    this.hideIcons.click();
    return this;
  }

  toggleDisplayCreateAction() {
    this.displayCreateAction.click();
    return this;
  }

  isDisplayTableHeaderRowSelected() {
    return this.displayTableHeaderRow.$('input').getAttribute('checked').then((checked) => {
      return !!checked;
    });
  }

  getPageSize() {
    return new SingleSelectMenu(this.pageSize);
  }

  isHeaderOptionSelected(selectedOption) {
    return selectedOption === this.radioGroup.getSelectedValue(this.headersRadioBtnSelector);
  }

  selectGridOption(optionToSelect) {
    this.radioGroup.selectValue(this.gridRadioBtnSelector, optionToSelect);
  }

  isGridOptionSelected(selectedOption) {
    return selectedOption === this.radioGroup.getSelectedValue(this.gridRadioBtnSelector);
  }

  toggleStripeRows() {
    this.stripeRows.click();
    return this;
  }

  isStripeRowsSelected() {
    return this.stripeRows.$('input').getAttribute('checked').then((checked) => {
      return !!checked;
    });
  }
}
module.exports.DTWOptionsTab = DTWOptionsTab;