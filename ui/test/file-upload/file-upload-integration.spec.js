import {FileUploadIntegration} from 'file-upload/file-upload-integration';

describe('FileUploadIntegration', () => {
  it('should start a request using the passed XHR object', () => {
    var fileUploadIntegration = new FileUploadIntegration();
    var fileUploadControlMock = sinon.spy();

    fileUploadIntegration.submit({
      submit: fileUploadControlMock
    });

    expect(fileUploadControlMock.called);
  });
});
