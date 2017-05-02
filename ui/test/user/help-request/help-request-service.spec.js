import {HelpRequestService} from 'user/help-request/help-request-service';
import {PromiseStub} from 'test/promise-stub';
import {DialogService} from 'components/dialog/dialog-service';
import {HelpDialog} from 'user/help-request/help-request-dialog';

describe('HelpRequestService', () => {

  describe('openDialog()', () => {
    it('should open dialog when HelpRequest is instantiated', () => {
      let dialogService = {
        create: sinon.spy()
      };
      let helpRequestService = new HelpRequestService(dialogService, null, null, null);

      helpRequestService.openDialog();

      expect(dialogService.create.calledOnce).to.be.true;
      expect(dialogService.create.args[0][0]).to.equal(HelpDialog);
      expect(dialogService.create.args[0][1].config['dialogConfiguration'].buttons.length).to.equal(2);

    });
  });

  describe('getDialogConfiguration()', () => {
    it('should have two buttons configured properly', () => {
      let helpRequestService = new HelpRequestService();

      let configuration = helpRequestService.getDialogConfiguration();

      expect(configuration.header).to.equal('help.request.dialog.header');
      expect(configuration.modalCls).to.equal('help-request');
      expect(configuration.buttons[0].id).to.equal(DialogService.OK);
      expect(configuration.buttons[0].label).to.equal('help.request.dialog.send.btn');
      expect(configuration.buttons[0].cls).to.equal('btn-primary');
      expect(configuration.buttons[0].disabled).to.equal(true);
      expect(configuration.buttons[1].id).to.equal(DialogService.CANCEL);
      expect(configuration.buttons[1].label).to.equal('dialog.button.cancel');
      expect(configuration.buttons[1].dismiss).to.equal(true);
    });
  });

  describe('sendRequest()', () => {
    it('should show error message', () => {
      let notificationService = {
        error: sinon.spy()
      };
      let translateService = {
        translateInstant: sinon.spy()
      };
      let helpRequestRestService = {
        sendHelpRequest: (data) => {return PromiseStub.reject({});}
      };
      let helpRequestService = new HelpRequestService(null, notificationService, translateService, helpRequestRestService);
      let dialogScope = {
        helpDialog: {
          prepareRequestModel: sinon.spy()
        }
      };

      helpRequestService.sendRequest(dialogScope, {});

      expect(translateService.translateInstant.calledOnce).to.be.true;
      expect(translateService.translateInstant.args[0][0]).to.equal('user.menu.help.request.message.error');
      expect(notificationService.error.calledOnce).to.be.true;
    });

    it('should show success message', () => {
      let notificationService = {
        success: sinon.spy()
      };
      let translateService = {
        translateInstant: sinon.spy()
      };
      let helpRequestRestService = {
        sendHelpRequest: (data) => {return PromiseStub.resolve({});}
      };
      let helpRequestService = new HelpRequestService(null, notificationService, translateService, helpRequestRestService);
      let dialogScope = {
        helpDialog: {
          prepareRequestModel: sinon.spy()
        }
      };
      let dialogConfig = {
        dismiss: sinon.spy()
      };

      helpRequestService.sendRequest(dialogScope, dialogConfig);

      expect(translateService.translateInstant.calledOnce).to.be.true;
      expect(translateService.translateInstant.args[0][0]).to.equal('user.menu.help.request.message.success');
      expect(notificationService.success.calledOnce).to.be.true;
      expect(dialogConfig.dismiss.calledOnce).to.be.true;

    });
  });

  describe('afterFormValidation()', () => {
    it('should send button be disabled if field(data) is not valid', () => {
      let helpRequestService = new HelpRequestService();
      let dialogConfiguration = {
        buttons: [{
          disabled: false
        }]
      };
      let data = [{isValid: false}];

      helpRequestService.afterFormValidation(dialogConfiguration, data);

      expect(dialogConfiguration.buttons[0].disabled).to.be.true;
    });

    it('should send button be enabled if field(data) is valid', () => {
      let helpRequestService = new HelpRequestService();
      let dialogConfiguration = {
        buttons: [{
          disabled: false
        }]
      };
      let data = [{isValid: true}];

      helpRequestService.afterFormValidation(dialogConfiguration, data);

      expect(dialogConfiguration.buttons[0].disabled).to.be.false;
    });
  });
});