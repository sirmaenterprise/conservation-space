import {CommentsFilterPanel} from 'idoc/comments/comments-filter/comments-filter-panel/comments-filter-panel';
import {Configuration} from 'common/application-config';
import {OPEN, RESOLVED} from 'idoc/comments/comment-status';
import {TranslateService} from 'services/i18n/translate-service';
import {stub} from 'test/test-utils';

describe('Tests for the comments filter panel', function () {

  let commentsFilterPanel;

  let config = {
    comments: ()=> {
      return Promise.resolve();
    }
  };

  let compile = ()=> {
    return (scope)=> {
    }
  };

  let element = {
    find: ()=> {
      return {
        replaceWith: ()=> {
          sinon.spy();
        }
      }
    }
  };

  beforeEach(() => {
    commentsFilterPanel = new CommentsFilterPanel(compile, {}, element, stub(TranslateService), stub(Configuration));
    commentsFilterPanel.ngOnInit();
  });

  it('should clear the filter', ()=> {
    commentsFilterPanel.filters = {
      keyword: 'keyword',
      status: 'OPEN',
      author: 'asdf'
    };
    commentsFilterPanel.clearFilters();
    for (let filter in commentsFilterPanel.filters) {
      expect(commentsFilterPanel.filters[filter]).to.equal('');
    }
  });

  it('should merge and save the config', ()=> {
    commentsFilterPanel.filters = {
      keyword: 'keyword',
      status: 'OPEN',
      author: 'asdf'
    };

    commentsFilterPanel.saveToConfig();
    for (let filter in commentsFilterPanel.config.filters) {
      expect(commentsFilterPanel.config.filters[filter]).to.equal(commentsFilterPanel.filters[filter]);
    }
  });

  it('should merge and save the config', ()=> {
    commentsFilterPanel.filters = {
      keyword: 'keyword',
      status: 'OPEN',
      author: 'asdf'
    };

    commentsFilterPanel.saveToConfig();
    for (let filter in commentsFilterPanel.config.filters) {
      expect(commentsFilterPanel.config.filters[filter]).to.equal(commentsFilterPanel.filters[filter]);
    }
  });

  it('should have comments status config properly configured', ()=> {
    expect(commentsFilterPanel.commentStatusConfig).to.exist;
    expect(commentsFilterPanel.commentStatusConfig.cssClass).to.eq('comment-status-field');

    expect(commentsFilterPanel.commentStatusConfig.data.length).to.eq(2);
    expect(commentsFilterPanel.commentStatusConfig.data[0].id).to.eq(OPEN);
    expect(commentsFilterPanel.commentStatusConfig.data[1].id).to.eq(RESOLVED);
  });

  it('should deep clone contents of config to filters', ()=> {
    commentsFilterPanel.config = {
      filters: {
        fromDate: 'from-date',
        toDate: 'to-date'
      }
    };

    commentsFilterPanel.ngOnInit();
    expect(commentsFilterPanel.filters).to.deep.eq({
      fromDate: 'from-date',
      toDate: 'to-date'
    });
  });

  it('should provide proper from-datetime configurations', ()=> {
    commentsFilterPanel.configuration.get.withArgs(Configuration.UI_DATE_FORMAT).returns('date-format');
    commentsFilterPanel.configuration.get.withArgs(Configuration.UI_TIME_FORMAT).returns('time-format');
    commentsFilterPanel.config = {filters: {fromDate: 'from-date'}};

    commentsFilterPanel.ngOnInit();
    expect(commentsFilterPanel.fromDateConfig).to.deep.eq({
      cssClass: 'from-date-field',
      defaultValue: 'from-date',
      placeholder: 'search.date.from.placeholder',
      dateFormat: 'date-format',
      timeFormat: 'time-format'
    });
  });

  it('should provide proper to-datetime configurations', ()=> {
    commentsFilterPanel.configuration.get.withArgs(Configuration.UI_DATE_FORMAT).returns('date-format');
    commentsFilterPanel.configuration.get.withArgs(Configuration.UI_TIME_FORMAT).returns('time-format');
    commentsFilterPanel.config = {filters: {toDate: 'to-date'}};

    commentsFilterPanel.ngOnInit();
    expect(commentsFilterPanel.toDateConfig).to.deep.eq({
      cssClass: 'end-date-field',
      defaultValue: 'to-date',
      placeholder: 'search.date.to.placeholder',
      dateFormat: 'date-format',
      timeFormat: 'time-format'
    });
  });

  it('should get the authors', ()=> {
    let comments = [
      {
        getAuthorId: ()=> {
          return 'first'
        },
        getAuthorLabel: ()=> {
          return 'first'
        },
        getReplies: ()=> {

        }
      },
      {
        getAuthorId: ()=> {
          return 'first'
        },
        getAuthorLabel: ()=> {
          return 'first'
        },
        getReplies: ()=> {

        }
      },
      {
        getAuthorId: ()=> {
          return 'second'
        },
        getAuthorLabel: ()=> {
          return 'second'
        },
        getReplies: ()=> {
          return [{
            getAuthorId: ()=> {
              return 'third'
            },
            getAuthorLabel: ()=> {
              return 'third'
            }
          }];
        }
      }
    ];

    let authors = commentsFilterPanel.getAuthors(comments);
    expect(authors.length).to.equal(3);
    expect(authors[0].id).to.equal('first');
    expect(authors[1].id).to.equal('second');
  });

});
