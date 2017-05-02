import {FileUploadPanel} from 'create/file-upload-panel';
import {PromiseStub} from 'test/promise-stub'

describe('FileUploadPanel', () => {

  var fileUploadPanel;
  beforeEach(() => {
    let modelsService = {
      getModels: sinon.spy(() => {
        return PromiseStub.resolve({
          errorMessage: "error"
        });
      })
    };
    fileUploadPanel = new FileUploadPanel(null, modelsService);
  });

  describe('init', () => {
    it('should validate the selected context', () => {
      expect(fileUploadPanel.config.onContextSelected).to.exist;
      expect(fileUploadPanel.modelsService.getModels.calledOnce).to.be.true;
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
      fileUploadPanel.eventbus = {
        publish: sinon.spy()
      };
      fileUploadPanel.uploadAll();

      expect(fileUploadPanel.eventbus.publish.called).to.be.true;
    });
  });
});