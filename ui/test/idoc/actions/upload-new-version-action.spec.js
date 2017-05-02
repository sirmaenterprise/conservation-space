import {UploadNewVersionAction} from 'idoc/actions/upload-new-version-action';
import {Configuration} from 'common/application-config';
import {DialogService} from 'components/dialog/dialog-service';

describe('UploadNewVersionAction', function () {

  var uploadAction;
  var dialogService;
  var context;
  const MAX_SIZE = 1000;

  beforeEach(()=> {
    var configuration = {
      get: sinon.stub()
    };

    configuration.get.withArgs(Configuration.UPLOAD_MAX_FILE_SIZE).returns(MAX_SIZE);

    dialogService = {
      create: sinon.spy()
    };

    uploadAction = new UploadNewVersionAction(dialogService, configuration, null);

    context = {
      currentObject: {
        id: 'test123',
        headers: {
          breadcrumb_header: 'header'
        }
      }
    };
  });

  it('should open dialog with configured file upload component', function () {
    var actionDefinition = {
      label: 'ActionLabel'
    }

    uploadAction.execute(actionDefinition, context);

    var uploadConfig = dialogService.create.getCall(0).args[1];

    expect(uploadConfig.config.id).to.equal(context.currentObject.id);
    expect(uploadConfig.config.header).to.equal(context.currentObject.headers.breadcrumb_header);
    expect(uploadConfig.config.maxFileSize).to.equal(MAX_SIZE);

    var dialogConfig = dialogService.create.getCall(0).args[2];
    expect(dialogConfig.header).to.equal(actionDefinition.label);
  });

  it('should dismiss the dialog when the close button is clicked', function () {
    uploadAction.execute({}, context);

    var dialogConfig = {
      dismiss: sinon.spy()
    };

    var buttonClickHandler = dialogService.create.getCall(0).args[2].onButtonClick;
    buttonClickHandler(DialogService.OK, null, dialogConfig);

    expect(dialogConfig.dismiss.callCount).to.equal(1);
  });
});