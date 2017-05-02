import {DeleteCommentAction} from 'idoc/comments/actions/delete-comment';
import {PromiseStub} from 'test/promise-stub';

describe('DeleteCommentAction', () => {
  const ID = 'emf:123456';

  let eventbus = {
    publish:function () {

    }
  };

  let context ={
    comment: {
      data: {
        getId:function(){
          return ID;
        },
        isReply:function(){

        }
      }
    },
      commentId: {
        getId: () => {
          return ID
        }
      },
    config: {
        dataProvider: {
          deleteComment: sinon.spy(()=>{
            return PromiseStub.resolve({})
          })
        }
    }
  };

  let action = {
    action: 'deleteAction'
  };

  it('should call service for delete with the object id', () => {
    let handler = new DeleteCommentAction(eventbus);
    handler.execute(action, context);
    expect(context.config.dataProvider.deleteComment.calledOnce).to.be.true;
    expect(context.config.dataProvider.deleteComment.getCall(0).args[0].commentId).to.equal(ID);
  });



});
