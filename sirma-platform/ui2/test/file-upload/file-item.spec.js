import {FileUploadItem} from 'file-upload/file-item';
import {UploadCompletedEvent} from 'file-upload/events';
import {Eventbus} from 'services/eventbus/eventbus';
import {PromiseStub} from 'test/promise-stub';
import {BeforeIdocSaveEvent} from 'idoc/actions/events/before-idoc-save-event';
import {TranslateService} from 'services/i18n/translate-service';
import {FileUploadIntegration} from 'file-upload/file-upload-integration';
import {stub} from 'test/test-utils';
import {NotificationService, DEFAULT_POSITION} from 'services/notification/notification-service';
import {AuthenticationService} from 'security/authentication-service';
import {AUTHORIZATION} from 'services/rest/http-headers';

describe('FileUploadItem', function () {

  var fileUploadItem;

  beforeEach(function () {
    fileUploadItem = new FileUploadItem(stubEventbus());
  });

  describe('ngOnInit', function () {

    it('should put parentId in the models for formConfig', function () {
      fileUploadItem.fileUpload = {
        config: {
          parentId: 'parent',
          eventEmitter: {
            subscribe: function subscribe() {
            }
          }
        }
      };
      fileUploadItem.entry = {
        file: {
          name: 'name'
        }
      };
      fileUploadItem.ngOnInit();
      expect(fileUploadItem.config.formConfig.models.parentId).to.equal('parent');
    });

    it('should disable the upload if an error message has occurred', function () {
      fileUploadItem.fileUpload = {
        config: {}
      };
      fileUploadItem.config = {};
      fileUploadItem.onContextValidated({errorMessage: 'error'});
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
    it('should fire UploadCompletedEvent event and emit onFileUploaded event', function () {
      fileUploadItem.onFileUploaded = sinon.spy();

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

      expect(fileUploadItem.eventbus.publish.calledTwice).to.be.true;
      expect(fileUploadItem.eventbus.publish.getCall(0).args[0] instanceof UploadCompletedEvent).to.be.true;

      expect(fileUploadItem.onFileUploaded.calledOnce).to.be.true;
      expect(fileUploadItem.onFileUploaded.getCall(0).args[0].event).to.deep.equal({
        instance: {
          headers: {}
        }
      });
    });
  });

  describe('checkValidAndUpload()', function () {
    it('should not upload the file if the entry is not valid', function () {
      fileUploadItem.valid = false;
      fileUploadItem.templateLoaded = true;
      fileUploadItem.upload = sinon.spy();
      fileUploadItem.uploadEnabled = true;

      fileUploadItem.checkValidAndUpload();

      expect(fileUploadItem.upload.called).to.be.false;
    });

    it('should upload the file if the entry is valid', function () {
      fileUploadItem.valid = true;
      fileUploadItem.templateLoaded = true;
      fileUploadItem.uploadEnabled = true;
      fileUploadItem.upload = sinon.spy();

      fileUploadItem.checkValidAndUpload();

      expect(fileUploadItem.upload.called).to.be.true;
    });

    it('should not upload the file if the template of entry is not loaded', function () {
      fileUploadItem.valid = true;
      fileUploadItem.templateLoaded = false;
      fileUploadItem.upload = sinon.spy();
      fileUploadItem.uploadEnabled = true;

      fileUploadItem.checkValidAndUpload();

      expect(fileUploadItem.upload.called).to.be.false;
    });

    it('should upload the file if the template of entry is loaded', function () {
      fileUploadItem.valid = true;
      fileUploadItem.templateLoaded = true;
      fileUploadItem.uploadEnabled = true;
      fileUploadItem.upload = sinon.spy();

      fileUploadItem.checkValidAndUpload();

      expect(fileUploadItem.upload.called).to.be.true;
    });
  });

  describe('ngOnDestroy()', function () {
    it('should unsubscribe method be called for uploadAllSubscription and contextChangedSubscription subscriptions', function () {
      let uploadAllSubscription = {
        unsubscribe: sinon.spy()
      };

      fileUploadItem.eventHandlers.push(uploadAllSubscription);

      fileUploadItem.ngOnDestroy();
      expect(uploadAllSubscription.unsubscribe.calledOnce).to.be.true;
    });
  });

  describe('onUploadComplete()', function () {
    it('should fire BeforeIdocSaveEvent when instance is ready to be persisted', function () {
      fileUploadItem.entry = {
        file: {
          id: '123'
        }
      };
      fileUploadItem.fileUpload = mockFileUpload();
      fileUploadItem.fileUpload.config = {};
      fileUploadItem.instanceRestService = mockInstanceRestService();
      fileUploadItem.translateService = stub(TranslateService);
      fileUploadItem.uploadAllSubscription = stubEventbus();
      fileUploadItem.contextChangedSubscription = stubEventbus();
      fileUploadItem.onFileUploaded = () => {
      };
      fileUploadItem.onUploadComplete();
      expect(fileUploadItem.eventbus.publish.getCall(0).args[0] instanceof BeforeIdocSaveEvent).to.be.true;
    });
  });

  describe('upload()', function () {
    beforeEach(function () {
      fileUploadItem.entry = {
        file: {
          id: '123'
        },
        uploadControl: {}
      };
      fileUploadItem.fileUpload = mockFileUpload();
      fileUploadItem.fileUpload.config = {};
      fileUploadItem.$scope = stubScope();
      fileUploadItem.fileUploadIntegration = stubFileUploadIntegration(false, true, {id: 'instanceId'});
      fileUploadItem.translateService = stubTranslateService();
      fileUploadItem.notificationService = stub(NotificationService);
      fileUploadItem.authenticationService = stubAuthenticationService();
      fileUploadItem.config = {
        formConfig: {
          models: ''
        }
      };
      fileUploadItem.onValidityChange = () => {
      };
    });

    it('should exit to onPersist() when skipEntityUpdate is true', function () {
      fileUploadItem.onPersist = sinon.stub();
      fileUploadItem.onUploadComplete = sinon.spy();

      fileUploadItem.skipEntityUpdate = true;
      fileUploadItem.upload();
      expect(fileUploadItem.onPersist.getCall(0).args[0]).to.deep.equal({id: 'instanceId'});
      expect(fileUploadItem.onUploadComplete.called).to.be.false;
    });

    it('should exit to onUploadComplete() when skipEntityUpdate is falsy', function () {
      fileUploadItem.onPersist = sinon.spy();
      fileUploadItem.onUploadComplete = sinon.spy();

      fileUploadItem.skipEntityUpdate = false;
      fileUploadItem.upload();
      expect(fileUploadItem.onUploadComplete.calledOnce).to.be.true;
      expect(fileUploadItem.onPersist.called).to.be.false;
    });

    it('should not notify for error when there is not message', function () {
      // Given
      // upload will fail without message
      fileUploadItem.fileUploadIntegration = stubFileUploadIntegration(true, false, {});

      // When upload file
      fileUploadItem.upload();

      // Then:
      // notification have to be not shown.
      expect(fileUploadItem.notificationService.error.called).to.be.false;
      expect(fileUploadItem.translateService.translateInstant.called).to.be.true;
      expect(fileUploadItem.uploadStarted).to.be.false;
      expect(fileUploadItem.uploadEnabled).to.be.false;
      expect(fileUploadItem.removeEnabled).to.be.true;
    });

    it('should not notify for error when there is not message', function () {
      // Given
      // upload will fail without message
      fileUploadItem.fileUploadIntegration = stubFileUploadIntegration(true, false, {responseJSON: {}});

      // When upload file
      fileUploadItem.upload();

      // Then:
      // notification have to be not shown.
      expect(fileUploadItem.notificationService.error.called).to.be.false;
      expect(fileUploadItem.translateService.translateInstant.called).to.be.true;
      expect(fileUploadItem.uploadStarted).to.be.false;
      expect(fileUploadItem.uploadEnabled).to.be.false;
      expect(fileUploadItem.removeEnabled).to.be.true;
    });

    it('should not notify for error when there is not message', function () {
      // Given
      // upload will fail without message
      fileUploadItem.fileUploadIntegration = stubFileUploadIntegration(true, false, {responseJSON: {message: 'upload fail'}});

      // When upload file
      fileUploadItem.upload();

      // Then:
      // notification have to be shown.
      expect(fileUploadItem.notificationService.error.called).to.be.true;
      expect(fileUploadItem.notificationService.error.getCall(0).args[0]).to.deep.equal({
        opts: {
          closeButton: false,
          hideOnHover: false,
          positionClass: DEFAULT_POSITION
        },
        message: 'upload fail'
      });

      expect(fileUploadItem.translateService.translateInstant.called).to.be.true;
      expect(fileUploadItem.uploadStarted).to.be.false;
      expect(fileUploadItem.uploadEnabled).to.be.false;
      expect(fileUploadItem.removeEnabled).to.be.true;
    });

    it('should add authorization header to upload control', () => {
      fileUploadItem.fileUploadIntegration = stubFileUploadIntegration(true, false, {responseJSON: {}});
      fileUploadItem.upload();

      expect(fileUploadItem.entry.uploadControl.headers).to.deep.equal({[AUTHORIZATION]: 'Bearer token'});
    });

    it('should append authorization header to upload control', () => {
      fileUploadItem.entry.uploadControl.headers = {
        'Accept-Language': 'bg-BG'
      };
      fileUploadItem.fileUploadIntegration = stubFileUploadIntegration(true, false, {responseJSON: {}});
      fileUploadItem.upload();

      expect(fileUploadItem.entry.uploadControl.headers).to.deep.equal({'Accept-Language': 'bg-BG', [AUTHORIZATION]: 'Bearer token'});
    });
  });

  function stubEventbus() {
    return stub(Eventbus);
  }

  function stubTranslateService() {
    let stubTranslateService = stub(TranslateService);
    stubTranslateService.translateInstant.withArgs('fileupload.fail').returns('error message');

    return stubTranslateService;
  }

  function stubScope() {
    return {
      $digest: sinon.spy()
    };
  }

  function stubFileUploadIntegration(failPromise = false, executeAlways = true, response = {}) {
    var integration = stub(FileUploadIntegration);
    integration.submit = sinon.spy(() => {
      var alwaysFn = (always) => {
        if (executeAlways) {
          always();
        }
      };

      var failFn = (fail) => {
        if (failPromise) {
          fail(response);
        }
        return {
          always: alwaysFn
        };
      };

      return {
        done: (done) => {
          if (!failPromise) {
            done(response);
          }
          return {
            fail: failFn
          };
        }
      };
    });
    return integration;
  }

  function mockInstanceRestService() {
    return {
      create: () => {
        return PromiseStub.resolve(
          {
            'data': {'headers': []}
          });
      }
    };
  }

  function mockFileUpload() {
    return {
      updateProgressBar: () => {
        return PromiseStub.resolve({});
      }
    };
  }

  function stubAuthenticationService() {
    let authenticationService = stub(AuthenticationService);
    authenticationService.buildAuthHeader.returns(PromiseStub.resolve('Bearer token'));
    return authenticationService;
  }

});