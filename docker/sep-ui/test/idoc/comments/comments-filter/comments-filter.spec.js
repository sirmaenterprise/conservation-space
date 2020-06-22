import {CommentsFilterService} from 'idoc/comments/comments-filter/comments-filter-service';


describe('Tests for the comments filter', function () {

  let commentsFilter;

  beforeEach(function () {
    let momentsAdapter = {};

    commentsFilter = new CommentsFilterService(momentsAdapter);
  });

  it('should return true if the comment description contains the searched keyword', function () {
    let comment = {
      getDescription: sinon.stub().returns('test123')
    };
    expect(commentsFilter.filterByKeyword('test')(comment)).to.be.true;
  });

  it('should filter through html contents of comment descriptions', () => {
    let comment = {
      getDescription: sinon.stub().returns(`<blockquote><ul><li><strong><span style="font-size:24px;">s</span><span style="background-color:#e74c3c;">t</span><a data-cke-saved-href="http://blabla.bla" target="_blank" href="http://blabla.bla">ron</a><em>g</em></strong><br></li></ul></blockquote>`),
      getReplies: () => {
        return [{getDescription: () => '`<blockquote><ul><span style="background-color:#e74c3c;">t</span>est</ul></blockquote>'}]
      }
    };
    expect(commentsFilter.filterByKeyword('span')(comment)).to.be.false;
  });

  it('should return false if the comment description does not contain the searched keyword', function () {
    let comment = {
      getDescription: sinon.stub().returns('asdf'),
      getReplies: () => {
        return [{
          getDescription: () => {
            return 'adsf';
          }
        }];
      }
    };
    expect(commentsFilter.filterByKeyword('test')(comment)).to.be.false;
  });

  it('should return true if the comment author is inside the authors in the filter', function () {
    let comment = {
      getAuthorId: sinon.stub().returns('emf:test_user')
    };
    expect(commentsFilter.filterByAuthor(['emf:test_user'])(comment)).to.be.true;
  });

  it('should return false if the comment author is not inside the authors in the filter', function () {
    let comment = {
      getAuthorId: sinon.stub().returns('emf:test_user'),
      getReplies: () => {
        return [{
          getAuthorId: sinon.stub.returns('emf:test_user')
        }]
      }
    };
    expect(commentsFilter.filterByAuthor(['emf:test_user2'])(comment)).to.be.false;
  });

  it('should return true if creation date of comment is after from date when calling filterByFromDate', function () {
    let comment = {
      getCreatedDate: sinon.stub()
    };
    commentsFilter.momentAdapter.isAfter = sinon.stub().returns(true);
    expect(commentsFilter.filterByFromDate(['testDate'])(comment)).to.be.true;
  });

  it('should return true if creation date of reply is after from date when calling filterByFromDate', function () {
    let comment = {
      getCreatedDate: sinon.stub().returns(5),
      getReplies: function () {
        return [{getCreatedDate: sinon.stub().returns(10)}];
      }
    };
    commentsFilter.momentAdapter.isAfter = sinon.stub();
    commentsFilter.momentAdapter.isAfter.withArgs(5, 'testDate').returns(false);
    commentsFilter.momentAdapter.isAfter.withArgs(10, 'testDate').returns(true);
    expect(commentsFilter.filterByFromDate('testDate')(comment)).to.be.true;
  });

  it('should return false if creation date of comment is after from date when calling filterByFromDate', function () {
    let comment = {
      getCreatedDate: sinon.stub().returns(5),
      getReplies: function () {
        return [{getCreatedDate: sinon.stub().returns(10)}];
      }
    };
    commentsFilter.momentAdapter.isAfter = sinon.stub().returns(false);
    expect(commentsFilter.filterByFromDate('testDate')(comment)).to.be.false;
  });

  it('should return true if creation date of comment is before to date when calling filterByToDate', function () {
    let comment = {
      getCreatedDate: sinon.stub()
    };
    commentsFilter.momentAdapter.isBefore = sinon.stub().returns(true);
    expect(commentsFilter.filterByToDate(['testDate'])(comment)).to.be.true;
  });

  it('should return true if creation date of reply is before to date when calling filterByToDate', function () {
    let comment = {
      getCreatedDate: sinon.stub().returns(5),
      getReplies: function () {
        return [{getCreatedDate: sinon.stub().returns(10)}];
      }
    };
    commentsFilter.momentAdapter.isBefore = sinon.stub();
    commentsFilter.momentAdapter.isBefore.withArgs(5, 'testDate').returns(false);
    commentsFilter.momentAdapter.isBefore.withArgs(10, 'testDate').returns(true);
    expect(commentsFilter.filterByToDate('testDate')(comment)).to.be.true;
  });

  it('should return false if creation date of reply is before to date when calling filterByToDate', function () {
    let comment = {
      getCreatedDate: sinon.stub().returns(5),
      getReplies: function () {
        return [{getCreatedDate: sinon.stub().returns(10)}];
      }
    };
    commentsFilter.momentAdapter.isBefore = sinon.stub().returns(false);
    expect(commentsFilter.filterByToDate('testDate')(comment)).to.be.false;
  });

  it('should return true if creation date of comment is before to date and after before data', function () {
    let comment = {
      getCreatedDate: sinon.stub()
    };
    commentsFilter.momentAdapter.isBefore = sinon.stub().returns(true);
    commentsFilter.momentAdapter.isAfter = sinon.stub().returns(true);
    expect(commentsFilter.filterByDate(['testFromDate'], ['tastToDate'])(comment)).to.be.true;
  });

  it('should return true when filtering by status', () => {
    let comment = {
      getStatus: () => {
        return 'Open'
      }
    };
    expect(commentsFilter.filterByStatus('Open')(comment)).to.be.true;
  });

  it('should filter the provided comments by author', function () {
    let comments = [{}, {}, {}];
    let filter = {author: 'author', keyword: '', toDate: '', fromDate: ''};
    commentsFilter.filterByAuthor = sinon.stub().returns(sinon.spy());
    commentsFilter.filter(comments, filter);

    expect(commentsFilter.filterByAuthor.callCount).to.equal(1);
    expect(commentsFilter.filterByAuthor.getCall(0).args[0]).to.equal('author');
  });

  it('should filter the provided comments by keyword', function () {
    let comments = [{}, {}, {}];
    let filter = {keyword: 'keyword', toDate: '', fromDate: ''};
    commentsFilter.filterByKeyword = sinon.stub().returns(sinon.spy());
    commentsFilter.filter(comments, filter);

    expect(commentsFilter.filterByKeyword.callCount).to.equal(1);
    expect(commentsFilter.filterByKeyword.getCall(0).args[0]).to.equal('keyword');
  });

  it('should filter the provided comments by date', function () {
    let comments = [{}, {}, {}];
    let filter = {keyword: '', toDate: 'toDate', fromDate: 'fromDate'};
    commentsFilter.filterByDate = sinon.stub().returns(sinon.spy());
    commentsFilter.filter(comments, filter);

    expect(commentsFilter.filterByDate.callCount).to.equal(1);
    expect(commentsFilter.filterByDate.getCall(0).args[0]).to.equal('fromDate');
  });

  it('should filter the provided comments by fromDate', function () {
    let comments = [{}, {}, {}];
    let filter = {keyword: '', toDate: '', fromDate: 'fromDate'};
    commentsFilter.filterByFromDate = sinon.stub().returns(sinon.spy());
    commentsFilter.filter(comments, filter);

    expect(commentsFilter.filterByFromDate.callCount).to.equal(1);
    expect(commentsFilter.filterByFromDate.getCall(0).args[0]).to.equal('fromDate');
  });

  it('should filter the provided comments by toDate', function () {
    let comments = [{}, {}, {}];
    let filter = {keyword: '', toDate: 'toDate', fromDate: ''};
    commentsFilter.filterByToDate = sinon.stub().returns(sinon.spy());
    commentsFilter.filter(comments, filter);

    expect(commentsFilter.filterByToDate.callCount).to.equal(1);
    expect(commentsFilter.filterByToDate.getCall(0).args[0]).to.equal('toDate');
  });

  it('should filter the provided comments by toDate', function () {
    let comments = [{}, {}, {}];
    let filter = {keyword: '', toDate: 'toDate', fromDate: ''};
    commentsFilter.filterByToDate = sinon.stub().returns(sinon.spy());
    commentsFilter.filter(comments, filter);

    expect(commentsFilter.filterByToDate.callCount).to.equal(1);
    expect(commentsFilter.filterByToDate.getCall(0).args[0]).to.equal('toDate');
  });

  it('should filter the provided comments by status', function () {
    let comments = [{}, {}, {}];
    let filter = {commentStatus: 'commentStatus', keyword: '', toDate: '', fromDate: ''};
    commentsFilter.filterByStatus = sinon.stub().returns(sinon.spy());
    commentsFilter.filter(comments, filter);

    expect(commentsFilter.filterByStatus.callCount).to.equal(1);
    expect(commentsFilter.filterByStatus.getCall(0).args[0]).to.equal('commentStatus');
  });
});