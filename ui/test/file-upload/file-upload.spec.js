import {FileUpload} from 'file-upload/file-upload';

var fileUpload;
var setIntervalSpy;

describe('FileUpload', function() {

  beforeEach(function() {
    setIntervalSpy = sinon.spy();
    FileUpload.prototype.config = {};
    fileUpload = new FileUpload(null, null, {getServiceUrl: () => {}}, null, {config: {}}, mockEventbus(), mockWindowAdapter());
  });

  describe('onUploadStarted()', function() {

    it('should start interval if not already started', function() {
      fileUpload.onUploadStarted();

      expect(setIntervalSpy.called).to.be.true;
      expect(fileUpload.eventbus.publish.called).to.be.true;
    });

    it('should not start interval if already started', function() {
      fileUpload.intervalId = 2;
      fileUpload.onUploadStarted();

      expect(setIntervalSpy.called).to.be.false;
    });
  });

  describe('onUploadCompleted()', function() {

    it('should not clear interval if there are active uploads', function() {
      fileUpload.fileCount = 2;
      fileUpload.onUploadCompleted();

      expect(fileUpload.windowAdapter.window.clearInterval.called).to.be.false;
    });

    it('should clear interval if there are no more active uploads', function() {
      fileUpload.fileCount = 1;
      fileUpload.onUploadCompleted();

      expect(fileUpload.windowAdapter.window.clearInterval.called).to.be.true;
    });
  });
});

function mockEventbus() {
  return {
    publish: sinon.spy()
  };
}

function mockWindowAdapter() {
  return {
    window: {
      setInterval: function(cb) {
        setIntervalSpy();
        cb();
        return 1;
      },
      clearInterval: sinon.spy()
    }
  };
}
