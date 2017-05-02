"use strict";

var SingleSelectMenu = require('../../../form-builder/form-control').SingleSelectMenu;

class ReportConfigTab {

  constructor() {
    this._element = $('.report-configuration-tab');
    this.orderBySelectClass = '.report-configuration-tab .group-by-select';
    browser.wait(EC.visibilityOf($(this.orderBySelectClass)), DEFAULT_TIMEOUT);
    this.orderBySelect = new SingleSelectMenu($(this.orderBySelectClass));
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this._element), DEFAULT_TIMEOUT);
  }

  selectOption(name) {
    this.orderBySelect.selectOption(name);
  }
}

module.exports.ReportConfigTab = ReportConfigTab;