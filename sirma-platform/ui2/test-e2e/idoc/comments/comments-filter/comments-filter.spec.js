'use strict';

var CommentsFilterSandboxPage = require('./comments-filter').CommentsFilterSandboxPage;
var CommentsFilter = require('./comments-filter').CommentsFilter;
var TestUtils = require('../../../test-utils');

describe('Tests for comments filter component', function () {

  var sandboxPage = new CommentsFilterSandboxPage();
  var commentsFilter = new CommentsFilter();

  beforeEach(function () {
    sandboxPage.open();
    commentsFilter.waitUntilLoaded();
  });

  it('should open modal dialog', function () {
    commentsFilter.openFilterPanel();
  });

  it('should contain all filter fields', function () {
    var panel = commentsFilter.openFilterPanel();
    expect(panel.keywordField().element.isPresent()).to.eventually.be.true;
    expect(panel.commentsStatusField().element.isPresent()).to.eventually.be.true;
    expect(panel.authorField().element.isPresent()).to.eventually.be.true;
    expect(panel.fromDateField().element.isPresent()).to.eventually.be.true;
    expect(panel.endDateField().element.isPresent()).to.eventually.be.true;

    panel.isClearButtonPresent();
  });

  it('should fill filter fields', function () {
    var panel = commentsFilter.openFilterPanel();
    var keywordField = panel.keywordField();
    keywordField.type(protractor.Key.NULL);
    keywordField.type('testKeyword');
    panel.filter();
    sandboxPage.isFilterApplied('testKeyword');
  });

  it('should have no filters when clear has been pressed and then filter', function () {
    sandboxPage.loadFilterData();
    var panel = commentsFilter.openFilterPanel();
    panel.clearFilters();
    panel.filter();
    sandboxPage.isFilterApplied('');
  });

  it('should indicate that a filter is applied', function () {
    sandboxPage.loadFilterData();
    let panel = commentsFilter.openFilterPanel();
    panel.filter();
    let filterButton = commentsFilter.getOpenFilterDialogButton();
    expect(TestUtils.hasClass(filterButton, 'filtered')).to.eventually.be.true;
  });

  it('should indicate that a filter is not applied', function () {
    sandboxPage.loadFilterData();
    let panel = commentsFilter.openFilterPanel();
    panel.clearFilters();
    panel.filter();
    let filterButton = commentsFilter.getOpenFilterDialogButton();
    expect(TestUtils.hasClass(filterButton, 'filtered')).to.eventually.be.false;
  });
});