import {FileUpload} from 'file-upload/file-upload';
import {EventEmitter} from 'common/event-emitter';
import {stub} from 'test/test-utils';

let fileUpload;
let setIntervalSpy;
let eventEmitterStub = stub(EventEmitter);

describe('FileUpload', function() {

  beforeEach(function() {
    setIntervalSpy = sinon.spy();
    FileUpload.prototype.config = {};
    fileUpload = new FileUpload(null, null, {
      getServiceUrl: () => {
      }
    }, null, {config: {}}, mockEventbus(), mockWindowAdapter(), null, null);
    fileUpload.config.eventEmitter = eventEmitterStub;
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

  describe('callOnFileUploadedCallback()', function() {
    it('should call fileUploadedCallback', function() {
      let event = {instance: {id: 1}};
      fileUpload.config.fileUploadedCallback = sinon.spy();
      fileUpload.callOnFileUploadedCallback(event);

      expect(fileUpload.config.fileUploadedCallback.calledOnce).to.be.true;

      let args = fileUpload.config.fileUploadedCallback.getCall(0).args;
      expect(args[0]).to.deep.eq(event.instance);
    });
  });

  describe('onUploadCompleted()', function() {
    it('should not clear interval if there are active uploads', function() {
      fileUpload.fileCount = 2;
      fileUpload.onUploadCompleted();

      expect(fileUpload.windowAdapter.window.clearInterval.called).to.be.false;
      expect(fileUpload.config.eventEmitter.publish.called).to.be.true;

    });

    it('should clear interval if there are no more active uploads', function() {
      fileUpload.fileCount = 1;
      fileUpload.onUploadCompleted();

      expect(fileUpload.windowAdapter.window.clearInterval.called).to.be.true;
      expect(fileUpload.config.eventEmitter.publish.called).to.be.true;

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
});