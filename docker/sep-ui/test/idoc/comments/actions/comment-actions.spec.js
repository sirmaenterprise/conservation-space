import {CommentActions} from 'idoc/comments/actions-menu/comment-actions';
import {Eventbus} from 'services/eventbus/eventbus';
import {AfterCommentExpandedEvent} from 'idoc/comments/events/after-comment-expanded-event';
import {PromiseAdapterMock} from 'test/adapters/angular/promise-adapter-mock';
import {ActionsHelper} from 'idoc/actions/actions-helper';

describe('CommentActions', () => {
  let element = {
    parents: function () {
      return [];
    }
  };

  let eventbus = new Eventbus();
  let commentActions;
  let getFilterCriteriaSpy;
  let extractActionsSpy;

  beforeEach(()=> {
    commentActions = new CommentActions(PromiseAdapterMock.mockAdapter(), element, eventbus);
    getFilterCriteriaSpy = sinon.spy(ActionsHelper, 'getFilterCriteria');
    extractActionsSpy = sinon.stub(ActionsHelper, 'extractActions').returns([{name: 'action'}]);
  });

  it('should not load comment actions if they are already loaded', ()=> {
    let publishSpy = sinon.spy(eventbus, 'publish');
    commentActions.comment = {
      data: {
        getActions: sinon.stub().returns([{name: 'action'}]),
        getId: ()=> {
          return 'id';
        }
      }
    };
    commentActions.loadItems();
    expect(publishSpy.called).to.be.true;
    expect(extractActionsSpy.called).to.be.true;
    expect(extractActionsSpy.args[0][0]).to.deep.equal([{name: 'action'}]);
    publishSpy.restore();
  });

  it('should load comment actions after the comment is expanded', ()=> {
    commentActions.comment = {
      data: {
        getActions: sinon.stub().returns([]),
        getId: ()=> {
          return 'id';
        }
      }
    };
    commentActions.loadItems();
    eventbus.publish(new AfterCommentExpandedEvent([{name: 'action'}]));
    expect(extractActionsSpy.called).to.be.true;
    expect(extractActionsSpy.args[0][0]).to.deep.equal([{name: 'action'}]);
  });

  afterEach(()=> {
    getFilterCriteriaSpy.restore();
    extractActionsSpy.restore();
  });
});
