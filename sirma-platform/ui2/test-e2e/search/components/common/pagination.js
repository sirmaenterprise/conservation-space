'use strict';

let PageObject = require('../../../page-object').PageObject;

const PAGE = '.page';
const FIRST_PAGE = '.first-page';
const LAST_PAGE = '.last-page';

const DISABLED_CLASS = 'disabled';

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
   * Waits unit at least one of the pagination buttons is active meaning the component is enabled and ready to use.
   */
  waitForActiveButton() {
    var activeButton = this.element.$('.page.active');
    browser.wait(EC.visibilityOf(activeButton), DEFAULT_TIMEOUT);
  }

  getPages() {
    return this.element.all(by.css(PAGE));
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
    return this.getPageButton(page).element(by.css('a'));
  }

  goToFirstPage() {
    return this.element.$(FIRST_PAGE + ' a').click();
  }

  goToPage(page) {
    return this.getPageButtonLink(page).click();
  }

  goToLastPage() {
    return this.element.$(LAST_PAGE + ' a').click();
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