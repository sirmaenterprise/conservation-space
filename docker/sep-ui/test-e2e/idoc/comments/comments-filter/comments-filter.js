'use strict';

var Dialog = require('../../../components/dialog/dialog');
var elementToStopMoving = require('../../../utils/conditions').elementToStopMoving;
var SandboxPage = require('../../../page-object').SandboxPage;

class CommentsFilterSandboxPage extends SandboxPage {

  open() {
    super.open('/sandbox/idoc/comments/comments-filter/');
  }

  isFilterApplied(value) {
    browser.wait(EC.textToBePresentInElement($('.keyword'), value), DEFAULT_TIMEOUT);
  }

  loadFilterData() {
    browser.wait(EC.elementToBeClickable(this.getLoadFilterDataBtn()), DEFAULT_TIMEOUT);
    this.getLoadFilterDataBtn().click();
  }

  getLoadFilterDataBtn() {
    return $('.load-filter');
  }
}

class CommentsFilter {
  constructor(element) {
    this.element = element || $('.comments-filter')
  }

  waitUntilLoaded() {
    browser.wait(EC.visibilityOf(this.element), DEFAULT_TIMEOUT);
  }

  openFilterPanel() {
    this.getOpenFilterDialogButton().click();
    return new CommentsFiltersPanel($('.comments-filter-panel'));
  }

  getOpenFilterDialogButton() {
    let button = this.element.$('.btn-filter-comment');
    browser.wait(EC.elementToBeClickable(button), DEFAULT_TIMEOUT);
    return button;
  }
}

class CommentsFiltersPanel extends Dialog {
  constructor(element) {
    super(element);
    this.waitUntilOpened();
  }

  keywordField() {
    return new Field(this.getKeywordField());
  }

  getKeywordField() {
    var keywordField = this.element.$('.keyword-field');
    browser.wait(EC.visibilityOf(keywordField), DEFAULT_TIMEOUT);
    return keywordField;
  }

  isClearButtonPresent() {
    this.getClearButton();
  }

  clearFilters() {
    var btn = this.getClearButton();
    browser.wait(EC.and(EC.visibilityOf(btn), EC.elementToBeClickable(btn)), DEFAULT_TIMEOUT);
    btn.click();
  }

  getClearButton() {
    var clearButton = this.element.$('.btn-clear-fields');
    // I guess the modal is repositioned and the button changes it's position
    var cond = EC.and(EC.elementToBeClickable(clearButton), elementToStopMoving(clearButton));
    browser.wait(cond, DEFAULT_TIMEOUT);
    return clearButton;
  }

  commentsStatusField() {
    browser.wait(EC.presenceOf(this.getCommentStatusField()), DEFAULT_TIMEOUT);
    return new Field(this.getCommentStatusField());
  }

  getCommentStatusField() {
    return this.element.$('.comment-status-field');
  }

  authorField() {
    browser.wait(EC.presenceOf(this.getAuthorField()), DEFAULT_TIMEOUT);
    return new Field(this.getAuthorField());
  }

  getAuthorField() {
    return this.element.$('.author-field');
  }

  fromDateField() {
    browser.wait(EC.presenceOf(this.getFromDateField()), DEFAULT_TIMEOUT);
    return new Field(this.getFromDateField());
  }

  getFromDateField() {
    return this.element.$('.from-date-field');
  }

  endDateField() {
    browser.wait(EC.presenceOf(this.getEndDateField()), DEFAULT_TIMEOUT);
    return new Field(this.getEndDateField());
  }

  getEndDateField() {
    return this.element.$('.end-date-field');
  }

  filter() {
    this.getFilterButton().click();
    this.waitUntilClosed();
  }

  getFilterButton() {
    var filterBtn = $('.seip-btn-filter');
    var cond = EC.and(EC.elementToBeClickable(filterBtn), elementToStopMoving(filterBtn));
    browser.wait(cond, DEFAULT_TIMEOUT);
    return filterBtn;
  }

}


class Field {
  constructor(element) {
    this.element = element;
  }

  type(text) {
    return this.element.sendKeys(text);
  }

}


module.exports = {
  CommentsFilter: CommentsFilter,
  CommentsFilterSandboxPage: CommentsFilterSandboxPage
};
