"use strict";

class DisplayOptions {

  constructor() {
    this._element = $('.display-options-tab');
    this.showFieldsBorder = $('.display-options-tab .with-border');
    this.displayShowMore = $('.display-options-tab .display-show-more');
    this.showRegionNames = $('.display-options-tab .show-region-names');
    this.hideIcons = $('.display-options-tab .hide-icons');
  }

  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this._element), DEFAULT_TIMEOUT);
  }

  isSelected(checkboxElement) {
    return checkboxElement.getAttribute('checked').then((checked) => {
      return !!checked;
    });
  }

  isSelectedShowMore() {
    return this.isSelected(this.displayShowMore.$('input'));
  }

  isSelectedShowRegionNames() {
    return this.isSelected(this.showRegionNames.$('input'));
  }

  isSelectedShowFieldsBorder() {
    return this.isSelected(this.showFieldsBorder.$('input'));
  }

  getShowMoreButton() {
    return this.displayShowMore;
  }

  toggleShowFieldsBorder() {
    this.showFieldsBorder.click();
    return this;
  }

  toggleDisplayShowMore() {
    this.displayShowMore.click();
    return this;
  }

  toggleDisplayIcons() {
    this.hideIcons.click();
    return this;
  }

  toggleShowRegionNames() {
    this.showRegionNames.click();
    return this;
  }

  toggleLabelAbove() {
    this.toggleLabelPosition('label-above');
  }

  toggleLabelLeft() {
    this.toggleLabelPosition('label-left');
  }

  toggleLabelHidden() {
    this.toggleLabelPosition('label-hidden');
  }

  toggleLabelTextLeft() {
    this.toggleLabelTextAlign('label-text-left');
  }

  toggleLabelTextRight() {
    this.toggleLabelTextAlign('label-text-right');
  }

  toggleShowDefaultHeader() {
    this.toggleShowInstanceLink('default_header');
  }

  toggleLabelPosition(position) {
    this._element.$('input[name="labelPosition"][value="' + position + '"]').element(by.xpath('..')).click();
  }

  toggleLabelTextAlign(align) {
    this._element.$('input[name="labelTextAlign"][value="' + align + '"]').element(by.xpath('..')).click();
  }

  toggleShowInstanceLink(link) {
    this._element.$('input[name="instanceLink"][value="' + link + '"]').element(by.xpath('..')).click();
  }

  getLabelPosition() {
    return this._element.$('input[name="labelPosition"]:checked').getAttribute('value');
  }

  isLabelPositionPanelVisible() {
    return this._element.$('.label-position-panel').isDisplayed();
  }
}

module.exports.DisplayOptions = DisplayOptions;