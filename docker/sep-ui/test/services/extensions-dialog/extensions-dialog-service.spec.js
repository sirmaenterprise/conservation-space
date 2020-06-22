import {ExtensionsDialogService} from 'services/extensions-dialog/extensions-dialog-service';
import {ExtensionsPanel} from 'components/extensions-panel/extensions-panel';
import {DialogService} from 'components/dialog/dialog-service';
import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';

describe('ExtensionsDialogService', () => {

  let extensionsDialogService;
  let dialogService;
  beforeEach(() => {
    dialogService = stub(DialogService);
    extensionsDialogService = new ExtensionsDialogService(dialogService, PromiseStub);
  });

  it('should construct a dialog configuration for extensions panel', () => {
    extensionsDialogService.openDialog();
    expect(dialogService.create.callCount).to.equal(1);
    expect(dialogService.create.getCall(0).args[0]).to.equal(ExtensionsPanel);
  });

  it('should construct a component configuration based on provided parameters', () => {
    let context = {
      getCurrentObject: () => {
      }
    };
    extensionsDialogService.openDialog({one: 'two'}, context);
    let expected = {
      config: {one: 'two'},
      context: context
    };
    expect(dialogService.create.callCount).to.equal(1);
    expect(dialogService.create.getCall(0).args[1]).to.deep.equal(expected);
  });

  it('should support header and help target configuration for the default dialog configuration', () => {
    extensionsDialogService.openDialog({
      header: 'Header',
      helpTarget: 'help.target',
      modalCls: 'big'
    });

    let dialogConfig = dialogService.create.getCall(0).args[2];
    expect(dialogConfig.header).to.equal('Header');
    expect(dialogConfig.helpTarget).to.equal('help.target');
    expect(dialogConfig.modalCls).to.equal('big');
  });

  it('should support warning configuration for the default dialog configuration', () => {
    extensionsDialogService.openDialog({
      warningMessage: 'warning.message',
      warningPopover: {
        style: {},
        title: 'title',
        body: 'body'
      }
    });

    let dialogConfig = dialogService.create.getCall(0).args[2];
    expect(dialogConfig.warningMessage).to.equal('warning.message');
    expect(dialogConfig.warningPopover).to.deep.equal({
      style: {},
      title: 'title',
      body: 'body'
    });
  });

  it('should construct a dialog configuration', () => {
    extensionsDialogService.openDialog();
    let expected = {
      largeModal: true,
      showHeader: true
    };
    expect(dialogService.create.callCount).to.equal(1);
    expect(dialogService.create.getCall(0).args[2]).to.include(expected);
  });

  it('should assign default dialog configurations to the provided one', () => {
    let dialogConfig = {
      largeModal: false,
      showHeader: false
    };
    extensionsDialogService.openDialog(undefined, undefined, dialogConfig);
    // Should've assigned the default buttons & handler
    expect(dialogConfig.buttons.length).to.equal(2);
    expect(dialogConfig.onButtonClick).to.exist;
    expect(dialogService.create.calledOnce).to.be.true;
    expect(dialogService.create.getCall(0).args[2]).to.equal(dialogConfig);
  });

  it('should extend the provided dialog reference', () => {
    let dialogConfig = {};
    extensionsDialogService.openDialog(undefined, undefined, dialogConfig);

    expect(dialogConfig.buttons).to.exist;
    expect(dialogConfig.onButtonClick).to.exist;

    expect(dialogService.create.callCount).to.equal(1);
    expect(dialogService.create.getCall(0).args[2]).to.deep.equal(dialogConfig);
  });

  it('should construct a dialog configuration with OK button', () => {
    extensionsDialogService.openDialog();
    expect(dialogService.create.callCount).to.equal(1);
    expect(dialogService.create.getCall(0).args[2].buttons[0]).to.contains({id: DialogService.OK});
  });

  it('should construct a dialog configuration with Cancel button', () => {
    extensionsDialogService.openDialog();
    expect(dialogService.create.callCount).to.equal(1);
    expect(dialogService.create.getCall(0).args[2].buttons[1]).to.contains({id: DialogService.CANCEL});
  });

  it('should resolve promise with extensions configurations', () => {
    let promise = extensionsDialogService.openDialog();
    let componentScope = {
      extensionsPanel: {
        config: {
          extensions: {
            selectedItems: ['1']
          }
        }
      }
    };
    let dialogConfig = {
      dismiss: sinon.spy()
    };
    let buttonClick = dialogService.create.getCall(0).args[2].onButtonClick;
    buttonClick(DialogService.OK, componentScope, dialogConfig);

    let resolvedExtensionsConfig = undefined;
    promise.then((result) => {
      resolvedExtensionsConfig = result;
    });
    expect(resolvedExtensionsConfig).to.deep.equal(componentScope.extensionsPanel.config.extensions);
  });

  it('should reject promise', () => {
    let promise = extensionsDialogService.openDialog();
    let componentScope = {
      selectedItems: ['1']
    };
    let dialogConfig = {
      dismiss: sinon.spy()
    };
    let buttonClick = dialogService.create.getCall(0).args[2].onButtonClick;
    buttonClick(DialogService.CANCEL, componentScope, dialogConfig);

    let catchedError = undefined;
    promise.catch(error => catchedError = error);
    expect(catchedError).to.exist;
  });

  it('should dismiss the dialog on OK', () => {
    extensionsDialogService.openDialog();
    let componentScope = {
      extensionsPanel: {
        config: {}
      }
    };
    let dialogConfig = {
      dismiss: sinon.spy()
    };
    let buttonClick = dialogService.create.getCall(0).args[2].onButtonClick;
    buttonClick(DialogService.OK, componentScope, dialogConfig);

    expect(dialogConfig.dismiss.callCount).to.equal(1);
  });

  it('should dismiss the dialog on Cancel', () => {
    extensionsDialogService.openDialog();
    let dialogConfig = {
      dismiss: sinon.spy()
    };
    let buttonClick = dialogService.create.getCall(0).args[2].onButtonClick;
    buttonClick(DialogService.CANCEL, {}, dialogConfig);

    expect(dialogConfig.dismiss.callCount).to.equal(1);
  });

});
