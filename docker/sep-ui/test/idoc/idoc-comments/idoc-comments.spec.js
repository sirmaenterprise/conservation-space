import _ from 'lodash';
import {IdocComments} from 'idoc/idoc-comments/idoc-comments';
import {IdocContext} from 'idoc/idoc-context';
import {InstanceObject} from 'models/instance-object';
import {stub} from 'test/test-utils';
import {PromiseStub} from 'test/promise-stub';

class IdocCommentsStub extends IdocComments {
  constructor(commentsRestService, eventbus, commentsFilterService) {
    super(commentsRestService, eventbus, commentsFilterService);
  }
}

IdocCommentsStub.prototype.tab = {id: 'tabId'};
IdocCommentsStub.prototype.context = stubIdocContext();

describe('Idoc comments', function () {

  let idocComments;
  let eventbus;
  let commentsFilterService = {
    filter: sinon.spy()
  };
  let commentsRestService = {
    loadComments: sinon.stub().returns(Promise.resolve({data: []}))
  };
  IdocComments.prototype.tab = {};

  beforeEach(function () {
    eventbus = {subscribe: _.noop};
    commentsFilterService.filter = sinon.spy();
    commentsRestService.loadComments = sinon.stub().returns(Promise.resolve({data: []}));
    commentsRestService.loadAllComments = sinon.stub().returns(Promise.resolve({data: []}));

    idocComments = new IdocCommentsStub(commentsRestService, eventbus, commentsFilterService);
  });

  it('should handle comments filtered event', function () {
    idocComments.init(commentsRestService, eventbus, commentsFilterService);
    idocComments.handleCommentsFilteredEvent(['tabId']);
    expect(commentsFilterService.filter.called).to.be.true;
  });

  it('should obtain the all comments', function () {
    idocComments.init(commentsRestService, eventbus, commentsFilterService);
    idocComments.dataProvider.loadComments = sinon.stub().returns({
      then: function (callback) {
        callback({data: []});
      }
    });
    idocComments.filtersConfig.comments();
    expect(idocComments.dataProvider.loadComments.called).to.be.true;
  });

});

function stubIdocContext() {
  var idocContext = stub(IdocContext);
  idocContext.getCurrentObjectId.returns('instanceId');

  var instanceObject = stub(InstanceObject);
  instanceObject.isPersisted.returns(false);
  idocContext.getCurrentObject.returns(PromiseStub.resolve(instanceObject));

  return idocContext;
}