'use strict';

var Widget = require('../widget').Widget;
var WidgetConfigDialog = require('../widget').WidgetConfigDialog;
var ObjectSelector = require('../object-selector/object-selector').ObjectSelector;
var SingleSelectMenu = require('../../../form-builder/form-control').SingleSelectMenu;
var SandboxPage = require('../../../page-object').SandboxPage;

const USER_ACTIVITY_SELECTOR = '.user-activity-entry';

class RecentActivitiesSandboxPage extends SandboxPage {

  constructor() {
    super();
    this.insertButton = $('#insert-widget-btn');
    this.modellingToggle = $('#modelling-toggle');
  }

  open() {
    super.open('/sandbox/idoc/widget/recent-activities');
    browser.wait(EC.visibilityOf(this.insertButton), DEFAULT_TIMEOUT);
  }

  insert() {
    this.insertButton.click();
    return new RecentActivitiesConfigDialog();
  }

  getWidget() {
    return new RecentActivities($('[widget="recent-activities"]'));
  }

  toggleModellingMode() {
    this.modellingToggle.click();
  }
}

class RecentActivities extends Widget {

  constructor(widgetElement) {
    super(widgetElement);
    this.waitToAppear();
  }

  /**
   * Waits for activities to be rendered.
   */
  waitForActivities() {
    var activities = this.widgetElement.$(USER_ACTIVITY_SELECTOR);
    browser.wait(EC.visibilityOf(activities), DEFAULT_TIMEOUT);
  }

  getItemsCount() {
    return this.widgetElement.all(by.css(USER_ACTIVITY_SELECTOR)).count();
  }

  waitStalenessOfPagination() {
    browser.wait(EC.stalenessOf(this.pagination), DEFAULT_TIMEOUT);
  }

  waitForPagination() {
    browser.wait(EC.presenceOf(this.pagination), DEFAULT_TIMEOUT);
  }

  get pagination() {
    return this.widgetElement.$('.seip-pagination');
  }
}

class RecentActivitiesConfigDialog extends WidgetConfigDialog {

  constructor() {
    super(RecentActivities.WIDGET_NAME);
  }

  switchToSelectObjectTab() {
    var tab = this.dialogElement.$('.select-object-tab > a');
    browser.wait(EC.elementToBeClickable(tab), DEFAULT_TIMEOUT);
    tab.click();
  }

  switchToDisplayOptionsTab() {
    var tab = this.dialogElement.$('.display-options-tab > a');
    browser.wait(EC.elementToBeClickable(tab), DEFAULT_TIMEOUT);
    tab.click();
  }

  getObjectSelector() {
    return new ObjectSelector();
  }

  getPageSizeConfigSelect() {
    return new SingleSelectMenu($('.page-size-config'));
  }

  selectAutomatically() {
    element.all(by.css('.inline-group .radio')).get(1).click();
    browser.wait(EC.visibilityOf($('.seip-search-wrapper')), DEFAULT_TIMEOUT);
  }
}

RecentActivities.WIDGET_NAME = 'recent-activities';

module.exports = {
  RecentActivitiesSandboxPage,
  RecentActivities,
  RecentActivitiesConfigDialog
};