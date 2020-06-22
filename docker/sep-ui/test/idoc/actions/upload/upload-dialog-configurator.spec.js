import {UploadDialogConfigurator} from 'idoc/actions/upload/upload-dialog-configurator';
import {Configuration} from 'common/application-config';
import {DialogService} from 'components/dialog/dialog-service';
import {PromiseAdapterMock} from 'test/adapters/angular/promise-adapter-mock';
import {stub} from 'test/test-utils';

describe('UploadDialogConfigurator', function () {

  let uploadConfigurator;
  let dialogService;
  let currentObject;
  let promiseAdapter;
  const MAX_SIZE = 1000;

  beforeEach(() => {
    let configuration = stub(Configuration);

    configuration.get.withArgs(Configuration.UPLOAD_MAX_FILE_SIZE).returns(MAX_SIZE);

    dialogService = stub(DialogService);
    promiseAdapter = PromiseAdapterMock.mockImmediateAdapter();
    uploadConfigurator = new UploadDialogConfigurator(dialogService, configuration, promiseAdapter);

    currentObject = {
      id: 'test123',
      headers: {
        breadcrumb_header: 'header'
      }
    };
  });

  it('should open dialog with configured file upload component', function () {
    let actionDefinition = {
      label: 'ActionLabel'
    };

    let url = '/instances/test123/content/';

    uploadConfigurator.createDialog(actionDefinition, currentObject, url);

    let uploadConfig = dialogService.create.getCall(0).args[1];

    expect(uploadConfig.config.id).to.equal(currentObject.id);
    expect(uploadConfig.config.url).to.equal(url);
    expect(uploadConfig.config.header).to.equal(currentObject.headers.breadcrumb_header);
    expect(uploadConfig.config.maxFileSize).to.equal(MAX_SIZE);

    let dialogConfig = dialogService.create.getCall(0).args[2];
    expect(dialogConfig.header).to.equal(actionDefinition.label);
  });

  it('should dismiss the dialog when the close button is clicked', function () {
    uploadConfigurator.createDialog({}, currentObject);
    let dialogConfig = {
      dismiss: sinon.spy()
    };
    let buttonClickHandler = dialogService.create.getCall(0).args[2].onButtonClick;
    buttonClickHandler(DialogService.OK, null, dialogConfig);

    expect(dialogConfig.dismiss.callCount).to.equal(1);
  });
});