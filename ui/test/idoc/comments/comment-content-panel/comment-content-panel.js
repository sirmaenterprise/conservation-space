import {CommentContentPanel} from 'idoc/comments/comment-content-panel/comment-content-panel';

describe('CreateCommentDialog', () => {
  let dialog;
  let timeout = {};

  beforeEach(() => {
    timeout = sinon.spy();
    dialog = new CommentContentPanel(timeout);
  });

  it('should strip html tags', () => {
    expect(dialog.isValid('<i>Description</i><div></div>')).to.be.true;
  });

  it('should remove html entities', () => {
    expect(dialog.isValid('<i>description</i>&nbsp;<div></div>')).to.be.true;
  });
});