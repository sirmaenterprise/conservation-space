import {HelpRequest} from 'layout/top-header/main-menu/user-menu/settings/help-request/help-request';

describe('HelpRequest', () => {

  describe('openDialog()', () => {
    it('should open dialog', () => {
      let helpRequestService = {};
      helpRequestService.openDialog = sinon.spy();
      let helpRequest = new HelpRequest(helpRequestService);

      helpRequest.openDialog();

      expect(helpRequestService.openDialog.calledOnce).to.be.true;
    });
  })
});