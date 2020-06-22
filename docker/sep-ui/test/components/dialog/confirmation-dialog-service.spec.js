import {ConfirmationDialogService} from 'components/dialog/confirmation-dialog-service';
import {DialogService} from 'components/dialog/dialog-service';
import {TranslateService} from 'services/i18n/translate-service';
import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';
import _ from 'lodash';

describe('CodeConfirmationService', () => {

  let confirmService;
  beforeEach(() => {
    confirmService = new ConfirmationDialogService(stubDialogService(), PromiseStub, stubTranslateService());
  });

  it('should provide proper dialog configuration when confirming', () => {
    confirmService.confirm({message: 'O rly ?'});

    expect(confirmService.dialogService.confirmation.calledWith('O rly ?')).to.be.true;

    let dialogConfig = getDialogConfig();
    expect(dialogConfig.buttons.length).to.equal(2);
    expect(dialogConfig.buttons[0]).to.equal(DialogService.YES);
    expect(dialogConfig.buttons[1]).to.equal(DialogService.NO);
    expect(_.isFunction(dialogConfig.onButtonClick)).to.be.true;
  });

  it('should resolve a promise after confirmation', () => {
    let resolved = false;
    let rejected = false;
    let confirmPromise = confirmService.confirm({message: 'O rly ?'});
    confirm();
    confirmPromise.then(() => resolved = true).catch(() => rejected = true);
    expect(resolved).to.be.true;
    expect(rejected).to.be.false;
  });

  it('should reject a promise after confirmation cancellation', () => {
    let resolved = false;
    let rejected = false;
    let confirmPromise = confirmService.confirm({message: 'O rly ?'});
    cancel();
    confirmPromise.then(() => resolved = true).catch(() => rejected = true);
    expect(resolved).to.be.false;
    expect(rejected).to.be.true;
  });

  it('should use the provided messages', () => {
    confirmService.confirm({
      message: 'O rly ?',
      header: 'Header'
    });
    expect(confirmService.dialogService.confirmation.calledWith('O rly ?', 'Header')).to.be.true;
  });

  function stubDialogService() {
    let dialogStub = stub(DialogService);
    dialogStub.createButton.returnsArg(0);
    return dialogStub;
  }

  function stubTranslateService() {
    let translateStub = stub(TranslateService);
    translateStub.translateInstant.returnsArg(0);
    return translateStub;
  }

  function getDialogConfig() {
    return confirmService.dialogService.confirmation.getCall(0).args[2];
  }

  function confirm() {
    let dialogConfig = getDialogConfig();
    dialogConfig.onButtonClick(DialogService.YES, undefined, {dismiss: sinon.spy()});
  }

  function cancel() {
    let dialogConfig = getDialogConfig();
    dialogConfig.onButtonClick(DialogService.CANCEL, undefined, {dismiss: sinon.spy()});
  }

});
