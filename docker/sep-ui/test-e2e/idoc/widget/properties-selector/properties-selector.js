'use strict';

let InputField = require('../../../form-builder/form-control').InputField;

class PropertiesSelector {

  constructor() {
    // Properties selector is a reusable component and is used in different contexts in some of which there might be more
    // than one properties selector on the page. Usually the main selector is parent of the others and those nested once
    // are accessible through the SubpropertiesSelector PO.
    this._element = $$('.properties-selector').first();
    this.selectAll = $$('.properties-selector .select-all').first();
    this.deselectAll = $$('.properties-selector .deselect-all').first();
    this.filterField = new InputField();
  }

  getSubpropertiesSelector() {
    return new SubpropertiesSelector();
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this._element), DEFAULT_TIMEOUT);
  }

  escapeId(id) {
    return id.replace(/:/g, '\\:');
  }

  selectAllProperties() {
    browser.wait(EC.elementToBeClickable(this.selectAll), DEFAULT_TIMEOUT);
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
    });
  }

  deselectAllProperties() {
    browser.wait(EC.elementToBeClickable(this.deselectAll), DEFAULT_TIMEOUT);
    this.deselectAll.click();
    return this;
  }

  selectProperty(identifier) {
    let escapedIdentifier = this.escapeId(identifier);
    const selector = `[data-identifier=${escapedIdentifier}]`;
    let propertyElement = this._element.$(selector);
    browser.wait(EC.elementToBeClickable(propertyElement), DEFAULT_TIMEOUT);
    propertyElement.click();
  }

  selectProperties(identifiers) {
    identifiers.forEach((identifier) => {
      this.selectProperty(identifier);
    });
  }

  selectRelationVisibility(identifier, showProperties) {
    let visibilityTypeSelector = showProperties ? 'related-object-properties' : 'related-object-header';
    let escapedIdentifier = this.escapeId(identifier);
    const selector = `[data-identifier=${escapedIdentifier}] ~ [data-identifier=${visibilityTypeSelector}]`;
    let propertyElement = this._element.$(selector);
    browser.wait(EC.elementToBeClickable(propertyElement), DEFAULT_TIMEOUT);
    propertyElement.click();
  }

  selectSubproperty(identifier, definition) {
    let subpropertiesSelector = this.getSubpropertiesSelector().getPropertiesSelector(identifier);
    subpropertiesSelector.selectProperty(definition);
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

class SubpropertiesSelector {

  constructor() {
    this._element = $('.sub-properties-selector');
  }

  escapeId(id) {
    return id.replace(/:/g, '\\:');
  }

  getPropertiesSelector(definition) {
    let escapedIdentifier = this.escapeId(definition);
    let propertiesSelector = new PropertiesSelector();
    propertiesSelector._element = this._element.$(`#${escapedIdentifier}`);
    propertiesSelector.selectAll = this._element.$(`#${escapedIdentifier} .select-all`);
    propertiesSelector.deselectAll = this._element.$(`#${escapedIdentifier} .deselect-all`);
    return propertiesSelector;
  }
}

module.exports.PropertiesSelector = PropertiesSelector;