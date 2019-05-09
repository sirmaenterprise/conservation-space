'use strict';

let PageObject = require('../../../page-object').PageObject;
let hasClass = require('../../../test-utils').hasClass;

const PAGE = '.page';
const FIRST_PAGE = '.first-page';
const LAST_PAGE = '.last-page';
const DISABLED = 'disabled';
/**
 * Page object for the pagination component.
 *
 * @author Mihail Radkov
 */
class Pagination extends PageObject {

  constructor(element) {
    super(element);
  }

  waitUntilOpened() {
    browser.wait(EC.presenceOf(this.element), DEFAULT_TIMEOUT);
  }

  waitUntilVisible() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  waitUntilNotVisible() {
    browser.wait(EC.invisibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  /**
   * Waits unit at least one of the pagination buttons is active & not disabled meaning the component is ready to use.
   */
  waitForActiveButton() {
    let activeButton = this.element.$('.page.active:not(.disabled)');
    browser.wait(EC.visibilityOf(activeButton), DEFAULT_TIMEOUT);
  }

  getPages() {
    return this.element.$$(PAGE);
  }

  getFirstPageButton() {
    return this.element.$(FIRST_PAGE);
  }

  getLastPageButton() {
    return this.element.$(LAST_PAGE);
  }

  getPageButton(page) {
    return this.element.$(PAGE + '[page="' + page + '"]');
  }

  getPageButtonLink(page) {
    return this.getPageButton(page).$('a');
  }

  goToFirstPage() {
    this.getFirstPageButton().$('a').click();
    this.waitForActiveButton();
  }

  goToPage(page) {
    this.getPageButtonLink(page).click();
    this.waitForActiveButton();
  }

  goToLastPage() {
    this.getLastPageButton().$('a').click();
    this.waitForActiveButton();
  }

  isFirstPageButtonDisabled() {
    return hasClass(this.getFirstPageButton(), DISABLED);
  }

  istLastPageButtonDisabled() {
    return hasClass(this.getLastPageButton(), DISABLED);
  }

}

Pagination.COMPONENT_SELECTOR = '.seip-pagination';

module.exports = Pagination;