import {CommentActions} from 'idoc/comments/actions-menu/comment-actions';
import {Eventbus} from 'services/eventbus/eventbus';
import {AfterCommentExpandedEvent} from 'idoc/comments/events/after-comment-expanded-event';
describe('CommentActions', () => {

  let comment = {
    data: {
      getActions: sinon.stub().returns([])
    },
    getId: ()=> {
      return 'id';
    }
  };

  let eventbus = new Eventbus();

  it('should expand the comment and obtain its actions', ()=> {
    CommentActions.prototype.comment = comment;
    CommentActions.prototype.eventbus = eventbus;
    CommentActions.prototype.buildAvailableActions = sinon.stub().returns([{}, {}]);
    CommentActions.prototype.loadItems();
    eventbus.publish(new AfterCommentExpandedEvent([]));
    expect(CommentActions.prototype.buildAvailableActions.called).to.be.true;
  });

});
