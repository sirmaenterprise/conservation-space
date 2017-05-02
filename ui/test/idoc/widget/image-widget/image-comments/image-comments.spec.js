import {ImageComments} from 'idoc/widget/image-widget/image-comments/image-comments';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';
import {IdocMocks} from 'test/idoc/idoc-mocks';
import {CommonMocks} from 'test/idoc/widget/properties-selector/common-mocks';

describe('ImageComments', () => {

  let eventbus = {
    subscribe: function () {
      return {
        unsubscribe: sinon.spy()
      };
    }
  };

  let commentsRestService = {
    createComment: sinon.spy(),
    updateComment: sinon.spy(),
    deleteComment: sinon.spy(),
    loadComments: sinon.spy()
  };

  let control = {
    getId: function () {
      return 'widgetId';
    }
  };

  let config = {
    commentConfig: {}
  };

  let mockTimeout = function (executeFunction) {
    executeFunction();
  };

  let mockDialogService = {};
  let mockScope = {};

  let imageComments;
  ImageComments.prototype.control = control;
  ImageComments.prototype.config = config;

  beforeEach(() => {
    imageComments = new ImageComments(eventbus, commentsRestService, mockTimeout, mockDialogService, mockScope, {});
  });

  it('should unsubscribe the events on widget destruction', () => {
    imageComments.innerScope = {
      $destroy: sinon.spy()
    };
    imageComments.ngOnDestroy();
    for (let event of imageComments.events) {
      expect(event.unsubscribe.callCount).to.equal(1);
    }
    expect(imageComments.innerScope.$destroy.callCount).to.equal(1);
  });

  it('should handle remove annotations event', ()=> {
    imageComments.commentDialog = {
      dialogService: {
        closeExistingDialogs: sinon.spy()
      }
    };
    imageComments.handleRemoveAnnotationsEvent(['widgetId']);
    expect(imageComments.commentDialog.dialogService.closeExistingDialogs.callCount).to.equal(1);
  });

  it('should handle comments dialog closed event', ()=> {
    imageComments.dataProvider = {
      dialogClosed: sinon.spy()
    };
    imageComments.commentDialog = {
      dialog: 'dialog'
    };
    imageComments.handleContentDialogClosedEvent(['widgetId']);
    expect(imageComments.commentDialog).to.be.undefined;
    expect(imageComments.dataProvider.dialogClosed.callCount).to.equal(1);
  });

  it('should handle annotation shape created by opening new dialog', ()=> {
    imageComments.openCreateDialog = sinon.spy();
    imageComments.handleAnnotationShapeCreatedEvent(['widgetId']);
    expect(imageComments.openCreateDialog.callCount).to.equal(1);
  });

  it('should handle annotation edit by opening new edit dialog', ()=> {
    imageComments.openEditDialog = sinon.spy();
    imageComments.handleEditAnnotationEvent(['widgetId']);
    expect(imageComments.openEditDialog.callCount).to.equal(1);
  });

});