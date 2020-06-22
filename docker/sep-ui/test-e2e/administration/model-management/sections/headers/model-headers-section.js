'use strict';

let hasClass = require('../../../../test-utils.js').hasClass;
let PageObject = require('../../../../page-object').PageObject;
let CollapsiblePanel = require('../../../../components/collapsible-panel').CollapsiblePanel;
let DropdownMenu = require('../../../../components/dropdownmenu/dropdown-menu').DropdownMenu;
let Sourcearea = require('../../../../components/sourcearea/sourcearea').Sourcearea;
let ModelControls = require('../../../../administration/model-management/model-controls').ModelControls;
let Dialog = require('../../../../components/dialog/dialog');

/**
 * Wrapper for the headers tab body. Provides access to rendered header panels and operates controls in the section, like
 * changing the language.
 */
class ModelHeadersSection extends PageObject {

  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  findPanel(id) {
    let panel = this.element.$(`#${id}`);
    browser.wait(EC.visibilityOf(panel), DEFAULT_TIMEOUT);
    return panel;
  }

  /**
   * Expands requested header view panel and returns page object that wraps it.
   *
   * @param headerId The id of the header rendered in the requested panel.
   * @return {HeaderViewPanel}
   */
  getHeaderViewPanel(headerId) {
    let headerViewPanel = new HeaderViewPanel(this.findPanel(headerId));
    headerViewPanel.expand();
    return headerViewPanel;
  }

  getLanguageSelector() {
    return new LanguageSelector(this.headerControls.$('.language-select-control'));
  }

  getModelControls() {
    return new ModelControls(this.headerDetails.$('.model-controls'));
  }

  get headerDetails() {
    let details = this.element.$('.headers-details');
    browser.wait(EC.visibilityOf(details), DEFAULT_TIMEOUT);
    return details;
  }

  get headerControls() {
    let controls = this.headerDetails.$('.header-controls');
    browser.wait(EC.visibilityOf(controls), DEFAULT_TIMEOUT);
    return controls;
  }
}

/**
 * Wrapper for simple dropdown menu.
 */
class LanguageSelector extends DropdownMenu {

  constructor(element) {
    super(element);
  }

  getSelectedLanguage() {
    return this.getTriggerButton().getText();
  }

  getAvailableLanguages() {
    let languages = this.open().getActions().map((item) => {
      return item.getText();
    });
    this.close();
    return languages;
  }

  selectLanguage(language) {
    this.open();
    let menuItem = this.getActionContainer().$(`li.${language}`);
    browser.wait(EC.visibilityOf(menuItem), DEFAULT_TIMEOUT, `Language ${language} should be present in the menu!`);
    menuItem.click();
    this.isClosed();
  }
}

LanguageSelector.LANGUAGE_EN = 'en';
LanguageSelector.LANGUAGE_BG = 'bg';
LanguageSelector.LANGUAGE_DE = 'de';
LanguageSelector.LANGUAGE_RO = 'ro';
LanguageSelector.LANGUAGE_FI = 'fi';

/**
 * Wrapper for a panel holding a single header component. Provides access to the header itself and operates controls which
 * are in the panel, like expand/collapse, restore inherited, apply default.
 */
class HeaderViewPanel extends CollapsiblePanel {

  constructor(element) {
    super(element);
    this.sourcearea = new Sourcearea(this.element.$(Sourcearea.SELECTOR));
  }

  getHeaderTypeTitle() {
    return this.element.$('.header-type-title');
  }

  getInheritanceFlag() {
    return this.element.$('.inherited');
  }

  getRestoreInheritedButton() {
    return this.element.$('.restore-header');
  }

  getCopyDefaultValueButton() {
    return this.element.$('.copy-default');
  }

  getModelHeaderAttribute() {
    return this.element.$('.model-header-attribute');
  }

  setHeaderValue(value) {
    this.sourcearea.setValue(value);
    return this;
  }

  getHeaderValue() {
    return this.sourcearea.getValue();
  }

  hasTitle(title) {
    browser.wait(EC.textToBePresentInElement(this.getHeaderTypeTitle(), title), DEFAULT_TIMEOUT, 'Header view panel should have title!');
  }

  isHeaderInherited(inherited) {
    let condition = inherited ? 'visibilityOf' : 'invisibilityOf';
    browser.wait(EC[condition](this.getInheritanceFlag()), DEFAULT_TIMEOUT, `Header ${inherited ? 'should' : 'shouldn\'t'} be inherited!`);
  }

  isHeaderInheritedFrom(inheritedFrom) {
    browser.wait(EC.textToBePresentInElement(this.getInheritanceFlag(), inheritedFrom), DEFAULT_TIMEOUT, `Header should inherit from ${inheritedFrom}!`);
  }

  canBeRestored() {
    return this.getRestoreInheritedButton().isPresent();
  }

  canCopyDefaultValue() {
    return this.getCopyDefaultValueButton().isPresent();
  }

  restoreHeader() {
    let button = this.getRestoreInheritedButton();
    browser.wait(EC.visibilityOf(button), DEFAULT_TIMEOUT, 'Copy default value button should be visible!');
    button.click();
    return this.getConfirmationDialog();
  }

  getConfirmationDialog() {
    let dialog = new Dialog($(Dialog.COMPONENT_SELECTOR));
    dialog.waitUntilOpened();
    return dialog;
  }

  copyDefaultValue() {
    let button = this.getCopyDefaultValueButton();
    browser.wait(EC.visibilityOf(button), DEFAULT_TIMEOUT, 'Copy default value button should be visible!');
    button.click();
  }

  getPanelHeading() {
    return this.element.$('.panel-heading');
  }

  isInvalid() {
    return hasClass(this.getPanelHeading(), 'has-error');
  }

  isDirty() {
    return hasClass(this.getPanelHeading(), 'dirty-model');
  }

  isMandatory() {
    return this.element.$$('.validation-rule-error').isPresent();
  }

}

ModelHeadersSection.COMPONENT_SELECTOR = '.section.model-headers';

module.exports.ModelHeadersSection = ModelHeadersSection;
module.exports.HeaderViewPanel = HeaderViewPanel;
module.exports.LanguageSelector = LanguageSelector;
