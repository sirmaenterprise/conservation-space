import {CodeListsUpload} from 'administration/code-lists/upload/code-lists-upload';
import {CodelistRestService} from 'services/rest/codelist-service';
import {DialogService} from 'components/dialog/dialog-service';
import {TranslateService} from 'services/i18n/translate-service';
import {FileUploadIntegration} from 'file-upload/file-upload-integration';
import {AuthenticationService} from 'security/authentication-service';
import {AUTHORIZATION} from 'services/rest/http-headers';
import {PromiseStub} from 'test/promise-stub';

import {stub} from 'test/test-utils';

describe('CodeListsUpload', () => {

  let codeListsUpload;
  beforeEach(() => {
    codeListsUpload = new CodeListsUpload(stubScope(), stubElement(), stub(DialogService), stubTranslateService(),
      stubCodelistRestService(), stubRestClient(), stubFileUploadIntegration(), stubAuthenticationService());
    codeListsUpload.ngOnInit();
  });

  it('should fetch HTTP headers for proper REST calls', () => {
    expect(codeListsUpload.headers).to.deep.equal({test: '123'});
  });

  it('should fetch code lists service urls for upload and overwrite', () => {
    expect(codeListsUpload.urls.update).to.equal('update');
    expect(codeListsUpload.urls.overwrite).to.equal('overwrite');
  });

  it('should translate confirmation messages', () => {
    expect(codeListsUpload.messages.update).to.contains('translated');
    expect(codeListsUpload.messages.overwrite).to.contains('translated');
  });

  describe('ngAfterViewInit()', () => {
    beforeEach(() => codeListsUpload.ngAfterViewInit());

    it('should initialize the file upload extension upon the current element', () => {
      expect(codeListsUpload.$element.fileupload.calledOnce).to.be.true;
      let fileUploadOptions = codeListsUpload.$element.fileupload.getCall(0).args[0];
      expect(fileUploadOptions.add).to.exist;
      expect(fileUploadOptions.type).to.equal('POST');
      expect(fileUploadOptions.url).to.equal(codeListsUpload.urls.overwrite);
      expect(fileUploadOptions.headers).to.deep.equal(codeListsUpload.headers);
    });

    it('should assign the selected file and trigger a digest cycle', () => {
      expect(codeListsUpload.$scope.$digest.called).to.be.false;
      let fileUploadOptions = codeListsUpload.$element.fileupload.getCall(0).args[0];
      let uploadControl = {};
      fileUploadOptions.add(undefined, uploadControl);
      expect(codeListsUpload.uploadControl).to.equal(uploadControl);
      expect(codeListsUpload.$scope.$digest.calledOnce).to.be.true;
    });
  });

  describe('overwrite()', () => {
    it('should open an overwrite dialog', () => {
      codeListsUpload.overwrite();
      let callArgs = codeListsUpload.dialogService.confirmation.getCall(0).args;
      expect(callArgs[0]).to.equal(codeListsUpload.messages.overwrite);
    });

    it('should configure the file upload to use the overwrite service', () => {
      codeListsUpload.overwrite();
      expect(codeListsUpload.$element.fileupload.calledWith('option', 'url', 'overwrite')).to.be.true;
    });
  });

  describe('update()', () => {
    it('should open an update dialog', () => {
      codeListsUpload.update();
      let callArgs = codeListsUpload.dialogService.confirmation.getCall(0).args;
      expect(callArgs[0]).to.equal(codeListsUpload.messages.update);
    });

    it('should configure the file upload to use the update service', () => {
      codeListsUpload.update();
      expect(codeListsUpload.$element.fileupload.calledWith('option', 'url', 'update')).to.be.true;
    });
  });

  describe('upload()', () => {
    it('should open a confirmation dialog', () => {
      codeListsUpload.upload(true);
      expect(codeListsUpload.dialogService.confirmation.calledOnce).to.be.true;

      let dialogOptions = codeListsUpload.dialogService.confirmation.getCall(0).args[2];
      expect(dialogOptions.onButtonClick).to.exist;
      expect(dialogOptions.buttons.length).to.equal(2);

      expect(codeListsUpload.dialogService.createButton.calledTwice).to.be.true;
      expect(codeListsUpload.dialogService.createButton.getCall(0).args[0]).to.equal(DialogService.YES);
      expect(codeListsUpload.dialogService.createButton.getCall(1).args[0]).to.equal(DialogService.NO);
    });

    it('should start the upload only on confirmation', () => {
      codeListsUpload.startUpload = sinon.spy();
      codeListsUpload.upload(true);

      let dialogConfig = {dismiss: sinon.spy()};
      let dialogOptions = codeListsUpload.dialogService.confirmation.getCall(0).args[2];

      dialogOptions.onButtonClick(DialogService.NO, undefined, dialogConfig);
      expect(codeListsUpload.startUpload.called).to.be.false;
      expect(dialogConfig.dismiss.calledOnce).to.be.true;

      dialogOptions.onButtonClick(DialogService.YES, undefined, dialogConfig);
      expect(codeListsUpload.startUpload.calledOnce).to.be.true;
      expect(dialogConfig.dismiss.calledTwice).to.be.true;
    });
  });

  describe('startUpload()', () => {
    it('should change previous message and flag uploading to true', () => {
      codeListsUpload.uploadControl = {};
      codeListsUpload.fileUploadIntegration = stubFileUploadIntegration(false, false);
      codeListsUpload.uploading = false;
      codeListsUpload.uploadMessage = 'Success!';
      codeListsUpload.startUpload(true);
      expect(codeListsUpload.uploading).to.be.true;
      expect(codeListsUpload.uploadMessage).to.equal(codeListsUpload.messages.success);
    });

    it('should start the upload process with the selected upload control', () => {
      codeListsUpload.uploadControl = {};
      codeListsUpload.startUpload(true);
      expect(codeListsUpload.fileUploadIntegration.submit.calledWith(codeListsUpload.uploadControl)).to.be.true;
    });

    it('should clean error in case of success', () => {
      codeListsUpload.uploadControl = {};
      codeListsUpload.fileUploadIntegration = stubFileUploadIntegration(false, true);
      codeListsUpload.error = true;
      codeListsUpload.startUpload(true);
      expect(codeListsUpload.error).to.be.false;
    });

    it('should use the error message in case of error', () => {
      codeListsUpload.uploadControl = {};
      codeListsUpload.fileUploadIntegration = stubFileUploadIntegration(true, true, {
        responseJSON: {message: 'Error'}
      });
      codeListsUpload.error = false;
      codeListsUpload.startUpload(true);
      expect(codeListsUpload.error).to.be.true;
      expect(codeListsUpload.uploadMessage).to.deep.equal(['Error']);
    });

    it('should not be flagged as uploading in case of both success and error', () => {
      codeListsUpload.uploadControl = {};
      codeListsUpload.fileUploadIntegration = stubFileUploadIntegration(false, true);
      codeListsUpload.uploading = true;
      codeListsUpload.startUpload(true);
      expect(codeListsUpload.uploading).to.be.false;

      codeListsUpload.fileUploadIntegration = stubFileUploadIntegration(true, true, {responseJSON: {}});
      codeListsUpload.uploading = true;
      codeListsUpload.startUpload(true);
      expect(codeListsUpload.uploading).to.be.false;
    });

    it('should trigger a digest cycle', () => {
      codeListsUpload.uploadControl = {};
      codeListsUpload.startUpload(true);
      expect(codeListsUpload.$scope.$digest.calledOnce).to.be.true;
    });

    it('should notify on upload', () => {
      codeListsUpload.uploadControl = {};
      codeListsUpload.onUpload = sinon.spy();
      codeListsUpload.startUpload(true);
      expect(codeListsUpload.onUpload.calledOnce).to.be.true;
    });

    it('should clear the uploading state', () => {
      codeListsUpload.uploadControl = {};
      codeListsUpload.startUpload();
      expect(codeListsUpload.uploadControl.files).to.deep.eq([]);
    });

    it('should add authorization header to upload control', () => {
      codeListsUpload.uploadControl = {};
      codeListsUpload.startUpload();
      expect(codeListsUpload.uploadControl.headers).to.deep.eq({[AUTHORIZATION]: 'Bearer token'});
    });

    it('should append authorization header to upload control', () => {
      codeListsUpload.uploadControl = {
        headers: {
          'Accept-Language': 'bg-BG'
        }
      };
      codeListsUpload.startUpload();
      expect(codeListsUpload.uploadControl.headers).to.deep.eq({'Accept-Language': 'bg-BG', [AUTHORIZATION]: 'Bearer token'});
    });
  });

  describe('ngOnDestroy', () => {
    it('should not destroy the fileupload component if not initialized', () => {
      codeListsUpload.$element.data = sinon.spy(() => false);
      codeListsUpload.ngOnDestroy();
      expect(codeListsUpload.$element.fileupload.calledWith('remove')).to.be.false;
    });

    it('should destroy the fileupload component if still present', () => {
      codeListsUpload.$element.data = sinon.spy(() => true);
      codeListsUpload.ngOnDestroy();
      expect(codeListsUpload.$element.fileupload.calledWith('destroy')).to.be.true;
    });
  });

  function stubScope() {
    return {
      $digest: sinon.spy()
    };
  }

  function stubElement() {
    return {
      fileupload: sinon.spy(),
      find: sinon.spy(() => {
        return stubElement();
      }),
      on: sinon.spy(),
      click: sinon.spy(),
      data: sinon.spy()
    };
  }

  function stubTranslateService() {
    let translate = stub(TranslateService);
    translate.translateInstant = sinon.spy((key) => {
      return key + ' translated';
    });
    return translate;
  }

  function stubCodelistRestService() {
    let clsService = stub(CodelistRestService);
    clsService.getUpdateServiceUrl.returns('update');
    clsService.getOverwriteServiceUrl.returns('overwrite');
    return clsService;
  }

  function stubRestClient() {
    // Using stub() does not allow to set the config
    return {
      config: {
        headers: {
          'test': '123'
        }
      }
    };
  }

  function stubFileUploadIntegration(failPromise = false, executeAlways = true, response = {}) {
    let integration = stub(FileUploadIntegration);
    integration.submit = sinon.spy(() => {
      let alwaysFn = (always) => {
        if (executeAlways) {
          always();
        }
      };

      let failFn = (fail) => {
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

  function stubAuthenticationService() {
    let authenticationService = stub(AuthenticationService);
    authenticationService.buildAuthHeader.returns(PromiseStub.resolve('Bearer token'));
    return authenticationService;
  }

});
