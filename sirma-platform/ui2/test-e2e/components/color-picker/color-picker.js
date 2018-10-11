'use strict';

class ColorPicker {
  constructor(element) {
    if (!element) {
      throw new Error('Color picker element must be provided to the constructor!');
    }
    this.element = element;
  }

  waitUntilOpen() {
    browser.wait(EC.presenceOf(this.element), DEFAULT_TIMEOUT);
  }

  /**
   * Select given color.
   * @param newColor should be rgb string. For example: 'rgb(182, 215, 168)'
   */
  selectColor(newColor) {
    browser.wait(EC.elementToBeClickable(this.element), DEFAULT_TIMEOUT);
    this.element.click();
    let colorPalette = this.getColorPalette();
    colorPalette.$(`[data-color="${newColor}"]`).click();
  }

  /**
   * Multiple pickers create multiple palette elements. Returns active one.
   */
  getColorPalette() {
    let colorPalette = $('.sp-container:not(.sp-hidden)');
    browser.wait(EC.visibilityOf(colorPalette), DEFAULT_TIMEOUT);
    return colorPalette;
  }
}

module.exports = ColorPicker;