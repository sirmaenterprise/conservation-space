import {AddIconsAction, UPLOAD_BUTTON_ID} from 'idoc/actions/add-icons-action';
import {InstanceObject} from 'models/instance-object';
import {DialogService} from 'components/dialog/dialog-service';
import {ClassIconsUpload} from 'administration/class-icons-upload/class-icons-uploader';
import {PromiseAdapterMock} from 'test/adapters/angular/promise-adapter-mock';

describe('AddIconsAction', () => {

  var action;
  beforeEach(() => {
    var dialogService = {
      create: sinon.spy(),
      dismiss: sinon.spy()
    };
    var configuration = {
      get: ()=> {
        return 500;
      }
    };
    var actionsService = {
      addIcons: sinon.spy()
    };
    var promiseAdapter = PromiseAdapterMock.mockImmediateAdapter();
    action = new AddIconsAction(dialogService, {}, configuration, actionsService, promiseAdapter);
  });

  it('should create the upload icons dialog', () => {
    var actionContext = getActionContext('id');
    var actionDefinition = {
      label: 'Add icons'
    };
    action.execute(actionDefinition, actionContext);
    expect(action.dialogService.create.called).to.be.true;
    expect(action.dialogService.create.args[0][0]).to.equal(ClassIconsUpload);
  });

  it('should handle the upload button clicked event', () => {
    var componentScope = {
      classIconsUpload: {
        icons: [
          {file: 'firstIcon'}, {file: 'secondIcon'}
        ]
      }
    };
    var dialogConfig = {
      dismiss: sinon.spy()
    };
    var resolveSpy = sinon.spy(() => {});
    action.processSelectedIcons = sinon.spy();
    action.handleButtonClickedEvent(UPLOAD_BUTTON_ID, componentScope, dialogConfig, resolveSpy);

    expect(action.processSelectedIcons.called).to.be.true;
    expect(dialogConfig.dismiss.called).to.be.true;
    expect(resolveSpy.calledOnce).to.be.true;
  });

  it('should process properly the selected icons', ()=> {
    var icons = [{file: 'firstIcon'}, {file: 'secondIcon'}];
    action.processSelectedIcons('id',icons);
    expect(action.actionsService.addIcons.callCount).to.equal(1);
    expect(action.actionsService.addIcons.args[0][0]).to.equal('id');
    expect(action.actionsService.addIcons.args[0][1][0].image).to.equal('firstIcon');
  });

  function getActionContext(id, scope) {
    var object = new InstanceObject(id);
    return {
      currentObject: object,
      scope: scope
    };
  }
});