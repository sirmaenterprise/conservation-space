import {FileUploadItem} from 'file-upload/file-item';
import {UploadCompletedEvent} from 'file-upload/events';

describe('FileUploadItem', function () {

  var fileUploadItem;
  var eventbus = {
    subscribe: function () {
      return {
        unsubscribe: sinon.spy()
      }
    }
  };
  beforeEach(function () {
    fileUploadItem = new FileUploadItem(eventbus);
  });

  describe('ngOnInit', function () {
    it('should put parentId in the models for formConfig', function () {
      fileUploadItem.fileUpload = {
        config: {
          parentId: 'parent'
        }
      };
      fileUploadItem.entry = {
        file: {
          name: 'name'
        }
      };
      fileUploadItem.$scope = {
        $watch: () => {
        }
      };
      fileUploadItem.ngOnInit();
      expect(fileUploadItem.config.formConfig.models.parentId).to.equal('parent');
    });

    it('should disable the upload if an error message has occurred', function () {
      fileUploadItem.fileUpload = {
        config: {
          errorMessage: 'error'
        }
      };
      fileUploadItem.config = {};
      fileUploadItem.onContextChanged();
      expect(fileUploadItem.config.disabled).to.equal(true);
    });
  });

  describe('getFileExtension', function () {
    it('should return the file extension', function () {
      var extension = fileUploadItem.getFileExtension('new.file.txt');
      expect(extension).to.equal('txt');
    });

    it('should return null if the file does not have extension', function () {
      var extension = fileUploadItem.getFileExtension('textfile');
      expect(extension).to.equal(null);
    });

    it('should return null if the last symbol is dot', function () {
      var extension = fileUploadItem.getFileExtension('textfile.');
      expect(extension).to.equal(null);
    });
  });

  describe('constructMetaData', function () {
    it('should set rdf type and instance properties', function () {
      var properties = {
        name: 'Test'
      };
      const TYPE = 'test-type';

      var metadata = fileUploadItem.constructMetaData(TYPE, properties);

      expect(metadata['rdf:type']).to.equal(TYPE);
      expect(metadata['name']).to.equal('Test');
    });
  });

  describe('onPersist(entity)', function () {

    it('should fire UploadCompletedEvent event', function () {
      fileUploadItem.eventbus = {
        publish: sinon.spy()
      };

      fileUploadItem.entry = {
        file: {}
      };

      fileUploadItem.translateService = {
        translateInstant: () => {
        }
      };

      fileUploadItem.fileUpload = {
        config: {},
        updateProgressBar: () => {
        }
      };

      fileUploadItem.onPersist({
        headers: {}
      });

      expect(fileUploadItem.eventbus.publish.calledOnce);
      expect(fileUploadItem.eventbus.publish.getCall(0).args[0] instanceof UploadCompletedEvent).to.be.true;
    });
  });

  describe('checkValidAndUpload()', function () {

    it('should not upload the file if the entry is not valid', function () {
      fileUploadItem.valid = false;
      fileUploadItem.upload = sinon.spy();
      fileUploadItem.uploadEnabled = true;

      fileUploadItem.checkValidAndUpload();

      expect(fileUploadItem.upload.called).to.be.false;
    });

    it('should upload the file if the entry is valid', function () {
      fileUploadItem.valid = true;
      fileUploadItem.uploadEnabled = true;
      fileUploadItem.upload = sinon.spy();

      fileUploadItem.checkValidAndUpload();

      expect(fileUploadItem.upload.called).to.be.true;
    });
  });
});