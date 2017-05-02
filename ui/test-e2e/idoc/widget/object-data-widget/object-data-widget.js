"use strict";

var Widget = require('../widget').Widget;
var WidgetConfigDialog = require('../widget').WidgetConfigDialog;
var ObjectSelector = require('./../object-selector/object-selector.js').ObjectSelector;
var PropertiesSelector = require('../properties-selector/properties-selector.js').PropertiesSelector;
var DisplayOptions = require('./display-options.js').DisplayOptions;
var FormWrapper = require('./../../../form-builder/form-wrapper').FormWrapper;
var SandboxPage = require('../../../page-object').SandboxPage;

class ObjectDataWidgetSandboxPage extends SandboxPage {
  open() {
    super.open('/sandbox/idoc/widget/object-data-widget');
    this.insertButton = $('#insert-widget-btn');
    this.waitUntilOpened();
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.insertButton), DEFAULT_TIMEOUT);
  }

  insertWidget() {
    this.insertButton.click();
  }

  changeSearchDataset(dataset) {
    var dataSetOption = $('#' + dataset);
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
    return this.showMoreButton.click();
  }

  getForm() {
    var formWrapper = new FormWrapper(this.widgetElement);
    formWrapper.waitUntilVisible();
    return formWrapper;
  }

  isHeaderVisible() {
    return this.selectedObjectHeader.isPresent();
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