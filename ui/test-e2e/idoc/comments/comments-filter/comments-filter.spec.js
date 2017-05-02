var CommentsFilterSandboxPage = require('./comments-filter').CommentsFilterSandboxPage;
var CommentsFilter = require('./comments-filter').CommentsFilter;

describe('Tests for comments filter component', function () {

  var sandboxPage = new CommentsFilterSandboxPage();
  var commentsFilter = new CommentsFilter();

  beforeEach(function () {
    sandboxPage.open();
    commentsFilter.waitUntilLoaded();
  });

  it('should open modal dialog', function () {
    var panel = commentsFilter.openFilterPanel();
    expect(panel.element.isPresent()).to.eventually.be.true;
  });

  it('should contain all filter fields', function () {
    var panel = commentsFilter.openFilterPanel();
    expect(panel.keywordField().element.isPresent()).to.eventually.be.true;
    expect(panel.commentsStatusField().element.isPresent()).to.eventually.be.true;
    expect(panel.authorField().element.isPresent()).to.eventually.be.true;
    expect(panel.fromDateField().element.isPresent()).to.eventually.be.true;
    expect(panel.endDateField().element.isPresent()).to.eventually.be.true;

    expect(panel.clearButton().isPresent()).to.eventually.be.true;
  });

  it('should fill filter fields', function () {
    var panel = commentsFilter.openFilterPanel();
    panel.keywordField().type('testKeyword').then(function () {
      panel.filter();
      expect(sandboxPage.getModelValueForField('keyword')).to.eventually.not.equal('');
    });
  });

  it('should have no filters when clear has been pressed and then filter', function () {
    sandboxPage.loadFilterData();
    var panel = commentsFilter.openFilterPanel();
    panel.clearFilters();
    panel.filter();
    expect(sandboxPage.getModelValueForField('keyword').getText()).to.eventually.equal('');
  });

});