"use strict";

var AdvancedSearch = require('../advanced/advanced-search.js').AdvancedSearch;
var BasicSearchCriteria = require('./basic-search-criteria.js').BasicSearchCriteria;

const BASIC_SEARCH_OPTION_CLASS = '.basic-search-option';
const ADVANCED_SEARCH_OPTION_CLASS = '.advanced-search-option';

/**
 * Page object for the MixedSearchCriteria.
 */
class MixedSearchCriteria {

  getBasicSearchOption() {
    return $('.mixed-search-criteria ' + BASIC_SEARCH_OPTION_CLASS);
  }

  getAdvancedSearchOption() {
    return $('.mixed-search-criteria ' + ADVANCED_SEARCH_OPTION_CLASS);
  }

  getCheckedOption() {
    return $('.mixed-search-criteria input:checked');
  }

  clickOption(option) {
    browser.wait(EC.elementToBeClickable(option), DEFAULT_TIMEOUT);
    option.click();
  }

  getAdvancedSearchForm() {
    return $(AdvancedSearch.COMPONENT_SELECTOR);
  }

  getBasicSearchForm() {
    return $(BasicSearchCriteria.COMPONENT_SELECTOR);
  }

}

module.exports = MixedSearchCriteria;