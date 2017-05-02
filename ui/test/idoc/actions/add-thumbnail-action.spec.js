import {AddThumbnailAction} from 'idoc/actions/add-thumbnail-action';
import {InstanceObject} from 'idoc/idoc-context';
import {PickerService, SEARCH_EXTENSION} from 'services/picker/picker-service';
import {DialogService} from 'components/dialog/dialog-service';
import {SINGLE_SELECTION} from 'search/search-selection-modes';

import {PromiseStub} from 'test/promise-stub';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';
import {PromiseAdapterMock} from 'test/adapters/angular/promise-adapter-mock';

describe('AddThumbnailAction', () => {

  var action;
  var extensionsDialogService;
  beforeEach(() => {
    var dialogService = {
      confirmation: sinon.spy()
    };
    extensionsDialogService = {
      openDialog: mockExtensionDialogService([{id: '#quality'}])
    };
    var pickerService = new PickerService(extensionsDialogService);
    var actionsService = {
      addThumbnail: sinon.spy(() => {
        return PromiseStub.resolve();
      })
    };
    var notificationService = {
      success: sinon.spy()
    };
    var translateService = {
      translateInstant: sinon.spy(() => {
        return 'translated';
      })
    };
    var promiseAdapter = PromiseAdapterMock.mockImmediateAdapter();
    action = new AddThumbnailAction(dialogService, pickerService, actionsService, notificationService, translateService, {}, promiseAdapter);
    action.refreshInstance = sinon.spy();
  });

  it('should show a confirmation message if there is a thumbnail', () => {
    var actionContext = getActionContext('id', 'thumb');
    action.execute(undefined, actionContext);
    expect(action.dialogService.confirmation.called).to.be.true;
  });

  it('should not show a confirmation message if there is no thumbnail', () => {
    var actionContext = getActionContext('id');
    action.addThumbnail = sinon.spy();

    action.execute(undefined, actionContext);

    expect(action.dialogService.confirmation.called).to.be.false;
    expect(action.addThumbnail.called).to.be.true;
  });

  it('should open a dialog with the picker extensions', () => {
    var actionContext = getActionContext('id');
    action.execute(undefined, actionContext);

    expect(extensionsDialogService.openDialog.called).to.be.true;

    var args = extensionsDialogService.openDialog.getCall(0).args;
    expect(args[1]).to.deep.equal(actionContext.idocContext);
  });

  it('should configure the service with correct context', () => {
    var actionContext = getActionContext('id');
    action.execute(undefined, actionContext);

    expect(extensionsDialogService.openDialog.called).to.be.true;

    var args = extensionsDialogService.openDialog.getCall(0).args;
    expect(args[0].extensionPoint).to.equal('picker');
    expect(args[0].extensions[SEARCH_EXTENSION].results.config.selection).to.equal(SINGLE_SELECTION);
  });

  it('should assign predefined types', () => {
    var actionContext = getActionContext('id');
    action.execute(undefined, actionContext);

    var args = extensionsDialogService.openDialog.getCall(0).args;
    expect(args[0].extensions[SEARCH_EXTENSION].predefinedTypes).to.deep.equal(['emf:Image']);
  });

  it('should configure the picker to trigger a search', () => {
    var actionContext = getActionContext('id');
    action.execute(undefined, actionContext);
    var args = extensionsDialogService.openDialog.getCall(0).args;
    expect(args[0].extensions[SEARCH_EXTENSION].triggerSearch).to.be.true;
  });

  it('should open a dialog with the picker extensions after confirming', () => {
    var actionContext = getActionContext('id', 'thumb');
    action.addThumbnail = sinon.spy();

    action.execute(undefined, actionContext);

    var dialogConfig = action.dialogService.confirmation.getCall(0).args[2];
    var dismissSpy = sinon.spy();
    dialogConfig.onButtonClick(DialogService.YES, undefined, {dismiss: dismissSpy});

    expect(dismissSpy.called).to.be.true;
    expect(action.addThumbnail.called).to.be.true;
    expect(action.addThumbnail.getCall(0).args[0]).to.deep.equal(actionContext);
  });

  it('should not open a dialog with the picker extensions if confirmation is cancelled', () => {
    var actionContext = getActionContext('id', 'thumb');
    action.addThumbnail = sinon.spy();

    action.execute(undefined, actionContext);

    var dialogConfig = action.dialogService.confirmation.getCall(0).args[2];
    var dismissSpy = sinon.spy();
    dialogConfig.onButtonClick(DialogService.CANCEL, undefined, {dismiss: dismissSpy});

    expect(dismissSpy.called).to.be.true;
    expect(action.addThumbnail.called).to.be.false;
  });

  it('should assign a watcher for the selected items', () => {
    var items = [{id: '#quality'}];
    var dialogConfigSpy = (config) => {
      config.buttons = [{
        disabled: false
      }];
    };
    extensionsDialogService.openDialog = mockExtensionDialogService(items, dialogConfigSpy);
    var mockedScope = mock$scope();
    mockedScope.$watchCollection = sinon.spy();
    var actionContext = getActionContext('id', undefined, mockedScope);

    action.execute(undefined, actionContext);

    expect(mockedScope.$watchCollection.called).to.be.true;
  });

  it('should disable the OK button by default', () => {
    var items = [{id: '#quality'}];
    var dialogConfig;
    var dialogConfigDecorator = (config) => {
      config.buttons = [{
        disabled: false
      }];
      dialogConfig = config;
    };
    extensionsDialogService.openDialog = mockExtensionDialogService(items, dialogConfigDecorator);
    var mockedScope = mock$scope();
    var actionContext = getActionContext('id', undefined, mockedScope);

    action.execute(undefined, actionContext);

    expect(dialogConfig.buttons[0].disabled).to.be.true;
  });

  it('should enable the OK button when a document is selected', () => {
    var items = [{id: '#quality'}];
    var dialogConfig;
    var dialogConfigDecorator = (config) => {
      config.buttons = [{
        disabled: false
      }];
      dialogConfig = config;
    };
    extensionsDialogService.openDialog = mockExtensionDialogService(items, dialogConfigDecorator);
    var mockedScope = mock$scope();
    var actionContext = getActionContext('id', undefined, mockedScope);

    action.execute(undefined, actionContext);

    var extensionDialogConfig = extensionsDialogService.openDialog.getCall(0).args[0];
    extensionDialogConfig.extensions[SEARCH_EXTENSION].results.config.selectedItems.push({});
    mockedScope.$digest();

    expect(dialogConfig.buttons[0].disabled).to.be.false;
  });

  it('should call the thumbnail service when the picker is closed with a selected document', () => {
    var items = [{id: '#quality'}];
    extensionsDialogService.openDialog = mockExtensionDialogService(items);

    var actionContext = getActionContext('id');
    action.execute(undefined, actionContext);

    var expected = {
      instanceId: 'id',
      thumbnailObjectId: '#quality'
    };
    var addThumbnailSpy = action.actionsService.addThumbnail;
    expect(addThumbnailSpy.called).to.be.true;
    expect(addThumbnailSpy.getCall(0).args[0]).to.deep.equal(expected);
  });

  it('should display a notification message after thumbnail is added', () => {
    extensionsDialogService.openDialog = mockExtensionDialogService([{
      id: '#quality'
    }]);

    var actionContext = getActionContext('id');
    action.execute(undefined, actionContext);

    expect(action.notificationService.success.called).to.be.true;
  });

  it('should configure a help target & header', () => {
    var actionContext = getActionContext('id');
    action.execute(undefined, actionContext);

    var extensionDialogConfig = extensionsDialogService.openDialog.getCall(0).args[0];
    expect(extensionDialogConfig.header).to.exist;
    expect(extensionDialogConfig.helpTarget).to.exist;
  });

  function getActionContext(id, thumbnail, scope) {
    var object = new InstanceObject(id);
    object.setThumbnail(thumbnail);
    return {
      currentObject: object,
      scope: scope
    };
  }

  function mockExtensionDialogService(items, dialogConfigDecorator) {
    return sinon.spy((extensionConf, context, dialogConfig) => {
      if (dialogConfigDecorator) {
        dialogConfigDecorator(dialogConfig)
      }
      return PromiseStub.resolve(mockObjectPickerExtension(items));
    });
  }

  function mockObjectPickerExtension(items) {
    var resolveResult = {};
    resolveResult[SEARCH_EXTENSION] = {
      results: {
        config: {
          selectedItems: items
        }
      }
    };
    return resolveResult;
  }

});