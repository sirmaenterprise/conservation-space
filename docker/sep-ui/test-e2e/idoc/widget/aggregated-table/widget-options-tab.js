"use strict";

var RadioButtonGroup = require('../../../form-builder/form-control').RadioButtonGroup;

class WidgetOptionsTab {

  constructor() {
    this._element = $('.display-options-tab');
    this.gridRadioBtnSelector = '.display-options-tab .grid-group';
    this.radioGroup = new RadioButtonGroup();
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this._element), DEFAULT_TIMEOUT);
  }

  selectGridOption(optionToSelect){
    this.radioGroup.selectValue(this.gridRadioBtnSelector, optionToSelect);
  }

  isGridOptionSelected(selectedOption){
    return selectedOption === this.radioGroup.getSelectedValue(this.gridRadioBtnSelector);
  }

}
module.exports.WidgetOptionsTab = WidgetOptionsTab;