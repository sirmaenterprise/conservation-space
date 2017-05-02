"use strict";

var InputField = require('../../../form-builder/form-control').InputField;

class PropertiesSelector {

  constructor() {
    this._element = $('.properties-selector');
    this.selectAll = $('.properties-selector .select-all');
    this.deselectAll = $('.properties-selector .deselect-all');
    this.filterField = new InputField();
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this._element), DEFAULT_TIMEOUT);
  }

  selectAllProperties() {
    this.selectAll.click();
    return this;
  }

  /**
   * Returns a promise that resolves with an array containing all selected properties labels.
   * @returns {wdpromise.Promise<any[]>}
   */
  getAllPropertiesLabels() {
    return this._element.$$('.checkbox').then((checkboxes) => {
      let promises = checkboxes.map((el) => {
        return el.getText();
      });
      return Promise.all(promises).then((labels) => {
        return labels;
      });
    })
  }

  deselectAllProperties() {
    this.deselectAll.click();
    return this;
  }

  selectProperty(identifier) {
    this._element.$('[data-identifier=' + identifier + ']').click();
  }

  selectProperties(identifiers) {
    identifiers.forEach((identifier) => {
      this.selectProperty(identifier);
    });
  }

  filter(propertyTitle) {
    this.filterField.clearValue('.properties-selector .filter-field')
      .then(() => {
        this.filterField.setValue('.properties-selector .filter-field', propertyTitle);
      });
  }

  hasGroupFor(definitionId) {
    return this._element.$(`.object-details-tab .panel[data-definition-id='${definitionId}'] .panel-body`).isPresent();
  }
}

module.exports.PropertiesSelector = PropertiesSelector;