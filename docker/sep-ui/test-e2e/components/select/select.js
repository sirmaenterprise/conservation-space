'use strict';

class Select {
  constructor(selector) {
    this.element = $(selector);
  }

  /**
   * Returns the number of available options in select's dropdown
   * @returns {wdpromise.Promise<T>}
   */
  getNumberOfDropdownOptions() {
    let selectElement = this.element.$('span.select2-selection');
    // open dropdown
    selectElement.click();
    return $$('.select2-container .select2-results li:not(.select2-results__message)').then((items) => {
      // close dropdown
      selectElement.click();
      return items.length;
    });
  }
}

module.exports.Select = Select;