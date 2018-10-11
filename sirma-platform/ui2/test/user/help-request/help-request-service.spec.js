import {HelpRequestService} from 'user/help-request/help-request-service';
import {TranslateService} from 'services/i18n/translate-service';
import {stub} from 'test/test-utils';

describe('HelpRequestService', () => {

  describe('openDialog()', () => {

    it('should open create new issue dialog', () => {
      let actionExecutor = {
        execute: sinon.spy()
      };
      let translateService = stub(TranslateService);
      translateService.translateInstant.returns('Report Issue');
      let helpRequestService = new HelpRequestService(actionExecutor, translateService);
      helpRequestService.openDialog();

      let expectedAction = {
        action: 'reportIssue',
        configuration: {
          contextSelectorDisabled: true,
          predefinedTypes: ['emf:Issue'],
          openInNewTab: true
        },
        cssClass: 'seip-action-reportIssue',
        disableButton: false,
        disabled: false,
        extensionPoint: 'settings-menu',
        id: 'reportIssue',
        label: 'Report Issue',
        name: 'createInstanceAction'
      };
      let expectedContext = {
        currentObject: {
        }
      };

      expect(actionExecutor.execute.calledOnce).to.be.true;
      expect(actionExecutor.execute.args[0][0]).to.eql(expectedAction);
      expect(actionExecutor.execute.args[0][1]).to.eql(expectedContext);
    });
  });
});