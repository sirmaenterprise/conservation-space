"use strict";

let Widget = require('../widget').Widget;
let WidgetConfigDialog = require('../widget').WidgetConfigDialog;
let ObjectSelector = require('./../object-selector/object-selector.js').ObjectSelector;
let PropertiesSelector = require('../properties-selector/properties-selector.js').PropertiesSelector;
let DisplayOptions = require('./display-options.js').DisplayOptions;
let FormWrapper = require('./../../../form-builder/form-wrapper').FormWrapper;
let SandboxPage = require('../../../page-object').SandboxPage;

class ObjectDataWidgetSandboxPage extends SandboxPage {
  open() {
    super.open('/sandbox/idoc/widget/object-data-widget');
    this.insertButton = $('#insert-widget-btn');
    this.modelingMode = $('#modelingMode');
    this.waitUntilOpened();
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.insertButton), DEFAULT_TIMEOUT);
  }

  insertWidget() {
    this.insertButton.click();
  }

  toggleModelingMode() {
    this.modelingMode.click();
  }

  changeSearchDataset(dataset) {
    let dataSetOption = $('#' + dataset);
    browser.wait(EC.visibilityOf(dataSetOption), DEFAULT_TIMEOUT);
    dataSetOption.click();
  }

  getWidget() {
    return new ObjectDataWidget($('.object-data-widget'));
  }
}

class ObjectDataWidget extends Widget {

  constructor(widgetElement) {
    super(widgetElement);
    this.showMoreButton = widgetElement.$('.show-more');
    this.selectedObjectHeader = widgetElement.$('.selected-object-header');
  }

  getShowMoreButton() {
    return this.showMoreButton;
  }

  toggleShowMoreButton() {
    browser.wait(EC.elementToBeClickable(this.showMoreButton), DEFAULT_TIMEOUT);
    return this.showMoreButton.click();
  }

  getForm() {
    let formWrapper = new FormWrapper(this.widgetElement);
    formWrapper.waitUntilVisible();
    return formWrapper;
  }

  isHeaderVisible() {
    return this.selectedObjectHeader.isPresent();
  }

  getObjectProperty(property) {
    return this.widgetElement.$(`#${property}-wrapper a`);
  }
}

class ObjectDataWidgetConfig extends WidgetConfigDialog {

  constructor() {
    super(ObjectDataWidget.WIDGET_NAME);
    this.selectObjectTab = $('.object-data-widget .select-object-tab-handler');
    this.objectDetailsTab = $('.object-data-widget .object-details-tab-handler');
    this.displayOptionsTab = $('.object-data-widget .display-options-tab-handler');
  }

  selectObjectSelectTab() {
    browser.wait(EC.visibilityOf(this.selectObjectTab), DEFAULT_TIMEOUT);
    this.selectObjectTab.click();
    return new ObjectSelector();
  }

  selectObjectDetailsTab() {
    browser.wait(EC.visibilityOf(this.objectDetailsTab), DEFAULT_TIMEOUT);
    this.objectDetailsTab.click();
    let propertiesSelector = new PropertiesSelector();
    propertiesSelector.waitUntilOpened();
    return propertiesSelector;
  }

  selectDisplayOptionsTab() {
    browser.wait(EC.visibilityOf(this.displayOptionsTab), DEFAULT_TIMEOUT);
    this.displayOptionsTab.click();
    let displayOptions = new DisplayOptions();
    displayOptions.waitUntilOpened();
    return displayOptions;
  }

}

ObjectDataWidget.WIDGET_NAME = 'object-data-widget';

module.exports.ObjectDataWidget = ObjectDataWidget;
module.exports.ObjectDataWidgetConfig = ObjectDataWidgetConfig;
module.exports.ObjectDataWidgetSandboxPage = ObjectDataWidgetSandboxPage;