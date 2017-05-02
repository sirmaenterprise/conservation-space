import {CommentsFilterPanel} from 'idoc/comments/comments-filter/comments-filter-panel/comments-filter-panel';

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

  beforeEach(function () {
    CommentsFilterPanel.prototype.config = config;
    commentsFilterPanel = new CommentsFilterPanel(compile, {}, element, mockTranslateService());
  });


  it('should clear the filter', ()=> {
    commentsFilterPanel = new CommentsFilterPanel({}, {}, {} , mockTranslateService());
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

function mockTranslateService() {
  return {
    translateInstant: () => {
      return ""
    }
  }
}

