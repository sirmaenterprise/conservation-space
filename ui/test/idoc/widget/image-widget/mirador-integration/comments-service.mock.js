import {buildThenable} from 'test/promise-stub';

export class CommentsRestServiceMock {


  static mock() {
    let loadAllComments = sinon.stub();

    loadAllComments.withArgs('all').returns(function () {
      var promise = buildThenable();
      promise.resolveValue = {
        data: [{
          id: 'comment1'
        }]
      };
      promise.rejected = false;
      promise.resolved = true;

      return promise;
    }());

    loadAllComments.withArgs('none').returns(function () {
      var promise = buildThenable();
      promise.resolveValue = {
        data: []
      };
      promise.rejected = false;
      promise.resolved = true;

      return promise;
    }());
    return {
      loadAllComments: loadAllComments,

      loadComments: sinon.stub().returns(function () {
        var promise = buildThenable();
        promise.resolveValue = {
          data: [{
            id: 'comment1'
          }]
        };
        promise.rejected = false;
        promise.resolved = true;

        return promise;
      }()),

      deleteComment: sinon.stub().returns(function () {
        var promise = buildThenable();
        promise.resolveValue = {};
        promise.rejected = false;
        promise.resolved = true;

        return promise;
      }()),

      updateComment: sinon.stub().returns(
        function () {
          var promise = buildThenable();
          promise.resolveValue = {data: {}};
          promise.rejected = false;
          promise.resolved = true;

          return promise;
        }()),

      createComment: sinon.stub().returns(
        function () {
          var promise = buildThenable();
          promise.resolveValue = {
            data: [{
              id: 'comment1'
            }]
          };
          promise.rejected = false;
          promise.resolved = true;

          return promise;
        }())
    }
  }

}