"use strict";

const PAGE = ".page";
const FIRST_PAGE = ".first-page";
const LAST_PAGE = ".last-page";

const DISABLED_CLASS = "disabled";

/**
 * Page object for the pagination component.
 *
 * TODO: Refactor to use element instead of selector CMF-19383
 *
 * @author Mihail Radkov
 */
class Pagination {

  constructor(selector) {
    if (!selector) {
      throw new Error('Cannot instantiate PO without wrapper selector!');
    }
    this.selector = selector;
  }

  waitUntilOpened() {
    var pagination = $(this.selector).$('.seip-pagination');
    browser.wait(EC.visibilityOf(pagination), DEFAULT_TIMEOUT);
  }

  /**
   * Waits unit at least one of the pagination buttons is active meaning the component is enabled and ready to use.
   */
  waitForActiveButton() {
    var activeButton = $(this.selector).$('.page.active');
    browser.wait(EC.visibilityOf(activeButton), DEFAULT_TIMEOUT);
  }

  getPages() {
    return $(this.selector).all(by.css(PAGE));
  }

  getFirstPageButton() {
    return $(this.selector).$(FIRST_PAGE);
  }

  getLastPageButton() {
    return $(this.selector).$(LAST_PAGE);
  }

  getPageButton(page) {
    return $(this.selector).$(PAGE + '[page="' + page + '"]');
  }

  getPageButtonLink(page) {
    return this.getPageButton(page).element(by.css('a'));
  }

  goToFirstPage() {
    return $(this.selector).$(FIRST_PAGE + ' a').click();
  }

  goToPage(page) {
    return this.getPageButtonLink(page).click();
  }

  goToLastPage() {
    return $(this.selector).$(LAST_PAGE + ' a').click();
  }

  getDisabledClass() {
    return DISABLED_CLASS;
  }

  getFirstPageButtonSelector() {
    return FIRST_PAGE;
  }

  getLastPageButtonSelector() {
    return LAST_PAGE;
  }

}
Pagination.COMPONENT_SELECTOR = '.seip-pagination';

module.exports = Pagination;