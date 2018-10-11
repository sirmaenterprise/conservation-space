"use strict";

var PageObject = require('../../../page-object').PageObject;
var SandboxPage = require('../../../page-object').SandboxPage;
var Button = require('../../../form-builder/form-control').Button;

const SANDBOX_URL = '/sandbox/search/components/common/results-toolbar';
const BASE_RESULTS_TOOLBAR = '#base-results-toolbar';

class ResultsToolbarSandboxPage extends SandboxPage {

  open() {
    super.open(SANDBOX_URL);
  }

  getResultsToolbar() {
    return new ResultsToolbar($(BASE_RESULTS_TOOLBAR));
  }

  clickSearchButton() {
    return new Button($('#search')).click();
  }

  clickNextPageButton() {
    return new Button($('#next-page')).click();
  }

  clickPrevPageButton() {
    return new Button($('#prev-page')).click();
  }

  clickToggleTypeButton() {
    return new Button($('#toggle-type')).click();
  }

  clickToggleFtsButton() {
    return new Button($('#toggle-fts')).click();
  }

  clickToggleContextButton() {
    return new Button($('#toggle-context')).click();
  }

  clickToggleResultsButton() {
    return new Button($('#toggle-results')).click();
  }
}

/**
 * Page object for the results toolbar component
 *
 * @author Svetlozar Iliev
 */
class ResultsToolbar extends PageObject {

  constructor(element) {
    super(element);
  }

  /**
   * Waits until the search results form is loaded and visible.
   */
  waitUntilOpened() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  getMessage(index) {
    // get all messages  wrapped inside spans by index
    return this.element.all(by.css('.message-wrapper > span')).get(index);
  }

  getBoundsMessage() {
    // first span should be the results message
    return this.getMessage(0).$('.search-bounds');
  }

  getCountMessage() {
    // maximum count of found results
    return this.element.$('.search-count');
  }

  getTypeMessage() {
    // second span should be the type message
    return this.getMessage(1).$('.search-type');
  }

  getContextMessage() {
    // third span should be the context message
    return this.getMessage(2).$('.instance-header');
  }

  getFtsMessage() {
    return this.getMessage(3).$('.text-info');
  }

  getBoundsMessageText() {
    return this.getBoundsMessage().getText();
  }

  getCountMessageText() {
    return this.getCountMessage().getText();
  }

  getTypeMessageText() {
    return this.getTypeMessage().getText();
  }

  getContextMessageText() {
    return this.getContextMessage().getText();
  }

  getFtsMessageText() {
    return this.getFtsMessage().getText();
  }
}

ResultsToolbar.COMPONENT_SELECTOR = '.results-toolbar';

module.exports = {
  ResultsToolbarSandboxPage,
  ResultsToolbar
};