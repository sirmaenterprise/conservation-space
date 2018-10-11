import {FileUploadPanel} from 'create/file-upload-panel';
import {EventEmitter} from 'common/event-emitter';
import {UPLOAD_ALL} from 'create/file-upload-panel';
import {stub} from 'test-utils';

describe('FileUploadPanel', () => {

  var fileUploadPanel;
  let eventEmitter;
  beforeEach(() => {
    fileUploadPanel = new FileUploadPanel();
    eventEmitter = stub(EventEmitter);
    fileUploadPanel.config.eventEmitter = eventEmitter;
  });

  describe('init', () => {
    it('should event emitter be configured', () => {
      fileUploadPanel = new FileUploadPanel();
      expect(fileUploadPanel.config.eventEmitter instanceof EventEmitter).to.be.true;
    });

    it('should have all controls shown & enabled by default', () => {
      let controls = fileUploadPanel.config.controls;
      expect(controls.showCancel).to.be.true;
      expect(controls.showUploadAll).to.be.true;
    });
  });

  describe('cancel()', () => {
    it('should invoke the configured function', () => {
      fileUploadPanel.config.onCancel = sinon.spy(() => {
      });
      fileUploadPanel.cancel();
      expect(fileUploadPanel.config.onCancel.calledOnce).to.be.true;
    });
  });

  describe('uploadAll()', () => {
    it('should publish the uploadAll event', () => {
      fileUploadPanel.uploadAll();
      expect(eventEmitter.publish.withArgs(UPLOAD_ALL).calledOnce).to.be.true;
    });
  });

  it('should invoke unsubscribeAll of event emitter', () => {
    fileUploadPanel.ngOnDestroy();
    expect(eventEmitter.unsubscribeAll.calledOnce).to.be.true;
  });
});