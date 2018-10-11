import {UploadRevisionAction} from 'idoc/actions/upload/upload-revision-action';
import {InstanceRestService} from 'services/rest/instance-service';
import {UploadDialogConfigurator} from 'idoc/actions/upload/upload-dialog-configurator';
import {stub} from 'test/test-utils';

describe('UploadRevisionAction', function () {

  let uploadAction;
  let configurator;
  let restService;
  let context;

  beforeEach(() => {
    context = {
      currentObject: {
        id: 'test123',
        headers: {
          breadcrumb_header: 'header'
        }
      }
    };
    configurator = stub(UploadDialogConfigurator);
    restService = stub(InstanceRestService);
    restService.getRevisionUploadUrl.withArgs(context.currentObject.id).returns(`/instances/${context.currentObject.id}/revision/`)
    uploadAction = new UploadRevisionAction(null, restService, configurator);
  });

  it('should open dialog with configured file upload component', function () {
    let actionDefinition = {
      label: 'ActionLabel'
    };

    let expectedUrl = '/instances/test123/revision/';

    uploadAction.execute(actionDefinition, context);
    expect(configurator.createDialog.called).to.be.true;
    expect(configurator.createDialog.getCall(0).args[0]).to.deep.equal(actionDefinition);
    expect(configurator.createDialog.getCall(0).args[1]).to.deep.equal(context.currentObject);
    expect(configurator.createDialog.getCall(0).args[2]).to.equal(expectedUrl);
  });
});