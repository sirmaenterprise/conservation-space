import {ExtensionsDialogService} from 'services/extensions-dialog/extensions-dialog-service';
import {ExtensionsPanel} from 'components/extensions-panel/extensions-panel';
import {DialogService} from 'components/dialog/dialog-service';
import {PromiseAdapterMock} from 'test/adapters/angular/promise-adapter-mock';

describe('ExtensionsDialogService', () => {

  var extensionsDialogService;
  var dialogService;
  beforeEach(()=> {
    dialogService = mockDialogService();
    extensionsDialogService = new ExtensionsDialogService(dialogService, PromiseAdapterMock.mockAdapter());
  });

  it('should construct a dialog configuration for extensions panel', () => {
    var promise = extensionsDialogService.openDialog();
    expect(dialogService.create.callCount).to.equal(1);
    expect(dialogService.create.getCall(0).args[0]).to.equal(ExtensionsPanel);
  });

  it('should construct a component configuration based on provided parameters', () => {
    var context = {
      getCurrentObject: () => {
      }
    };
    var promise = extensionsDialogService.openDialog({one: 'two'}, context);
    var expected = {
      config: {one: 'two'},
      context: context
    };
    expect(dialogService.create.callCount).to.equal(1);
    expect(dialogService.create.getCall(0).args[1]).to.deep.equal(expected);
  });

  it('should support header and help target configuration for the default dialog configuration', () => {
    extensionsDialogService.openDialog({
      header: 'Header',
      helpTarget: 'help.target'
    });

    var dialogConfig = dialogService.create.getCall(0).args[2];
    expect(dialogConfig.header).to.equal('Header');
    expect(dialogConfig.helpTarget).to.equal('help.target');
  });

  it('should construct a dialog configuration', () => {
    var promise = extensionsDialogService.openDialog();
    var expected = {
      largeModal: true,
      showHeader: true
    };
    expect(dialogService.create.callCount).to.equal(1);
    expect(dialogService.create.getCall(0).args[2]).to.include(expected);
  });

  it('should use provided dialog configuration with higher priority', () => {
    var dialogConfig = {
      largeModal: false,
      showHeader: false
    };
    var promise = extensionsDialogService.openDialog(undefined, undefined, dialogConfig);
    expect(dialogConfig.buttons).to.not.exist;
    expect(dialogService.create.callCount).to.equal(1);
    expect(dialogService.create.getCall(0).args[2]).to.deep.equal(dialogConfig);
  });

  it('should extend the provided dialog reference', () => {
    var dialogConfig = {  };
    var promise = extensionsDialogService.openDialog(undefined, undefined, dialogConfig);

    expect(dialogConfig.buttons).to.exist;
    expect(dialogConfig.onButtonClick).to.exist;

    expect(dialogService.create.callCount).to.equal(1);
    expect(dialogService.create.getCall(0).args[2]).to.deep.equal(dialogConfig);
  });

  it('should construct a dialog configuration with OK button', () => {
    var promise = extensionsDialogService.openDialog();
    expect(dialogService.create.callCount).to.equal(1);
    expect(dialogService.create.getCall(0).args[2].buttons[0]).to.contains({id: DialogService.OK});
  });

  it('should construct a dialog configuration with Cancel button', () => {
    var promise = extensionsDialogService.openDialog();
    expect(dialogService.create.callCount).to.equal(1);
    expect(dialogService.create.getCall(0).args[2].buttons[1]).to.contains({id: DialogService.CANCEL});
  });

  it('should resolve promise with selected items', (done) => {
    var promise = extensionsDialogService.openDialog();
    var componentScope = {
      extensionsPanel: {
        config: {
          extensions: {
            selectedItems: ['1']
          }
        }
      }
    };
    var dialogConfig = {
      dismiss: sinon.spy()
    };
    var buttonClick = dialogService.create.getCall(0).args[2].onButtonClick;
    buttonClick(DialogService.OK, componentScope, dialogConfig);

    promise.then((result) => {
      expect(result).to.deep.equal(componentScope.extensionsPanel.config.extensions);
      done();
    }).catch(done);
  });

  it('should reject promise', (done) => {
    var promise = extensionsDialogService.openDialog();
    var componentScope = {
      selectedItems: ['1']
    };
    var dialogConfig = {
      dismiss: sinon.spy()
    };
    var buttonClick = dialogService.create.getCall(0).args[2].onButtonClick;
    buttonClick(DialogService.CANCEL, componentScope, dialogConfig);

    promise.then(done).catch((error) => {
      expect(error).to.exist;
      done();
    });
  });

  it('should dismiss the dialog on OK', () => {
    var promise = extensionsDialogService.openDialog();
    var componentScope = {
      extensionsPanel: {
        config: {}
      }
    };
    var dialogConfig = {
      dismiss: sinon.spy()
    };
    var buttonClick = dialogService.create.getCall(0).args[2].onButtonClick;
    buttonClick(DialogService.OK, componentScope, dialogConfig);

    expect(dialogConfig.dismiss.callCount).to.equal(1);
  });

  it('should dismiss the dialog on Cancel', () => {
    var promise = extensionsDialogService.openDialog();
    var dialogConfig = {
      dismiss: sinon.spy()
    };
    var buttonClick = dialogService.create.getCall(0).args[2].onButtonClick;
    buttonClick(DialogService.CANCEL, {}, dialogConfig);

    expect(dialogConfig.dismiss.callCount).to.equal(1);
  });

});

function mockDialogService() {
  return {
    create: sinon.spy()
  };
}